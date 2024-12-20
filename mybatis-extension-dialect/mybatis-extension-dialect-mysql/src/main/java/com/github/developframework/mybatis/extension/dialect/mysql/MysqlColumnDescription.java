package com.github.developframework.mybatis.extension.dialect.mysql;

import com.github.developframework.mybatis.extension.core.dialect.ColumnDescription;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author qiushui on 2023-08-31.
 */
@Setter
@Getter
@EqualsAndHashCode(of = {"Field", "Type", "Null", "Default", "Extra"})
public class MysqlColumnDescription implements ColumnDescription {

    private String Field;

    private String Type;

    private String Null;

    private String Key;

    private String Default;

    private String Extra;

    private String Comment;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("`")
                .append(Field)
                .append("` ")
                .append(Type);
        if ("YES".equals(Null)) {
            sb.append(" NULL");
            if (Default != null && !Default.isEmpty()) {
                sb.append(" DEFAULT '").append(Default).append("'");
            } else {
                sb.append(" DEFAULT NULL");
            }
        } else {
            sb.append(" NOT NULL");
        }
        if ("auto_increment".equals(Extra)) {
            sb.append(" AUTO_INCREMENT");
        }
        if (Comment != null && !Comment.isEmpty()) {
            sb.append(" COMMENT '").append(Comment).append("'");
        }
        return sb.toString();
    }
}
