package com.github.ibatis.statement.util;

/**
 * @author junjie
 * @date 2020/2/23
 */
public class StringUtils {

    /**
     * 驼峰转下划线
     * @param str
     * @return
     */
    public static String camelCaseToUnderscore(String str){
        int length = str.length();
        if (str == null || length == 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        char c = chars[0];
        if (c >= 'A' && c <= 'Z'){
            sb.append((char) (c + 32));
        }else {
            sb.append(c);
        }
        for (int i = 1; i < length ;i++) {
            c = chars[i];
            if (c >= 'A' && c <= 'Z'){
                sb.append("_").append((char) (c + 32));
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
