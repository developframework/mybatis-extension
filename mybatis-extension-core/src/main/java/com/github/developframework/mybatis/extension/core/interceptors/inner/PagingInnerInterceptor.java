package com.github.developframework.mybatis.extension.core.interceptors.inner;

import com.github.developframework.mybatis.extension.core.annotation.CountStatement;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInvocation;
import com.github.developframework.mybatis.extension.core.interceptors.InterceptContext;
import com.github.developframework.mybatis.extension.core.structs.Page;
import com.github.developframework.mybatis.extension.core.structs.Pager;
import com.github.developframework.mybatis.extension.core.structs.ParameterKeys;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author qiushui on 2023-09-19.
 */
public class PagingInnerInterceptor implements InnerInterceptor {

    /**
     * 拦截Executor#query 在查询列表之前先查询一次总数
     */
    @Override
    public Object executorQuery(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        final Object[] args = innerInvocation.getInvocation().getArgs();
        final Object parameter = args[1];
        final Pager pager = MybatisUtils.find(parameter, Pager.class);
        if (pager != null) {
            final Method mapperMethod = context.getMappedStatementMetadata().getMapperMethod();
            if (mapperMethod.getReturnType() == Page.class) {
                pager.setTotal(
                        queryCount(innerInvocation, mapperMethod, (MappedStatement) args[0], parameter, (RowBounds) args[2], (ResultHandler<?>) args[3])
                );
            }
        }
        return innerInvocation.proceed();
    }

    /**
     * 拦截 StatementHandler#prepare 把原SQL加上LIMIT
     */
    @Override
    public Object statementHandlerPrepare(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        final StatementHandler statementHandler = (StatementHandler) innerInvocation.getInvocation().getTarget();
        final Pager pager = MybatisUtils.find(statementHandler.getParameterHandler().getParameterObject(), Pager.class);
        if (pager != null) {
            // 魔改SQL 拼上LIMIT
            final BoundSql boundSql = statementHandler.getBoundSql();
            final String newSql = boundSql.getSql() + " LIMIT ?, ?";
            final MetaObject boundSqlMeta = SystemMetaObject.forObject(boundSql);
            boundSqlMeta.setValue("sql", newSql);

            /*
                添加参数值
                参考 DefaultParameterHandler的setParameters方法，优先从boundSql.additionalParameters内取值
             */
            boundSql.setAdditionalParameter(ParameterKeys.LIMIT_OFFSET, pager.getOffset());
            boundSql.setAdditionalParameter(ParameterKeys.LIMIT_SIZE, pager.getSize());

            // 添加参数值映射关系
            final List<ParameterMapping> parameterMappings = new ArrayList<>(boundSql.getParameterMappings());
            /*
                需要每次设置一个新的List，因为@Select写法采用的是StaticSqlNode，
                它存下了List<ParameterMapping>，这里需要一个副本增加新的参数映射
             */
            boundSqlMeta.setValue("parameterMappings", parameterMappings);
            final Configuration configuration = (Configuration) SystemMetaObject.forObject(statementHandler).getValue("delegate.configuration");
            parameterMappings.add(
                    new ParameterMapping.Builder(configuration, ParameterKeys.LIMIT_OFFSET, int.class)
                            .mode(ParameterMode.IN)
                            .build()
            );
            parameterMappings.add(
                    new ParameterMapping.Builder(configuration, ParameterKeys.LIMIT_SIZE, int.class)
                            .mode(ParameterMode.IN)
                            .build()
            );
        }
        return innerInvocation.proceed();
    }

    /**
     * 拦截 StatementHandler#query 把返回结果处理成PageResult
     */
    @Override
    public Object statementHandlerQuery(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        final StatementHandler statementHandler = (StatementHandler) innerInvocation.getInvocation().getTarget();
        final Pager pager = MybatisUtils.find(statementHandler.getParameterHandler().getParameterObject(), Pager.class);
        if (pager != null) {
            final Long total = pager.getTotal();
            pager.setTotal(null);
            return new Page<>(
                    pager,
                    total == 0L ? new ArrayList<>() : (List<?>) innerInvocation.proceed(),
                    total
            );
        }
        return innerInvocation.proceed();
    }

    private long queryCount(InnerInvocation innerInvocation, Method mapperMethod, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler<?> resultHandler) throws SQLException {
        final MappedStatement countMs;
        final BoundSql countBoundSql;
        final CountStatement countStatement = mapperMethod.getAnnotation(CountStatement.class);
        if (countStatement == null) {
            final BoundSql boundSql = ms.getBoundSql(parameter);
            final String originalSql = boundSql.getSql();
            final String countSql;
            if (originalSql.substring(0, 13).equalsIgnoreCase("SELECT * FROM")) {
                countSql = "SELECT COUNT(*) FROM" + originalSql.substring(13);
            } else {
                countSql = String.format("SELECT COUNT(*) FROM (%s) _original", originalSql);
            }
            countMs = buildCountMappedStatement(ms);
            countBoundSql = new BoundSql(ms.getConfiguration(), countSql, boundSql.getParameterMappings(), parameter);
            // 需要把boundSql里的additionalParameters都移植过来
            final Map<String, Object> additionalParameters = (Map<String, Object>) SystemMetaObject.forObject(boundSql).getValue("additionalParameters");
            additionalParameters.forEach(countBoundSql::setAdditionalParameter);

        } else {
            String statementId = countStatement.value();
            if (!statementId.contains(".")) {
                statementId = mapperMethod.getDeclaringClass().getName() + "." + statementId;
            }
            countMs = ms.getConfiguration().getMappedStatement(statementId);
            countBoundSql = countMs.getBoundSql(parameter);
        }

        // 排除Pager的parameter
        Object withoutPagerParameter = null;
        if (parameter instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap<Object> paramMap = new MapperMethod.ParamMap<>();
            ((MapperMethod.ParamMap<?>) parameter).forEach((k, v) -> {
                if (!(v instanceof Pager)) {
                    paramMap.put(k, v);
                }
            });
            withoutPagerParameter = paramMap;
        }
        final Executor executor = (Executor) innerInvocation.getInvocation().getTarget();
        final CacheKey cacheKey = executor.createCacheKey(countMs, withoutPagerParameter, rowBounds, countBoundSql);
        final List<?> result = executor.query(countMs, withoutPagerParameter, rowBounds, resultHandler, cacheKey, countBoundSql);
        if (result.isEmpty()) {
            return 0L;
        } else {
            return (Long) result.get(0);
        }
    }

    /**
     * 构建一个新的总数查询的MappedStatement
     */
    private MappedStatement buildCountMappedStatement(MappedStatement ms) {
        final Configuration configuration = ms.getConfiguration();
        return new MappedStatement.Builder(
                configuration,
                ms.getId() + "_count",
                ms.getSqlSource(),
                ms.getSqlCommandType()
        )
                .resource(ms.getResource())
                .fetchSize(ms.getFetchSize())
                .statementType(ms.getStatementType())
                .timeout(ms.getTimeout())
                .parameterMap(ms.getParameterMap())
                .resultMaps(
                        Collections.singletonList(
                                new ResultMap.Builder(configuration, "pager-count-map", Long.class, Collections.emptyList()).build()
                        )
                )
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache())
                .build();
    }
}
