package com.file_upload_service.entity.dto;

/**
 * Created by Administrator on 2017/8/30.
 */
public class MyPartETag {

    private int partNumber;
    private String etag;
    private long partSize;
    private Long partCRC;

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public Long getPartCRC() {
        return partCRC;
    }

    public void setPartCRC(Long partCRC) {
        this.partCRC = partCRC;
    }
}
