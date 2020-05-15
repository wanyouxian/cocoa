package com.rocky.cocoa.core.client.azkaban.model;

import lombok.Data;

@Data
public class Execution {
    private String projectId;
    private String flowId;
    private String execId;
    private String status;
    private String submitUser;
    private String submitTime;
    private String startTime;
    private String endTime;
}
