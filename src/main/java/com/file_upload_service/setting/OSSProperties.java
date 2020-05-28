package com.file_upload_service.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oss",
        ignoreUnknownFields = false)
public class OSSProperties {

    private String endpointUpload;
    private String endpointManager;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String getEndpointUpload() {
        return endpointUpload;
    }

    public void setEndpointUpload(String endpointUpload) {
        this.endpointUpload = endpointUpload;
    }

    public String getEndpointManager() {
        return endpointManager;
    }

    public void setEndpointManager(String endpointManager) {
        this.endpointManager = endpointManager;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
