package com.github.developframework.mybatis.boot;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.stereotype.Component;

/**
 * @author qiushui on 2023-10-07.
 */
@Component
public class SimpleConfigurationCustomizer implements ConfigurationCustomizer {

    @Override
    public void customize(Configuration configuration) {
        // 使用驼峰命名法转换字段
        configuration.setMapUnderscoreToCamelCase(true);
        // 允许JDBC 生成主键。需要驱动器支持。如果设为了true，这个设置将强制使用被生成的主键，有一些驱动器不兼容不过仍然可以执行
        configuration.setUseGeneratedKeys(true);
        // 配置日志前缀打印SQL
        configuration.setLogPrefix("resource.mybatis.");
    }
}
