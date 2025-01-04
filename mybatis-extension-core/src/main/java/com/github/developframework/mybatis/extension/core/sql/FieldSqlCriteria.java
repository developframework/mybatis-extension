package com.github.developframework.mybatis.extension.core.sql;

import com.github.developframework.mybatis.extension.core.sql.criteria.CriteriaParameter;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;

/**
 * @author qiushui on 2023-09-19.
 */
public abstract class FieldSqlCriteria extends SqlCriteria {

    /**
     * 构建检查null的SqlNode
     */
    protected SqlNode buildSqlNodeCheckNull(SqlNode contentSqlNode, CriteriaParameter... parameters) {
        String test = "";
        for (CriteriaParameter parameter : parameters) {
            if (parameter.type() == CriteriaParameter.CriteriaParameterType.LITERAL) {
                if (!test.isEmpty()) {
                    test += " and ";
                }
                test += parameter.ognlNeqNull();
            }
        }
        if (test.isEmpty()) {
            return contentSqlNode;
        } else {
            return new IfSqlNode(contentSqlNode, test);
        }
    }
}
