package part_05_jooq.code;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.SQLDataType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = JooQSpringSetup.MySpringConfig.class)
public class JooQSpringSetup {

    @Autowired
    private DSLContext create;

    @Test
    public void setup() {
        System.out.println("Yay, JooQ and Spring are setup!");
        create.createTable("bids")
                .column("id", SQLDataType.INTEGER)
                .column("name", SQLDataType.TIMESTAMP)
                .execute();
        // TODO ... create.select();
    }

    // our spring java-config
    @Configuration
    public static class MySpringConfig {

        @Bean
        public DefaultConfiguration configuration() {
            DefaultConfiguration config = new DefaultConfiguration();
            config.setSQLDialect(SQLDialect.H2);
            config.setConnectionProvider(connectionProvider());
            return config;
        }

        @Bean
        public DSLContext context() {
            return new DefaultDSLContext(configuration());
        }

        @Bean
        public ConnectionProvider connectionProvider() {
            return new DataSourceConnectionProvider
                    (dataSource());
        }

        @Bean
        public JdbcDataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("sa");
            return ds;
        }
    }
}
