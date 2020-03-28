package part_05_jooq.code;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Marco Behler
 */
public class JooqTransactions {

    @Test
    public void transactions() {
        try (Connection connection = getConnection()) {
            // TODO exercise: use DSL.using(configuration) here
            final DSLContext create = DSL.using(connection, SQLDialect.H2);

            create.transaction(new TransactionalRunnable() {
                @Override
                public void run(Configuration configuration)  throws
                        Exception {
                    System.out.println("Yay, we are inside a transaction");
                    create.createTable("bids")
                            .column("id", SQLDataType.INTEGER)
                            .column("name", SQLDataType.TIMESTAMP)
                            .execute();
                    // yay we have a transaction here
                    // implicit commit executed here

                    // TODO throw new Exception..what happens ? commit
                    // or rollback or nothing?
                }
            });
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
