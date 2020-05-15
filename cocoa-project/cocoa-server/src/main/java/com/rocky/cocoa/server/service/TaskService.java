package com.rocky.cocoa.server.service;

import com.rocky.cocoa.core.client.azkaban.model.Execution;
import com.rocky.cocoa.core.client.azkaban.response.FetchExecFlowResponse;
import com.rocky.cocoa.core.client.azkaban.response.FetchExecJobLogs;
import com.rocky.cocoa.entity.task.JobExecData;
import com.rocky.cocoa.entity.task.JobInfo;
import com.rocky.cocoa.entity.task.TaskWorkFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface TaskService {
    //列出task列表（分页）

    //创建task
    void createTask(TaskWorkFlow taskWorkFlow) throws IOException;

    //删除task

    //执行task

    //调度task

    //取消调度task

    //获取执行详情

    //获取任务详情


    Page<TaskWorkFlow> listTasks(int page, int size, String sort, Sort.Direction direction);

    public void deleteTaskById(String taskId);

    public void updateTask(TaskWorkFlow taskInfo) throws Exception;

    public String executeTask(String taskId);

    public TaskWorkFlow scheduleTask(String taskId) throws UnsupportedEncodingException;

    public TaskWorkFlow unscheduleTask(String taskId);

    public FetchExecFlowResponse getTaskExecutionDetail(String execId);

    public List<Execution> getTaskExecutions(String taskId, int offset, int limit);

    public List<JobInfo> getJobsOfTask(String taskId);

    JobInfo getJobOfTaskByName(String taskName, String name);

    public TaskWorkFlow getTaskById(String taskId);

    public TaskWorkFlow getTaskByName(String name);

    public void stopExecution(String execId);

    public FetchExecJobLogs getTaskExecutionLog(String execId, String jobname, int offset, int maxLength);

    public List<TaskWorkFlow> findTaskByProject(long projectId);

    TaskWorkFlow getTaskDetailInfo(String taskId);

    public void saveJobExecData(JobExecData execData);

    public void updateJobExecData(JobExecData execData);

    public List<JobExecData> getJobExecDataOfTask(String taskId, String execId);

    public JobExecData getJobExecData(String execId, String jobName);

    List<JobExecData> getJobExecDataByExecId(String execId);

    List<String> listTaskNames(String team);

    List<String> listJobNames(String taskName);

    List<String> listJobOutputParams(String taskName, String jobName);
}
