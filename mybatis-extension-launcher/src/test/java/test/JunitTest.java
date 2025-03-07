package test;

import com.github.developframework.mybatis.extension.core.autoinject.AutoInjectProvider;
import com.github.developframework.mybatis.extension.core.structs.Pager;
import com.github.developframework.mybatis.extension.launcher.DataSourceMetadata;
import com.github.developframework.mybatis.extension.launcher.MybatisCustomize;
import com.github.developframework.mybatis.extension.launcher.MybatisExtensionLauncher;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import test.mapper.GoodsMapper;
import test.search.GoodsSearch;
import test.typehandler.GoodsSpecArrayTypeHandler;

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

        SqlSessionFactory sqlSessionFactory = MybatisExtensionLauncher.open(metadata, new MybatisCustomize() {

            @Override
            public void handleConfiguration(Configuration configuration) {
                configuration.getMapperRegistry().addMapper(GoodsMapper.class);
                configuration.getTypeHandlerRegistry().register(GoodsSpecArrayTypeHandler.class);
            }

            @Override
            public boolean enableDDL() {
                return true;
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
            Pager pager = new Pager();
            GoodsSearch search = new GoodsSearch();
//            search.setGoodsName("a");
            mapper.select(search, null);
        }
    }
}
