package com.github.developframework.mybatis.extension.core.sql.builder;

import com.github.developframework.mybatis.extension.core.parser.naming.Interval;
import com.github.developframework.mybatis.extension.core.parser.naming.Operate;
import com.github.developframework.mybatis.extension.core.sql.FieldSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.MixedSqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlCriteria;
import com.github.developframework.mybatis.extension.core.sql.SqlFieldPart;
import com.github.developframework.mybatis.extension.core.sql.criteria.*;
import com.github.developframework.mybatis.extension.core.structs.EntityDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author qiushui on 2023-09-15.
 */
@Getter
@RequiredArgsConstructor
public class SqlCriteriaBuilder {

    private final Configuration configuration;

    private final EntityDefinition entityDefinition;

    public SqlCriteria and(SqlCriteria... criteriaChain) {
        if (criteriaChain.length == 1) {
            return criteriaChain[0];
        } else {
            return new MixedSqlCriteria(Interval.AND, criteriaChain);
        }
    }

    public SqlCriteria and(List<SqlCriteria> criteriaChain) {
        return and(criteriaChain.toArray(SqlCriteria[]::new));
    }

    public SqlCriteria or(SqlCriteria... criteriaChain) {
        if (criteriaChain.length == 1) {
            return criteriaChain[0];
        } else {
            return new MixedSqlCriteria(Interval.OR, criteriaChain);
        }
    }

    public SqlCriteria or(List<SqlCriteria> criteriaChain) {
        return or(criteriaChain.toArray(SqlCriteria[]::new));
    }

    public SqlCriteria eq(Object value1, Object value2) {
        return new SimpleSqlCriteria(value1, value2, Operate.EQ);
    }

    public SqlCriteria ne(Object value1, Object value2) {
        return new SimpleSqlCriteria(value1, value2, Operate.NE);
    }

    public SqlCriteria gt(Object value1, Object value2) {
        return new SimpleSqlCriteria(value1, value2, Operate.GT);
    }

    public SqlCriteria gte(Object value1, Object value2) {
        return new SimpleSqlCriteria(value1, value2, Operate.GTE);
    }

    public SqlCriteria lt(Object value1, Object value2) {
        return new SimpleSqlCriteria(value1, value2, Operate.LT);
    }

    public SqlCriteria lte(Object value1, Object value2) {
        return new SimpleSqlCriteria(value1, value2, Operate.LTE);
    }

    public SqlCriteria like(SqlFieldPart fieldPart, String value) {
        return new SimpleSqlCriteria(fieldPart, value, Operate.LIKE);
    }

    public SqlCriteria likeHead(SqlFieldPart fieldPart, String value) {
        return new SimpleSqlCriteria(fieldPart, value, Operate.LIKE_HEAD);
    }

    public SqlCriteria likeTail(SqlFieldPart fieldPart, String value) {
        return new SimpleSqlCriteria(fieldPart, value, Operate.LIKE_TAIL);
    }

    public SqlCriteria isNull(SqlFieldPart fieldPart) {
        return new CompareSqlCriteria(fieldPart, Operate.ISNULL);
    }

    public SqlCriteria isNotNull(SqlFieldPart fieldPart) {
        return new CompareSqlCriteria(fieldPart, Operate.NOTNULL);
    }

    public SqlCriteria eqTrue(SqlFieldPart fieldPart) {
        return new CompareSqlCriteria(fieldPart, Operate.EQ_TRUE);
    }

    public SqlCriteria eqFalse(SqlFieldPart fieldPart) {
        return new CompareSqlCriteria(fieldPart, Operate.EQ_FALSE);
    }

    public SqlCriteria in(SqlFieldPart fieldPart, Collection<?> collection) {
        return new WithInSqlCriteria(fieldPart, collection, Operate.IN);
    }

    @SafeVarargs
    public final <T> SqlCriteria in(SqlFieldPart fieldPart, T... array) {
        return new WithInSqlCriteria(fieldPart, array, Operate.IN);
    }

    public SqlCriteria notIn(SqlFieldPart fieldPart, Collection<?> collection) {
        return new WithInSqlCriteria(fieldPart, collection, Operate.NOT_IN);
    }

    @SafeVarargs
    public final <T> SqlCriteria notIn(SqlFieldPart fieldPart, T... array) {
        return new WithInSqlCriteria(fieldPart, array, Operate.NOT_IN);
    }

    public SqlCriteria between(Object target, Object value1, Object value2) {
        return new BetweenSqlCriteria(target, value1, value2);
    }

    public SqlCriteria terminate() {
        return new TerminateSqlCriteria();
    }

    public SqlCriteria complex(FieldSqlCriteria criteria) {
        return criteria;
    }

    public SqlCriteria complex(Supplier<SqlCriteria> supplier) {
        return supplier.get();
    }
}
