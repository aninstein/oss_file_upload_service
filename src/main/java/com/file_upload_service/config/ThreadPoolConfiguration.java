package com.file_upload_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tjhgm on 2017/7/24.
 */
@Configuration
public class ThreadPoolConfiguration {
    @Bean
    public ExecutorService initFixedPool() {
        return Executors.newFixedThreadPool(20);
    }
}
