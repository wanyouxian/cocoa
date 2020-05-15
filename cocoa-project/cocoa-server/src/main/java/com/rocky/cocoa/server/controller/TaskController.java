package com.rocky.cocoa.server.controller;

import cn.hutool.core.util.IdUtil;
import com.rocky.cocoa.core.exception.CocoaException;
import com.rocky.cocoa.core.exception.ErrorCodes;
import com.rocky.cocoa.entity.meta.ProjectInfo;
import com.rocky.cocoa.entity.plugin.PluginPackage;
import com.rocky.cocoa.entity.system.PrivilegeType;
import com.rocky.cocoa.entity.task.JobInfo;
import com.rocky.cocoa.entity.task.TaskWorkFlow;
import com.rocky.cocoa.server.BaseController;
import com.rocky.cocoa.server.jwt.ContextUtil;
import com.rocky.cocoa.server.jwt.LoginRequired;
import com.rocky.cocoa.server.jwt.PrivilegeCheck;
import com.rocky.cocoa.server.log.OperationObj;
import com.rocky.cocoa.server.log.OperationRecord;
import com.rocky.cocoa.server.plugin.JobTypeParamSchemas;
import com.rocky.cocoa.server.service.MetaService;
import com.rocky.cocoa.server.service.PluginService;
import com.rocky.cocoa.server.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cocoa/v1/task")
@CrossOrigin
public class TaskController extends BaseController {

    @Autowired
    PluginService pluginManager;

    @Autowired
    TaskService taskManager;

    @Autowired
    MetaService metaService;

    @ResponseBody
    @GetMapping("workflows")
    @LoginRequired
    @PrivilegeCheck(privilegeType = PrivilegeType.TASK)
    @OperationRecord(value = "列出TASK列表")
    public Object listTasks(@RequestParam(name = "pageIndex", required = true, defaultValue = "1")
                                    int pageIndex,
                            @RequestParam(name = "pageSize", required = true, defaultValue = "20")
                                    int pageSize) {
        Page<TaskWorkFlow> taskWorkFlows = taskManager.listTasks(pageIndex-1, pageSize, null, null);
        Map<String,Object> pages = new HashMap<>();
        pages.put("pages",taskWorkFlows.getContent());
        pages.put("pageIndex",pageIndex);
        pages.put("pageSize",pageSize);
        pages.put("pageCount",taskWorkFlows.getTotalPages());
        return getResult(pages);
    }

    @ResponseBody
    @PostMapping("workflow")
    @OperationRecord("创建task")
    @LoginRequired
    public Object createTask(@RequestBody @OperationObj TaskGraph taskGraph) throws Exception {
        Pattern pattern = Pattern.compile("[\\w+\\-]+");
        Matcher matcher = pattern.matcher(taskGraph.getTaskName());
        if (!matcher.matches()) {
            return getError(ErrorCodes.ERROR_PARAM, "task name can only contains a-zA-Z0-9-_");
        }
        if(taskGraph.getTaskId()!=null){
            //删除之前的task，重新提交task
            taskManager.deleteTaskById(taskGraph.getTaskId());
        }
        TaskWorkFlow taskInfo = this.buildTaskInfo(taskGraph);
        this.taskManager.createTask(taskInfo);
        return getResult(true);
    }


    private TaskWorkFlow buildTaskInfo(TaskGraph taskGraph) {
        TaskWorkFlow taskInfo = new TaskWorkFlow();
        taskInfo.setIsTrash(false);
        taskInfo.setCreateTime(new Date());
        ProjectInfo projectInfo = metaService.findProjectInfoByName(taskGraph.getProjectName());
        if (projectInfo != null) {
            taskInfo.setProjectName(projectInfo.getName());
            taskInfo.setProjectId(projectInfo.getId());
        }else {
            throw new CocoaException("project not exists", ErrorCodes.ERROR_PARAM);
        }
        taskInfo.setName(taskGraph.getTaskName());
        // todo set taskwork admin and team
        taskInfo.setAdmin(ContextUtil.getCurrentUser().getName());
        taskInfo.setTeam(ContextUtil.getCurrentUser().getTeam());
        taskInfo.setScheduled(taskGraph.isScheduled());
        taskInfo.setScheduleCron(taskGraph.getScheduleCron());
        taskInfo.setId(IdUtil.simpleUUID());
        taskInfo.setDescription(taskGraph.getDesc());
        taskInfo.setIsTrash(false);
        taskInfo.setCreateTime(new Date());
        Map<String, JobInfo> jobInfoMap = new HashMap<>();
        for (TaskGraph.TaskNode taskNode : taskGraph.getNodeList()) {
            JobInfo jobInfo = new JobInfo();
            jobInfo.setIsTrash(false);
            jobInfo.setCreateTime(new Date());
            jobInfo.setTaskId(taskInfo.getId());
            jobInfo.setId(taskNode.getId().replace("-",""));
            jobInfo.setName(taskNode.getName());
            PluginPackage pkg = pluginManager.getPlugin(taskNode.getPkgId());
            JobInfo jobInfoInDb = taskManager.getJobOfTaskByName(taskInfo.getName(), taskNode.getName());
            if (pkg == null) {
                if (jobInfoInDb == null) {
                    throw new CocoaException("pkg and job info not found", ErrorCodes.ERROR_PARAM);
                }
                jobInfo.setPkgId(jobInfoInDb.getPkgId());
                jobInfo.setPkgName(jobInfoInDb.getPkgName());
                jobInfo.setPkgVersion(jobInfoInDb.getPkgVersion());
                taskNode.getParams().forEach(stringObjectMap -> {
                    jobInfoInDb.getParams().put(stringObjectMap.get("name").toString(),stringObjectMap.get("value"));
                });
                jobInfo.setParams(jobInfoInDb.getParams());
                Map<String,String> style=new HashMap<>();
                style.put("left",taskNode.getLeft());
                style.put("top",taskNode.getTop());
                jobInfo.setStyleInfo(style);
                jobInfo.setPkgPath(jobInfoInDb.getPkgPath());
                jobInfo.setJobType(jobInfoInDb.getJobType());
                jobInfo.setPkgLang(jobInfoInDb.getPkgLang());
                jobInfo.setTaskName(jobInfoInDb.getTaskName());
                jobInfoMap.put(jobInfo.getId(), jobInfo);
            } else {
                jobInfo.setPkgId(pkg.getId());
                jobInfo.setPkgName(pkg.getName());
                jobInfo.setPkgVersion(pkg.getVersion());
                Map<String, Object> params = new HashMap<>();
                pkg.getDefaultParams().forEach(packageParam -> {
                    params.put(packageParam.getName(), packageParam.getDefaultValue());
                });
                taskNode.getParams().forEach(stringObjectMap -> {
                    params.put(stringObjectMap.get("name").toString(),stringObjectMap.get("value"));
                });
                jobInfo.setParams(params);
                Map<String,String> style=new HashMap<>();
                style.put("left",taskNode.getLeft());
                style.put("top",taskNode.getTop());
                jobInfo.setStyleInfo(style);
                jobInfo.setPkgPath(pkg.getPkgPath());
                jobInfo.setJobType(pkg.getJobType());
                jobInfo.setPkgLang(pkg.getLang());
                jobInfo.setTaskName(taskInfo.getName());
                jobInfoMap.put(jobInfo.getId(), jobInfo);
            }

        }
        for (TaskGraph.NodeEdge nodeEdge : taskGraph.getLineList()) {
            JobInfo jobInfo = jobInfoMap.get(nodeEdge.getTo().replace("-",""));
            if (jobInfo.getDepJobs() == null) {
                jobInfo.setDepJobs(new ArrayList<JobInfo>(3));
            }
            JobInfo depJobInfo = jobInfoMap.get(nodeEdge.getFrom().replace("-",""));
            jobInfo.getDepJobs().add(depJobInfo);
        }
        List<JobInfo> jobInfoList = new ArrayList<>(jobInfoMap.size());
        jobInfoList.addAll(jobInfoMap.values());
        taskInfo.setJobInfoList(jobInfoList);
        return taskInfo;
    }

    private TaskGraph buildTaskGraph(TaskWorkFlow taskInfo) {
        TaskGraph taskGraph = new TaskGraph();
        taskGraph.setProjectId(taskInfo.getProjectId());
        taskGraph.setProjectName(taskInfo.getProjectName());
        taskGraph.setAdmin(taskInfo.getAdmin());
        taskGraph.setTeam(taskInfo.getTeam());
        taskGraph.setScheduleCron(taskInfo.getScheduleCron());
        taskGraph.setTaskName(taskInfo.getName());
        taskGraph.setTaskId(taskInfo.getId());
        taskGraph.setScheduled(taskInfo.getScheduled());
        taskGraph.setDesc(taskInfo.getDescription());
        List<TaskGraph.NodeEdge> edges = new ArrayList<>();
        taskGraph.setLineList(edges);
        taskInfo.getJobInfoList().forEach(new Consumer<JobInfo>() {
            @Override
            public void accept(JobInfo jobInfo) {
                if (jobInfo.getDepJobs() != null) {
                    for (JobInfo jb : jobInfo.getDepJobs()) {
                        TaskGraph.NodeEdge edge = new TaskGraph.NodeEdge();
                        edge.setTo(jobInfo.getId());
                        edge.setFrom(jb.getId());
                        edges.add(edge);
                    }
                }
            }
        });
        List<TaskGraph.TaskNode> nodes =
                taskInfo.getJobInfoList().stream().map(new Function<JobInfo, TaskGraph.TaskNode>() {
                    @Override
                    public TaskGraph.TaskNode apply(JobInfo jobInfo) {
                        TaskGraph.TaskNode node = new TaskGraph.TaskNode();
                        Map<String, String> styleInfo = jobInfo.getStyleInfo();
                        node.setLeft(styleInfo.get("left"));
                        node.setTop(styleInfo.get("top"));
                        node.setName(jobInfo.getName());
                        node.setId(jobInfo.getId());
                        List<Map<String, Object>> jobParams = filterOutInternalParams(jobInfo);
                        node.setParams(jobParams);
                        node.setPkgId(jobInfo.getPkgId());
                        node.setPkgName(jobInfo.getPkgName());
                        node.setPkgVersion(jobInfo.getPkgVersion());
                        return node;
                    }
                }).collect(Collectors.toList());
        taskGraph.setNodeList(nodes);
        return taskGraph;
    }


    public List<Map<String, Object>> filterOutInternalParams(JobInfo jobInfo) {
        List<Map<String, Object>> copyParams = new ArrayList<>();
        jobInfo.getParams().keySet().stream().forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                Map<String,Object> param = new HashMap<>();
                if (JobTypeParamSchemas.isInternalParam(s)) {
                    return;
                }
                Object value = jobInfo.getParams().get(s);
                param.put("name",s);
                param.put("disable",!JobTypeParamSchemas.isUserSetAble(jobInfo.getJobType(),jobInfo.getPkgLang(),s));
                if (value != null) {
                    param.put("value", jobInfo.getParams().get(s).toString());
                } else {
                    param.put("value", "");
                }
                copyParams.add(param);
            }
        });
        return copyParams;
    }

    @ResponseBody
    @GetMapping("workflow")
    @LoginRequired
    public Object getTaskInfo(@RequestParam("id") String id) {
        return getResult(buildTaskGraph(taskManager.getTaskDetailInfo(id)));
    }

    @ResponseBody
    @GetMapping("workflow/exec")
    @LoginRequired
    @OperationRecord("执行TASK")
    public Object execTask(@OperationObj @RequestParam("id") String id) {
        String execId = taskManager.executeTask(id);
        return getResult(execId);
    }

    @ResponseBody
    @GetMapping("workflow/schedule")
    @OperationRecord("调度Task")
    @LoginRequired
    public Object scheduleTask(@OperationObj @RequestParam("id") String id) throws UnsupportedEncodingException {
        TaskWorkFlow taskById = taskManager.getTaskById(id);
        if (taskById.getScheduled()) {
            taskManager.unscheduleTask(id);
        } else {
            taskManager.scheduleTask(id);
        }

        return getResult(true);
    }

    @ResponseBody
    @DeleteMapping("workflow")
    @LoginRequired
    @OperationRecord("删除Task")
    public Object delWorkflow(@OperationObj @RequestParam("id") String id) throws IOException {
        taskManager.deleteTaskById(id);
        return getResult(true);
    }

    @ResponseBody
    @GetMapping("log")
    @LoginRequired
    @OperationRecord("获取日志")
    public Object showExecLog(@RequestParam("execId") String id,
                              @RequestParam("jobName") String jobName,
                              @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                              @RequestParam(value = "length", required = false, defaultValue = "2000") int length) {
        return getResult(taskManager.getTaskExecutionLog(id, jobName, offset, length));
    }

    @ResponseBody
    @GetMapping("names")
    @LoginRequired
    public Object getTaskName() {
        return getResult(taskManager.listTaskNames(ContextUtil.getCurrentUser().getTeam()));
    }

    @ResponseBody
    @GetMapping("task/jobs")
    @LoginRequired
    public Object getJobName(@RequestParam String taskName) {
        return getResult(taskManager.listJobNames(taskName));
    }

    @ResponseBody
    @GetMapping("task/job/output")
    @LoginRequired
    public Object getJobOutputParams(@RequestParam String taskName,
                                     @RequestParam String jobName) {
        return getResult(taskManager.listJobOutputParams(taskName, jobName));
    }
}
