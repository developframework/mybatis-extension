package com.github.developframework.mybatis.extension.core.interceptors.inner;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInterceptor;
import com.github.developframework.mybatis.extension.core.interceptors.InnerInvocation;
import com.github.developframework.mybatis.extension.core.interceptors.InterceptContext;
import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.sql.MixedSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlSortPart;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaAssembler;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilder;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlRoot;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
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
        final MappedStatementMetadata metadata = context.getMappedStatementMetadata();
        if (metadata.isHasSqlCriteriaAssembler()) {
            final SqlCriteriaAssembler sqlCriteriaAssembler = MybatisUtils.find(parameter, SqlCriteriaAssembler.class);

            MappedStatement mappedStatement = (MappedStatement) args[0];
            final Configuration configuration = mappedStatement.getConfiguration();
            final EntityDefinition entityDefinition = context.getEntityDefinition();
            final SqlRoot root = new SqlRoot(entityDefinition);
            final SqlCriteriaBuilder builder = new SqlCriteriaBuilder(configuration, entityDefinition);
            SqlCriteria sqlCriteria = null;
            if (sqlCriteriaAssembler != null) {
                sqlCriteria = sqlCriteriaAssembler.assemble(root, builder);
            }

            // 多租户
            if (entityDefinition.hasMultipleTenant()) {
                final SqlCriteria[] addSqlCriterias = Arrays
                        .stream(entityDefinition.getMultipleTenantColumnDefinitions())
                        .map(cd -> {
                            final AutoInjectProvider autoInjectProvider = context.getAutoInjectProviderRegistry().getAutoInjectProvider(cd.getAutoInjectProviderClass());
                            final Object provideValue = autoInjectProvider.provide(entityDefinition, cd, parameter);
                            return builder.eq(root.get(cd.getProperty()), provideValue);
                        })
                        .toArray(SqlCriteria[]::new);
                if (sqlCriteria == null) {
                    sqlCriteria = new MixedSqlCriteria(configuration, Interval.AND, addSqlCriterias);
                } else if (addSqlCriterias.length == 1) {
                    sqlCriteria = new MixedSqlCriteria(configuration, Interval.AND, new SqlCriteria[]{addSqlCriterias[0], sqlCriteria});
                } else {
                    sqlCriteria = new MixedSqlCriteria(configuration, Interval.AND, ArrayUtils.add(addSqlCriterias, sqlCriteria));
                }
            }

            final SqlSortPart sqlSortPart = MybatisUtils.find(parameter, SqlSortPart.class);

            // 改写MappedStatement
            args[0] = buildMappedStatement(metadata, mappedStatement, entityDefinition, sqlCriteria, sqlSortPart);

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
    private MappedStatement buildMappedStatement(MappedStatementMetadata metadata, MappedStatement mappedStatement, EntityDefinition entityDefinition, SqlCriteria sqlCriteria, SqlSortPart sqlSortPart) {
        final boolean exists = "exists".equals(metadata.getMapperMethod().getName());
        final String basicSql;
        if (exists) {
            basicSql = "SELECT 1 FROM " + entityDefinition.wrapTableName();
        } else {
            basicSql = "SELECT * FROM " + entityDefinition.wrapTableName();
        }

        final SqlNode sqlNode = sqlCriteria == null ? null : sqlCriteria.toSqlNode().apply(Interval.EMPTY);
        final String orderBySql = sqlSortPart == null ? "" : (" ORDER BY " + sqlSortPart.toSql(entityDefinition));
        final Configuration configuration = mappedStatement.getConfiguration();
        final SqlSource sqlSource;
        if (sqlNode == null) {
            String finalSql = basicSql + orderBySql;
            if (exists) {
                finalSql = "SELECT IFNULL((" + finalSql + "LIMIT 1), 0) `exists`";
            }
            sqlSource = new StaticSqlSource(configuration, finalSql);
        } else {
            List<SqlNode> sqlNodes = new ArrayList<>(5);
            if (exists) {
                sqlNodes.add(new StaticTextSqlNode("SELECT IFNULL(("));
            }
            sqlNodes.add(new StaticTextSqlNode(basicSql));
            sqlNodes.add(new WhereSqlNode(configuration, sqlNode));
            sqlNodes.add(new StaticTextSqlNode(orderBySql));
            if (exists) {
                sqlNodes.add(new StaticTextSqlNode("LIMIT 1), 0) `exists`"));
            }
            sqlSource = new DynamicSqlSource(configuration, new MixedSqlNode(sqlNodes));
        }
        return new MappedStatement.Builder(configuration, mappedStatement.getId(), sqlSource, mappedStatement.getSqlCommandType())
                .resource(mappedStatement.getResource())
                .fetchSize(mappedStatement.getFetchSize())
                .statementType(mappedStatement.getStatementType())
                .timeout(mappedStatement.getTimeout())
                .parameterMap(mappedStatement.getParameterMap())
                .resultMaps(mappedStatement.getResultMaps())
                .resultSetType(mappedStatement.getResultSetType())
                .cache(mappedStatement.getCache())
                .flushCacheRequired(mappedStatement.isFlushCacheRequired())
                .useCache(mappedStatement.isUseCache())
                .build();
    }
}
