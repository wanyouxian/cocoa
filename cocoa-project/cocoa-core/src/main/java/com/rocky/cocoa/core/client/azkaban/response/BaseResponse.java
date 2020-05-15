package com.rocky.cocoa.core.client.azkaban.response;

import lombok.Data;

import java.util.Objects;

@Data
public class BaseResponse {
    private String status;
    private String error;
    private String message;

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";


    public void correction() {
        if (!ERROR.equals(this.status) && Objects.isNull(this.error)) {
            this.status = SUCCESS;
        } else {
            this.status = ERROR;
            if (Objects.isNull(this.error)) {
                this.error = this.message;
            } else if (Objects.isNull(this.message)) {
                this.message = this.error;
            }
        }
    }

}
