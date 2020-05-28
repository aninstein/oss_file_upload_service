package com.file_upload_service.entity.dto;

/**
 * Created by tjhgm on 2017/8/24.
 */
public class AppendFileUploadDTO extends FileUploadDTO {
    private long nextPosition;

    public long getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(long nextPosition) {
        this.nextPosition = nextPosition;
    }
}
