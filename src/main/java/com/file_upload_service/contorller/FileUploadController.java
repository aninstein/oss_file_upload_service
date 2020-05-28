package com.file_upload_service.contorller;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.file_upload_service.entity.ProcessResult;
import com.file_upload_service.entity.dto.*;
import com.file_upload_service.service.OSSService;
import com.file_upload_service.service.ResponseErrorService;
import com.file_upload_service.service.impl.PartUploaderServiceImpl;
import com.file_upload_service.setting.OSSProperties;
import com.file_upload_service.utils.UtilService;
import com.iemylife.iot.webtoolkit.BaseController;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tjhgm on 2017/8/22.
 */
@RestController
public class FileUploadController extends BaseController {

    @Autowired
    private OSSClient ossClientForUpload;

    @Autowired
    private ResponseErrorService responseErrorService;

    @Autowired
    private OSSProperties ossProperties;

    @Autowired
    private OSSService ossService;

    @Autowired
    ExecutorService executorService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/uploadFilesOSS")
    @ResponseBody
    public ResponseEntity<?> uploadSingleFile(HttpServletRequest request) {
        if (request.getContentType().equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            ProcessResult errorResult = ProcessResult.unspupportedMediaType(
                    1, "请求仅支持Content-Type为 multipart/form-data的类型");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }


        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);

        //读文件
        Map<String, MultipartFile> files = params.getFileMap();

        //读文件名
        String serviceName = params.getParameter("serviceName");
        if (UtilService.isNullOrEmpty(serviceName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    1, "serviceName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        serviceName = serviceName.toLowerCase();
        //读文件路径
        String path = params.getParameter("path");
        if (UtilService.isNullOrEmpty(path)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    2, "path不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.toLowerCase();
        //文件名称
        String fileName = params.getParameter("fileName");
        if (UtilService.isNullOrEmpty(fileName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    3, "fileName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        fileName = fileName.toLowerCase();
        //文件类型
        String ext = params.getParameter("ext");
        if (UtilService.isNullOrEmpty(ext)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "ext不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        ext = ext.toLowerCase();

        //文件权限
        CannedAccessControlList accessControlList = CannedAccessControlList.PublicReadWrite;

        FileUploadDTO fileUploadDTO = new FileUploadDTO();

        MultipartFile file = null;

        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            file = entry.getValue();
            if (!file.isEmpty()) {
                String key = serviceName + "/" + path + "/" + fileName + "." + ext;
                String contentType = null;
                try {
                    contentType = Files.probeContentType(Paths.get(key));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //读取文件流
                byte[] bytes = new byte[0];
                try {
                    bytes = file.getBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 创建上传Object的Metadata
                ObjectMetadata meta = new ObjectMetadata();
                // 设置上传MD5校验
                String md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(bytes));
                meta.setContentMD5(md5);
                // 设置上传内容类型
                if (contentType != null) {
                    meta.setContentType(contentType);
                }

                InputStream is = new ByteArrayInputStream(bytes);
                ossClientForUpload.putObject(ossProperties.getBucketName(), key, is, meta);
                ossClientForUpload.setObjectAcl(ossProperties.getBucketName(), key, accessControlList);

                String backUrl = ossClientForUpload.getEndpoint().toString().replace("://", "://" + ossProperties.getBucketName() + ".") + "/" + key;
                if (backUrl.indexOf("https") < 0) {
                    backUrl = backUrl.replace("http", "https");
                }
                fileUploadDTO.setFilePath(backUrl);
            } else {
                ProcessResult errorResult = ProcessResult.badRequest(
                        5, "未检索到上传的文件");
                return fail(responseErrorService.changeErrorResult(errorResult),
                        responseErrorService.changeHttpStatus(errorResult));
            }
        }
        return success(fileUploadDTO);
    }

    @PostMapping("/appendFiles")
    @ResponseBody
    public ResponseEntity<?> appendFiles(HttpServletRequest request) {
        if (request.getContentType().equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            ProcessResult errorResult = ProcessResult.unspupportedMediaType(
                    1, "请求仅支持Content-Type为 multipart/form-data的类型");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }

        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);

        //读文件
        Map<String, MultipartFile> files = params.getFileMap();

        //读文件名
        String serviceName = params.getParameter("serviceName");
        if (UtilService.isNullOrEmpty(serviceName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    1, "serviceName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        serviceName = serviceName.toLowerCase();
        //读文件路径
        String path = params.getParameter("path");
        if (UtilService.isNullOrEmpty(path)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    2, "path不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.toLowerCase();
        //文件名称
        String fileName = params.getParameter("fileName");
        if (UtilService.isNullOrEmpty(fileName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    3, "fileName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        fileName = fileName.toLowerCase();
        //文件类型
        String ext = params.getParameter("ext");
        if (UtilService.isNullOrEmpty(ext)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "ext不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        ext = ext.toLowerCase();

        //文件上传位置
        Long position = 0L;
        try {
            position = Long.valueOf(params.getParameter("position"));
        } catch (Exception ex) {

        }

        AppendFileUploadDTO fileUploadDTO = new AppendFileUploadDTO();

        MultipartFile file = null;

        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            file = entry.getValue();
            if (!file.isEmpty()) {
                String key = serviceName + "/" + path + "/" + fileName + "." + ext;

                ProcessResult<Long> result = ossService.appendFile(file, key, position);
                if (result.getError().isPresent()) {
                    return fail(responseErrorService.changeErrorResult(result),
                            responseErrorService.changeHttpStatus(result));
                }

                String backUrl = ossClientForUpload.getEndpoint().toString().replace("://", "://" + ossProperties.getBucketName() + ".") + "/" + key;
                if (backUrl.indexOf("https") < 0) {
                    backUrl = backUrl.replace("http", "https");
                }
                fileUploadDTO.setFilePath(backUrl);
                fileUploadDTO.setNextPosition(result.getValue().get());
            } else {
                ProcessResult errorResult = ProcessResult.badRequest(
                        5, "未检索到上传的文件");
                return fail(responseErrorService.changeErrorResult(errorResult),
                        responseErrorService.changeHttpStatus(errorResult));
            }
        }
        return success(fileUploadDTO);
    }

    @PostMapping("/uploadFileWithBadNetwork")
    @ResponseBody
    public ResponseEntity<?> uploadFileWithBadNetwork(HttpServletRequest request) {
        if (request.getContentType().equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            ProcessResult errorResult = ProcessResult.unspupportedMediaType(
                    1, "请求仅支持Content-Type为 multipart/form-data的类型");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }

        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);

        //读文件
        Map<String, MultipartFile> files = params.getFileMap();

        //读文件名
        String serviceName = params.getParameter("serviceName");
        if (UtilService.isNullOrEmpty(serviceName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    1, "serviceName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        serviceName = serviceName.toLowerCase();
        //读文件路径
        String path = params.getParameter("path");
        if (UtilService.isNullOrEmpty(path)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    2, "path不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.toLowerCase();
        //文件名称
        String fileName = params.getParameter("fileName");
        if (UtilService.isNullOrEmpty(fileName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    3, "fileName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        fileName = fileName.toLowerCase();
        //文件类型
        String ext = params.getParameter("ext");
        if (UtilService.isNullOrEmpty(ext)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "ext不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        ext = ext.toLowerCase();

        //读文件路径
        String filePath = params.getParameter("filePath");
        if (UtilService.isNullOrEmpty(filePath)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    5, "filePath不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }

        FileUploadDTO fileUploadDTO = new FileUploadDTO();

        String key = serviceName + "/" + path + "/" + fileName + "." + ext;

        ProcessResult<Long> result = ossService.uploadFileWithBadNetwork(filePath, key);
        if (result.getError().isPresent()) {
            return fail(responseErrorService.changeErrorResult(result),
                    responseErrorService.changeHttpStatus(result));
        }

        String backUrl = ossClientForUpload.getEndpoint().toString().replace("://", "://" + ossProperties.getBucketName() + ".") + "/" + key;
        if (backUrl.indexOf("https") < 0) {
            backUrl = backUrl.replace("http", "https");
        }
        fileUploadDTO.setFilePath(backUrl);

        return success(fileUploadDTO);
    }

    /**
     * 一下都是分片上传的代码
     * @param request
     */
    @PostMapping("/createUploadId")
    @ResponseBody
    public String createUploadId(HttpServletRequest request) throws IOException {
        String key=request.getParameter("key");
        if (UtilService.isNullOrEmpty(key)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "key不正确");
        }
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        /**
         * 获取uploadId
         */
        ProcessResult<String> uploadId = ossService.createPart(ossProperties.getBucketName(),key);

        PartUploadResultDTO partUploadResultDTO=new PartUploadResultDTO();
        if(uploadId.getValue().get()!=null){
            return uploadId.getValue().get();
        }else{
            throw new IllegalArgumentException("create uploadId fail the reason is key is null");
        }
    }

    @PostMapping("/sliceUploadFile")
    @ResponseBody
    public ResponseEntity<?> sliceUploadFile(HttpServletRequest request) throws IOException {
        if (request.getContentType().equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            ProcessResult errorResult = ProcessResult.unspupportedMediaType(1, "请求仅支持Content-Type为 multipart/form-data的类型");
            return fail(responseErrorService.changeErrorResult(errorResult), responseErrorService.changeHttpStatus(errorResult));
        }

        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);

        //读文件Key
        Map<String, MultipartFile> files = params.getFileMap();

        String key=request.getParameter("Key");
        if (UtilService.isNullOrEmpty(key)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "key不正确");
        }
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        //分片号
        String partNo = params.getParameter("partNo");
        if (UtilService.isNullOrEmpty(partNo)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "partNo不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (partNo.startsWith("0")) {
            partNo = "1";
        }

        //分片所属文件号
        String uploadId = params.getParameter("fileId");
        if (UtilService.isNullOrEmpty(uploadId)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "uploadId不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }

        //分片号
        String partCount = params.getParameter("partCount");
        if (UtilService.isNullOrEmpty(partCount)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "partCount不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (partCount.startsWith("0")) {
            partCount = "1";
        }

//        List<PartETag> partETags = Collections.synchronizedList(new ArrayList<PartETag>());

        PartETag partETag=new PartETag(Integer.parseInt(partNo),"");

        PartUploadResultDTO partUploadResultDTO = new PartUploadResultDTO();

        MultipartFile file = null;

        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            file = entry.getValue();
            if (!file.isEmpty()) {
                /**
                 * 上传分片
                 */
                long fileLength = file.getSize();
                PartUploadDTO partUploadDTO=new PartUploadDTO();
                partUploadDTO.setLocalFile(file);
                partUploadDTO.setKey(key);
                partUploadDTO.setUploadId(uploadId);
                partUploadDTO.setPartSize(fileLength);
                partUploadDTO.setPartNumber(Integer.parseInt(partNo));
                partUploadDTO.setPartETag(partETag);
                ProcessResult<PartUploadDTO> uploadDTOProcessResult=ossService.uploadPart(partUploadDTO);

                /**
                 * 验证分片已经上传成功
                 */
                if (uploadDTOProcessResult.getValue().get().getPartETag() != null) {
                    System.out.println("sliceUpload Success!");
                } else {
                    throw new IllegalStateException("Upload multiparts fail due to some parts are not finished yet");
                }

                //返回文件Url地址，代表上传成功
                String backUrl = ossClientForUpload.getEndpoint().toString().replace("://", "://" + ossProperties.getBucketName() + ".") + "/" + key;//URLEncoder.encode(fileName,"utf-8")
                if (backUrl.indexOf("https") < 0) {
                    backUrl = backUrl.replace("http", "https");
                }

                /**
                 * 设置返回值
                 */
                partUploadResultDTO.setPartNumber(Integer.parseInt(partNo));
                partUploadResultDTO.setPartETag(uploadDTOProcessResult.getValue().get().getPartETag());
                partUploadResultDTO.setUploadId(uploadId);
            } else {
                ProcessResult errorResult = ProcessResult.badRequest(
                        5, "未检索到上传的文件");
                return fail(responseErrorService.changeErrorResult(errorResult),
                        responseErrorService.changeHttpStatus(errorResult));
            }
        }
        return success(partUploadResultDTO);
    }

    @PostMapping("/completePart")
    @ResponseBody
    public ResponseEntity<?> completePart(HttpServletRequest request) throws IOException {
        if (request.getContentType().equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            ProcessResult errorResult = ProcessResult.unspupportedMediaType(1, "请求仅支持Content-Type为 multipart/form-data的类型");
            return fail(responseErrorService.changeErrorResult(errorResult), responseErrorService.changeHttpStatus(errorResult));
        }

        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);

//        //读文件名
//        String serviceName = params.getParameter("completeserviceName");
//        if (UtilService.isNullOrEmpty(serviceName)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    1, "completeserviceName不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        serviceName = serviceName.toLowerCase();
//
//        //读文件路径
//        String path = params.getParameter("completePath");
//        if (UtilService.isNullOrEmpty(path)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    2, "completePath不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        if (path.startsWith("/")) {
//            path = path.substring(1);
//        }
//        path = path.toLowerCase();
//
//        //文件名称
//        String fileName = params.getParameter("completeName");
//        if (UtilService.isNullOrEmpty(fileName)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    3, "completeName不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        if (fileName.startsWith("/")) {
//            fileName = fileName.substring(1);
//        }
//        fileName = fileName.toLowerCase();
//
//        //文件类型
//        String ext = params.getParameter("completeext");
//        if (UtilService.isNullOrEmpty(ext)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    4, "completeext不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        if (ext.startsWith(".")) {
//            ext = ext.substring(1);
//        }
//        ext = ext.toLowerCase();

        String key=request.getParameter("Key");
        if (UtilService.isNullOrEmpty(key)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "key不正确");
        }
        if (key.startsWith("/")) {
            key = key.substring(1);
        }


        //文件号
        String uploadId = params.getParameter("uploadId");
        if (UtilService.isNullOrEmpty(uploadId)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "uploadId不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }

        //分片的partEtags
        String[] requestpartEtags = params.getParameterValues("partEtag");
        if (requestpartEtags.length==0) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "partEtags不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }

        //总的分片数
        String partNumber = params.getParameter("partCount");
        if (UtilService.isNullOrEmpty(partNumber)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "partCount不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }


        /**
         * 把partEtags字符串变成PartEtag对象
         */
        List<PartETag> partETags = Collections.synchronizedList(new ArrayList<PartETag>());//多线程状态下用于防止同时写或者读list
        //List<PartETag> partETags = new ArrayList<PartETag>();
        for (int i = 0; i <requestpartEtags.length ; i++) {
            System.out.println(requestpartEtags[i]);
            MyPartETag myPartETag=objectMapper.readValue(requestpartEtags[i],MyPartETag.class);
            PartETag partETag=new PartETag(
                    myPartETag.getPartNumber(),
                    myPartETag.getEtag());
            partETags.add(partETag);
        }

        FileUploadDTO fileUploadDTO = new FileUploadDTO();

        if (requestpartEtags.length!=Integer.parseInt(partNumber)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    5, "还有未上传完成的分片");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));

        } else {
            ProcessResult<CompleteMultipartUploadResult> completeResult=ossService.completePart(partETags,key,uploadId);
            if (completeResult.getValue().get() != null) {
                System.out.println("CompletePart Success!");
            } else {
                throw new IllegalStateException("CompletePart fail!");
            }

            String backUrl = ossClientForUpload.getEndpoint().toString().replace("://", "://" + ossProperties.getBucketName() + ".") + "/" + key;
            if (backUrl.indexOf("https") < 0) {
                backUrl = backUrl.replace("http", "https");
            }
            fileUploadDTO.setFilePath(backUrl);
        }
        return success(fileUploadDTO);
    }

    @PostMapping("/cancelPart")
    @ResponseBody
    public ResponseEntity<?> cancelPart(HttpServletRequest request) throws IOException {

        //MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);

//        //读文件名
//        String serviceName = params.getParameter("completeserviceName");
//        if (UtilService.isNullOrEmpty(serviceName)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    1, "completeserviceName不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        serviceName = serviceName.toLowerCase();
//
//        //读文件路径
//        String path = params.getParameter("completePath");
//        if (UtilService.isNullOrEmpty(path)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    2, "completePath不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        if (path.startsWith("/")) {
//            path = path.substring(1);
//        }
//        path = path.toLowerCase();
//
//        //文件名称
//        String fileName = params.getParameter("completeName");
//        if (UtilService.isNullOrEmpty(fileName)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    3, "completeName不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        if (fileName.startsWith("/")) {
//            fileName = fileName.substring(1);
//        }
//        fileName = fileName.toLowerCase();
//
//        //文件类型
//        String ext = params.getParameter("completeext");
//        if (UtilService.isNullOrEmpty(ext)) {
//            ProcessResult errorResult = ProcessResult.badRequest(
//                    4, "completeext不正确");
//            return fail(responseErrorService.changeErrorResult(errorResult),
//                    responseErrorService.changeHttpStatus(errorResult));
//        }
//        if (ext.startsWith(".")) {
//            ext = ext.substring(1);
//        }
//        ext = ext.toLowerCase();

        String key=request.getParameter("key");
        if (UtilService.isNullOrEmpty(key)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "key不正确");
        }
        if (key.startsWith("/")) {
            key = key.substring(1);
        }


        //文件号
        String uploadId = request.getParameter("uploadId");
        if (UtilService.isNullOrEmpty(uploadId)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "uploadId不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }

        //取消上传
        ossService.cancelPart(uploadId,key);
        return success(true);
    }

    @PostMapping("/sliceUploadFileTest")
    @ResponseBody
    public ResponseEntity<?> sliceUploadFileTest(HttpServletRequest request) throws IOException {
        if (request.getContentType().equals(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
            ProcessResult errorResult = ProcessResult.unspupportedMediaType(1, "请求仅支持Content-Type为 multipart/form-data的类型");
            return fail(responseErrorService.changeErrorResult(errorResult), responseErrorService.changeHttpStatus(errorResult));
        }

        MultipartHttpServletRequest params = ((MultipartHttpServletRequest) request);

        //读文件
        Map<String, MultipartFile> files = params.getFileMap();

        //读文件名
        String serviceName = params.getParameter("serviceName");
        if (UtilService.isNullOrEmpty(serviceName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    1, "serviceName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        serviceName = serviceName.toLowerCase();
        //读文件路径
        String path = params.getParameter("path");
        if (UtilService.isNullOrEmpty(path)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    2, "path不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.toLowerCase();
        //文件名称
        String fileName = params.getParameter("fileName");
        if (UtilService.isNullOrEmpty(fileName)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    3, "fileName不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        fileName = fileName.toLowerCase();
        //文件类型
        String ext = params.getParameter("ext");
        if (UtilService.isNullOrEmpty(ext)) {
            ProcessResult errorResult = ProcessResult.badRequest(
                    4, "ext不正确");
            return fail(responseErrorService.changeErrorResult(errorResult),
                    responseErrorService.changeHttpStatus(errorResult));
        }
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        ext = ext.toLowerCase();

        List<PartETag> partETags = Collections.synchronizedList(new ArrayList<PartETag>());

        FileUploadDTO fileUploadDTO = new FileUploadDTO();

        MultipartFile file = null;

        for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
            file = entry.getValue();
            if (!file.isEmpty()) {
                String key = serviceName + "/" + path + "/" + fileName + "." + ext;

                /*
                 * Calculate how many parts to be divided
                 * 计算需要多少块，这里每块5M
                 */
                final long partSize = 5 * 1024 * 1024L;   // 5MB
                long fileLength = file.getSize();
                int partCount = (int) (fileLength / partSize);
                if (fileLength % partSize != 0) {
                    partCount++;
                }
                if (partCount > 10000) {//最多只能发10000片
                    throw new RuntimeException("Total parts count should not exceed 10000");
                } else {
                    System.out.println("Total parts count " + partCount + "\n");
                }


                /**
                 * 获取uploadId
                 */
                ProcessResult<String> uploadId= ossService.createPart(ossProperties.getBucketName(),key);


                /**
                 * 对上传文件的格式跟类型做了定义
                 */
                String contentType = null;
                try {
                    contentType = Files.probeContentType(Paths.get(key));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                /**
                 * 分片上传代码
                 */
                for (int i = 0; i < partCount; i++) {
                    long startPos = i * partSize;
                    long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                    PartUploaderServiceImpl partUploader=new PartUploaderServiceImpl(file, startPos, curPartSize, i + 1, uploadId.getValue().get(),key,partETags);
                    partUploader.setOss(ossClientForUpload,ossProperties);
                    executorService.execute(partUploader);
                    partETags=partUploader.getpartETags();


//                    InputStream instream = file.getInputStream();
//                    instream.skip(startPos);
//
//                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
//                    uploadPartRequest.setBucketName(ossProperties.getBucketName());
//                    uploadPartRequest.setKey(key);
//                    uploadPartRequest.setUploadId(uploadId.getValue().get());
//                    uploadPartRequest.setInputStream(instream);
//                    uploadPartRequest.setPartSize(curPartSize);
//                    uploadPartRequest.setPartNumber(i+1);
//
//                    UploadPartResult uploadPartResult = ossClientForUpload.uploadPart(uploadPartRequest);
//                    System.out.println("Part#" + (i+1) + " done\n");
//                    synchronized (partETags) {
//                        partETags.add(uploadPartResult.getPartETag());
//                    }

                }

                /**
                 * 等待所有所有线程都执行完，然后关闭线程池
                 */
                executorService.shutdown();
                while (!executorService.isTerminated()) {
                    try {
                        executorService.awaitTermination(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /*
                 * Verify whether all parts are finished
                 * 验证所有的片都已经上传成功
                 */
                if (partETags.size() != partCount) {
                    throw new IllegalStateException("Upload multiparts fail due to some parts are not finished yet");
                } else {
                    System.out.println("sliceUpload Success!");
                }

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
                CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(ossProperties.getBucketName(), key, uploadId.getValue().get(), partETags);
                ossClientForUpload.completeMultipartUpload(completeMultipartUploadRequest);

                //文件权限
                CannedAccessControlList accessControlList = CannedAccessControlList.PublicRead;
                ossClientForUpload.setObjectAcl(ossProperties.getBucketName(), key, accessControlList);

                //返回文件Url地址，代表上传成功
                String backUrl = ossClientForUpload.getEndpoint().toString().replace("://", "://" + ossProperties.getBucketName() + ".") + "/" + serviceName + "/" + path + "/" + URLEncoder.encode(fileName,"utf-8") + "." + ext;
                if (backUrl.indexOf("https") < 0) {
                    backUrl = backUrl.replace("http", "https");
                }
                fileUploadDTO.setFilePath(backUrl);
            } else {
                ProcessResult errorResult = ProcessResult.badRequest(
                        5, "未检索到上传的文件");
                return fail(responseErrorService.changeErrorResult(errorResult),
                        responseErrorService.changeHttpStatus(errorResult));
            }
        }
        return success(fileUploadDTO);
    }
}