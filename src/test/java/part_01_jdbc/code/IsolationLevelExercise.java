package part_01_jdbc.code;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author Marco Behler
 */
public class IsolationLevelExercise {


    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void isolation_level_exercise() throws SQLException {
        System.out.println("Do we reach the end of the test without a " +
                "deadlock?...");
        // lets insert an item
        try (Connection connectionFromJackBauer = getConnection()) {
            connectionFromJackBauer.setAutoCommit(false);
            connectionFromJackBauer.createStatement().execute(
                    "insert into items " +
                    "(name) values ('CTU Field Agent Report')");
            connectionFromJackBauer.commit();
        }

        // then update it
        try (Connection connectionFromJackBauer = getConnection()) {
            connectionFromJackBauer.setAutoCommit(false);
            connectionFromJackBauer.createStatement().executeUpdate(
                    "update items set name = 'chloes report' " +
                    "where name = 'CTU Field Agent Report'");

            try (Connection connectionFromHabibMarwan = getConnection()) {
                // h2 unfortunately ignores this and goes back to
                // READ_COMMITTED
                connectionFromHabibMarwan.setTransactionIsolation(
                        Connection.TRANSACTION_READ_UNCOMMITTED);
                connectionFromHabibMarwan.setAutoCommit(false);

                ResultSet resultSet = connectionFromHabibMarwan
                        .createStatement().executeQuery(
                                "select count(*) as count from items " +
                                        "where name = 'chloes report'");
                resultSet.next();
                int itemsCount = resultSet.getInt("count");

                System.out.println("Habib can see how many items in the " +
                        "table?: " + itemsCount);
                // this should be 1 with the right
                // database/driver/settings..is 0 with h2
                connectionFromHabibMarwan.commit();
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
                    "identity, name VARCHAR)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // simply what we did in OpenConnectionExerciseJava6/7.java
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:exercise_db;" +
                "DB_CLOSE_DELAY=-1;");
    }




}
