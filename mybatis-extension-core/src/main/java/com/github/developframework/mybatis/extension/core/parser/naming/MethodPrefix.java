package com.github.developframework.mybatis.extension.core.parser.naming;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.SqlCommandType;

/**
 * 方法名预设前缀
 *
 * @author qiushui on 2023-08-30.
 */
@Getter
@RequiredArgsConstructor
public enum MethodPrefix {

    SELECT_BY("selectBy", SqlCommandType.SELECT, "SELECT * FROM "),
    FIND_BY("findBy", SqlCommandType.SELECT, "SELECT * FROM "),
    QUERY_BY("queryBy", SqlCommandType.SELECT, "SELECT * FROM "),
    EXISTS_BY("existsBy", SqlCommandType.SELECT, "SELECT 1 FROM "),
    HAS_BY("hasBy", SqlCommandType.SELECT, "SELECT 1 FROM "),
    COUNT_BY("countBy", SqlCommandType.SELECT, "SELECT COUNT(*) FROM "),
    DELETE_BY("deleteBy", SqlCommandType.DELETE, "DELETE FROM "),
    REMOVE_BY("removeBy", SqlCommandType.DELETE, "DELETE FROM "),
    INSERT("insert", SqlCommandType.INSERT, "INSERT INTO "),
    INSERT_IGNORE("insertIgnore", SqlCommandType.INSERT, "INSERT IGNORE INTO "),
    REPLACE("replace", SqlCommandType.INSERT, "REPLACE INTO "),
    UPDATE("update", SqlCommandType.UPDATE, "UPDATE ");

    private final String keyword;

    private final SqlCommandType sqlCommandType;

    private final String sqlPrefix;
}
