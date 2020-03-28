package part_01_jdbc.code;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Marco Behler
 */
public class OpenConnectionExerciseJava6 {

    private static final Integer NO_TIMEOUT = 0;

    @Test
    public void open_jdbc_connection_java_6() {
        Connection conn = null;
        try {
            // here we open the connection. that is all there is, really!
            // just like the mysql command line client, but we are using
            // an in-memory h2 database here
            // so we do not have to specify a username/password
            conn = DriverManager.getConnection(
                    "jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");

            // just checking that i am not lying to you
            System.out.println("Are we connected to the database? : "
                    + conn.isValid(NO_TIMEOUT));

            // let us create the base tables for our Ebay Clone!
            conn.createStatement().execute("create table bids " +
                    "(id identity, user VARCHAR, time TIMESTAMP , " +
                    "amount NUMBER, currency VARCHAR) ");

            conn.createStatement().execute("create table items " +
                    "(id identity, name VARCHAR)");

            System.out.println("Yay, tables created!");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // we need to make sure to close connections once we do not
            // use them anymore. else they will idle around forever!
            if (conn != null) {
                try {
                    // unfortunately closing a connection can also throw
                    // an exception, hence the wrapping
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
