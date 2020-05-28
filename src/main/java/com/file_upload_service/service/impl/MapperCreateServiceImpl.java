package com.file_upload_service.service.impl;

import com.file_upload_service.entity.po.DatabaseTables;
import com.file_upload_service.entity.po.TableColumns;
import com.file_upload_service.entity.po.TableIndexs;
import com.file_upload_service.mapper.DataBaseTablesMapper;
import com.file_upload_service.mapper.TableColumnsMapper;
import com.file_upload_service.service.MapperCreateService;
import com.file_upload_service.setting.AutoMapperSetting;
import com.file_upload_service.utils.UtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tjhgm on 2017/7/13.
 */
@Component
public class MapperCreateServiceImpl implements MapperCreateService {
    @Autowired
    private DataBaseTablesMapper dataBaseTablesMapper;
    @Autowired
    private TableColumnsMapper tableColumnsMapper;

    @Autowired
    private AutoMapperSetting autoMapperSetting;

    @Autowired
    private DataSourceProperties dataSourceProperties;


    @Override
    public void createTable() {
        try {
            String basePackageStr = autoMapperSetting.getGroup() + "."
                    + autoMapperSetting.getName();
            String packageStr = basePackageStr + ".entity.po";


            String javaPath = "java";
            String resourcesPath = "resources";
            String pathString = basePackageStr.replace(".", "\\");

            List<DatabaseTables> databaseTablesList = dataBaseTablesMapper.selectAll(
                    UtilService.getDataBaseName(dataSourceProperties.getUrl())
            );
            List<List<TableColumns>> tableInfoArray = new ArrayList<>();

            for (DatabaseTables item : databaseTablesList) {
                if (item.getTable_type().equals("VIEW")) {
                    continue;
                }

                String tableName = item.getTable_name();
                String entityFileName = UtilService.toUpperCaseFirstOne(tableName);

                //生成entity

                StringBuilder sb = new StringBuilder();

                String packageInfo = "package " + packageStr + ";\n" +
                        "\n";

                sb.append(packageInfo);

                String headerInfo = "import java.util.Date;\n" +
                        "import java.util.List;\n" +
                        "import java.math.BigDecimal;\n" +
                        "\n" +
                        "/**\n" +
                        " * Created by tjhgm.\n " +
                        " */\n" +
                        "public class " + entityFileName + " {\n";

                sb.append(headerInfo);

                //设置code
                //读取老信息
                String oldCodeString = UtilService.readFile(javaPath + "\\" + pathString + "\\entity\\po\\" + entityFileName + ".java",
                        UtilService.lineUserCodeStartSign, UtilService.lineUserCodeEndSign);

                sb.append(UtilService.lineUserCodeStartSign + "\n" +
                        oldCodeString +
                        UtilService.lineUserCodeEndSign + "\n" +
                        "\n");

                List<TableColumns> tableColumnsList = tableColumnsMapper.selectAll(tableName);
                for (TableColumns columnItem : tableColumnsList) {
                    sb.append("    private " + UtilService.changeDataTyple(columnItem.getType())
                            + " " + columnItem.getField() + ";\n");
                }
                sb.append("\n");

                for (TableColumns columnItem : tableColumnsList) {

                    String functionString = "    public " + UtilService.changeDataTyple(columnItem.getType()) + " get"
                            + UtilService.toUpperCaseFirstOne(columnItem.getField()) + "() {\n" +
                            "        return " + columnItem.getField() + ";\n " +
                            "    }\n" +
                            "\n" +
                            "    public void set" + UtilService.toUpperCaseFirstOne(columnItem.getField())
                            + "(" + UtilService.changeDataTyple(columnItem.getType()) + " " + columnItem.getField() + ") {\n" +
                            "        this." + columnItem.getField() + " = " + columnItem.getField() + ";\n" +
                            "    }\n" +
                            "\n";

                    sb.append(functionString);
                }
                sb.append("}");
                //创建文件
                UtilService.writeFile(javaPath + "\\" + pathString + "\\entity\\po\\" + entityFileName + ".java", sb.toString());


                //生成mapper
                String priFunctionName = "";
                int priCount = 0;
                boolean hasDoublePri = false;

                for (TableColumns columnItem : tableColumnsList) {
                    if (columnItem.getKey().equals("PRI")) {
                        priFunctionName = priFunctionName + "By" + UtilService.toUpperCaseFirstOne(columnItem.getField());
                        priCount = priCount + 1;
                    }
                }
                hasDoublePri = priCount > 1;


                String mapperFileName = entityFileName + "Mapper";

                StringBuilder sbMapper = new StringBuilder();

                sbMapper.append("package " + basePackageStr + ".mapper;\n" +
                        "\n" +
                        "import " + packageStr + "." + entityFileName + ";\n" +
                        "import org.apache.ibatis.annotations.Mapper;\n" +
                        "import org.apache.ibatis.annotations.Param;\n" +
                        "\n" +
                        "import java.util.List;\n" +
                        "import java.util.Map;\n" +
                        "\n" +
                        "/**\n" +
                        " * Created by tjhgm.\n" +
                        " */\n" +
                        "@Mapper\n" +
                        "public interface " + mapperFileName + " {\n");

                //读取老信息
                String oldMapperCodeString = UtilService.readFile(
                        javaPath + "\\" + pathString + "\\mapper\\" + mapperFileName + ".java",
                        UtilService.lineUserCodeStartSign, UtilService.lineUserCodeEndSign);

                sbMapper.append(UtilService.lineUserCodeStartSign + "\n" +
                        oldMapperCodeString +
                        UtilService.lineUserCodeEndSign + "\n" +
                        "\n");

                sbMapper.append("    boolean insert(" + entityFileName
                        + " " + tableName + ");\n" +
                        "\n" +
                        "    boolean update" + priFunctionName + "(" + entityFileName
                        + " " + tableName + ");\n" +
                        "\n" +
                        "    boolean delete" + priFunctionName + "(" + entityFileName
                        + " " + tableName + ");\n" +
                        "\n");

                //增加Updatewithmap
                sbMapper.append("    boolean update" + priFunctionName + "WithMap(@Param(\"" + tableName + "\") "
                        + entityFileName
                        + " " + tableName + ", @Param(\"map\") Map<String, Object> map);\n" +
                        "\n"
                );


                //select by index
                List<TableIndexs> indexArray = tableColumnsMapper.selectIndex(tableName);

                if (indexArray.size() > 0) {
                    //初始化变量
                    String tempIndexName = indexArray.get(0).getKey_name();
                    String selectFunctionName = "By" + UtilService.toUpperCaseFirstOne(indexArray.get(0).getColumn_name());
                    boolean hasMoreColumn = false;
                    List<TableIndexs> indexFilterArray = new ArrayList<TableIndexs>();
                    indexFilterArray.add(indexArray.get(0));
                    boolean isUNIQUE = indexArray.get(0).getNon_unique().equals("0");

                    String indexFilterString = "";
                    String indexFilterByPageString = "";

                    for (int i = 1; i < indexArray.size(); i++) {
                        TableIndexs tableIndexs = indexArray.get(i);

                        if (tableIndexs.getKey_name().equals(tempIndexName)) {
                            //累加
                            if (tableIndexs.getColumn_name().toLowerCase().equals("isactive")) {
                                continue;
                            }

                            selectFunctionName = selectFunctionName
                                    + "By" + UtilService.toUpperCaseFirstOne(tableIndexs.getColumn_name());
                            hasMoreColumn = true;
                        } else {
                            //生成文件
                            for (TableIndexs createItem : indexFilterArray) {
                                indexFilterString = indexFilterString +
                                        (hasMoreColumn ? (", @Param(\"" + createItem.getColumn_name() + "\") ") : ", ")
                                        + UtilService.changeDataTyple(getColumnType(createItem.getColumn_name(), tableColumnsList))
                                        + " " + createItem.getColumn_name();

                                indexFilterByPageString = indexFilterByPageString +
                                        (", @Param(\"" + createItem.getColumn_name() + "\") ")
                                        + UtilService.changeDataTyple(getColumnType(createItem.getColumn_name(), tableColumnsList))
                                        + " " + createItem.getColumn_name();

                            }

                            if (!UtilService.isNullOrEmpty(indexFilterString)) {
                                indexFilterString = indexFilterString.substring(2);
                            }
                            if (!UtilService.isNullOrEmpty(indexFilterByPageString)) {
                                indexFilterByPageString = indexFilterByPageString.substring(2);
                            }
                            sbMapper.append("    " + (isUNIQUE ? entityFileName
                                    : ("List<" + entityFileName + ">"))
                                    + " select" + selectFunctionName + (isUNIQUE ? "Unique" : "") + "(" + indexFilterString + ");\n"
                                    + "\n");
                            //生成分页
                            if (!isUNIQUE) {
                                sbMapper.append("    " + "List<" + entityFileName + ">"
                                        + " select" + selectFunctionName + "ByPage" + "(" + indexFilterByPageString
                                        + ", @Param(\"startIndex\") int startIndex, @Param(\"pageSize\") int pageSize);\n"
                                        + "\n");
                            }


                            //初始化变量
                            tempIndexName = tableIndexs.getKey_name();
                            selectFunctionName = "By" + UtilService.toUpperCaseFirstOne(tableIndexs.getColumn_name());
                            hasMoreColumn = false;
                            indexFilterArray = new ArrayList<TableIndexs>();

                            isUNIQUE = tableIndexs.getNon_unique().equals("0");
                            indexFilterString = "";
                            indexFilterByPageString = "";
                        }
                        indexFilterArray.add(tableIndexs);
                    }

                    //生成文件
                    for (TableIndexs createItem : indexFilterArray) {
                        indexFilterString = indexFilterString +
                                (hasMoreColumn ? (", @Param(\"" + createItem.getColumn_name() + "\") ") : ", ")
                                + UtilService.changeDataTyple(getColumnType(createItem.getColumn_name(), tableColumnsList))
                                + " " + createItem.getColumn_name();
                        indexFilterByPageString = indexFilterByPageString +
                                (", @Param(\"" + createItem.getColumn_name() + "\") ")
                                + UtilService.changeDataTyple(getColumnType(createItem.getColumn_name(), tableColumnsList))
                                + " " + createItem.getColumn_name();
                    }

                    if (!UtilService.isNullOrEmpty(indexFilterString)) {
                        indexFilterString = indexFilterString.substring(2);
                    }
                    if (!UtilService.isNullOrEmpty(indexFilterByPageString)) {
                        indexFilterByPageString = indexFilterByPageString.substring(2);
                    }
                    sbMapper.append("    " + (isUNIQUE ? entityFileName
                            : ("List<" + entityFileName + ">"))
                            + " select" + selectFunctionName + (isUNIQUE ? "Unique" : "") + "(" + indexFilterString + ");\n"
                            + "\n");
                    //生成分页
                    if (!isUNIQUE) {
                        sbMapper.append("    " + "List<" + entityFileName + ">"
                                + " select" + selectFunctionName + "ByPage" + "(" + indexFilterByPageString
                                + ", @Param(\"startIndex\") int startIndex, @Param(\"pageSize\") int pageSize);\n"
                                + "\n");
                    }
                }

                //查全部
                sbMapper.append("    List<" + entityFileName + "> selectAll();\n");

                sbMapper.append("    List<" + entityFileName + "> selectAllByPage(@Param(\"startIndex\") int startIndex, @Param(\"pageSize\") int pageSize);\n");

                sbMapper.append("}");


                UtilService.writeFile(javaPath + "\\" + pathString + "\\mapper\\" + mapperFileName + ".java",
                        sbMapper.toString());


                //生成xml
                StringBuilder sbMapperXML = new StringBuilder();
                sbMapperXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE mapper\n" +
                        "        PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
                        "        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                        "<mapper namespace=\"" + basePackageStr + ".mapper."
                        + mapperFileName + "\">\n");

                //读取老信息
                String oldMapperXMLCodeString = UtilService.readFile(
                        resourcesPath + "\\" + pathString + "\\mapper\\" + mapperFileName + ".xml",
                        UtilService.lineUserCodeStartSignXML, UtilService.lineUserCodeEndSignXML);

                sbMapperXML.append(UtilService.lineUserCodeStartSignXML + "\n" +
                        oldMapperXMLCodeString +
                        UtilService.lineUserCodeEndSignXML + "\n" +
                        "\n");

                //设置insert
                String autoKey = "";
                String insertColumns = "";
                String insertColumnsValue = "";
                for (TableColumns columnItem : tableColumnsList) {
                    if (!columnItem.getExtra().equals("auto_increment")) {
                        insertColumns = insertColumns + ", " + columnItem.getField();
                        insertColumnsValue = insertColumnsValue + ",#{" + columnItem.getField() + "}";
                    } else {
                        if (columnItem.getKey().equals("PRI")) {
                            autoKey = columnItem.getField();
                        }
                    }
                }
                if (!UtilService.isNullOrEmpty(insertColumns)) {
                    insertColumns = insertColumns.substring(2);
                }
                if (!UtilService.isNullOrEmpty(insertColumnsValue)) {
                    insertColumnsValue = insertColumnsValue.substring(1);
                }

                sbMapperXML.append("    <insert id=\"insert\" parameterType=\"" + entityFileName + "\" useGeneratedKeys=\"true\" keyProperty=\"" + autoKey + "\">\n" +
                        "       INSERT INTO " + tableName + "\n" +
                        "         (" + insertColumns + ") VALUES\n" +
                        "        (" + insertColumnsValue + ");\n" +
                        "    </insert>\n");

                //设置update
                boolean hasIsActive = false;
                String updateColumns = "";
                String filterString = "";
                String mapfilterString = "";
                for (TableColumns columnItem : tableColumnsList) {
                    //创建时间不参与更新
                    if (columnItem.getField().equals("createTime")) {
                        continue;
                    }

                    //isActive不参与更新
                    if (columnItem.getField().equals("isActive")) {
                        hasIsActive = true;
                        continue;
                    }
                    //主键不参与更新
                    if (columnItem.getKey().equals("PRI")) {
                        filterString = filterString + " AND " + columnItem.getField()
                                + "=#{" + columnItem.getField() + "}";
                        mapfilterString = mapfilterString + " AND " + columnItem.getField()
                                + "=#{" + tableName + "." + columnItem.getField() + "}";
                        continue;
                    }
                    //自增列不参与更新
                    if (columnItem.getExtra().equals("auto_increment")) {
                        continue;
                    }
                    updateColumns = updateColumns + ",\n" + columnItem.getField()
                            + "=#{" + columnItem.getField() + "}";
                }
                if (!UtilService.isNullOrEmpty(updateColumns)) {
                    updateColumns = updateColumns.substring(2);
                }
                if (!UtilService.isNullOrEmpty(filterString)) {
                    filterString = filterString.substring(5);
                }
                if (!UtilService.isNullOrEmpty(mapfilterString)) {
                    mapfilterString = mapfilterString.substring(5);
                }
                //附加查询条件
                if (hasIsActive) {
                    filterString = filterString + " AND isActive=1";
                    mapfilterString = mapfilterString + " AND isActive=1";
                }

                sbMapperXML.append("    <update id=\"update" + priFunctionName + "\" parameterType=\"" + entityFileName + "\">\n" +
                        "        UPDATE " + tableName + " SET " + updateColumns +
                        "\n WHERE " + filterString + ";\n" +
                        "    </update>\n");

                //updateWithMap
                sbMapperXML.append("    <update id=\"update" + priFunctionName + "WithMap\">\n" +
                        "        UPDATE " + tableName + "\n" +
                        "        <foreach item=\"item\" index=\"key\" collection=\"map\" open=\"SET\" separator=\",\" close=\" \">\n" +
                        "            ${key}=#{item}\n" +
                        "        </foreach>" +
                        "\n WHERE " + mapfilterString + ";\n" +
                        "    </update>\n");

                //delete
                if (hasIsActive) {
                    String updateColumnsDelete = "";

                    for (TableColumns columnItem : tableColumnsList) {
                        if (columnItem.getField().equals("lastChangeTime")) {
                            updateColumnsDelete = updateColumnsDelete + ",\n" + columnItem.getField()
                                    + "=#{" + columnItem.getField() + "}";
                        }
                        if (columnItem.getField().equals("ts")) {
                            updateColumnsDelete = updateColumnsDelete + ",\n" + columnItem.getField()
                                    + "=#{" + columnItem.getField() + "}";
                        }
                    }
                    sbMapperXML.append("    <update id=\"delete" + priFunctionName + "\" parameterType=\"" + entityFileName + "\">\n" +
                            "        UPDATE " + tableName + " SET isActive=0"
                            + updateColumnsDelete +
                            "\n WHERE " + filterString + ";\n" +
                            "    </update>\n");
                } else {
                    sbMapperXML.append("    <delete id=\"delete" + priFunctionName + "\" parameterType=\"" + entityFileName + "\">\n" +
                            "        DELETE FROM " + tableName + " WHERE " + filterString + ";\n" +
                            "    </delete>\n");
                }

                //select all
                sbMapperXML.append("    <select id=\"selectAll\" resultType=\"" + entityFileName + "\">\n" +
                        "        select * from " + tableName + (hasIsActive ? " where isActive=1" : "") + ";\n" +
                        "    </select>\n");
                sbMapperXML.append("    <select id=\"selectAllByPage\" resultType=\"" + entityFileName + "\">\n" +
                        "        select * from " + tableName + (hasIsActive ? " where isActive=1" : "")
                        + " LIMIT #{startIndex},#{pageSize}" + ";\n" +
                        "    </select>\n");

                //select by index
                if (indexArray.size() > 0) {
                    //初始化信息 开始
                    String tempIndexName = indexArray.get(0).getKey_name();
                    String selectFunctionName = "By" + UtilService.toUpperCaseFirstOne(indexArray.get(0).getColumn_name());
                    boolean hasMoreColumn = false;
                    boolean isUNIQUE = indexArray.get(0).getNon_unique().equals("0");

                    String columnName = indexArray.get(0).getColumn_name();
                    String columnType = "";
                    for (TableColumns columnsItem : tableColumnsList) {
                        if (columnsItem.getField().equals(columnName)) {
                            columnType = UtilService.changeDataTyple(columnsItem.getType());
                            break;
                        }
                    }

                    String indexFilterString = " AND " + columnName
                            + "=#{" + columnName + "}";

                    for (int i = 1; i < indexArray.size(); i++) {
                        TableIndexs tableIndexs = indexArray.get(i);

                        if (tableIndexs.getKey_name().equals(tempIndexName)) {
                            if (tableIndexs.getColumn_name().toLowerCase().equals("isactive")) {
                                continue;
                            }
                            //累加
                            selectFunctionName = selectFunctionName
                                    + "By" + UtilService.toUpperCaseFirstOne(tableIndexs.getColumn_name());
                            hasMoreColumn = true;
                            indexFilterString = indexFilterString + " AND " + tableIndexs.getColumn_name()
                                    + "=#{" + tableIndexs.getColumn_name() + "}";
                        } else {
                            //生成文件 结束
                            if (!UtilService.isNullOrEmpty(indexFilterString)) {
                                indexFilterString = indexFilterString.substring(5);
                            }
                            //附加查询条件
                            if (hasIsActive) {
                                indexFilterString = indexFilterString + " AND isActive=1";
                            }

                            sbMapperXML.append("    <select id=\"select" + selectFunctionName + (isUNIQUE ? "Unique" : "") + "\" "
                                    + (hasMoreColumn ? "" : "parameterType=\"" + columnType + "\"") + " resultType=\"" + entityFileName + "\">\n" +
                                    "        SELECT * FROM " + tableName + " WHERE " + indexFilterString + (isUNIQUE ? " limit 1" : "") + ";\n" +
                                    "    </select>\n");
                            sbMapperXML.append("    <select id=\"select" + selectFunctionName + "ByPage" + "\" "
                                    + " resultType=\"" + entityFileName + "\">\n" +
                                    "        SELECT * FROM " + tableName + " WHERE " + indexFilterString + " LIMIT #{startIndex},#{pageSize}" + ";\n" +
                                    "    </select>\n");

                            //初始化信息 开始
                            tempIndexName = tableIndexs.getKey_name();
                            selectFunctionName = "By" + UtilService.toUpperCaseFirstOne(tableIndexs.getColumn_name());
                            hasMoreColumn = false;
                            isUNIQUE = tableIndexs.getNon_unique().equals("0");

                            columnName = tableIndexs.getColumn_name();
                            columnType = "";
                            for (TableColumns columnsItem : tableColumnsList) {
                                if (columnsItem.getField().equals(columnName)) {
                                    columnType = UtilService.changeDataTyple(columnsItem.getType());
                                    break;
                                }
                            }

                            indexFilterString = " AND " + columnName
                                    + "=#{" + columnName + "}";
                        }
                    }

                    //生成文件 结束
                    if (!UtilService.isNullOrEmpty(indexFilterString)) {
                        indexFilterString = indexFilterString.substring(5);
                    }
                    //附加查询条件
                    if (hasIsActive) {
                        indexFilterString = indexFilterString + " AND isActive=1";
                    }


                    sbMapperXML.append("    <select id=\"select" + selectFunctionName + (isUNIQUE ? "Unique" : "") + "\" "
                            + (hasMoreColumn ? "" : "parameterType=\"" + columnType + "\"") + " resultType=\"" + entityFileName + "\">\n" +
                            "        SELECT * FROM " + tableName + " WHERE " + indexFilterString + (isUNIQUE ? " limit 1" : "") + ";\n" +
                            "    </select>\n");
                    sbMapperXML.append("    <select id=\"select" + selectFunctionName + "ByPage" + "\" "
                            + " resultType=\"" + entityFileName + "\">\n" +
                            "        SELECT * FROM " + tableName + " WHERE " + indexFilterString + " LIMIT #{startIndex},#{pageSize}" + ";\n" +
                            "    </select>\n");
                }
                sbMapperXML.append("</mapper> ");

                UtilService.writeFile(
                        resourcesPath + "\\" + pathString + "\\mapper\\" + mapperFileName + ".xml",
                        sbMapperXML.toString());

                tableInfoArray.add(tableColumnsList);

                System.out.println("ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getColumnType(String colmumnName, List<TableColumns> tableColumnsList) {
        for (TableColumns item : tableColumnsList) {
            if (item.getField().equals(colmumnName)) {
                return item.getType();
            }
        }
        throw new NullPointerException("The field type was not found");
    }

    @Override
    public void createView() {

    }
}
