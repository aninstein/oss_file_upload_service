package com.file_upload_service.mapper;

import com.file_upload_service.entity.po.TableColumns;
import com.file_upload_service.entity.po.TableIndexs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by tjhgm on 2017/2/27.
 */
@Mapper
public interface TableColumnsMapper {
    List<TableColumns> selectAll(@Param("tableName") String tableName);

    List<TableIndexs> selectIndex(@Param("tableName") String tableName);
}
