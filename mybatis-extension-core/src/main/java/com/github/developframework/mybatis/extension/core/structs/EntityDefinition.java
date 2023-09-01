package com.github.developframework.mybatis.extension.core.structs;

import com.github.developframework.mybatis.extension.core.annotation.*;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 实体定义
 *
 * @author qiushui on 2023-08-30.
 */
@Getter
@Setter
public class EntityDefinition {

    private static final String DEFAULT_ID = "id";

    private final Class<?> entityClass;

    private final String tableName;

    private final String comment;

    private final Map<String, ColumnDefinition> columnDefinitions;

    // 主键字段
    private final ColumnDefinition[] primaryKeyColumnDefinitions;

    // 索引
    private final IndexDefinition[] indexDefinitions;

    public EntityDefinition(Class<?> entityClass) {
        final Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException(entityClass.getName() + "未标注@Table");
        }
        this.entityClass = entityClass;
        this.tableName = table.value();
        this.comment = table.comment();
        this.columnDefinitions = new LinkedHashMap<>();

        List<ColumnDefinition> primaryKeyColumnDefinitionList = new ArrayList<>();
        ColumnDefinition preparatoryPrimaryKeyColumnDefinition = null;

        for (Field field : entityClass.getDeclaredFields()) {
            // 处理@Transient
            if (field.isAnnotationPresent(Transient.class)) {
                continue;
            }
            ColumnDefinition columnDefinition = new ColumnDefinition();
            columnDefinition.setProperty(field.getName());
            columnDefinition.setPropertyType(field.getGenericType());

            // 处理@Column
            final Column column = field.getAnnotation(Column.class);
            if (column == null) {
                columnDefinition.setColumn(NameUtils.camelcaseToUnderline(field.getName()));
            } else {
                columnDefinition.setColumn(
                        column.name().isEmpty() ? NameUtils.camelcaseToUnderline(field.getName()) : column.name()
                );

                ColumnBuildMetadata metadata = new ColumnBuildMetadata();
                metadata.customizeType = column.customizeType();
                metadata.nullable = column.nullable();
                metadata.length = column.length();
                metadata.scale = column.scale();
                metadata.unsigned = column.unsigned();
                metadata.defaultValue = column.defaultValue().isEmpty() ? null : column.defaultValue();
                metadata.comment = column.comment().isEmpty() ? null : column.comment();
                columnDefinition.setColumnBuildMetadata(metadata);

                ColumnMybatisPlaceholder placeholder = new ColumnMybatisPlaceholder();
                placeholder.javaType = column.javaType();
                placeholder.jdbcType = column.jdbcType();
                placeholder.typeHandlerClass = column.typeHandler();
                columnDefinition.setColumnMybatisPlaceholder(placeholder);
            }

            if (preparatoryPrimaryKeyColumnDefinition == null && field.getName().equals(DEFAULT_ID)) {
                // 预备主键字段 字段名为id
                columnDefinition.setPrimaryKey(true);
                columnDefinition.setUseGeneratedKey(true);
                preparatoryPrimaryKeyColumnDefinition = columnDefinition;
            }

            // 处理@Id
            final Id id = field.getAnnotation(Id.class);
            if (id != null) {
                columnDefinition.setPrimaryKey(true);
                columnDefinition.setUseGeneratedKey(id.useGeneratedKey());
                primaryKeyColumnDefinitionList.add(columnDefinition);
            }

            columnDefinitions.put(columnDefinition.getProperty(), columnDefinition);
        }

        if (primaryKeyColumnDefinitionList.isEmpty()) {
            if (preparatoryPrimaryKeyColumnDefinition == null) {
                throw new IllegalArgumentException(entityClass.getName() + "未申明id字段");
            } else {
                primaryKeyColumnDefinitionList.add(preparatoryPrimaryKeyColumnDefinition);
            }
        }

        primaryKeyColumnDefinitions = primaryKeyColumnDefinitionList.toArray(ColumnDefinition[]::new);

        // 处理@Index
        this.indexDefinitions = parseIndexes(table.indexes());
    }

    private IndexDefinition[] parseIndexes(Index[] indexes) {
        IndexDefinition[] indexDefinitions = new IndexDefinition[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            indexDefinitions[i] = new IndexDefinition();
            indexDefinitions[i].setName(indexes[i].name());
            indexDefinitions[i].setType(indexes[i].type());
            indexDefinitions[i].setMode(indexes[i].mode());
            indexDefinitions[i].setColumnDefinitions(
                    Arrays
                            .stream(indexes[i].properties())
                            .map(property -> {
                                final ColumnDefinition columnDefinition = columnDefinitions.get(property);
                                if (columnDefinition == null) {
                                    throw new IllegalArgumentException("申明索引时属性不存在：" + property);
                                }
                                return columnDefinition;
                            })
                            .toArray(ColumnDefinition[]::new)
            );
        }
        return indexDefinitions;
    }

    public ColumnDefinition getColumnDefinition(String property) {
        ColumnDefinition columnDefinition = columnDefinitions.get(property);
        if (columnDefinition == null) {
            throw new IllegalArgumentException("没有字段" + property);
        }
        return columnDefinition;
    }

    public String wrapTableName() {
        return NameUtils.wrap(tableName);
    }
}
