package com.file_upload_service.entity.dto;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PartETag;
import com.file_upload_service.setting.OSSProperties;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2017/8/28.
 */
public class PartUploadDTO {

    private MultipartFile localFile;
    private long startPos;
    private long partSize;
    private int partNumber;
    private String uploadId;
    private String Key;
    private PartETag partETag;

    private OSSProperties ossProperties;
    private OSSClient ossClientForUpload;

    public PartUploadDTO() {
    }

    public PartUploadDTO(MultipartFile localFile, long startPos, long partSize, int partNumber, String uploadId, String key, PartETag partETag) {
        this.localFile = localFile;
        this.startPos = startPos;
        this.partSize = partSize;
        this.partNumber = partNumber;
        this.uploadId = uploadId;
        Key = key;
        this.partETag = partETag;
    }


    public MultipartFile getLocalFile() {
        return localFile;
    }

    public void setLocalFile(MultipartFile localFile) {
        this.localFile = localFile;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
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

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public PartETag getPartETag() {
        return partETag;
    }

    public void setPartETag(PartETag partETag) {
        this.partETag = partETag;
    }

    public OSSProperties getOssProperties() {
        return ossProperties;
    }

    public void setOssProperties(OSSProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    public OSSClient getOssClientForUpload() {
        return ossClientForUpload;
    }

    public void setOssClientForUpload(OSSClient ossClientForUpload) {
        this.ossClientForUpload = ossClientForUpload;
    }

    public void setOssClientForUpload(OSSClient ossClientForUpload,OSSProperties ossProperties) {
        this.ossClientForUpload = ossClientForUpload;
        this.ossProperties=ossProperties;
    }
}
