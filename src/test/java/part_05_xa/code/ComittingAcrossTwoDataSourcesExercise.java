package part_05_xa.code;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = BitronixJTADataSourceExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class ComittingAcrossTwoDataSourcesExercise {

    @Autowired
    @Qualifier(value = "dataSourceOne")
    private DataSource ds1;

    @Autowired
    @Qualifier(value = "dataSourceTwo")
    private DataSource ds2;

    @Autowired
    private PlatformTransactionManager txManager;


    @Test
    public void committing_across_two_datasources() throws SQLException {
        // look back at the spring chapter if you do not know what we
        // are doing here. you can also use TransactionTemplates
        System.out.println("Let's open up a distributed TS!");
        TransactionStatus transaction = txManager.getTransaction(null);

        final String name = "Shannon Briggs";
        final Integer limit = 1234567;

        // let's commit the user in one database....and yes, that create
        // table statement should not be here, but is for simplicity ; )
        new JdbcTemplate(ds1).execute(
                "create table if not exists users " +
                        "(id bigint auto_increment primary key," +
                        " name varchar)");
        new SimpleJdbcInsert(ds1).withTableName("users").execute(
                new MapSqlParameterSource("name", name));

        // let's assume we want to save this user's atm withdrawal
        // limits to another database...
        // (and yes, there is no relationship or foreign
        // key in this example ; )
        new JdbcTemplate(ds2).execute(
                "create table if not exists atm_widthdrawal_limits " +
                        "(id bigint auto_increment primary key, " +
                        "amount int )");
        new SimpleJdbcInsert(ds2).withTableName("atm_widthdrawal_limits")
                .execute(new MapSqlParameterSource("amount", limit));

        txManager.commit(transaction);

        System.out.println("Yay, this is our first XA-commit");
    }
}
