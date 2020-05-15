package com.rocky.cocoa.server.controller;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskGraph {

    private String taskId;
    private Long projectId;
    private String projectName;
    private String taskName;
    private String admin;
    private String team;
    private String desc;
    private String scheduleCron;
    private List<TaskNode> nodeList;
    private List<NodeEdge> lineList;
    private boolean scheduled;

    @Data
    public static class TaskNode {

        private long pkgId;
        private String id;
        private String name;
        private String pkgName;
        private String pkgVersion;
        private String left;
        private String top;
        private String ico = "el-icon-odometer";
        private boolean show = true;
        private List<Map<String, Object>> params;
    }

    @Data
    public static class NodeEdge {

        private String from;//source job name
        private String to;//target job name
    }

    @Override
    public String toString() {
        return String.format("TaskName=%s,ProjectName=%s", taskName, projectName);
    }
}


