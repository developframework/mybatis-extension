package test.entity;

import com.github.developframework.mybatis.extension.core.annotation.CreateTime;
import com.github.developframework.mybatis.extension.core.annotation.LastModifyTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * @author qiushui on 2023-10-19.
 */
@Getter
@Setter
@FieldNameConstants
public abstract class AuditPO {

    @CreateTime
    private LocalDateTime createTime;

    @LastModifyTime
    private LocalDateTime lastModifyTime;
}
