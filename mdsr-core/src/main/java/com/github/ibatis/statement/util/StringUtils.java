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

    /**
     * 下划线转驼峰
     * @param str
     * @param firstCharUpperCase 首字母是否大写
     * @return
     */
    public static String camelUnderscoreToCase(String str ,boolean firstCharUpperCase){
        char[] chars = str.toCharArray();
        StringBuilder result = new StringBuilder();
        boolean beforeUnderscore = firstCharUpperCase;
        for (char c : chars) {
            if (c == '_'){
                beforeUnderscore = true;
            }else {
                result.append(beforeUnderscore ? Character.toUpperCase(c) : c);
                beforeUnderscore = false;
            }
        }
        return result.toString();
    }

    public static String camelUnderscoreToCase(String str){
        return camelUnderscoreToCase(str ,false);
    }

}
