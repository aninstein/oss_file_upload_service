package com.file_upload_service.run;

import com.file_upload_service.service.MapperCreateService;
import com.file_upload_service.setting.AutoMapperSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created by tjhgm on 2017/7/24.
 */
@Component
public class MapperAutoRun implements CommandLineRunner {
    @Autowired
    private MapperCreateService mapperCreateService;
    @Autowired
    private AutoMapperSetting autoMapperSetting;

    @Override
    public void run(String... args) throws Exception {
        if (autoMapperSetting.getOpenCreate()) {
            mapperCreateService.createTable();
            mapperCreateService.createView();
        }
    }
}
