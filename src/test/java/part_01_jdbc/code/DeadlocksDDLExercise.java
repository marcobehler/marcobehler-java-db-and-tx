package part_01_jdbc.code;

import org.h2.jdbc.JdbcSQLException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Marco Behler
 */
public class DeadlocksDDLExercise {


    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = JdbcSQLException.class)
    public void deadlockDDL_exercise() throws SQLException {
        System.out.println("Do we reach the end of the test? (If " +
                "deadlock, then no, we won't)...");
        try (Connection connectionFromJackBauer = getConnection()) {
            connectionFromJackBauer.setAutoCommit(false);
            connectionFromJackBauer.createStatement().execute("" +
                    "insert into items " +
                    "(name) values ('CTU Field Agent Report')");
            try (Connection connectionFromHabibMarwan = getConnection()) {
                connectionFromHabibMarwan.setAutoCommit(false);
                connectionFromHabibMarwan.createStatement().execute(
                    "alter table items add column (release_date date null)");
                // do not forget to do the study drills!
                connectionFromHabibMarwan.commit();
            }

            connectionFromJackBauer.commit();
        }
        System.out.println("Yes!");
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


    // simply what we did in OpenConnectionExerciseJava6/7.java
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:exercise_db;" +
                "DB_CLOSE_DELAY=-1");
    }



}
