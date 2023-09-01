package com.github.developframework.mybatis.extension.launcher;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author qiushui on 2023-08-30.
 */
@Setter
@Getter
@Accessors(chain = true)
public class DataSourceMetadata {

    private String driverClass = "com.mysql.cj.jdbc.Driver";

    private String jdbcUrl;

    private String username;

    private String password;

}
