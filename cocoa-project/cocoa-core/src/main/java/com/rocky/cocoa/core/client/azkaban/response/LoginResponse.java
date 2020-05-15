package com.rocky.cocoa.core.client.azkaban.response;

import lombok.Data;

@Data
public class LoginResponse extends BaseResponse {
    private String sessionId;
}
