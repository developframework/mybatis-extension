## mybatis-extension

> 对mybatis的功能扩展包，丝滑接入不影响原有mybatis功能

### 快速使用

#### 基于spring-boot

```xml
<dependency>
    <groupId>com.github.developframework</groupId>
    <artifactId>mybatis-extension-spring-boot-starter</artifactId>
</dependency>
```

```yml
mybatis:
  mapperLocations: 'classpath:mybatis/mapper/*.xml'
  typeAliasesPackage: '自己的实体包路径'
  extension:
    enableDDL: true # 开启自动创建表
```

这里不需要使用`mybatis.configLocation`参数，因为jar包里已实现了`ConfigurationCustomizer`覆盖默认配置

SQL日志打印前缀为mybatis.extension

```xml
<logger name="mybatis.extension" additivity="false" level="DEBUG">
    <appender-ref ref="console"/>
</logger>
```

#### 脚本

```xml
<dependency>
    <groupId>com.github.developframework</groupId>
    <artifactId>mybatis-extension-launcher</artifactId>
</dependency>
```

```java
// 数据源信息
DataSourceMetadata metadata = new DataSourceMetadata()
                .setJdbcUrl("jdbc:mysql://")
                .setUsername("")
                .setPassword("");

// 构建SqlSessionFactory
SqlSessionFactory sqlSessionFactory = ExtensionMybatisLauncher.open(metadata, new MybatisCustomize() {

    @Override
    public void handleConfiguration(Configuration configuration) {
        // 注册Mapper接口
        configuration.getMapperRegistry().addMapper(GoodsMapper.class);
        // 可以注册转换器
        configuration.getTypeHandlerRegistry().register(GoodsSpecArrayTypeHandler.class);
    }

    @Override
    public boolean enableDDL() {
        // 开启自动建表
        return true;
    }

    @Override
    public List<? extends AutoInjectProvider> customAutoInjectProviders() {
        return List.of(
            // 配置自动注入
        );
    }
});
try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
    // 获取Mapper开始脚本处理
    final GoodsMapper mapper = sqlSession.getMapper(GoodsMapper.class);
    
}
```

### BaseMapper 通用Mapper接口

提供通用方法

| 方法                | 说明                                      |
| ------------------- | ----------------------------------------- |
| insert              | 插入记录                                  |
| insertAll           | 批量插入记录                              |
| replace             | 替换记录                                  |
| replaceAll          | 批量替换记录                              |
| update              | 更新记录                                  |
| deleteById          | 根据id删除记录                            |
| existsById          | 根据id查询存在                            |
| selectById          | 根据id查询记录                            |
| selectByIdLock      | 根据id查询记录（可以锁）                  |
| selectByIdArray     | 根据id数组查询记录                        |
| selectByIdArrayLock | 根据id数组查询记录（可以锁）              |
| selectByIds         | 根据id集合查询记录                        |
| selectByIdsLock     | 根据id集合查询记录（可以锁）              |
| selectAll           | 查询所有记录                              |
| exists              | 根据SelectSqlAssembler拼装SQL查询存在     |
| select              | 根据SelectSqlAssembler拼装SQL查询记录     |
| selectPager         | 根据SelectSqlAssembler拼装SQL分页查询记录 |

示例：

```java
public interface GoodsMapper extends BaseMapper<GoodsPO, Integer> {
    // 其中已包含了上述所有SQL操作
}
```

### 实体标注

其中实体类中可以用注解标注字段申明

```java
@Getter
@Setter
@Table("goods") // 标注表名
public class GoodsPO {

    @Id // 标注ID字段
    private Integer id;

    // 商品名称
    private String goodsName;

    // 数量
    private Integer quantity;

    // 创建时间
    @CreateTime
    private LocalDateTime createTime;

    // 规格 多个值
    @Column(nullable = false, typeHandler = StringArrayTypeHandler.class) // 标注自定义类型处理器
    private String[] specifications;
}
```

预设注解：

| 注解              | 说明                   |
| --------------- | -------------------- |
| @Table          | 标注表信息                |
| @Id             | 标注主键                 |
| @Column         | 标注字段特性（没有特殊指定特性可不标注） |
| @Transient      | 排除字段，不属于数据库字段        |
| @Version        | 乐观锁字段，详见乐观锁章节        |
| @CreateTime     | 自动注入创建时间，详见自动注入章节    |
| @LastModifyTime | 自动注入修改时间，详见自动注入章节    |

#### @Table

```java
@Table(
    value = "goods", // 表名
    indexes = {
        @Index(type = IndexType.UNIQUE, properties = "goodsName") // 标注索引
    },
    comment = "商品表"	// 表注释
) // 标注表名
public class GoodsPO {

}
```

#### @Id

##### 单一主键

```java
@Id(
    idGenerator = AutoIncrementIdGenerator.class, // ID生成器
    useGeneratedKey = true // 局部开启自增回填功能
)
private Integer id;
```

如果实体类没有标注@Id，会把名字叫`id`的字段作为主键

```java
Optional<GoodsPO> goods = selectById(1);
```

```sql
SELECT * FROM goods WHERE `id` = 1
```

##### 复合主键

可以标注多个`@Id`，但不适用自增

```java
@Id(idGenerator = NoIdGenerator.class)
private String name;

@Id(idGenerator = NoIdGenerator.class)
private String mobile;
```

接口上可以使用CompositeId代表复合主键

```java
public interface PersonMapper extends BaseMapper<PersonPO, CompositeId> {

}
```

```java
Optional<PersonPO> goods = selectById(
    new CompositeId()
        .id("name", "张三")
        .id("mobile", "18888888888")
);
```

最终执行的SQL效果

```sql
SELECT * FROM person WHERE `name` = '张三' AND `mobile` = '18888888888'
```

##### ID生成器

可以实现`IdGenerator`接口定义自己的ID生成器

```java
public interface IdGenerator {

    Object generate(Object entity);
}
```

默认自带的实现有两个：

+ `AutoIncrementIdGenerator` 数据库自增实现
+ `NoIdGenerator` 无ID生成器

#### @Column

```java
@Column(
    name = "specifications",    // 重定义字段名 很少用 DDL相关
    customizeType = "VARCHAR(100)", // 自定义类型申明
    javaType = void.class,  // 定义mybatis的javaType 很少用
    jdbcType = JdbcType.UNDEFINED,  // 定义mybatis的jdbcType 很少用
    typeHandler = StringArrayTypeHandler.class, // // 标注自定义类型处理器
    nullable = false  // 该字段是否能null DDL相关
    length = 50, // 长度 DDL相关
    scale = 2, // 精度 DDL相关
    unsigned = true, // 是否无符号 DDL相关
    defaultValue = "0", // null时的默认值 DDL相关
    comment = "" // 字段注释 DDL相关
) 
private String[] specifications;
```

+ `nullable` 会影响使用update的策略

```java
goodsMapper.update(goods);
```

当`nullable=true`时对象内的null值字段会被修改成null

当`nullable=false`时对象内的null值会被跳过不修改

### 自动注入

提供`@AutoInject`注解来自动注入预设值，需要实现`AutoInjectProvider`接口来申明值的来源

```java
public interface AutoInjectProvider {

    /**
     * 哪些SQL操作类型需要注入
     * <p>
     * INSERT or UPDATE
     */
    SqlCommandType[] needInject();

    /**
     * 提供注入值
     */
    Object provide(Type fieldType);
}
```

已设置两个审计常用注入值`@CreateTime`、`@LastModifyTime`，支持字段类型：

- LocalDateTime
- ZonedDateTime
- LocalDate
- LocalTime
- Instant
- java.util.Date
- java.sql.Date
- java.sql.Timestamp

```java
@CreateTime  // 等价于内置@AutoInject(AuditCreateTimeAutoInjectProvider.class)
private LocalDateTime createTime;

@LastModifyTime  // 等价于内置@AutoInject(AuditModifyTimeAutoInjectProvider.class)
private LocalDateTime modifyTime;

@AutoInject(OtherValueAutoInjectProvider.class)    // 自定义注入值
private Object otherValue;
```

#### 多租户功能

`multipleTenant = true` 开启多租户功能

```java
@AutoInject(
    value = DomainIdAutoInjectProvider.class, 
    multipleTenant = true
)
private Integer domainId;
```

```java
public class DomainIdAutoInjectProvider implements AutoInjectProvider {
    @Override
    public SqlCommandType[] needInject() {
        return SqlCommandType.values();
    }

    @Override
    public Object provide(Type fieldType) {
        return 1;
    }
}
```

```java
// SELECT * FROM `order` WHERE `id` = 1 AND `domain_id` = 1 LIMIT 1
mapper.selectById(1);
// SELECT * FROM `order` WHERE `id` IN (1) AND `domain_id` = 1
mapper.selectByIds(List.of(1));
// UPDATE `order` SET ... WHERE `id` = 1 AND `domain_id` = 1
mapper.update(order);
// DELETE FROM `order` WHERE `id` = 1 AND `domain_id` = 1 LIMIT 1
mapper.deleteById(1);
// SELECT * FROM `order` WHERE `domain_id` = 1
mapper.selectAll();
```

### 多样查询

#### 分页查询功能

在Mapper接口的方法参数里加入`Pager`，返回值类型为`Page`就能实现分页查询

```java
public interface GoodsMapper extends BaseMapper<GoodsPO, Integer> {

    // @CountStatement("pagerCount")
    @Select("SELECT * FROM goods")
    Page<GoodsPO> selectGoods(Pager pager, @Param("goodsName") String goodsName);

    // long pagerCount(@Param("goodsName") String goodsName);
}
```

```java
Pager pager = new Pager(0, 20); // 页码从0开始
Page<GoodsPO> page = goodsMapper.selectGoods(pager, "面包");
long recordTotal = page.getRecordTotal(); // 获取记录总数
int pageTotal = page.getPageTotal(); // 获取分页总数
page.forEach(item -> {}); // page对象实际上是List，可以迭代处理本页数据
```

+ 方法多参数时，`Pager`可以任意放置
+ `@CountStatement`注解以及查询数量的statement不是必须的，如果主查询列表的语句是简单SELECT语句，可以略写该注解采用自动生成的查询总条数的SELECT
  COUNT语句；如果主查询列表语句是一个JOIN或嵌套子查询，`@CountStatement`可以重定义一个简单的SELECT COUNT语句

示例：

简单分页查询：

```sql
-- 主列表查询
SELECT * FROM goods
-- 自动生成的查询总数语句
SELECT COUNT(*) FROM (SELECT * FROM goods WHERE goods_name = #{goodsName}) _count
```

复杂分页查询：

```sql
-- 主列表查询
SELECT * FROM goods g LEFT JOIN xxx x ON g.id = x.goods_id WHERE g.goods_name = #{goodsName})
-- 自动生成的查询总数语句
SELECT COUNT(*) FROM (SELECT * FROM goods g LEFT JOIN xxx x ON g.id = x.goods_id WHERE g.goods_name = #{goodsName}) _count
-- 其实在查询总数时没必要去执行LEFT JOIN 可以采用@CountStatement来重定义查总数语句
SELECT COUNT(*) FROM goods WHERE goods_name = #{goodsName}
```

#### 根据方法名自动生成简单的SQL语句

支持的方法语法开头：

| 语法        | 方法开头                              | 等价SQL                                                    |
| --------- | --------------------------------- | -------------------------------------------------------- |
| 插入        | `insert`、`insertIgnore`、`replace` | INSERT INTO ... INSERT IGNORE INTO ... REPLACE INTO ... |
| 修改        | `update`                          | UPDATE ...                                               |
| 删除        | `deleteBy`、`removeBy`             | DELETE FROM ...                                          |
| 查询        | `selectBy`、`findBy`、`queryBy`     | SELECT * FROM ...                                        |
| 查询数量、查询存在 | `existsBy`、`hasBy`、`countBy`      | SELECT COUNT(*) FROM ...                                 |

```java
int insertGoodsNameQuantity(Goods goods);
```

```java
int updateGoodsNameQuantity(Goods goods);
```

查询支持的关键字：

| 关键字       | 示例   | 等价SQL语句  |
| --------- | -------------- | ------------------- |
| EQ        | selectByGoodsName(String GoodsName) 或 selectByGoodsName**Eq**(String GoodsName) | WHERE goods_name= #{param1}                          |
| ISNULL    | selectByGoodsName**IsNull**()                                                   | WHERE goods_name IS NULL                             |
| NOTNULL   | selectByGoodsName**NotNull**()                                                  | WHERE goods_name IS NOT NULL                         |
| GT        | selectByQuantity**Gt**(int quantity)                                            | WHERE quantity > #{param1}                           |
| GTE       | selectByQuantity**Gte**(int quantity)                                           | WHERE quantity >= #{param1}                          |
| LT        | selectByQuantity**Lt**(int quantity)                                            | WHERE quantity < #{param1}                           |
| LTE       | selectByQuantity**Lte**(int quantity)                                           | WHERE quantity <= #{param1}                          |
| BETWEEN   | selectByQuantity**Between**(Integer quantityStart, Integer quantityEnd)         | WHERE quantity BETWEEN #{param1} AND #{param2}       |
| LIKE      | selectByGoodsName**Like**(String GoodsName)                                     | WHERE goods_name LIKE CONCAT('%', #{param1}, '%')    |
| LIKE_HEAD | selectByGoodsName**LikeHead**(String GoodsName)                                 | WHERE goods_name LIKE CONCAT(#{param1}, '%')         |
| LIKE_TAIL | selectByGoodsName**LikeTail**(String GoodsName)                                 | WHERE goods_name LIKE CONCAT('%', #{param1})         |
| IN        | selectByGoodsName**In**(String[] GoodsNames)                                    | WHERE goods_name IN(...)                             |
| NOT IN    | selectByGoodsName**NotIn**(String[] GoodsNames)                                 | WHERE goods_name NOT IN(...)                         |
| AND       | selectByGoodsName**And**QuantityGt(String GoodsName, Integer quantity)          | WHERE goods_name= #{param1} AND quantity > #{param2} |
| OR        | selectByGoodsName**OR**QuantityGt(String GoodsName, Integer quantity)           | WHERE goods_name= #{param1} OR quantity > #{param2}  |

+ 可以使用`@Dynamic`注解，如果入参值为空则会忽略该条件，实现动态拼接SQL
+ `BETWEEN`   如果开始值或结束值为空则会转变为GTE或LTE
+ 该方式只支持简单条件拼接，**不支持带括号的OR多条件查询**
+ **方法参数的顺序必须严格按照方法名描述的顺序申明**，`BETWEEN`可以占用两个参数，内部是以mybatis的参数命名方式`paramN`取值的，**所以不必使用`@Param`注解**

使用`@Select(SQL_BY_NAMING)`标注在方法上申明采用命名方式生成SQL，可以使用`@SqlCustomized`对字段进行函数处理

```java
public interface GoodsMapper extends BaseMapper<GoodsPO, Integer> {

  	@Dynamic
    Page<GoodsPO> selectByCreateTime(Pager pager, @SqlCustomized(ColumnFunction.DATE) LocalDate date);
}
```

+ `@SqlCustomized`可以申明字段使用的函数

等价于如下SQL

```SQL
SELECT * FROM goods WHERE DATE(create_time) = #{param1}
```

**需要注意的是**，该种方式生成SQL的时机是在启动程序后初始化Mybatis时去解析的，本质上是修改了Mybatis默认生成的MappedStatement内的SqlSource，不会影响查询的性能问题

#### SqlCriteriaAssembler查询SQL装配器

以代码方式动态拼装SQL，实现`SqlCriteriaAssembler`接口描述查询SQL如何拼接（只能拼接单表非聚合查询语句）

```java
@FunctionalInterface
public interface SqlCriteriaAssembler {

    SqlCriteria assemble(SqlRoot root, SqlCriteriaBuilder builder);
}
```

示例：

```java
mapper.select(
    (root, builder) ->
    builder.or(
        builder.between(root.function("YEAR", root.get(Goods.Fields.createTime)), 2021, 2023),
        builder.and(
            builder.in(root.get(Goods.Fields.goodsName), "雪碧", "可乐"),
            builder.gte(root.get(Goods.Fields.quantity), 1)
        )
    ),
    Sort.by(
        Sort.desc(Goods.Fields.quantity),
        Sort.asc(Goods.Fields.goodsName)
    )
);
```

如上代码最终将会拼成SQL

```
SELECT * FROM `goods` WHERE ( 
	YEAR(`create_time`) BETWEEN ? AND ? 
	OR ( 
		`goods_name` IN (?,?) AND `quantity` >= ? 
	) 
) 
ORDER BY `quantity` DESC, `goods_name` ASC
```

**需要注意的是**，该种方式生成SQL的时机是在Mybatis执行查询时，使用插件拦截了`Executor`
的query方法，用新的MappedStatment替换掉本次查询引用的MappedStatment，和原生mybatis相比，每次查询都会去动态拼装一遍SQL，稍微有点影响查询性能。

### 数据库锁

#### 乐观锁

提供`@Version`标注乐观锁版本字段（**单实体类仅能设置一个**），支持标注的字段类型：

+ int
+ Integer
+ long
+ Long

```java
@Getter
@Setter
@Table("goods")
public class GoodsPO {

    @Id
    private Integer id;

    // 商品名称
    private String goodsName;

    @Version
    private int version;
}
```

```java
goodsMapper
    .selectById(1)
    .ifPresent(goods -> {
        try {
            mapper.update(goods);
        } catch (OptimisticLockException e) {
            // 触发乐观锁异常
        }
    });
```

生成实际SQL：

```sql
UPDATE `goods` SET `goods_name` = ?, `version` = `version` + 1 WHERE `id` = ? AND `version` = ?
```

#### 悲观锁

提供`@Lock`标注在需要开启悲观锁的查询语句上，BaseMapper也提供了相应带LockType参数的查询语句

##### 排它锁

```java
@Lock // 或等价于@Lock(LockType.WRITE)
List<Goods> selectByName(String name);
```

等价于SQL：

```sql
SELECT * FROM `goods` WHERE name = #{name} FOR UPDATE
```

##### 共享锁

```java
@Lock(LockType.READ)
@Select(SQL_BY_NAMING)
List<Goods> selectByName(String name);
```

等价于SQL：

```sql
SELECT * FROM `goods` WHERE name = #{name} LOCK IN SHARE MODE
```

### 自动建表 （DDL）

结合模块`happy-develop-framework-resource-mybatis`使用，有个开关可以开启（默认关闭）：

```yml
# 开启DDL
resource:
  mybatis:
    enableDDL: true
```

#### 创建/修改表

系统启动时会自动创建实体对应的数据表，字段的申明来源于`@Column`的属性配置，与DDL相关的属性：

```java
@Column(
    name = "specifications",    // 重定义字段名 很少用 DDL相关
    customizeType = "VARCHAR(100)", // 自定义类型申明
    nullable = false  // 该字段是否能null DDL相关
    length = 50, // 长度 DDL相关
    scale = 2, // 精度 DDL相关
    unsigned = true, // 是否无符号 DDL相关
    defaultValue = "0", // null时的默认值 DDL相关
    comment = "规格" // 字段注释 DDL相关
) 
```

如果没有定义`customizeType`属性值，则字段类型会自动按照实体属性类型匹配，匹配规则如下表：

| 属性类型                                                     | 默认字段类型             | 说明 |
| ------------------------------------------------------------ | ------------------------ | ---- |
| String 或 其它类型                                           | VARCHAR(100)             | 长度按length |
| Integer int                                                  | INT                      |      |
| Long long                                                    | BIGINT                   |      |
| Boolean boolean                                              | BIT(1)                   |      |
| Float float                                                  | FLOAT(6,2)               |      |
| Double double                                                | DOUBLE(12,2)             | 长度按length，精度按scale |
| BigDecimal                                                   | DECIMAL(10,2)            | 长度按length，精度按scale |
| LocalDateTime ZonedDateTime java.util.Date java.util.Calendar | DATETIME                 |      |
| LocalDate java.sql.Date                                      | DATE                     |      |
| LocalTime                                                    | TIME                     |      |
| java.sql.Timestamp                                           | TIMESTAMP                |      |
| 枚举类型                                                     | ENUM('value1', 'value2') | 会自动识别枚举值 |

日志

```
【Mybatis DDL】 goods: ADD COLUMN `goods_name` varchar(100) NOT NULL
```

#### 创建/修改索引

系统启动时会自动维护索引，索引的申明来源于`@Table`的属性`indexes`

```java
@Table(
        value = "goods",
        indexes = {
                @Index(type = IndexType.UNIQUE, properties = {"goodsName"})
        }
)
```

日志

```
【Mybatis DDL】 goods: ADD UNIQUE `UKgoods_name`(`goods_name`) USING BTREE
```

