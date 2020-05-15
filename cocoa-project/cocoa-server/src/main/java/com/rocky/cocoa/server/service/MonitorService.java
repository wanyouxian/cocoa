package com.rocky.cocoa.server.service;

import com.rocky.cocoa.entity.cluster.HdfsSummary;
import com.rocky.cocoa.entity.cluster.QueueMetrics;
import com.rocky.cocoa.entity.cluster.YarnSummary;

import java.util.List;

public interface MonitorService {
    //添加hdfs summary
    void addHdfsSummary(HdfsSummary hdfsSummary);

    //添加yarn summary
    void addYarnSummary(YarnSummary yarnSummary);

    //添加queue metric
    void addQueueMetrics(List<QueueMetrics> queueMetrics);

    //根据时间查找最近一次的hdfs summary
    HdfsSummary findHdfsSummary(int selectTime);

    //根据时间查找最近一次的yarn summary
    YarnSummary findYarnSummary(int selectTime);

    //根据时间查找最近一次的queue metric
    List<QueueMetrics> findQueueMetrics(int selectTime);

    //查询某段时间hdfs summary
    List<HdfsSummary> findHdfsSummaryBetween(int startTime, int endTime);

    //查询某段时间yarn summary
    List<YarnSummary> findYarnSummaryBetween(int startTime, int endTime);

}
