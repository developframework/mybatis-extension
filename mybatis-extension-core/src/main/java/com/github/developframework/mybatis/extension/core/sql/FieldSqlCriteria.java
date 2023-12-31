package com.github.developframework.mybatis.extension.core.sql;

import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * @author qiushui on 2023-09-19.
 */
public abstract class FieldSqlCriteria extends SqlCriteria {

    public FieldSqlCriteria(Configuration configuration) {
        super(configuration);
    }

    protected IfSqlNode buildIfSqlNode(String paramName, SqlFieldPart sqlFieldPart, SqlNode sqlNode) {
        String test;
        if (sqlFieldPart instanceof SqlField sqlField && sqlField.getColumnDefinition().getPropertyType() == String.class) {
            test = paramName + " neq null and " + paramName + " neq ''";
        } else {
            test = paramName + " neq null";
        }
        return new IfSqlNode(sqlNode, test);
    }
}
