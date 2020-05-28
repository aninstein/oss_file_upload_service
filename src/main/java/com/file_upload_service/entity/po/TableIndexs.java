package com.file_upload_service.entity.po;

/**
 * Created by tjhgm on 2017/3/1.
 */
public class TableIndexs {
    private String key_name;
    private String column_name;
    private String non_unique;

    public String getNon_unique() {
        return non_unique;
    }

    public void setNon_unique(String non_unique) {
        this.non_unique = non_unique;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public String getColumn_name() {
        return column_name;
    }

    public void setColumn_name(String column_name) {
        this.column_name = column_name;
    }
}
