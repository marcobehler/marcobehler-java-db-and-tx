package part_05_jooq.code;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = JooQSpringTransactional.MySpringConfig.class)
public class JooQSpringTransactional {

    @Autowired
    private BankTeller teller;

    @Test
    public void exercise() {
        // the getAccountBalance call happens in a transaction
        Long balance = teller.getAccountBalance("Donald Trump");
        assertThat(balance, is(Long.MAX_VALUE));
    }

    public static class BankTeller {

        @Autowired
        private DSLContext create;

        @Transactional(propagation = Propagation.REQUIRED)
        public Long getAccountBalance(String name) {
            System.out.println("Yay,JooQ participates in the Spring " +
                    "transaction");
            // every JooQ statement you execute will be part of the
            // transaction. Nice, isn't it? Now do it! have a look at
            // the spring chapters and complete the code here

            // TODO create.insertInto()
            // TODO create.select()
            return Long.MAX_VALUE;
        }
        // and once we leave that method , spring commits the tx/closes
        // the connection. as usual
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    public static class MySpringConfig {

        @Bean
        public BankTeller teller() {
            return new BankTeller();
        }

        @Bean
        public JdbcDataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("sa");
            return ds;
        }

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
                    (transactionAwareDataSource());
        }

        @Bean // this glues together JooQ and Spring's transactional
        // support
        public TransactionAwareDataSourceProxy transactionAwareDataSource() {
            return new TransactionAwareDataSourceProxy(dataSource());
        }

        @Bean
        public PlatformTransactionManager txManager() {
            return new DataSourceTransactionManager(dataSource());
        }
    }
}
