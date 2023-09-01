package test;

import com.github.developframework.mybatis.extension.launcher.DataSourceMetadata;
import com.github.developframework.mybatis.extension.launcher.ExtensionMybatisLauncher;
import com.github.developframework.mybatis.extension.launcher.MybatisCustomize;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import test.mapper.GoodsMapper;

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
                return true;
            }
        });

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            final GoodsMapper mapper = sqlSession.getMapper(GoodsMapper.class);
//            final Optional<Goods> optional = mapper.selectByPK(32);
//            optional.ifPresent(System.out::println);
        }
    }
}
