package com.github.developframework.mybatis.extension.core.interceptors.inner;

import com.github.developframework.mybatis.extension.core.exception.OptimisticLockException;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInvocation;
import com.github.developframework.mybatis.extension.core.interceptors.InterceptContext;

/**
 * @author qiushui on 2023-09-19.
 */
public class OptimisticLockInnerInterceptor implements InnerInterceptor {

    /**
     * 带乐观锁的修改语句执行条数为0是需要报出OptimisticLockException异常
     */
    @Override
    public Object executorUpdate(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        final int modifyCount = (int) innerInvocation.proceed();
        if (context.getEntityDefinition().hasOptimisticLock()) {
            boolean optimisticLock = context.getMappedStatementMetadata().isMethodName("update");
            if (optimisticLock && modifyCount == 0) {
                throw new OptimisticLockException();
            }
        }
        return modifyCount;
    }
}
