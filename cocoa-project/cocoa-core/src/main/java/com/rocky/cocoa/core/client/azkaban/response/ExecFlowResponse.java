package com.rocky.cocoa.core.client.azkaban.response;

import lombok.Data;

@Data
public class ExecFlowResponse extends BaseResponse {
    private String project;
    private String flow;
    private String execid;
}
