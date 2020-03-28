package part_03_spring.code;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = CallingYourselfExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class CallingYourselfExercise {

    @Autowired
    private BankTeller teller;


    @Test
    public void exercise() throws SQLException {
        Long balance = teller.getAccountBalance("Donald Trump");
        assertThat(balance, is(Long.MAX_VALUE));
    }


    public static class BankTeller {

        @Resource(name = MySpringConfig.BANKTELLER_ID)
        private BankTeller self;


        @Transactional(propagation = Propagation.REQUIRED)
        public Long getAccountBalance(String name) {
            System.out.println("getting the acount balance");
            // todo: some database call

            // this method call will "ignore" the @Transactional
            // annotation, as there is no proxy call involved
            saveAccountActivity("Get Account Balance", name);

            // this method call WILL use the @Transactional
            // annotation as it is going thorugh the proxy , hence execute
            // in its own connection/tx
            self.saveAccountActivity("Get Account balance", name);

            return Long.MAX_VALUE;
        }


        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveAccountActivity(final String activity, final String
                accoutName)
                {
            // todo: some database call
            System.out.println(activity + " happened on account " + accoutName);
        }

    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    public static class MySpringConfig {

        public static final String BANKTELLER_ID = "bankTeller";

        @Bean(name = BANKTELLER_ID)
        public BankTeller teller() {
            return new BankTeller();
        }

        @Bean
        public DataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("sa");
            return ds;
        }

        @Bean
        public PlatformTransactionManager txManager() {
            return new DataSourceTransactionManager(dataSource());
        }
    }
}


