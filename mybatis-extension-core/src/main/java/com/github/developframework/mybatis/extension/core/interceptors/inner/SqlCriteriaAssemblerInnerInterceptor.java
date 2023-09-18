package com.github.developframework.mybatis.extension.core.interceptors.inner;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInvocation;
import com.github.developframework.mybatis.extension.core.interceptors.InterceptContext;
import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.sql.MixedSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaAssembler;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilder;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlRoot;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author qiushui on 2023-09-18.
 */
public class SqlCriteriaAssemblerInnerInterceptor implements InnerInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public Object executorQuery(InnerInvocation innerInvocation, InterceptContext context) throws Throwable {
        final Object[] args = innerInvocation.getInvocation().getArgs();
        final Object parameter = args[1];
        final SqlCriteriaAssembler sqlCriteriaAssembler = MybatisUtils.find(parameter, SqlCriteriaAssembler.class);
        if (sqlCriteriaAssembler != null) {
            final EntityDefinition entityDefinition = context.getEntityDefinition();
            final SqlRoot root = new SqlRoot(entityDefinition);
            final SqlCriteriaBuilder builder = new SqlCriteriaBuilder(entityDefinition);
            SqlCriteria sqlCriteria = sqlCriteriaAssembler.assemble(root, builder);

            // 多租户
            if (entityDefinition.hasMultipleTenant()) {
                final SqlCriteria[] addSqlCriterias = Arrays
                        .stream(entityDefinition.getMultipleTenantColumnDefinitions())
                        .map(cd -> {
                            final AutoInjectProvider autoInjectProvider = context.getAutoInjectProviderRegistry().getAutoInjectProvider(cd.getAutoInjectProviderClass());
                            final Object provideValue = autoInjectProvider.provide(entityDefinition, cd.getPropertyType());
                            return builder.eq(root.get(cd.getProperty()), provideValue);
                        })
                        .toArray(SqlCriteria[]::new);
                if (sqlCriteria == null) {
                    sqlCriteria = new MixedSqlCriteria(Interval.AND, addSqlCriterias);
                } else if (addSqlCriterias.length == 1) {
                    sqlCriteria = new MixedSqlCriteria(Interval.AND, new SqlCriteria[]{addSqlCriterias[0], sqlCriteria});
                } else {
                    sqlCriteria = new MixedSqlCriteria(Interval.AND, ArrayUtils.add(addSqlCriterias, sqlCriteria));
                }
            }

            // 改写MappedStatement
            args[0] = buildMappedStatement((MappedStatement) args[0], entityDefinition, sqlCriteria);

            // 填充参数
            final MapperMethod.ParamMap<Object> criteriaParamMap = builder.getCriteriaParamMap();
            if (parameter instanceof Map) {
                ((Map<String, Object>) parameter).putAll(criteriaParamMap);
            } else {
                args[1] = criteriaParamMap;
            }
        }
        return innerInvocation.proceed();
    }

    /**
     * 构建一个新的MappedStatement
     */
    private MappedStatement buildMappedStatement(MappedStatement ms, EntityDefinition entityDefinition, SqlCriteria sqlCriteria) {
        final String basicSql = "SELECT * FROM " + entityDefinition.wrapTableName();
        final SqlNode sqlNode = sqlCriteria == null ? null : sqlCriteria.toSqlNode().apply(null);
        final Configuration configuration = ms.getConfiguration();
        final SqlSource sqlSource;
        if (sqlNode == null) {
            sqlSource = new StaticSqlSource(configuration, basicSql);
        } else {
            WhereSqlNode whereSqlNode = new WhereSqlNode(configuration, sqlNode);
            sqlSource = new DynamicSqlSource(configuration, new MixedSqlNode(List.of(new StaticTextSqlNode(basicSql), whereSqlNode)));
        }
        return new MappedStatement.Builder(configuration, ms.getId(), sqlSource, ms.getSqlCommandType())
                .resource(ms.getResource())
                .fetchSize(ms.getFetchSize())
                .statementType(ms.getStatementType())
                .timeout(ms.getTimeout())
                .parameterMap(ms.getParameterMap())
                .resultMaps(ms.getResultMaps())
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache())
                .build();
    }
}
