package com.file_upload_service.mapper;

import com.file_upload_service.entity.po.DatabaseTables;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by tjhgm on 2017/2/27.
 */
@Mapper
public interface DataBaseTablesMapper {
    List<DatabaseTables> selectAll(String databaseName);

    List<DatabaseTables> selectAllViews(String databaseName);
}
