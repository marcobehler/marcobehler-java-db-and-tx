package part_03_spring.code;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = TransactionalNestedExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class TransactionalNestedExercise {

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
        // the getAccountBalance call happens in a transaction
        Long balance = teller.getAccountBalance("Donald Trump");
        assertThat(balance, is(Long.MAX_VALUE));
    }


    public static class BankTeller {

        @Autowired
        private DataSource ds;

        @Resource(name = "bankTeller")
        private BankTeller self;

        // here spring opens up a connection + tx

        // Propagation.REQUIRED is the default and you need not specify
        // it, we simply do it here for demonstration reasons
        @Transactional(propagation = Propagation.REQUIRED)
        public Long getAccountBalance(String name) {
            // let's return the balance from a database table
            Long balance = new JdbcTemplate(ds).queryForObject(
                    "select balance from accounts " +
                            "where name = ?", Long.class, name);
            System.out.println("The balance for : " + name + " is: " +
                    balance);
            // will this call open up another transactin? No, it will
            // use the same one!
            self.audit(name, "Get Account Balance");
            return balance;
        }


        // What will NESTED actually do? Yep, it will create a
        // SAVEPOINT! (which is a bit nonsensical in this example here,
        // but hey!
        @Transactional(propagation = Propagation.NESTED)
        public void audit(final String name, final String activity) {
            System.out.println("Beginning nested transaction....");
            System.out.println("Saving " + activity + " into the " +
                    "database");
            MapSqlParameterSource params =
                    new MapSqlParameterSource("name", name);
            params.addValue("date_occurred", new Date());
            params.addValue("description", activity);
            new SimpleJdbcInsert(ds).withTableName("account_activity")
                    .execute(params);
        }
    }


    private void createTables(Connection conn) {
        try {
            conn.createStatement().execute("create table if not exists" +
                    " accounts "
                    + "(name varchar primary key, balance bigint)");
            conn.createStatement().execute("create table if not exists " +
                    "account_activity "
                    + "(date_occurred date, description VARCHAR," +
                    " name varchar, " +
                    "foreign key (name) references accounts(name))");
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


