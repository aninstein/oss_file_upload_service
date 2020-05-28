package com.file_upload_service.service;

import com.aliyun.oss.model.CompleteMultipartUploadResult;
import com.aliyun.oss.model.PartETag;
import com.file_upload_service.entity.ProcessResult;
import com.file_upload_service.entity.dto.PartUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by tjhgm on 2017/8/24.
 */
public interface OSSService  {
    ProcessResult<Long> appendFile(MultipartFile file, String filePathKey, Long position);

    ProcessResult<Long> uploadFileWithBadNetwork(String filePath, String filePathKey);

    ProcessResult<String> createPart(String bucketName,String filePathKey);

    ProcessResult<PartUploadDTO> uploadPart(PartUploadDTO partUploadDTO);

    ProcessResult<CompleteMultipartUploadResult> completePart(List<PartETag> partETags, String filePathKey, String uploadId);

    void cancelPart(String uploadId,String filePathKey);

    ProcessResult<List<String>> getPartListSuccess(String uploadId,String filePathKey);

    ProcessResult<List<String>>  getPartListAll(String uploadId,String filePathKey);
}
