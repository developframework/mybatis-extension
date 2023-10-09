package com.github.developframework.mybatis.boot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author qiushui on 2023-10-07.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mybatis.extension")
public class MybatisExtensionProperties {

    private boolean enableDDL;
}
