package com.file_upload_service.entity.dto;

import com.aliyun.oss.model.PartETag;

/**
 * Created by Administrator on 2017/8/29.
 */
public class PartUploadResultDTO {

    private PartETag partETag;

    private int partNumber;

    private String uploadId;

    public PartETag getPartETag() {
        return partETag;
    }

    public void setPartETag(PartETag partETag) {
        this.partETag = partETag;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
