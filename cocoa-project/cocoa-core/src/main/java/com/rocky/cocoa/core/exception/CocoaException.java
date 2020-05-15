package com.rocky.cocoa.core.exception;

import lombok.Getter;

@Getter
public class CocoaException extends RuntimeException {
    private String errorMessage;
    private int errorCode;

    public CocoaException(String errorMessage, int errorCode, Throwable cause){
        super(cause);
        this.errorMessage=errorMessage;
        this.errorCode=errorCode;
    }

    public CocoaException(String errorMessage, int errorCode){
        super(errorMessage);
        this.errorMessage=errorMessage;
        this.errorCode=errorCode;
    }
}
