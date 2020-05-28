package com.file_upload_service.service;

import com.aliyun.oss.model.PartETag;

import java.util.List;

/**
 * Created by Administrator on 2017/8/25.
 */
public interface PartUploaderService extends Runnable {

    void run();

    List<PartETag> getpartETags();

}
