package com.file_upload_service.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tjhgm on 2017/6/25.
 */
public class UtilService {

    public static boolean isNullOrEmpty(String string) {

        return (string == null || string.equals("")) ? true : false;
    }

    public static String changeDataTyple(String dbType) {
        if (dbType.indexOf("bigint") > -1) {
            return "long";
        } else if (dbType.indexOf("int") > -1) {
            return "int";
        } else if (dbType.indexOf("varchar") > -1) {
            return "String";
        } else if (dbType.indexOf("datetime") > -1) {
            return "Date";
        } else if (dbType.indexOf("decimal") > -1) {
            return "double";
        } else if (dbType.indexOf("float") > -1) {
            return "double";
        } else if (dbType.indexOf("double") > -1) {
            return "double";
        } else {
            throw new NullPointerException(dbType + "未定义");
        }
    }

    //首字母转大写
    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    //首字母转小写
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    private static String autoCodePath = "src\\main";//"autoMapper";//

    public static void writeFile(String path, String content) {
        try {
            if (path.startsWith("\\")) {
                path = autoCodePath + path;
            } else {
                path = autoCodePath + "\\" + path;
            }
            File file = new File(path);
            if (!file.exists()) {
                int index = path.lastIndexOf("\\");
                if (index > 0) {
                    File dir = new File(path.substring(0, index));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                }
                file.createNewFile();
            }

            FileOutputStream outSTr = new FileOutputStream(file);

            BufferedOutputStream Buff = new BufferedOutputStream(outSTr);

            Buff.write(content.getBytes());

            Buff.flush();

            Buff.close();
        } catch (Exception ex) {

        }
    }

    public static String lineUserCodeStartSign = "/* code start */";
    public static String lineUserCodeEndSign = "/* code end */";

    public static String lineUserCodeStartSignXML = "<!-- code start -->";
    public static String lineUserCodeEndSignXML = "<!-- code end -->";

    public static String readFile(String path) {
        return readFile(path, null, null);
    }

    public static String readFile(String path, String startSign, String endSign) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            if (path.startsWith("\\")) {
                path = autoCodePath + path;
            } else {
                path = autoCodePath + "\\" + path;
            }
            File file = new File(path);
            if (!file.exists()) {
                int index = path.lastIndexOf("\\");
                if (index > 0) {
                    File dir = new File(path.substring(0, index));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                }
                file.createNewFile();
            }

            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            boolean startRead = false;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                if (!UtilService.isNullOrEmpty(startSign)) {
                    if (tempString.toLowerCase().indexOf(startSign.toLowerCase()) > -1) {
                        startRead = true;
                        continue;
                    }
                } else {
                    startRead = true;
                }

                if (!UtilService.isNullOrEmpty(endSign)) {
                    if (tempString.toLowerCase().indexOf(endSign.toLowerCase()) > -1) {
                        break;
                    }
                }

                if (startRead) {
                    sb.append(tempString + "\n");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    public static List<String[]> readFileLine(String path) {
        List<String[]> result = new ArrayList<String[]>();
        try {
            File file = new File(path);

            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), "UTF-8");//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    result.add(lineTxt.split("\t"));
                }
                read.close();
                bufferedReader.close();
            }
        } catch (Exception e) {

        }
        return result;
    }

    public static String getTSUUID() {

        Random d = new Random(UUID.randomUUID().getLeastSignificantBits());
        Date date = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);//获取年份
        int month = cal.get(Calendar.MONTH);//获取月份
        int day = cal.get(Calendar.DATE);//获取日
        int hour = cal.get(Calendar.HOUR);//小时
        int minute = cal.get(Calendar.MINUTE);//分
        int second = cal.get(Calendar.SECOND);//秒
        int millisecond = cal.get(Calendar.MILLISECOND);
        //订单唯一编号(yyyy+1005)+(MM+10)+(dd+20)+(HH+12)+(mm+30)+(ss+25)+(fff)+(1000+自增主键)
        cal.set(year + 1005, month, day);
        SimpleDateFormat fullString = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
        return fullString.format(cal.getTime()) + (d.nextInt(899) + 100);
    }

    /**
     * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String getDataBaseName(String dataSourceUrl) {
        int point = dataSourceUrl.lastIndexOf('?');
        if (point > -1) {
            dataSourceUrl = dataSourceUrl.substring(0, point);
        }
        return dataSourceUrl.substring(dataSourceUrl.lastIndexOf('/') + 1);
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static String changeContentType(String mime) {
        return "";
    }
}