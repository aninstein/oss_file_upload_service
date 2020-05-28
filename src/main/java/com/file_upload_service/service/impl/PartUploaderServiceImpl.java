package com.file_upload_service.service.impl;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.file_upload_service.service.PartUploaderService;
import com.file_upload_service.setting.OSSProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by aninstein on 2017/8/25.
 */
@Component
public class PartUploaderServiceImpl implements PartUploaderService {
    private MultipartFile localFile;
    private long startPos;
    private long partSize;
    private int partNumber;
    private String uploadId;
    private String Key;
    List<PartETag> partETags;

    private OSSProperties ossProperties;
    private OSSClient ossClientForUpload;

    public void setOss(OSSClient ossClientForUpload,OSSProperties ossProperties) {
        this.ossClientForUpload = ossClientForUpload;
        this.ossProperties = ossProperties;
    }

    public PartUploaderServiceImpl() {//在spring中必须需要一个无参的构造函数
    }

    public PartUploaderServiceImpl(MultipartFile localFile, long startPos, long partSize, int partNumber, String uploadId, String key, List<PartETag> partETags) {
        this.localFile = localFile;
        this.startPos = startPos;
        this.partSize = partSize;
        this.partNumber = partNumber;
        this.uploadId = uploadId;
        Key = key;
        this.partETags = partETags;
    }

    @Override
    public void run() {
        InputStream instream = null;
        try {
            instream = this.localFile.getInputStream();
            instream.skip(this.startPos);

            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(this.ossProperties.getBucketName());
            uploadPartRequest.setKey(Key);
            uploadPartRequest.setUploadId(this.uploadId);
            uploadPartRequest.setInputStream(instream);
            uploadPartRequest.setPartSize(this.partSize);
            uploadPartRequest.setPartNumber(this.partNumber);

            UploadPartResult uploadPartResult = this.ossClientForUpload.uploadPart(uploadPartRequest);
            System.out.println("Part#" + this.partNumber + " done\n");
            synchronized (partETags) {
                partETags.add(uploadPartResult.getPartETag());
                setPartETags(partETags);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void setPartETags(List<PartETag> partETags) {
        this.partETags = partETags;
    }

    public List<PartETag> getpartETags(){
        return this.partETags;
    }
}