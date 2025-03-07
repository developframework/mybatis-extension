package com.github.developframework.mybatis.extension.core.interceptors.inner;

import com.github.developframework.mybatis.extension.core.BaseMapper;
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
import com.github.developframework.mybatis.extension.core.sql.builder.SqlCriteriaBuilderContext;
import com.github.developframework.mybatis.extension.core.sql.builder.SqlRoot;
import com.github.developframework.mybatis.extension.core.structs.ColumnDefinition;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import com.github.developframework.mybatis.extension.core.structs.MappedStatementMetadata;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.LinkedList;
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
            final MappedStatement mappedStatement = (MappedStatement) args[0];
            if (isAutomaticSql(mappedStatement, parameter)) {
                // 自动生成语句
                final Configuration configuration = mappedStatement.getConfiguration();
                final EntityDefinition entityDefinition = context.getEntityDefinition();
                final SqlCriteriaBuilder builder = new SqlCriteriaBuilder(configuration, entityDefinition);
                final SqlCriteriaAssembler sqlCriteriaAssembler = MybatisUtils.find(parameter, SqlCriteriaAssembler.class);
                final SqlCriteria finalSqlCriteria = mergeSqlCriteria(context, entityDefinition, parameter, builder, sqlCriteriaAssembler);
                final SqlSortPart sqlSortPart = MybatisUtils.find(parameter, SqlSortPart.class);

                final SqlCriteriaBuilderContext builderContext = new SqlCriteriaBuilderContext();
                // 改写MappedStatement
                args[0] = buildMappedStatement(metadata, mappedStatement, entityDefinition, finalSqlCriteria, sqlSortPart, builderContext);

                // 填充参数
                final MapperMethod.ParamMap<Object> criteriaParamMap = builderContext.getCriteriaParamMap();
                if (parameter instanceof Map) {
                    ((Map<String, Object>) parameter).putAll(criteriaParamMap);
                } else {
                    args[1] = criteriaParamMap;
                }
            }
        }
        return innerInvocation.proceed();
    }

    private boolean isAutomaticSql(MappedStatement mappedStatement, Object parameter) {
        final SqlSource sqlSource = mappedStatement.getSqlSource();
        final BoundSql boundSql = sqlSource.getBoundSql(parameter);
        return BaseMapper.AUTOMATIC_SQL.equals(boundSql.getSql());
    }

    private SqlCriteria mergeSqlCriteria(InterceptContext context, EntityDefinition entityDefinition, Object parameter, SqlCriteriaBuilder builder, SqlCriteriaAssembler sqlCriteriaAssembler) {
        final SqlRoot root = new SqlRoot(entityDefinition);
        final List<SqlCriteria> sqlCriteriaList = new LinkedList<>();
        final SqlCriteria[] injectSqlCriterias = injectSqlCriterias(context, entityDefinition, parameter, root, builder);

        if (injectSqlCriterias.length == 1) {
            sqlCriteriaList.add(injectSqlCriterias[0]);
        } else if (injectSqlCriterias.length > 1) {
            sqlCriteriaList.add(
                    new MixedSqlCriteria(Interval.AND, injectSqlCriterias)
            );
        }
        if (sqlCriteriaAssembler != null) {
            final SqlCriteria sqlCriteria = sqlCriteriaAssembler.assemble(root, builder);
            if (sqlCriteria != null) {
                sqlCriteriaList.add(sqlCriteria);
            }
        }
        return new MixedSqlCriteria(Interval.AND, sqlCriteriaList.toArray(SqlCriteria[]::new));
    }

    private SqlCriteria[] injectSqlCriterias(InterceptContext context, EntityDefinition entityDefinition, Object parameter, SqlRoot root, SqlCriteriaBuilder builder) {
        final List<SqlCriteria> injectSqlCriteriaList = new LinkedList<>();
        // 逻辑删除
        if (entityDefinition.hasLogicDelete()) {
            final ColumnDefinition cd = entityDefinition.getLogicDeleteColumnDefinition();
            injectSqlCriteriaList.add(builder.eqFalse(root.get(cd.getProperty())));
        }
        // 多租户
        if (entityDefinition.hasMultipleTenant()) {
            for (ColumnDefinition cd : entityDefinition.getMultipleTenantColumnDefinitions()) {
                final AutoInjectProvider autoInjectProvider = context.getAutoInjectProviderRegistry().getAutoInjectProvider(cd.getAutoInjectProviderClass());
                final Object provideValue = autoInjectProvider.provide(entityDefinition, cd, parameter);
                injectSqlCriteriaList.add(builder.eq(root.get(cd.getProperty()), provideValue));
            }
        }
        return injectSqlCriteriaList.toArray(SqlCriteria[]::new);
    }

    /**
     * 构建一个新的MappedStatement
     */
    private MappedStatement buildMappedStatement(
            MappedStatementMetadata metadata,
            MappedStatement mappedStatement,
            EntityDefinition entityDefinition,
            SqlCriteria sqlCriteria,
            SqlSortPart sqlSortPart,
            SqlCriteriaBuilderContext builderContext
    ) {
        final boolean exists = "exists".equals(metadata.getMapperMethod().getName());
        final String basicSql;
        if (exists) {
            basicSql = "SELECT 1 FROM " + entityDefinition.wrapTableName();
        } else if ("count".equals(metadata.getMapperMethod().getName())) {
            basicSql = "SELECT COUNT(1) FROM " + entityDefinition.wrapTableName();
        } else {
            basicSql = "SELECT * FROM " + entityDefinition.wrapTableName();
        }

        final SqlNode sqlNode = sqlCriteria == null ? null : sqlCriteria.toSqlNode(mappedStatement.getConfiguration(), builderContext, Interval.EMPTY);
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
