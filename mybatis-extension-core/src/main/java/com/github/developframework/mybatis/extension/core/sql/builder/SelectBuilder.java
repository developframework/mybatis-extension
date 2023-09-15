package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.sql.SqlStatement;

/**
 * @author qiushui on 2023-09-15.
 */

public class SelectBuilder {

    private final SqlStatement sqlStatement = new SqlStatement();

    public SelectBuilder(SqlFieldPart... fieldParts) {
        sqlStatement.setSelectParts(fieldParts);
    }


}
