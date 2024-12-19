package com.github.developframework.mybatis.extension.core;

import com.github.developframework.mybatis.extension.core.structs.*;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.*;

/**
 * 数据库DDL执行器
 *
 * @author qiushui on 2023-09-01.
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseDDLExecutor {

    private final SqlSessionFactory sqlSessionFactory;

    private final EntityDefinitionRegistry entityDefinitionRegistry;

    public void executeDDL() {
        log.info("【Mybatis DDL】开启DDL");
        sqlSessionFactory
                .getConfiguration()
                .getMapperRegistry()
                .getMappers()
                .stream()
                .filter(BaseMapper.class::isAssignableFrom)
                .forEach(mapperClass -> {
                    final Class<?> entityClass = MybatisUtils.getEntityClass(mapperClass);
                    final EntityDefinition entityDefinition = entityDefinitionRegistry.get(entityClass);
                    final SqlSession sqlSession = sqlSessionFactory.openSession(false);
                    try (sqlSession) {
                        final BaseMapper<?, ?> baseMapper = (BaseMapper<?, ?>) sqlSession.getMapper(mapperClass);
                        // 创建表
                        baseMapper.createTable();

                        // 修改表
                        alter(baseMapper, entityDefinition);
                        sqlSession.commit();
                    } catch (Throwable e) {
                        sqlSession.rollback();
                        log.error("【Mybatis DDL错误】修改表结构 " + entityDefinition.getTableName() + " 失败：" + e.getMessage());
                    }
                });
    }

    private void alter(BaseMapper<?, ?> baseMapper, EntityDefinition entityDefinition) {
        // 需要修改的语句
        final List<String> alterColumnSqls = new LinkedList<>();
        // 收集变更的列ALTER语句
        collectColumns(baseMapper, entityDefinition, alterColumnSqls);
        // 收集变更的索引ALTER语句
        collectIndexes(baseMapper, entityDefinition, alterColumnSqls);

        if (!alterColumnSqls.isEmpty()) {
            for (String alterColumnDesc : alterColumnSqls) {
                log.info("【Mybatis DDL】 {}: {}", entityDefinition.getTableName(), alterColumnDesc);
            }
            baseMapper.alter(alterColumnSqls);
        }
    }

    private void collectColumns(BaseMapper<?, ?> baseMapper, EntityDefinition entityDefinition, List<String> alterColumnSqls) {
        final Collection<ColumnDefinition> columnDefinitions = entityDefinition.getColumnDefinitions().values();
        final List<ColumnDesc> columnDescs = baseMapper.desc();
        // 多余无用的属性
        final List<String> unusedColumns = new LinkedList<>();
        for (ColumnDesc columnDesc : columnDescs) {
            Optional<ColumnDefinition> firstMatch = columnDefinitions
                    .stream()
                    .filter(cd -> cd.getColumn().equals(columnDesc.getField()))
                    .findFirst();
            if (firstMatch.isPresent()) {
                final ColumnDefinition columnDefinition = firstMatch.get();
                final ColumnDesc columnDescFromDefinition = ColumnDesc.fromColumnDefinition(entityDefinition.getDialect(), columnDefinition);
                if (!columnDescFromDefinition.equals(columnDesc)) {
                    alterColumnSqls.add("MODIFY COLUMN " + columnDescFromDefinition);
                }
            } else {
                unusedColumns.add(columnDesc.getField());
            }
        }

        for (ColumnDefinition columnDefinition : columnDefinitions) {
            if (columnDescs.stream().noneMatch(c -> c.getField().equals(columnDefinition.getColumn()))) {
                final ColumnDesc newColumnDesc = ColumnDesc.fromColumnDefinition(entityDefinition.getDialect(), columnDefinition);
                alterColumnSqls.add("ADD COLUMN " + newColumnDesc);
            }
        }

        if (!unusedColumns.isEmpty()) {
            log.warn("【Mybatis DDL警告】 {} 多余的字段： {}", entityDefinition.getTableName(), String.join(", ", unusedColumns));
        }
    }

    private void collectIndexes(BaseMapper<?, ?> baseMapper, EntityDefinition entityDefinition, List<String> alterColumnSqls) {
        List<IndexDesc> indexDescs = baseMapper.showIndex();
        Map<String, IndexCompare> databaseIndexCompares = new HashMap<>();
        for (IndexDesc indexDesc : indexDescs) {
            databaseIndexCompares.compute(indexDesc.getKeyName(), (k, oldValue) -> {
                IndexType indexType;
                if (indexDesc.isNonUnique()) {
                    indexType = indexDesc.getIndexType() == IndexMode.FULLTEXT ? IndexType.FULLTEXT : IndexType.NORMAL;
                } else {
                    indexType = IndexType.UNIQUE;
                }
                return new IndexCompare(
                        indexDesc.getKeyName(),
                        oldValue == null ? new String[]{indexDesc.getColumnName()} : ArrayUtils.add(oldValue.getColumns(), indexDesc.getColumnName()),
                        indexDesc.getIndexType(),
                        indexType
                );
            });
        }

        IndexDefinition[] indexDefinitions = entityDefinition.getIndexDefinitions();
        for (IndexDefinition indexDefinition : indexDefinitions) {
            IndexCompare indexCompare = indexDefinition.toIndexCompare();
            IndexCompare databaseIndexCompare = databaseIndexCompares.get(indexCompare.getName());
            if (databaseIndexCompare == null) {
                alterColumnSqls.add("ADD " + indexCompare);
            } else if (!databaseIndexCompare.equals(indexCompare)) {
                alterColumnSqls.add("DROP INDEX" + NameUtils.wrap(indexCompare.getName()));
                alterColumnSqls.add("ADD " + indexCompare);
            }
        }
    }
}
