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
public class SetupExercise {

    @Test
    public void setupJooq() {
        try (Connection connection = getConnection()) {
            // TODO exercise: use DSL.using(configuration) here
            DSLContext create = DSL.using(connection, SQLDialect.H2);
            create.createTable("bids")
                    .column("id", SQLDataType.INTEGER)
                    .column("name", SQLDataType.TIMESTAMP)
                    .execute();
            // TODO exercise: do a plain JooQ SQL select (hint: create
            // .query(sql) on the bids table

            // TODO to see how jooq usually works, finish this query

            Field<Integer> ID = DSL.field("id", SQLDataType.INTEGER);
            Table<Record> bids = DSL.table("bids");
            // todo complete .... -> create.select(ID).from(bids).

            System.out.println("We have a DSL Context, yay!");

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
