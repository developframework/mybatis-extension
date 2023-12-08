package com.github.developframework.mybatis.extension.core.structs;

import com.github.developframework.mybatis.extension.core.annotation.*;
import com.github.developframework.mybatis.extension.core.autoinject.IdGeneratorAutoInjectProvider;
import com.github.developframework.mybatis.extension.core.idgenerator.AutoIncrementIdGenerator;
import com.github.developframework.mybatis.extension.core.idgenerator.NoIdGenerator;
import com.github.developframework.mybatis.extension.core.utils.MybatisUtils;
import com.github.developframework.mybatis.extension.core.utils.NameUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
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

    private final MysqlEngine engine;

    private final Map<String, ColumnDefinition> columnDefinitions;

    // 主键字段
    private final ColumnDefinition[] primaryKeyColumnDefinitions;

    // 多租户字段
    private final ColumnDefinition[] multipleTenantColumnDefinitions;

    // 自动注入字段
    private final ColumnDefinition[] autoInjectColumnDefinitions;

    // 乐观锁版本字段
    private final ColumnDefinition versionColumnDefinition;

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
        this.engine = table.engine();
        this.columnDefinitions = new LinkedHashMap<>();

        List<ColumnDefinition> primaryKeyColumnDefinitionList = new ArrayList<>();
        List<ColumnDefinition> multipleTenantColumnDefinitionList = new ArrayList<>();
        List<ColumnDefinition> autoInjectColumnDefinitionList = new ArrayList<>();
        ColumnDefinition preparatoryPrimaryKeyColumnDefinition = null;

        ColumnDefinition versionColumnDefinition = null;

        for (Field field : MybatisUtils.getAllFields(entityClass)) {
            // 处理@Transient
            if (field.isAnnotationPresent(Transient.class)) {
                continue;
            }
            ColumnDefinition columnDefinition = new ColumnDefinition();
            columnDefinition.setProperty(field.getName());
            columnDefinition.setPropertyType(field.getGenericType());

            // 处理@Column
            final Column column = field.getAnnotation(Column.class);
            ColumnBuildMetadata metadata = new ColumnBuildMetadata();
            ColumnMybatisPlaceholder placeholder = new ColumnMybatisPlaceholder();
            columnDefinition.setColumnBuildMetadata(metadata);
            columnDefinition.setColumnMybatisPlaceholder(placeholder);
            if (column == null) {
                columnDefinition.setColumn(NameUtils.camelcaseToUnderline(field.getName()));
            } else {
                columnDefinition.setColumn(
                        column.name().isEmpty() ? NameUtils.camelcaseToUnderline(field.getName()) : column.name()
                );
                metadata.customizeType = column.customizeType();
                metadata.nullable = column.nullable();
                metadata.length = column.length();
                metadata.scale = column.scale();
                metadata.unsigned = column.unsigned();
                metadata.defaultValue = column.defaultValue().isEmpty() ? null : column.defaultValue();
                metadata.comment = column.comment().isEmpty() ? null : column.comment();

                placeholder.javaType = column.javaType();
                placeholder.jdbcType = column.jdbcType();
                placeholder.typeHandlerClass = column.typeHandler();
            }

            // 处理@Version
            if (field.isAnnotationPresent(Version.class) && (field.getType() == int.class || field.getType() == long.class)) {
                if (versionColumnDefinition != null) {
                    throw new IllegalArgumentException(entityClass + "只能标注一个字段为@Version");
                }
                columnDefinition.setVersion(true);
                versionColumnDefinition = columnDefinition;
            }

            if (preparatoryPrimaryKeyColumnDefinition == null && field.getName().equals(DEFAULT_ID)) {
                // 预备主键字段 字段名为id
                columnDefinition.setPrimaryKey(true);
                columnDefinition.setUseGeneratedKey(true);
                columnDefinition.setIdGeneratorClass(AutoIncrementIdGenerator.class);
                columnDefinition.setAutoInjectProviderClass(IdGeneratorAutoInjectProvider.class);
                preparatoryPrimaryKeyColumnDefinition = columnDefinition;
            }

            // 处理@Id
            final Id id = field.getAnnotation(Id.class);
            if (id != null) {
                columnDefinition.setPrimaryKey(true);
                columnDefinition.setUseGeneratedKey(id.useGeneratedKey());
                columnDefinition.setIdGeneratorClass(id.idGenerator());
                if (id.idGenerator() == NoIdGenerator.class) {
                    columnDefinition.setAutoInjectProviderClass(null);
                } else {
                    columnDefinition.setAutoInjectProviderClass(IdGeneratorAutoInjectProvider.class);
                }
                primaryKeyColumnDefinitionList.add(columnDefinition);
            }

            // 处理@AutoInject
            final AutoInject autoInject = getAutoInject(field);
            if (autoInject != null) {
                columnDefinition.setAutoInjectProviderClass(autoInject.value());
                columnDefinition.setMultipleTenant(autoInject.multipleTenant());
                autoInjectColumnDefinitionList.add(columnDefinition);
                if (autoInject.multipleTenant()) {
                    multipleTenantColumnDefinitionList.add(columnDefinition);
                }
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

        this.primaryKeyColumnDefinitions = primaryKeyColumnDefinitionList.toArray(ColumnDefinition[]::new);
        this.multipleTenantColumnDefinitions = multipleTenantColumnDefinitionList.toArray(ColumnDefinition[]::new);
        this.autoInjectColumnDefinitions = autoInjectColumnDefinitionList.toArray(ColumnDefinition[]::new);
        this.versionColumnDefinition = versionColumnDefinition;

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

    private static AutoInject getAutoInject(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation instanceof AutoInject) {
                return (AutoInject) annotation;
            } else {
                AutoInject autoInject = annotation.annotationType().getAnnotation(AutoInject.class);
                if (autoInject != null) {
                    return autoInject;
                }
            }
        }
        return null;
    }

    public String wrapTableName() {
        return NameUtils.wrap(tableName);
    }

    /**
     * 是否复合主键
     */
    public boolean isCompositeId() {
        return primaryKeyColumnDefinitions.length > 1;
    }

    /**
     * 是否有乐观锁
     */
    public boolean hasOptimisticLock() {
        return versionColumnDefinition != null;
    }

    /**
     * 是否多租户功能实体
     */
    public boolean hasMultipleTenant() {
        return multipleTenantColumnDefinitions.length > 0;
    }

    /**
     * 是否有自动注入
     */
    public boolean hasAutoInject() {
        return autoInjectColumnDefinitions.length > 0;
    }
}
