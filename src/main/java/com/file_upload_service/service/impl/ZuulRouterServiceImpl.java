package com.file_upload_service.service.impl;

import com.file_upload_service.service.ZuulRouterService;
import org.springframework.stereotype.Component;

/**
 * Created by tjhgm on 2017/3/30.
 */
@Component
public class ZuulRouterServiceImpl implements ZuulRouterService {

    private String verifyPath(String relativePath) {
        if (relativePath.startsWith("/")) {
            return relativePath;
        }
        return "/" + relativePath;
    }
}
