package part_01_jdbc.code;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * @author Marco Behler
 */
public class OptimisticLockingExercise {


    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test(expected = OptimisticLockingException.class)
    public void optimistic_locking_exercise() throws SQLException {
        try (Connection connectionFromJackBauer = getConnection()) {
            connectionFromJackBauer.setAutoCommit(false);
            connectionFromJackBauer.createStatement().execute(
                    "insert into items " +
                            "(name, release_date, version) values " +
                            "('CTU Field Agent " +
                            "Report', current_date() - 100, 0)");

            // oops. we inserted the wrong release_date. let us quickly
            // update the release_date and increase the version number
            int updatedRows = connectionFromJackBauer.createStatement()
                    .executeUpdate(
                    "update items set release_date = current_date(), " +
                            " version = version + 1 " +
                            "where name = 'CTU Field Agent Report'" +
                            " and version = 0");
            System.out.println("Rows updated by Jack Bauer: " +
                    updatedRows);
            connectionFromJackBauer.commit();
        }

        // meanwhile, habib marwin is trying to set the release_date to
        // today + 10 days. but he is trying to do it on version 0
        try (Connection connectionFromHabibMarwan = getConnection()) {
            connectionFromHabibMarwan.setAutoCommit(false);
            int updatedRows = connectionFromHabibMarwan.createStatement()
                    .executeUpdate(
                    "update items set release_date = current_date() + 10," +
                            "  version = version + 1" +
                            "where name = 'CTU Field Agent Report'" +
                            " and version = 0");
            System.out.println("Rows updated by Habib Marwan: " +
                    updatedRows);
            // this line is how pretty much all (java) frameworks do
            // optimistic locking. Checking the updatedRow count.
            if (updatedRows == 0) throw new OptimisticLockingException();
        }
    }

    // self made. check out the one from hibernate or spring
    public static class OptimisticLockingException extends
            RuntimeException {}


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
                    "identity, name VARCHAR, release_date date," +
                    " version NUMBER default " +
                    "0)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
