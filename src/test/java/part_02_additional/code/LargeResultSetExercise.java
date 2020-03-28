package part_02_additional.code;

import org.h2.jdbcx.JdbcDataSource;
import org.jdbcdslog.JDBCDSLogException;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.fail;

/**
 * @author Marco Behler <marco@marcobehler.com>
 */
public class LargeResultSetExercise {

    private DataSource ds;

    @Before
    public void setUp() {
        ds = getDataSource();

        try (Connection connection = ds.getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void exercise() throws JDBCDSLogException {
        /* this would register it with a JNDI service. ignore for now
        Context ctx = new InitialContext();
        ctx.bind("jdbc/datasource", ds);*/
        System.out.println("We are about to load thousands of records...");
        try (Connection connection = ds.getConnection()) {
            // for fetchSize below to work properly, you have to make
            // sure to start a tx/setAutocommit to false

            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();

            // we only want to query the server for batches of 1000 !
            // but make sure to read your JDBC driver documentation
            // regarding this property. your driver is allowed to ignore
            // it!
            statement.setFetchSize(1000);

            ResultSet results = statement.executeQuery(
                    "SELECT my_magic_number from numbers");
            results.next();
            while (results.next()) {
                int number = results.getInt("my_magic_number");
                System.out.println(number);
            }
            // these two would also be automatically closed once you close
            // the connection
            results.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private DataSource getDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }

    // we are inserting 10000 numbers into a table
    private void createTables(Connection conn) {
        try {
            conn.createStatement().execute("create table numbers "
                    + "(my_magic_number number unique)");
            for (int i = 0; i < 10000; i++) {
                conn.createStatement().execute("insert into numbers " +
                        "values (" + i + ")");
            }
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

}
