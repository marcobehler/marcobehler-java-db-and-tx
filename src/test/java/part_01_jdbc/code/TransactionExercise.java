package part_01_jdbc.code;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * @author Marco Behler
 */
public class TransactionExercise {


    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void transaction_exercise()  {
        try (Connection connection = getConnection()) {
            // this is the ONLY way you start a transaction in Java
            // with plain JDBC.
            System.out.println("Opening up a jdbc transaction...");
            connection.setAutoCommit(false);

            // the three statements are sent to the database, but not
            // yet commmited, i.e. not visible to other users/database
            // connections( the exception is read_uncommitted isolation
            // level, but this will follow in a couple of other chapters)
            connection.createStatement().execute("insert into items" +
                    " (name) values ('Windows 10 Premium Edition')");
            connection.createStatement().execute("insert into bids " +
                    "" +
                    "(user, time, amount, currency) values ('Hans', now()," +
                    " 1" +", 'EUR')");
            connection.createStatement().execute("insert into bids (user, "
                    +"time, amount, currency) values ('Franz',now() , 2," +
                    " 'EUR')");

            // and this is how you commit your transaction
            connection.commit();

            System.out.println("Commit worked! Now everything is in the " +
                    "database");

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
