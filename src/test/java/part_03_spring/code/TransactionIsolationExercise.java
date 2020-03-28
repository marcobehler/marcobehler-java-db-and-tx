package part_03_spring.code;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = TransactionIsolationExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class TransactionIsolationExercise {

    @Autowired
    private BankTeller teller;

    @Autowired
    private DataSource ds;


    @Before
    public void setUp() {
        try (Connection connection = ds.getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void exercise() throws SQLException {
        Long balance = teller.getAccountBalance("Donald Trump");
        assertThat(balance, is(Long.MAX_VALUE));
    }


    public static class BankTeller {

        @Autowired
        private DataSource ds;


        // here we open up a connection + tx
        // and HERE is where you specify the transaction isolation
        @Transactional(propagation = Propagation.REQUIRED,
                isolation = Isolation.READ_COMMITTED)
        public Long getAccountBalance(String name) throws SQLException {
            System.out.println("Set a READ_COMMITTED tx level...");
           // let's return the balance from a database table
           Long balance = new JdbcTemplate(ds).queryForObject(
                    "select balance from accounts " +
                    "where name = ?", Long.class, name);
            System.out.println("The balance for : " + name + " is: " +
                    balance);
            return balance;
        }

    }


    private void createTables(Connection conn) {
        try {
            conn.createStatement().execute("create table if not exists " +
                    "account_activity "
                    + "(date_occurred date," +
                    " account_holder VARCHAR, description VARCHAR)");
            conn.createStatement().execute("create table if not exists " +
                    "accounts "
                    + "(name varchar primary key, balance bigint)");
            conn.createStatement().execute("insert into accounts values"
                    + "('Donald Trump'," + Long.MAX_VALUE + ")");
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    public static class MySpringConfig {

        @Bean(name = "bankTeller")
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


