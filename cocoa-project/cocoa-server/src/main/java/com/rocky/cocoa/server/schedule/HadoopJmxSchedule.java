package com.rocky.cocoa.server.schedule;

import com.rocky.cocoa.core.util.StatefulHttpClient;
import com.rocky.cocoa.entity.cluster.HdfsSummary;
import com.rocky.cocoa.entity.cluster.QueueMetrics;
import com.rocky.cocoa.entity.cluster.YarnSummary;
import com.rocky.cocoa.server.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class HadoopJmxSchedule {
    @Value("${custom.hadoop.nn.uri}")
    private String nnUriStr;
    @Value("${custom.hadoop.rm.uri}")
    private String rmUriStr;

    @Autowired
    MonitorService monitorService;
    private StatefulHttpClient client = new StatefulHttpClient(null);

    public static final String JMXSERVERURLFORMAT = "http://%s/jmx?qry=%s";
    public static final String NAMENODEINFO = "Hadoop:service=NameNode,name=NameNodeInfo";
    public static final String FSNAMESYSTEM = "Hadoop:service=NameNode,name=FSNamesystem";
    public static final String FSNAMESYSTEMSTATE = "Hadoop:service=NameNode,name=FSNamesystemState";
    public static final String QUEUEMETRICS = "Hadoop:service=ResourceManager,name=QueueMetrics,q0=root";
    public static final String CLUSTERMETRICS = "Hadoop:service=ResourceManager,name=ClusterMetrics";

    public static final String QUEUEMETRICSALL = "Hadoop:service=ResourceManager,name=QueueMetrics,*";

    //获取active namenode uri
    private String getActiveNameNodeUri(List<String> nameNodeUri) throws IOException {
        String activeNameNodeUri = nameNodeUri.get(0);
        if (nameNodeUri.size() > 1) {
            for (String uri :
                    nameNodeUri) {
                String fsNameSystemUrl = String.format(JMXSERVERURLFORMAT, uri, FSNAMESYSTEM);
                HadoopMetrics hadoopMetrics = client.get(HadoopMetrics.class, fsNameSystemUrl, null, null);
                if (hadoopMetrics.getMetricsValue("tag.HAState").toString().equals("active")) {
                    activeNameNodeUri = uri;
                    break;
                }
            }
        }
        return activeNameNodeUri;
    }

    //获取active resource manager uri
    private String getActiveRmUri(List<String> rmUris) throws IOException {
        String activeRmUri = rmUris.get(0);
        if (rmUris.size() > 1) {
            for (String uri :
                    rmUris) {
                String clusterMetricsUrl = String.format(JMXSERVERURLFORMAT, uri, CLUSTERMETRICS);
                HadoopMetrics hadoopMetrics = client.get(HadoopMetrics.class, clusterMetricsUrl, null, null);
                if (hadoopMetrics.getMetricsValue("tag.ClusterMetrics").toString().equals("ResourceManager")) {
                    activeRmUri = uri;
                    break;
                }
            }
        }
        return activeRmUri;
    }



    //定时执行 获取jmx信息
    @Scheduled(cron = "0 * * * * ?")
    public void hadoopMetricsCollect() {
        //收集hdfs jmx
        try {
            HdfsSummary hdfsSummary = reportHdfsSummary(client);
            if (hdfsSummary != null) {
                monitorService.addHdfsSummary(hdfsSummary);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        //收集yarn jmx
        YarnSummary yarnSummary = reportYarnSummary(client);
        if (yarnSummary != null) {
            monitorService.addYarnSummary(yarnSummary);
        }
        //收集队列的jmx

        try {
            List<QueueMetrics> queueMetricses = queryQueueMetrics();
            monitorService.addQueueMetrics(queueMetricses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private YarnSummary reportYarnSummary(StatefulHttpClient client) {
        YarnSummary yarnSummary = new YarnSummary();
        List<String> rmUris = Arrays.asList(rmUriStr.split(";"));
        if (rmUris.isEmpty()) {
            yarnSummary.setIsTrash(true);
            return yarnSummary;
        }

        try {
            String clusterMetricsUrl = String.format(JMXSERVERURLFORMAT, getActiveRmUri(rmUris), CLUSTERMETRICS);
            HadoopMetrics clusterMetrics = client
                    .get(HadoopMetrics.class, clusterMetricsUrl, null, null);
            if (clusterMetrics.getMetricsValue("tag.ClusterMetrics").toString()
                    .equals("ResourceManager")) {
                yarnSummary
                        .setLiveNodeManagerNums((int) clusterMetrics.getMetricsValue("NumActiveNMs"));
                yarnSummary.setDeadNodeManagerNums((int) clusterMetrics.getMetricsValue("NumLostNMs"));
                yarnSummary
                        .setUnhealthyNodeManagerNums(
                                (int) clusterMetrics.getMetricsValue("NumUnhealthyNMs"));

                String queueMetricsUrl = String.format(JMXSERVERURLFORMAT, getActiveRmUri(rmUris), QUEUEMETRICS);
                HadoopMetrics hadoopMetrics = client
                        .get(HadoopMetrics.class, queueMetricsUrl, null, null);
                yarnSummary.setSubmittedApps((int) hadoopMetrics.getMetricsValue("AppsSubmitted"));
                yarnSummary.setRunningApps((int) hadoopMetrics.getMetricsValue("AppsRunning"));
                yarnSummary.setPendingApps((int) hadoopMetrics.getMetricsValue("AppsPending"));
                yarnSummary.setCompletedApps((int) hadoopMetrics.getMetricsValue("AppsCompleted"));
                yarnSummary.setKilledApps((int) hadoopMetrics.getMetricsValue("AppsKilled"));
                yarnSummary.setFailedApps((int) hadoopMetrics.getMetricsValue("AppsFailed"));
                yarnSummary.setAllocatedMem(
                        Long.parseLong(hadoopMetrics.getMetricsValue("AllocatedMB").toString()));
                yarnSummary.setAllocatedCores((int) hadoopMetrics.getMetricsValue("AllocatedVCores"));
                yarnSummary
                        .setAllocatedContainers((int) hadoopMetrics.getMetricsValue("AllocatedContainers"));
                yarnSummary.setAvailableMem(
                        Long.parseLong(hadoopMetrics.getMetricsValue("AvailableMB").toString()));
                yarnSummary.setAvailableCores((int) hadoopMetrics.getMetricsValue("AvailableVCores"));
                yarnSummary
                        .setPendingMem(
                                Long.parseLong(hadoopMetrics.getMetricsValue("PendingMB").toString()));
                yarnSummary.setPendingCores((int) hadoopMetrics.getMetricsValue("PendingVCores"));
                yarnSummary
                        .setPendingContainers((int) hadoopMetrics.getMetricsValue("PendingContainers"));
                yarnSummary
                        .setReservedMem(
                                Long.parseLong(hadoopMetrics.getMetricsValue("ReservedMB").toString()));
                yarnSummary.setReservedCores((int) hadoopMetrics.getMetricsValue("ReservedVCores"));
                yarnSummary
                        .setReservedContainers((int) hadoopMetrics.getMetricsValue("ReservedContainers"));

                yarnSummary.setCreateTime(new Date());
                yarnSummary.setIsTrash(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            yarnSummary.setIsTrash(true);
        }
        return yarnSummary;
    }

    private HdfsSummary reportHdfsSummary(StatefulHttpClient client) throws IOException {
        //调用get active namenode uri
        List<String> nameNodeUris = Arrays.asList(nnUriStr.split(";"));
        if (nameNodeUris.isEmpty()) {
            return null;
        }
        String activeNameNodeUri = getActiveNameNodeUri(nameNodeUris);
        HdfsSummary hdfsSummary = new HdfsSummary();
        try {
            String nameNodeInfoUrl = String.format(JMXSERVERURLFORMAT,
                    activeNameNodeUri, NAMENODEINFO);
            HadoopMetrics hadoopMetrics = client.get(HadoopMetrics.class,
                    nameNodeInfoUrl, null, null);
            hdfsSummary
                    .setTotal(Long.parseLong(
                            hadoopMetrics.getMetricsValue("Total").toString()));
            hdfsSummary
                    .setDfsUsed(Long.parseLong(hadoopMetrics.getMetricsValue("Used").toString()));
            hdfsSummary.setPercentUsed(
                    Float.parseFloat(hadoopMetrics.getMetricsValue("PercentUsed").toString()));
            hdfsSummary
                    .setDfsFree(Long.parseLong(hadoopMetrics.getMetricsValue("Free").toString()));
            hdfsSummary.setNonDfsUsed(
                    Long.parseLong(hadoopMetrics.getMetricsValue("NonDfsUsedSpace").toString()));
            hdfsSummary.setTotalBlocks(
                    Long.parseLong(hadoopMetrics.getMetricsValue("TotalBlocks").toString()));
            hdfsSummary
                    .setTotalFiles(Long.parseLong(hadoopMetrics.getMetricsValue("TotalFiles").toString()));
            hdfsSummary.setMissingBlocks(
                    Long.parseLong(hadoopMetrics.getMetricsValue("NumberOfMissingBlocks").toString()));

            String fsNameSystemStateUrl = String
                    .format(JMXSERVERURLFORMAT, activeNameNodeUri, FSNAMESYSTEMSTATE);
            HadoopMetrics fsNameSystemMetrics = client
                    .get(HadoopMetrics.class, fsNameSystemStateUrl, null, null);
            hdfsSummary
                    .setLiveDataNodeNums((int) fsNameSystemMetrics.getMetricsValue("NumLiveDataNodes"));
            hdfsSummary
                    .setDeadDataNodeNums((int) fsNameSystemMetrics.getMetricsValue("NumDeadDataNodes"));
            hdfsSummary
                    .setVolumeFailuresTotal((int) fsNameSystemMetrics.getMetricsValue("VolumeFailuresTotal"));
        } catch (Exception e) {
            log.error(e.getMessage());
            hdfsSummary.setIsTrash(true);
        }
        hdfsSummary.setCreateTime(new Date());
        hdfsSummary.setIsTrash(false);

        System.err.println(hdfsSummary);
        return hdfsSummary;
    }

    private List<QueueMetrics> queryQueueMetrics() throws IOException {
        List<QueueMetrics> queueMetricses = new ArrayList<>();
        Date now = new Date();
        now.setSeconds(0);
        long timestamp = (now.getTime() / 1000);

        List<String> rmUris = Arrays.asList(rmUriStr.split(";"));
        if (rmUris.isEmpty()) {
            return queueMetricses;
        }

        String queueMetricsUrl = String.format(JMXSERVERURLFORMAT, getActiveRmUri(rmUris), QUEUEMETRICSALL);
        HadoopMetrics clusterMetrics = client
                .get(HadoopMetrics.class, queueMetricsUrl, null, null);
        List<Map<String, Object>> beans = clusterMetrics.getBeans();
        if (beans != null) {
            for (Map<String, Object> bean : beans) {
                QueueMetrics qm = new QueueMetrics();
                qm.setAppsPending((Integer) bean.get("AppsPending"));
                qm.setAppsRunning((Integer) bean.get("AppsRunning"));
                qm.setActiveUsers((Integer) bean.get("ActiveUsers"));
                qm.setAllocatedContainers((Integer) bean.get("AllocatedContainers"));
                qm.setAllocatedMB((Integer) bean.get("AllocatedMB"));
                qm.setAvailableMB((Integer) bean.get("AvailableMB"));
                qm.setReservedMB((Integer) bean.get("ReservedMB"));
                qm.setPendingContainers((Integer) bean.get("PendingContainers"));
                qm.setPendingMB((Integer) bean.get("PendingMB"));
                qm.setMetricsTime((int) timestamp);
                qm.setQueueName((String) bean.get("tag.Queue"));
                qm.setCreateTime(new Date());
                queueMetricses.add(qm);
            }
        }
        return queueMetricses;
    }
}
