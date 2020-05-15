package com.rocky.cocoa.core.client.azkaban.response;

import com.rocky.cocoa.core.client.azkaban.model.Node;
import lombok.Data;

import java.util.List;

@Data
public class FetchExecFlowResponse extends BaseResponse {
    private String id;
    private String project;
    private String projectId;
    private String flow;
    private String flowId;
    private String execid;
    private String nestedId;
    private String type;
    private Integer attempt;
    private String submitUser;
    private String status;
    private Long submitTime;
    private Long updateTime;
    private Long startTime;
    private Long endTime;
    private List<Node> nodes;


}
