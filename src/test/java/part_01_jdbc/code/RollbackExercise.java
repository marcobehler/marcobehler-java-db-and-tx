package part_01_jdbc.code;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Marco Behler
 */
public class RollbackExercise {


    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void rollback_exercise() throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            // the three statements are sent to the database, but not
            // yet commmited, i.e. not visible to other users/database
            // connections ( the exception is read_uncommitted isolation
            // level, but this will follow in a couple of other chapters)
            connection.createStatement().execute("insert into items " +
                    "(name) values ('Windows 10 Premium Edition')");
            connection.createStatement().execute("insert into bids" +
                    " (user, time, amount, currency) values ('Hans', " +
                    "now(), 1, 'EUR')");
            connection.createStatement().execute("insert into bids " +
                    "(user, time, amount, currency) values ('Franz'," +
                    "now() , 2, 'EUR')");

            // ok, we are having second thoughts. we do not want
            // the database to remember those statements anymore.
            // this is how you rollback a transaction
            connection.rollback();
            System.out.println("We successfully rolled back our " +
                    "transaction!");

            // now how many items are there in the items table. Yes, NONE!
            assertThat(getItemsCount(connection), equalTo(0));
        }
    }

    private int getItemsCount(Connection connection) throws SQLException {
        // forget this for now, we simply want to know how many items
        // there are in the items table after rolling back
        ResultSet resultSet = connection.createStatement()
                .executeQuery("select count(*) as count from items");
        resultSet.next();
        int count = resultSet.getInt("count");
        System.out.println("Items in the items table: " + count);
        resultSet.close();
        return count;
    }

    // simply what we did in OpenConnectionExerciseJava6/7.java
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:exercise_db;" +
                "DB_CLOSE_DELAY=-1");
    }


    private void createTables(Connection conn) {
        try {
            conn.createStatement().execute("create table bids (id " +
                    "identity, user VARCHAR, time TIMESTAMP ," +
                    " amount NUMBER, currency VARCHAR) ");
            conn.createStatement().execute("create table items (id " +
                    "identity, name VARCHAR)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
