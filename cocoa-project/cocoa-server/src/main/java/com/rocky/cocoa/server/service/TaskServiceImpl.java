package com.rocky.cocoa.server.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.base.Strings;
import com.rocky.cocoa.core.client.azkaban.AzkabanApi;
import com.rocky.cocoa.core.client.azkaban.AzkabanApiImpl;
import com.rocky.cocoa.core.client.azkaban.model.Execution;
import com.rocky.cocoa.core.client.azkaban.model.Flow;
import com.rocky.cocoa.core.client.azkaban.response.*;
import com.rocky.cocoa.core.exception.ErrorCodes;
import com.rocky.cocoa.core.exception.CocoaException;
import com.rocky.cocoa.core.util.FileUtil;
import com.rocky.cocoa.entity.plugin.PackageOutParam;
import com.rocky.cocoa.entity.plugin.PackageParam;
import com.rocky.cocoa.entity.plugin.PluginPackage;
import com.rocky.cocoa.entity.task.JobDep;
import com.rocky.cocoa.entity.task.JobExecData;
import com.rocky.cocoa.entity.task.JobInfo;
import com.rocky.cocoa.entity.task.TaskWorkFlow;
import com.rocky.cocoa.repository.task.JobDepRepository;
import com.rocky.cocoa.repository.task.JobExecDataRepository;
import com.rocky.cocoa.repository.task.JobInfoRepository;
import com.rocky.cocoa.repository.task.TaskWorkFlowRepository;
import com.rocky.cocoa.server.plugin.JobTypeParamSchemas;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class TaskServiceImpl implements TaskService {

    @Value("${custom.task.azkaban.server}")
    private String azServer;
    @Value("${custom.task.azkaban.user}")
    private String azUser;
    @Value("${custom.task.azkaban.password}")
    private String azPwd;
    @Value("${custom.task.azkaban.execHome}")
    private String execHome;
    @Value("${custom.task.api.server}")
    private String apiServer;
    @Value("${custom.task.api.port}")
    private String apiPort;
    @Value("${custom.spark.home}")
    private String sparkHome;

    private AzkabanApi azkabanApi;

    @Resource
    TaskWorkFlowRepository taskWorkFlowRepository;
    @Resource
    JobInfoRepository jobInfoRepository;
    @Resource
    JobDepRepository jobDepRepository;
    @Resource
    JobExecDataRepository jobExecDataRepository;
    @Resource
    PluginService pluginService;

    @PostConstruct
    public void init() {
        this.azkabanApi = new AzkabanApiImpl(azUser, azPwd, azServer);
    }


    public TaskWorkFlow getTaskDetailInfo(String taskId) {
        TaskWorkFlow taskWorkFlow = taskWorkFlowRepository.findById(taskId).get();
        List<JobInfo> jobInfoList = jobInfoRepository.findByTaskId(taskId);
        List<JobDep> depInfos = jobDepRepository.findByTaskId(taskId);
        Map<String, List<JobInfo>> depJobs = new HashMap<>();
        for (JobDep depInfo : depInfos) {
            List<JobInfo> list = depJobs.computeIfAbsent(depInfo.getJobId(), k -> new ArrayList<>(3));
            List<JobInfo> depList =
                    jobInfoList
                            .stream()
                            .filter(new Predicate<JobInfo>() {
                                @Override
                                public boolean test(JobInfo jobInfo) {
                                    return jobInfo.getId().equals(depInfo.getJobDepId());
                                }
                            })
                            .collect(Collectors.toList());
            list.addAll(depList);
        }
        jobInfoList.forEach(
                new Consumer<JobInfo>() {
                    @Override
                    public void accept(JobInfo jobInfo) {
                        if (depJobs.get(jobInfo.getId()) != null) {
                            jobInfo.setDepJobs(depJobs.get(jobInfo.getId()));
                        }
                    }
                });
        taskWorkFlow.setJobInfoList(jobInfoList);
        return taskWorkFlow;
    }

    @Override
    public void createTask(TaskWorkFlow taskWorkFlow) throws IOException {
        boolean createSuccess = true;
        boolean exception = false;
        try {
            taskWorkFlowRepository.save(taskWorkFlow);
            List<JobInfo> jobInfoList = taskWorkFlow.getJobInfoList();
            for (JobInfo jobInfo : jobInfoList) {
                jobInfoRepository.save(jobInfo);
                List<JobInfo> depJobs = jobInfo.getDepJobs();
                if(depJobs==null){
                    continue;
                }
                for (JobInfo depJob : depJobs) {
                    JobDep jobDep = new JobDep();
                    jobDep.setTaskId(taskWorkFlow.getId());
                    jobDep.setJobId(jobInfo.getId());
                    jobDep.setJobName(jobInfo.getName());
                    jobDep.setJobDepId(depJob.getId());
                    jobDep.setJobDepName(depJob.getName());
                    jobDep.setTaskName(taskWorkFlow.getName());
                    jobDep.setCreateTime(new Date());
                    jobDep.setIsTrash(false);
                    jobDepRepository.save(jobDep);
                }
            }
            azkabanApi.login();
            azkabanApi.createProject(taskWorkFlow.getName(), taskWorkFlow.getDescription());
            FetchFlowsResponse fetchFlowsResponse = azkabanApi.fetchProjectFlows(taskWorkFlow.getName());
            if (fetchFlowsResponse == null || fetchFlowsResponse.getProjectId() == null) {
                createSuccess = false;
                throw new CocoaException("create task on azkaban failed", ErrorCodes.SYSTEM_EXCEPTION);
            }
            int azId = Integer.parseInt(fetchFlowsResponse.getProjectId());
            taskWorkFlow.setAzId(azId);
            taskWorkFlowRepository.save(taskWorkFlow);
            //todo 根据job 生成zip包 上传到azkaban
            submitTask(taskWorkFlow.getId());
            scheduleTask(taskWorkFlow.getId());
        } catch (Exception e) {
            exception = true;
            throw e;
        } finally {
            if (exception && createSuccess) {
                azkabanApi.deleteProject(taskWorkFlow.getName());
            }
        }
    }

    private void submitTask(String id) throws IOException {
        //获取job信息， 将插程序拷贝到tmp目录
        String tmp = System.getProperty("user.dir") + File.separator + "tmp";
        TaskWorkFlow taskDetailInfo = getTaskDetailInfo(id);
        String workdir = tmp + File.separator + taskDetailInfo.getName();
        File dir = new File(workdir);
        dir.mkdirs();

        List<JobInfo> jobInfoList = taskDetailInfo.getJobInfoList();
        for (JobInfo jobInfo : jobInfoList) {
            File[] files = new File(jobInfo.getPkgPath()).listFiles(
                    new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return !pathname.getName().equals("meta.json");
                        }
                    }
            );
            File jobDir = new File(workdir + File.separator + jobInfo.getName());
            jobDir.mkdirs();
            for (File file : files) {
                FileUtil.copyFileOrDirectory(file, jobDir.getAbsolutePath(), file.getName());
            }
            //todo 生成对应的job文件
            this.writeJobPkgParams(
                    taskDetailInfo,
                    jobInfo,
                    jobDir.getAbsolutePath());
            this.writeJobParams(taskDetailInfo, jobInfo, workdir);
        }
        //根据job type 生成job文件

        //压缩成zip包，上传到azkaban
        String zipFile = tmp + File.separator + taskDetailInfo.getName() + ".zip";
        FileUtil.zipFolderContent(new File(workdir), new File(zipFile));
        azkabanApi.uploadProjectZip(zipFile, taskDetailInfo.getName());
        FileUtil.deleteFileOrDir(dir);
        FileUtil.deleteFileOrDir(new File(zipFile));
    }


    private Map<String, Object> getJobParams(JobInfo jobInfo) {
        Map<String, String> jobDefaultParams =
                JobTypeParamSchemas.getJobTypeDefaultParams(jobInfo.getJobType(), jobInfo.getPkgLang());
        Map<String, Object> params = new HashMap<>(jobDefaultParams);
        PluginPackage jobPlugin = pluginService.getPluginByNameAndVersion(
                jobInfo.getPkgName(), jobInfo.getPkgVersion());
        List<PackageParam> defaultParams = jobPlugin.getDefaultParams();
        if (defaultParams != null) {
            defaultParams.forEach(packageParam -> {
                params.put(packageParam.getName(), packageParam.getDefaultValue());
            });
        }
        Map<String, Object> jobParams = jobInfo.getParams();
        params.putAll(jobParams);
        params.put("type", jobInfo.getJobType());
        params.put("name", jobInfo.getName());
        if (jobInfo.getDepJobs() != null && jobInfo.getDepJobs().size() > 0) {
            StringBuffer sbu = new StringBuffer();
            jobInfo.getDepJobs().forEach(
                    new Consumer<JobInfo>() {
                        @Override
                        public void accept(JobInfo jobInfo) {
                            sbu.append(jobInfo.getName()).append(",");
                        }
                    });
            sbu.deleteCharAt(sbu.length() - 1);
            params.put("dependencies", sbu.toString());
        }

        return params;
    }

    private void writeJobParams(TaskWorkFlow taskInfo, JobInfo jobInfo, String workdir)
            throws IOException {
        File jobFile = new File(workdir + File.separator + jobInfo.getName() + ".job");
        FileOutputStream fos = new FileOutputStream(jobFile, true);
        Map<String, Object> params = this.getJobParams(jobInfo);
        params.put("user.to.proxy", taskInfo.getTeam());
        params.put("name", jobInfo.getName());
        //todo all job is command type
        params.put("type", "command");
        //todo generate command by meta json
        params.put("command", generateCmd(jobInfo));
        StringBuffer jars = new StringBuffer(String.format("%s/runtime.properties,", jobInfo.getName()));
        if (jobInfo.getParams().containsKey("jars")) {
            jars.append(jobInfo.getParams().get("jars").toString());
        }
        params.put("jars", jars.toString());
        if (jobInfo.getDepJobs() != null && !jobInfo.getDepJobs().isEmpty()) {
            StringBuffer sbu = new StringBuffer();
            jobInfo.getDepJobs().forEach(
                    new Consumer<JobInfo>() {
                        @Override
                        public void accept(JobInfo jobInfo) {
                            sbu.append(jobInfo.getName()).append(",");
                        }
                    });
            sbu.deleteCharAt(sbu.length() - 1);
            params.put("dependencies", sbu.toString());
        }

        String extraProps = "";
        if (params.get("extraProps") != null) {
            extraProps = params.remove("extraProps").toString();
        }
        List<PackageParam> schemas =
                JobTypeParamSchemas.getJobTypeParamSchemas(jobInfo.getJobType(), jobInfo.getPkgLang());
        for (String key : params.keySet()) {
            boolean jobParam = false;
            for (PackageParam schema : schemas) {
                if (schema.getName().equals(key)) {
                    jobParam = true;
                    break;
                }
            }
            if (!jobParam) {
                continue;
            }
            String value = params.get(key).toString();
            if (!Strings.isNullOrEmpty(value)) {
                String line = key + "=" + value + "\r\n";
                fos.write(line.getBytes(Charset.forName("UTF-8")));
            }
        }
        if (extraProps != null) {
            String[] lines = extraProps.split(",");
            for (String line : lines) {
                fos.write(line.getBytes(Charset.forName("UTF-8")));
                fos.write("\r\n".getBytes());
            }
        }

        fos.close();
    }

    private String generateCmd(JobInfo jobInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        switch (jobInfo.getJobType()) {
            case "command":
                stringBuilder.append(jobInfo.getParams().get("command").toString());
                break;
            case "java":
                stringBuilder.append("java ")
                        .append(jobInfo.getParams().getOrDefault("jvm.args", " "))
                        .append(" -Xmx").append(jobInfo.getParams().getOrDefault("Xmx", " "))
                        .append(" -Xms").append(jobInfo.getParams().getOrDefault("Xms", " "))
                        .append(" -Dazkaban.projectname=${azkaban.flow.projectname} -Dazkaban.flowid=${azkaban.flow.flowid} -Dazkaban.execid=${azkaban.flow.execid} -Dazkaban.jobid=${azkaban.job.id} -Dazkaban.jobname=${azkaban.job.id} ")
                        .append(" -cp ").append(jobInfo.getParams().get("classpath"))
                        .append(" ").append(jobInfo.getParams().get("job.class"));
                break;
            case "spark":
                stringBuilder.append(String.format("%s/bin/spark-submit ", sparkHome))
                        .append(" --master ").append(jobInfo.getParams().getOrDefault("master", "yarn"))
                        .append(" --deploy-mode ").append(jobInfo.getParams().getOrDefault("deploy-mode", "cluster"))
                        .append(" --driver-memory ").append(jobInfo.getParams().getOrDefault("driver-memory", " "))
                        .append(" --driver-cores ").append(jobInfo.getParams().getOrDefault("driver-cores", " "))
                        .append(" --executor-memory ").append(jobInfo.getParams().getOrDefault("executor-memory", " "))
                        .append(" --executor-cores ").append(jobInfo.getParams().getOrDefault("executor-cores", " "))
                        .append(" --num-executors ").append(jobInfo.getParams().getOrDefault("num-executors", " "))
                        .append(" --jars ").append(String.format("%s/runtime.properties", jobInfo.getName()))
                        .append(Strings.isNullOrEmpty(jobInfo.getParams().getOrDefault("jars", "").toString()) ? "" : ",")
                        .append(jobInfo.getParams().getOrDefault("jars", " "));
                if (jobInfo.getPkgLang().equals("java")) {
                    stringBuilder.append(" --class ").append(jobInfo.getParams().get("class"))
                            .append(" ").append(jobInfo.getParams().getOrDefault("params", " "));
                } else if (jobInfo.getPkgLang().equals("python")) {
                    stringBuilder.append(" --py-files ").append(jobInfo.getParams().getOrDefault("py-files", " "));
                }
                stringBuilder.append(" ").append(jobInfo.getParams().get("execution-jar"))
                        .append(" ").append(jobInfo.getParams().getOrDefault("params", " "));
                break;
        }
        return stringBuilder.toString();
    }

    private void writeJobPkgParams(TaskWorkFlow taskInfo, JobInfo jobInfo, String jobDir) throws IOException {
        File jobDirFile = new File(jobDir);
        jobDirFile.mkdirs();
        FileOutputStream runtTimeFileOus =
                new FileOutputStream(jobDir + File.separator + "runtime.properties");
        runtTimeFileOus.write(("job.name=" + jobInfo.getName() + "\r\n").getBytes());
        runtTimeFileOus.write(("api.server=" + this.apiServer + "\r\n").getBytes());
        runtTimeFileOus.write(("api.server.port=" + this.apiPort + "\r\n").getBytes());
        StringBuffer authSbu = new StringBuffer();
        authSbu.append("taskid=").append(jobInfo.getTaskId()).append("\r\n");
        authSbu.append("jobid=").append(jobInfo.getId()).append("\r\n");
        authSbu.append("team=").append(taskInfo.getTeam()).append("\r\n");
        runtTimeFileOus.write(authSbu.toString().getBytes());
        runtTimeFileOus.close();
    }


    @Override
    public Page<TaskWorkFlow> listTasks(int page, int size, String sort, Sort.Direction direction) {
        TaskWorkFlow taskInfo = new TaskWorkFlow();
        taskInfo.setIsTrash(false);
        return taskWorkFlowRepository.findAll(Example.of(taskInfo),
                PageRequest.of(page, size,
                        Sort.by(direction == null ? Sort.Direction.DESC : direction, ObjectUtil.isNull(sort) ? "id" : sort)));

    }


    @Override
    public void deleteTaskById(String taskId) {
        taskWorkFlowRepository.findById(taskId).ifPresent(taskWorkFlow -> {
            try {
                azkabanApi.login();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.azkabanApi.deleteProject(taskWorkFlow.getName());
            jobDepRepository.deleteByTaskId(taskId);
            jobInfoRepository.deleteByTaskId(taskId);
            taskWorkFlowRepository.deleteById(taskId);
        });
    }

    @Override
    public void updateTask(TaskWorkFlow taskInfo) throws Exception {
        deleteTaskById(taskInfo.getId());
        createTask(taskInfo);
    }

    @Override
    public String executeTask(String taskId) {
        TaskWorkFlow taskWorkFlow = taskWorkFlowRepository.findById(taskId).get();
        String flow = this.getFlow(taskWorkFlow.getName());
        try {
            azkabanApi.login();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.azkabanApi.executeFlow(taskWorkFlow.getName(), flow).getExecid();
    }

    private String getFlow(String taskName) {
        try {
            azkabanApi.login();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Flow> flows = this.azkabanApi.fetchProjectFlows(taskName).getFlows();
        if (flows == null || flows.size() == 0) {
            return null;
        }
        return flows.get(flows.size() - 1).getFlowId();
    }

    @Override
    public TaskWorkFlow scheduleTask(String taskId) throws UnsupportedEncodingException {
        TaskWorkFlow taskInfo = taskWorkFlowRepository.findById(taskId).get();
        taskInfo.setScheduled(true);
        taskWorkFlowRepository.save(taskInfo);
        try {
            azkabanApi.login();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Flow> flows = this.azkabanApi.fetchProjectFlows(taskInfo.getName()).getFlows();
        String flow = flows.get(0).getFlowId();
        String cron = taskInfo.getScheduleCron();
        ScheduleCronFlowResponse scheduleCronFlowResponse = this.azkabanApi.scheduleCronFlow(taskInfo.getName(), flow, cron);
        taskInfo.setScheduleId(scheduleCronFlowResponse.getScheduleId());
        taskWorkFlowRepository.save(taskInfo);
        return taskInfo;
    }

    @Override
    public TaskWorkFlow unscheduleTask(String taskId) {
        TaskWorkFlow taskInfo = taskWorkFlowRepository.findById(taskId).get();
        taskInfo.setScheduled(false);
        String scheduleId = taskInfo.getScheduleId();
        taskWorkFlowRepository.save(taskInfo);
        try {
            azkabanApi.login();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.azkabanApi.removeSchedule(scheduleId);
        return taskInfo;
    }

    @Override
    public FetchExecFlowResponse getTaskExecutionDetail(String execId) {
        return this.azkabanApi.fetchExecFlow(execId);
    }

    @Override
    public List<Execution> getTaskExecutions(String taskId, int offset, int limit) {
        TaskWorkFlow taskInfo = taskWorkFlowRepository.findById(taskId).get();
        String flow = this.getFlow(taskInfo.getName());
        FetchFlowExecutionsResponse fetchFlowExecutionsResponse = this.azkabanApi.fetchFlowExecutions(taskInfo.getName(), flow, offset, limit);
        return fetchFlowExecutionsResponse.getExecutions();
    }

    @Override
    public List<JobInfo> getJobsOfTask(String taskId) {
        return jobInfoRepository.findByTaskId(taskId);
    }

    @Override
    public JobInfo getJobOfTaskByName(String taskName, String name) {
        return jobInfoRepository.findByTaskNameAndName(taskName, name);
    }

    @Override
    public TaskWorkFlow getTaskById(String taskId) {
        return taskWorkFlowRepository.findById(taskId).get();
    }

    @Override
    public TaskWorkFlow getTaskByName(String name) {
        return taskWorkFlowRepository.findOneByName(name);
    }

    @Override
    public void stopExecution(String execId) {
        this.azkabanApi.cancelFlow(execId);
    }

    @Override
    public FetchExecJobLogs getTaskExecutionLog(String execId, String jobName, int offset,
                                                int maxLength) {
        try {
            azkabanApi.login();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.azkabanApi.fetchExecJobLogs(execId, jobName, offset, maxLength);
    }

    @Override
    public List<TaskWorkFlow> findTaskByProject(long projectId) {
        return null;
    }


    @Override
    public void saveJobExecData(JobExecData execData) {
        jobExecDataRepository.save(execData);
    }

    @Override
    public void updateJobExecData(JobExecData execData) {
        jobExecDataRepository.save(execData);
    }


    @Override
    public List<JobExecData> getJobExecDataOfTask(String taskId, String execId) {
        return jobExecDataRepository.findByTaskIdAndExecId(taskId, execId);
    }

    @Override
    public JobExecData getJobExecData(String execId, String jobName) {
        return jobExecDataRepository.findByExecIdAndJobName(execId, jobName);
    }

    @Override
    public List<JobExecData> getJobExecDataByExecId(String execId) {
        return jobExecDataRepository.findByExecId(execId);
    }

    @Override
    public List<String> listTaskNames(String team) {
        TaskWorkFlow taskWorkFlow = new TaskWorkFlow();
        taskWorkFlow.setTeam(team);
        taskWorkFlow.setIsTrash(false);
        return taskWorkFlowRepository.findAll(Example.of(taskWorkFlow)).stream().map(TaskWorkFlow::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> listJobNames(String taskName) {
        List<JobInfo> byTaskName = jobInfoRepository.findByTaskName(taskName);
        return byTaskName.stream().map(JobInfo::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> listJobOutputParams(String taskName, String jobName) {
        JobInfo byTaskNameAndName = jobInfoRepository.findByTaskNameAndName(taskName, jobName);
        PluginPackage plugin = pluginService.getPlugin(byTaskNameAndName.getPkgId());
        return plugin.getOutParams().stream().map(PackageOutParam::getName).collect(Collectors.toList());
    }
}
