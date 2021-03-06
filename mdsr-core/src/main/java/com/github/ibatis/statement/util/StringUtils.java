package com.github.ibatis.statement.util;

/**
 * @author X1993
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
     * 首字母大写
     * @param name
     * @return
     */
    public static String firstCharUpperCase(String name) {
        char[] cs = name.toCharArray();
        if (cs[0] >= 'a' && cs[0] <= 'z'){
            cs[0] -= 32;
        }
        return String.valueOf(cs);
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

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param keyword
     * @return
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (keyword != null && keyword.length() > 0) {
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

}
