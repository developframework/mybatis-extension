package com.github.developframework.mybatis.extension.core.utils;

/**
 * @author qiushui on 2023-09-01.
 */
public abstract class NameUtils {

    public static String wrap(String name) {
        return "`" + name + "`";
    }

    /**
     * 驼峰转下划线
     */
    public static String camelcaseToUnderline(String camelcaseString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelcaseString.length(); i++) {
            char c = camelcaseString.charAt(i);
            if (i != 0 && c >= 'A' && c <= 'Z') {
                sb.append('_').append((char) (c + 32));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
