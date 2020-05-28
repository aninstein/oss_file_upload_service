package com.file_upload_service.entity.po;

/**
 * Created by tjhgm on 2017/2/27.
 */
public class TableColumns {
    private String field;
    private String type;
    /**
     * PRI
     */
    private String key;

    /**
     * auto_increment
     */
    private String extra;


    private String Null;

    public String getNull() {
        return Null;
    }

    public void setNull(String aNull) {
        Null = aNull;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
