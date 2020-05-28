package com.file_upload_service.Test;

/**
 * Created by Administrator on 2017/8/24.
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;

/**
 * This sample demonstrates how to upload multiparts to Aliyun OSS
 * using the OSS SDK for Java.
 */
public class MultipartUploadSample {

    private static String endpoint = "http://oss-cn-beijing.aliyuncs.com";
    private static String accessKeyId = "LTAICXnmGVrxO9ms";
    private static String accessKeySecret = "nekWLOcOCeyjW9v1taacZlQzsKiNVf";

    private static OSSClient client = null;
    private static String Key="";
    private static String bucketName = "iot-iemylife-predev";
    private static String localFilePath = "G:\\Desktop\\笔记\\很有用的技术书\\技术之瞳+阿里巴巴技术笔试心得.pdf";

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static List<PartETag> partETags = Collections.synchronizedList(new ArrayList<PartETag>());

    public static void main(String[] args) throws IOException {
        /*
         * Constructs a client instance with your account for accessing OSS
         */
        ClientConfiguration conf = new ClientConfiguration();
        conf.setIdleConnectionTime(1000);
        client = new OSSClient(endpoint, accessKeyId, accessKeySecret, conf);

        try {


            /*
             * Calculate how many parts to be divided
             * 计算需要多少块，这里每块5M
             */
            final long partSize = 5 * 1024 * 1024L;   // 5MB
            final File sampleFile = createSampleFile();
            long fileLength = sampleFile.length();
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
             * 设置文件格式
             */
            System.out.println("Begin to upload multiparts to OSS from a file\n");
            Key = "a-cucu/MultipartUpload/" + sampleFile.getName();
            String contentType = null;
            try {
                contentType = Files.probeContentType(Paths.get(Key));
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
             * Claim a upload id firstly
             * 先声明一个upload id
             */
            String uploadId = claimUploadId();
            System.out.println("Claiming a new upload id " + uploadId + "\n");


            /*
             * Upload multiparts to your bucket
             * 上传
             */

            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                executorService.execute(new PartUploader(sampleFile, startPos, curPartSize, i + 1, uploadId));
            }

            /*
             * Waiting for all parts finished
             * 等待所有片上传完关闭连接
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
                System.out.println("Succeed to complete multiparts into an object named " + Key + "\n");
            }

            /*
             * View all parts uploaded recently
             * 查看上传的片
             */
            listAllParts(uploadId);

            /*
             * Complete to upload multiparts
             * 完成上传后合成
             */
            completeMultipartUpload(uploadId);

            /*
             * Fetch the object that newly created at the step below.
             */

//            String backUrl = ossClientForUpload.getEndpoint().toString().replace("://", "://" + ossProperties.getBucketName() + ".") + "/" + Key;
//            if (backUrl.indexOf("https") < 0) {
//                backUrl = backUrl.replace("http", "https");
//            }

            System.out.println("Fetching an object");
//            System.out.println("https://"+bucketName+"."+endpoint+"/"+Key);
            client.getObject(new GetObjectRequest(bucketName, Key), new File(localFilePath));

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorCode());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            if (client != null) {
                client.shutdown();
            }
        }
    }

    public static byte[] File2byte(File file)
    {
        byte[] buffer = null;
        try
        {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }

    private static class PartUploader implements Runnable {

        private File localFile;
        private long startPos;

        private long partSize;
        private int partNumber;
        private String uploadId;

        public PartUploader(File localFile, long startPos, long partSize, int partNumber, String uploadId) {
            this.localFile = localFile;
            this.startPos = startPos;
            this.partSize = partSize;
            this.partNumber = partNumber;
            this.uploadId = uploadId;
        }

        @Override
        public void run() {
            InputStream instream = null;
            try {
                instream = new FileInputStream(this.localFile);
                instream.skip(this.startPos);

                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(Key);
                uploadPartRequest.setUploadId(this.uploadId);
                uploadPartRequest.setInputStream(instream);
                uploadPartRequest.setPartSize(this.partSize);
                uploadPartRequest.setPartNumber(this.partNumber);

                UploadPartResult uploadPartResult = client.uploadPart(uploadPartRequest);
                System.out.println("Part#" + this.partNumber + " done\n");
                synchronized (partETags) {
                    partETags.add(uploadPartResult.getPartETag());
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
    }

    private static File createSampleFile() throws IOException {
//        File file = File.createTempFile("oss-java-sdk-", ".txt");
//        file.deleteOnExit();
//
//        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
//        for (int i = 0; i < 1000000; i++) {
//            writer.write("abcdefghijklmnopqrstuvwxyz\n");
//            writer.write("0123456789011234567890\n");
//        }
//        writer.close();
        File file=new File("G:\\Desktop\\笔记\\很有用的技术书\\技术之瞳+阿里巴巴技术笔试心得.pdf");

        return file;
    }

    private static String claimUploadId() {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, Key);
        InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);
        return result.getUploadId();
    }

    private static void completeMultipartUpload(String uploadId) {
        // Make part numbers in ascending order
        Collections.sort(partETags, new Comparator<PartETag>() {

            @Override
            public int compare(PartETag p1, PartETag p2) {
                return p1.getPartNumber() - p2.getPartNumber();
            }
        });

        System.out.println("Completing to upload multiparts\n");
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, Key, uploadId, partETags);
        client.completeMultipartUpload(completeMultipartUploadRequest);
    }

    private static void listAllParts(String uploadId) {
        System.out.println("Listing all parts......");
        ListPartsRequest listPartsRequest = new ListPartsRequest(bucketName, Key, uploadId);
        PartListing partListing = client.listParts(listPartsRequest);

        int partCount = partListing.getParts().size();
        for (int i = 0; i < partCount; i++) {
            PartSummary partSummary = partListing.getParts().get(i);
            System.out.println("\tPart#" + partSummary.getPartNumber() + ", ETag=" + partSummary.getETag());
        }
        System.out.println();
    }
}