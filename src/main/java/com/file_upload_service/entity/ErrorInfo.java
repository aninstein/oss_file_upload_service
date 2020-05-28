package com.file_upload_service.entity;

/**
 * Created by tjhgm on 2017/4/25.
 */
public class ErrorInfo {
    private int errorCode;
    private String errorMessage;

    public ErrorInfo(){

    }

    public ErrorInfo(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
