package com.file_upload_service.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by tjhgm on 2017/6/23.
 */
@Configuration
@ConfigurationProperties(prefix = "auto.mapper",
        ignoreUnknownFields = false)
public class AutoMapperSetting {
    private String group;
    private String name;
    private Boolean openCreate;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getOpenCreate() {
        return openCreate;
    }

    public void setOpenCreate(Boolean openCreate) {
        this.openCreate = openCreate;
    }
}
