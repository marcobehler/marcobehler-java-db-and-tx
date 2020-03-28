package part_01_jdbc.code;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * @author Marco Behler
 */
public class AutocommitExercise {


    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void autocommit_true() {
        try (Connection connection = getConnection()) {
            connection.createStatement().execute("insert into items (name)"
                    + " values ('Windows 10 Premium Edition')");
            // database saved our lonely, precious item!
            connection.createStatement().execute("insert into bids (user,"
                    + " time, amount, currency) values ('Hans', now(), 1" +
                    ", 'EUR')");
            // database saved our first bid
            connection.createStatement().execute("insert into bids (user,"
                    + " time, amount, currency) values ('Franz',now() ," +
                    " 2" + ", 'EUR')");
            //database saved our second bid
            System.out.println("everything saved to the database with " +
                    "autocommit=true");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // simply what we did in OpenConnectionExerciseJava6/7.java
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:exercise_db;" +
                "DB_CLOSE_DELAY=-1");
    }


    private void createTables(Connection conn) {
        try {
            conn.createStatement().execute("create table bids " +
                    "(id identity, user VARCHAR, time TIMESTAMP ," +
                    " amount NUMBER, currency VARCHAR) ");
            conn.createStatement().execute("create table items (id" +
                    " identity, name VARCHAR)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
