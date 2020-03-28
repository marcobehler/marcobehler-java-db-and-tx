package part_05_xa.code;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = BitronixJTADataSourceExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class BitronixJTADataSourceExercise {

    @Autowired
    @Qualifier(value = "dataSourceOne")
    private DataSource ds1;

    @Autowired
    @Qualifier(value = "dataSourceTwo")
    private DataSource ds2;

    @Autowired
    private PlatformTransactionManager txManager;


    @Test
    public void setupTwoDataSources_and_txManager() throws SQLException {
        assertNotNull(ds1);
        assertNotNull(ds2);
        assertNotNull(txManager);
        System.out.println("Yay, everything is set-up for distributed " +
                "transactions!");
    }

    // our spring java-config
    @Configuration
    public static class MySpringConfig {

        @Bean(destroyMethod = "close", name = "dataSourceOne",
                initMethod = "init")
        public DataSource dataSourceOne() {
            PoolingDataSource ds = new PoolingDataSource();
            ds.setClassName("org.h2.jdbcx.JdbcDataSource");
            ds.setUniqueName("ds1");
            ds.setMaxPoolSize(10);
            Properties props = new Properties();
            props.put("url", "jdbc:h2:mem:ds1");
            props.put("user", "sa");
            props.put("password", "");
            ds.setDriverProperties(props);
            return ds;
        }

        @Bean(destroyMethod = "close", name = "dataSourceTwo",
                initMethod = "init")
        public DataSource dataSourceTwo() {
            PoolingDataSource ds = new PoolingDataSource();
            ds.setClassName("org.h2.jdbcx.JdbcDataSource");
            ds.setUniqueName("ds2");
            ds.setMaxPoolSize(10);
            Properties props = new Properties();
            props.put("url", "jdbc:h2:mem:ds2");
            props.put("user", "sa");
            props.put("password", "");
            ds.setDriverProperties(props);
            return ds;
        }


        // spring's JtaTransactionManager is also a regular
        // PlatformTransactionManager
        @Bean
        public PlatformTransactionManager jtaTransactionManager() {
            return new JtaTransactionManager
                    (bitronixManager(), bitronixManager());
        }

        @Bean(destroyMethod = "shutdown")
        public BitronixTransactionManager bitronixManager() {
            return TransactionManagerServices.getTransactionManager();
        }
    }
}
