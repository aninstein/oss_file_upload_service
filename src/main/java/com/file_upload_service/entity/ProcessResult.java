package com.file_upload_service.entity;

import java.util.Optional;

/**
 * Created by tjhgm on 2017/4/25.
 */
public class ProcessResult<T> {
    private Optional<T> value;
    private Optional<ErrorInfo> error;
    private int errorType;

    public ProcessResult(Optional<T> value, Optional<ErrorInfo> error, int errorType) {
        this.value = value;
        this.error = error;
        this.errorType = errorType;
    }

    public Optional<T> getValue() {
        return value;
    }

    public Optional<ErrorInfo> getError() {
        return this.error;
    }

    public int getErrorType() {
        return errorType;
    }

    public static <T> ProcessResult<T> success(T value) {

        return new ProcessResult(Optional.ofNullable(value), Optional.empty(),
                ProcessResultErrorTypeEnum.ok.ordinal());
    }

    public static <T> ProcessResult<T> fail(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.internalServer.ordinal());
    }

    public static <T> ProcessResult<T> notFound(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.notFound.ordinal());
    }

    public static <T> ProcessResult<T> badRequest(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.badRequest.ordinal());
    }

    public static <T> ProcessResult<T> forbidden(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.forbidden.ordinal());
    }

    public static <T> ProcessResult<T> conflict(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.conflict.ordinal());
    }

    public static <T> ProcessResult<T> unauthorized(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.unauthorized.ordinal());
    }

    public static <T> ProcessResult<T> internalServer(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.internalServer.ordinal());
    }

    public static <T> ProcessResult<T> unspupportedMediaType(int code, String msg) {
        return new ProcessResult(Optional.empty(), Optional.of(new ErrorInfo(code, msg)),
                ProcessResultErrorTypeEnum.unsupportedMediaType.ordinal());
    }
}
