package test;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.launcher.DataSourceMetadata;
import com.github.developframework.mybatis.extension.launcher.ExtensionMybatisLauncher;
import com.github.developframework.mybatis.extension.launcher.MybatisCustomize;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import test.entity.AuditPO;
import test.entity.Goods;
import test.mapper.GoodsMapper;

import java.time.LocalDate;
import java.util.List;

/**
 * @author qiushui on 2023-08-30.
 */
public class JunitTest {

    @Test
    public void test() {
        DataSourceMetadata metadata = new DataSourceMetadata()
                .setJdbcUrl("jdbc:mysql://dev.mysql.server:3306/test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai")
                .setUsername("root")
                .setPassword("pgc%DB112");

        SqlSessionFactory sqlSessionFactory = ExtensionMybatisLauncher.open(metadata, new MybatisCustomize() {

            @Override
            public void handleConfiguration(Configuration configuration) {
                configuration.getMapperRegistry().addMapper(GoodsMapper.class);
            }

            @Override
            public boolean enableDDL() {
                return false;
            }

            @Override
            public List<? extends AutoInjectProvider> customAutoInjectProviders() {
                return List.of(
                        new DomainIdAutoInjectProvider()
                );
            }
        });
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            final GoodsMapper mapper = sqlSession.getMapper(GoodsMapper.class);
//            Goods goods = new Goods();
//            goods.setGoodsName("雪碧");
//            goods.setQuantity(4);
//            mapper.insert(goods);

            final boolean exists = mapper.exists(
                    (root, builder) -> {
                        return builder.or(
                                builder.in(root.get(Goods.Fields.goodsName), "面包", "雪碧"),
                                builder.between(root.function("DATE_FORMAT", AuditPO.Fields.createTime, "%Y-%m-%d"), LocalDate.of(2023, 9, 1), LocalDate.of(2023, 10, 1))
                        );
                    }
            );
            System.out.println(exists);
        }
    }
}
