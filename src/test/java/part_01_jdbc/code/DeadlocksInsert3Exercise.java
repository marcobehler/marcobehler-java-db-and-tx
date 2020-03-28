package part_01_jdbc.code;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;


/**
 * @author Marco Behler
 */
public class DeadlocksInsert3Exercise {

    @Before
    public void setUp() {
        try (Connection connection = getConnection()) {
            createTables(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static final Long WAIT_BEFORE_COMMIT_MS = 1300l;
    private static final Long ORDERING_SLEEP = 150l;


    @Test
    public void deadlock_insert_exercise_part3() throws Exception {
        System.out.println("Do we reach the end of the test without a " +
                "deadlock?...");

        Thread i1 = new Thread(new Inserter("Jack Bauer",
                WAIT_BEFORE_COMMIT_MS));

        Thread i2 = new Thread(new Inserter("Habib Marwan"));

        i1.start();
        // dirty way to make sure i2 _really_ starts after i1
        Thread.sleep(ORDERING_SLEEP);
        i2.start();

        i1.join();
        i2.join();

        System.out.println("Yes!");
    }


    public class Inserter implements Runnable {

        private String name;
        private Long waitBeforeCommit;


        public Inserter(String name) {
            this.name = name;
        }

        public Inserter(String name, Long waitBeforeCommit) {
            this.waitBeforeCommit = waitBeforeCommit;
            this.name = name;
        }

        @Override
        public void run() {
            long start = System.nanoTime();

            try (Connection connection = getConnection()) {
                connection.setAutoCommit(false);
                connection.createStatement().execute(
                        "insert into items " +
                                "(name) values ('CTU Field Agent Report')");

                if (waitBeforeCommit != null) {
                    // let's wait a bit before committing
                    Thread.sleep(WAIT_BEFORE_COMMIT_MS);
                }
                connection.commit();
            } catch (Exception e) {
                if (e instanceof SQLException) {
                    String errorCode = ((SQLException) e).getSQLState();
                    System.err.println("Got error code " + errorCode + " " +
                            "when trying to insert a row into the items " +
                            "tablew");
                } else {
                    e.printStackTrace();
                }
            } finally {
                long end = System.nanoTime();
                long durationMs = (end - start) / 1000000;
                System.out.println("User[= " + name + "]. The whole " +
                        "getTransactionalConnection/insertion" +
                        " " +
                        "process took: " +
                        durationMs + " ms");
                assertTrue(durationMs > WAIT_BEFORE_COMMIT_MS - ORDERING_SLEEP);
            }
        }
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
