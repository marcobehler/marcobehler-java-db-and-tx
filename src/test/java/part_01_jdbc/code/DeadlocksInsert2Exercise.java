package part_01_jdbc.code;

import org.h2.jdbc.JdbcSQLException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * @author Marco Behler
 */
public class DeadlocksInsert2Exercise {

    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test(expected = JdbcSQLException.class)
    public void deadlock_insert_exercise_part2() throws SQLException {
        System.out.println("Do we reach the end of the test without a " +
                "deadlock?...");
        try (Connection connectionFromJackBauer = getConnection()) {
            connectionFromJackBauer.setAutoCommit(false);
            connectionFromJackBauer.createStatement().execute(
                    "insert into items " +
                    "(name) values ('CTU Field Agent Report')");

            try (Connection connectionFromHabibMarwan = getConnection()) {
                connectionFromHabibMarwan.setAutoCommit(false);
                connectionFromHabibMarwan.createStatement().execute(
                        "insert into items " +
                        "(name) values ('CTU Field Agent Report')");
            }

        }
        System.out.println("Yes!");
    }

    private void createTables(Connection conn) {
        try {
            conn.createStatement().execute("create table bids (id " +
                    "identity, user VARCHAR, time TIMESTAMP ," +
                    " amount NUMBER, currency VARCHAR) ");
            conn.createStatement().execute("create table items (id " +
                    "identity, name VARCHAR unique)");
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
