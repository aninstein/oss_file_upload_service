package com.file_upload_service.service.impl;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.file_upload_service.entity.ProcessResult;
import com.file_upload_service.entity.dto.PartUploadDTO;
import com.file_upload_service.service.OSSService;
import com.file_upload_service.setting.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by tjhgm on 2017/8/24.
 */
@Component
public class OSSServiceImpl implements OSSService {

    @Autowired
    private OSSClient ossClientForUpload;

    @Autowired
    private OSSProperties ossProperties;

    //追加上传
    @Override
    public ProcessResult<Long> appendFile(MultipartFile file, String filePathKey, Long position) {
        AppendObjectResult appendObjectResult = null;
        try {
            //文件首次创建
            if (position.longValue() == 0) {
                ObjectMetadata meta = new ObjectMetadata();
                String contentType = null;
                try {
                    contentType = Files.probeContentType(Paths.get(filePathKey));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 设置上传内容类型
                if (contentType != null) {
                    meta.setContentType(contentType);
                }
                appendObjectResult = ossClientForUpload.appendObject(
                        new AppendObjectRequest(ossProperties.getBucketName(), filePathKey, file.getInputStream(), meta).withPosition(position));
                CannedAccessControlList accessControlList = CannedAccessControlList.PublicRead;
                ossClientForUpload.setObjectAcl(ossProperties.getBucketName(), filePathKey, accessControlList);
            }
            //追加文件
            else {
                appendObjectResult = ossClientForUpload.appendObject(
                        new AppendObjectRequest(ossProperties.getBucketName(), filePathKey, file.getInputStream()).withPosition(position));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (appendObjectResult != null) {
            return ProcessResult.success(appendObjectResult.getNextPosition());
        } else {
            return ProcessResult.internalServer(1, "IOException");
        }
    }

    //断点续传
    @Override
    public ProcessResult uploadFileWithBadNetwork(String filePath, String filePathKey) {
        // 设置断点续传请求
        UploadFileRequest uploadFileRequest = new UploadFileRequest(ossProperties.getBucketName(), filePathKey);
        // 指定上传的本地文件
        uploadFileRequest.setUploadFile(filePath);
        // 指定上传并发线程数
        uploadFileRequest.setTaskNum(5);
        // 指定上传的分片大小单位byte
        uploadFileRequest.setPartSize(512 * 1024);

        //设置媒体信息MIME
        ObjectMetadata meta = new ObjectMetadata();
        String contentType = null;
        try {
            contentType = Files.probeContentType(Paths.get(filePathKey));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 设置上传内容类型
        if (contentType != null) {
            meta.setContentType(contentType);
        }
        uploadFileRequest.setObjectMetadata(meta);
        // 开启断点续传
        uploadFileRequest.setEnableCheckpoint(true);
        // 断点续传上传
        try {

            ossClientForUpload.uploadFile(uploadFileRequest);

            CannedAccessControlList accessControlList = CannedAccessControlList.PublicRead;
            ossClientForUpload.setObjectAcl(ossProperties.getBucketName(), filePathKey, accessControlList);
            return ProcessResult.success(null);
        } catch (Throwable throwable) {
            throwable.printStackTrace();

            return ProcessResult.internalServer(0, throwable.getMessage());
        }
    }


    /**
     * 以下都是分片上传的代码
     */

    //创建分片Id
    @Override
    public ProcessResult<String> createPart(String bucketName,String filePathKey) {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setIdleConnectionTime(1000);
        ossClientForUpload = new OSSClient(ossProperties.getEndpointUpload(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret(), conf);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, filePathKey);
        InitiateMultipartUploadResult result = ossClientForUpload.initiateMultipartUpload(request);

        return ProcessResult.success(result.getUploadId());
    }

    //上传分片
    @Override
    public ProcessResult<PartUploadDTO> uploadPart(PartUploadDTO partUploadDTO) {
        InputStream instream = null;
        int retimes=3;//允许重试3次
        for (int i = 1; i <=retimes ; i++) {
            try {
                instream = partUploadDTO.getLocalFile().getInputStream();
//            instream.skip(this.startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(ossProperties.getBucketName());
                uploadPartRequest.setKey(partUploadDTO.getKey());
                uploadPartRequest.setUploadId(partUploadDTO.getUploadId());
                uploadPartRequest.setInputStream(instream);
                uploadPartRequest.setPartSize(partUploadDTO.getPartSize());
                uploadPartRequest.setPartNumber(partUploadDTO.getPartNumber());

                UploadPartResult uploadPartResult = ossClientForUpload.uploadPart(uploadPartRequest);
                System.out.println("Part#" + partUploadDTO.getPartNumber() + " done\n");
                synchronized (partUploadDTO.getPartETag()) {
                    partUploadDTO.setPartETag(uploadPartResult.getPartETag());
                }
                break;
            } catch (Exception e) {
                if(i!=retimes){
                    System.out.println("Part#" + partUploadDTO.getPartNumber() + " fail\n");
                    continue;
                }else {
                    e.printStackTrace();
                }
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
        return ProcessResult.success(partUploadDTO);
    }

    //合成分片
    @Override
    public ProcessResult<CompleteMultipartUploadResult> completePart(List<PartETag> partETags, String filePathKey, String uploadId) {

        CompleteMultipartUploadResult result = null;
        /**
         * 合成分片
         */
        Collections.sort(partETags, new Comparator<PartETag>() {
            @Override
            public int compare(PartETag p1, PartETag p2) {
                return p1.getPartNumber() - p2.getPartNumber();
            }
        });
        System.out.println("Completing to upload multiparts\n");
        try {
            CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(ossProperties.getBucketName(), filePathKey, uploadId, partETags);
            result=ossClientForUpload.completeMultipartUpload(completeMultipartUploadRequest);

            //文件权限
            CannedAccessControlList accessControlList = CannedAccessControlList.PublicRead;
            ossClientForUpload.setObjectAcl(ossProperties.getBucketName(), filePathKey, accessControlList);
        }catch (Exception e){
            e.printStackTrace();
        }

        return ProcessResult.success(result);
    }

    //取消上传
    @Override
    public void cancelPart(String uploadId,String filePathKey) {

        // 取消分片上传，其中uploadId来自于initiateMultipartUpload
        AbortMultipartUploadRequest abortMultipartUploadRequest =
                new AbortMultipartUploadRequest(ossProperties.getBucketName(), filePathKey, uploadId);
        ossClientForUpload.abortMultipartUpload(abortMultipartUploadRequest);
    }

    //获取分片列表
    @Override
    public ProcessResult<List<String>> getPartListSuccess(String uploadId,String filePathKey) {
        List<String> partList=new ArrayList<>();
        //System.out.println("Listing all parts......");
        ListPartsRequest listPartsRequest = new ListPartsRequest(ossProperties.getBucketName(), filePathKey, uploadId);
        PartListing partListing = ossClientForUpload.listParts(listPartsRequest);
        int partCount = partListing.getParts().size();
        for (int i = 0; i < partCount; i++) {
            PartSummary partSummary = partListing.getParts().get(i);
            partList.add("Part#" + partSummary.getPartNumber() + ", ETag=" + partSummary.getETag());
        }
        return ProcessResult.success(partList);
    }

    @Override
    public ProcessResult<List<String>>  getPartListAll(String uploadId,String filePathKey){
        List<String> partList=new ArrayList<>();
        // 分页列举已上传的分片
        PartListing partListing;
        ListPartsRequest listPartsRequest = new ListPartsRequest(ossProperties.getBucketName(), filePathKey, uploadId);

        partListing = ossClientForUpload.listParts(listPartsRequest);
        for (PartSummary part : partListing.getParts()) {
            partList.add("Part#:"+part.getPartNumber()+", PartSize:"+part.getSize()+", Etag:"+part.getETag()+", LastModified:"+part.getLastModified());
        }
        return ProcessResult.success(partList);
    }


}
