package com.rocky.cocoa.server.controller;

import cn.hutool.core.date.DateUtil;
import com.rocky.cocoa.core.exception.ErrorCodes;
import com.rocky.cocoa.entity.meta.ProjectInfo;
import com.rocky.cocoa.entity.task.JobExecData;
import com.rocky.cocoa.entity.task.JobInfo;
import com.rocky.cocoa.entity.task.TaskWorkFlow;
import com.rocky.cocoa.entity.var.ProjectVar;
import com.rocky.cocoa.entity.var.VariableValue;
import com.rocky.cocoa.server.BaseController;
import com.rocky.cocoa.server.jwt.ContextUtil;
import com.rocky.cocoa.server.jwt.LoginRequired;
import com.rocky.cocoa.server.log.OperationObj;
import com.rocky.cocoa.server.log.OperationRecord;
import com.rocky.cocoa.server.service.MetaService;
import com.rocky.cocoa.server.service.TaskService;
import com.rocky.cocoa.server.var.ParamReference;
import com.rocky.cocoa.server.var.VariableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/cocoa/v1/param")
@CrossOrigin
public class TaskParamController extends BaseController {


    @Autowired
    TaskService taskService;
    @Autowired
    VariableManager variableManager;
    @Autowired
    MetaService metaService;


    @GetMapping("vars")
    @ResponseBody
    @LoginRequired
    @OperationRecord("获取变量列表")
    public Object getVar(@RequestParam(value = "pageIndex", defaultValue = "1") int pageIndex,
                         @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Page<ProjectVar> variables = variableManager.getVariables(ContextUtil.getCurrentUser().getTeam(), pageIndex - 1, pageSize, null, null);
        Map<String, Object> pages = new HashMap<>();
        pages.put("pages", variables.getContent());
        pages.put("pageIndex", pageIndex);
        pages.put("pageSize", pageSize);
        pages.put("pageCount", variables.getTotalPages());
        return getResult(pages);
    }

    @PostMapping("var")
    @ResponseBody
    @LoginRequired
    @OperationRecord("添加变量")
    public Object createVar(@RequestBody @OperationObj ProjectVar var) {
        if (var.getProjectName() == null) {
            return getError(ErrorCodes.ERROR_PARAM, "must bind projectName");
        }
        ProjectInfo projectInfoByName = metaService.findProjectInfoByName(var.getProjectName());
        if (projectInfoByName == null) {
            return getError(ErrorCodes.ERROR_PARAM, "project not exists");
        }
        var.setProjectId(projectInfoByName.getId());
        var.setAdmin(ContextUtil.getCurrentUser().getName());
        var.setIsTrash(false);
        var.setCreateTime(new Date());
        var.setTeam(ContextUtil.getCurrentUser().getTeam());
        if (var.getInitValue() != null) {
            var.setInitTime(DateUtil.toIntSecond(new Date()));
        }
        variableManager.createVariable(var);
        return getResult(true);
    }

    @PutMapping("var")
    @ResponseBody
    @LoginRequired
    @OperationRecord("更新变量")
    public Object updateVar(@RequestBody @OperationObj ProjectVar var) {

        variableManager.update(var);
        return getResult(true);
    }

    @GetMapping("var")
    @ResponseBody
    @LoginRequired
    @OperationRecord("查询变量")
    public Object getVar(@RequestParam String projectName, @OperationObj @RequestParam String varName) {

        ProjectVar variable = variableManager.getVariable(projectName, varName);
        return getResult(variable);
    }

    @DeleteMapping("var")
    @ResponseBody
    @LoginRequired
    @OperationRecord("删除变量")
    public Object deleteVar(@RequestParam @OperationObj long id) {

        variableManager.deleteVariable(id);
        return getResult(true);
    }

    @GetMapping("job")
    @ResponseBody
    public Object getJobConfig(@RequestParam("taskName") String taskName,
                               @RequestParam("jobName") String jobName,
                               @RequestParam(value = "execId", defaultValue = "-1") String execId) {

        JobInfo jobInfo = taskService.getJobOfTaskByName(taskName, jobName);
        Map<String, Object> params = jobInfo.getParams();
        for (String key : params.keySet()) {
            if (params.get(key) == null) {
                continue;
            }

            String value = params.get(key).toString();
            ParamReference reference = ParamReference.parseFrom(value);

            if (reference == null) {
                continue;
            }
            if (reference.isByProject()) {
                ProjectVar variable = variableManager.getVariable(reference.getProjectName(), reference.getVarName());
                VariableValue currentValue = variable.getCurrentValue();
                if (currentValue != null) {
                    params.put(key, currentValue.getValue());
                }
            } else if (reference.isInput()) {
                JobExecData jobExecData = taskService.getJobExecData(execId, jobName);
                if (jobExecData.getParams() != null) {
                    params.put(key, jobExecData.getParamValue(reference.getVarName()));
                } else {
                    JobInfo jobOfTaskByName = taskService.getJobOfTaskByName(taskName, reference.getJobName());
                    params.put(key, jobOfTaskByName.getParams().getOrDefault(reference.getVarName(), ""));
                }
            } else if (reference.isOutput()) {
                JobExecData jobExecData = taskService.getJobExecData(execId, jobName);
                params.put(key, jobExecData.getParamValue(reference.getVarName()));
            }

        }
        return params;
    }


    @PostMapping("runtime")
    @ResponseBody
    public Object saveJobRuntimeConfig(@RequestBody Map<String, Object> params) throws IOException {
        String taskName = params.get("taskName").toString();
        String jobName = params.get("jobName").toString();
        String execId = params.get("execId").toString();
        Map<String, Object> jobConfig = (Map<String, Object>) params.get("config");
        JobExecData jobExecData = new JobExecData();
        JobExecData execData = taskService.getJobExecData(execId, jobName);
        boolean insert = true;
        if (execData != null) {
            insert = false;
        }
        jobExecData.setParams(jobConfig);
        jobExecData.setExecId(execId);
        jobExecData.setJobName(jobName);
        jobExecData.setTaskName(taskName);
        TaskWorkFlow taskByName = taskService.getTaskByName(taskName);
        jobExecData.setTaskId(taskByName.getId());
        List<JobInfo> jobInfos = taskService.getJobsOfTask(taskByName.getId());
        for (JobInfo jobInfo : jobInfos) {
            if (jobInfo.getName().equals(jobName)) {
                jobExecData.setJobId(jobInfo.getId());
            }
        }
        jobExecData.setOutParams(null);
        if (insert) {
            taskService.saveJobExecData(jobExecData);
        } else {
            taskService.updateJobExecData(jobExecData);
        }
        return "success";
    }

    @GetMapping("runtime")
    public Object getJobRuntimeConfig(@RequestParam("taskName") String taskName,
                                      @RequestParam("jobName") String jobName, @RequestParam("execId") String execId) {
        JobExecData jobExecData = taskService.getJobExecData(execId, jobName);
        return jobExecData.getParams();
    }

    @GetMapping("output/job")
    public Object getJobOutputParam(@RequestParam("taskName") String taskName,
                                    @RequestParam("jobName") String jobName,
                                    @RequestParam("execId") String execId) {
        JobExecData jobExecData = this.taskService.getJobExecData(execId, jobName);
        if (jobExecData != null && jobExecData.getOutParams() != null) {
            return jobExecData.getOutParams();
        } else {
            return Collections.emptyMap();
        }
    }

    @GetMapping("output/task")
    public Object getTaskOutputParam(@RequestParam("taskName") String taskName,
                                     @RequestParam("execId") String execId) {
        TaskWorkFlow taskByName = this.taskService.getTaskByName(taskName);
        List<JobExecData> dataList =
                this.taskService.getJobExecDataOfTask(taskByName.getId(), execId);
        Map<String, Map<String, Object>> map = new HashMap<>();
        for (JobExecData data : dataList) {
            if (data.getOutParams() != null) {
                map.put(data.getJobName(), data.getOutParams());
            } else {
                map.put(data.getJobName(), Collections.EMPTY_MAP);
            }
        }
        return map;
    }

    @PostMapping("output")
    public Object saveJobOutputParam(@RequestBody Map<String, Object> params) throws IOException {
        String taskName = params.get("taskName").toString();
        String jobName = params.get("jobName").toString();
        String execId = params.get("execId").toString();
        Map<String, Object> jobConfig = (Map<String, Object>) params.get("config");
        JobExecData jobExecData = this.taskService.getJobExecData(execId, jobName);
        boolean insert = false;
        if (jobExecData == null) {
            jobExecData = new JobExecData();
            jobExecData.setTaskName(taskName);
            jobExecData.setJobName(jobName);
            jobExecData.setExecId(execId);
            TaskWorkFlow taskByName = taskService.getTaskByName(taskName);
            List<JobInfo> jobInfos = taskService.getJobsOfTask(taskByName.getId());
            for (JobInfo jobInfo : jobInfos) {
                if (jobInfo.getName().equals(jobName)) {
                    jobExecData.setJobId(jobInfo.getId());
                    break;
                }
            }
            jobExecData.setTaskId(taskByName.getId());
            insert = true;
        }
        jobExecData.setOutParams(params);
        if (insert) {
            taskService.saveJobExecData(jobExecData);
        } else {
            taskService.updateJobExecData(jobExecData);
        }
        variableManager.applyUpdate(taskName, jobName, params);
        return "success";
    }

    /**
     * save table info.
     *
     * @param taskName  taskName
     * @param dsName    dsName
     * @param dbName    dbName
     * @param tableName tableName
     * @return success
     */
    @PostMapping("bloodline")
    public Object saveTableInfo(@RequestParam("taskName") String taskName,
                                @RequestParam("dsName") String dsName,
                                @RequestParam("dbName") String dbName,
                                @RequestParam("tableName") String tableName,
                                @RequestParam(value = "tableSchema", defaultValue = "") String tableDesc) {
        TaskWorkFlow taskByName = this.taskService.getTaskByName(taskName);
        //todo add data blood line
        return "success";
    }
}