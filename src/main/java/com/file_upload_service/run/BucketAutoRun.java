package com.file_upload_service.run;

import com.aliyun.oss.OSSClient;
import com.file_upload_service.setting.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created by tjhgm on 2017/8/23.
 */
@Component
public class BucketAutoRun implements CommandLineRunner {
    @Autowired
    private OSSClient ossClientForUpload;

    @Autowired
    private OSSProperties ossProperties;

    @Override
    public void run(String... args) throws Exception {
//        //创建bucket
//        if (!ossClientForUpload.doesBucketExist(ossProperties.getBucketName())) {
//            ossClientForUpload.createBucket(ossProperties.getBucketName());
//
//            //增加bucket防盗链功能
//            List<String> refererList = new ArrayList<String>();
//            // 添加referer项
//            refererList.add("http://*.aliyun.com");
//            refererList.add("http://localhost:*");
//            refererList.add("https://*.aliyuncs.com");
//            // 允许referer字段为空，并设置Bucket Referer列表
//            BucketReferer br = new BucketReferer(false, refererList);
//            ossClientForUpload.setBucketReferer(ossProperties.getBucketName(), br);
//        }
    }
}
