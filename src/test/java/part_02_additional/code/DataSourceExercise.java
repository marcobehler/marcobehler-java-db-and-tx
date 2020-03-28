package part_02_additional.code;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Marco Behler <marco@marcobehler.com>
 */
public class DataSourceExercise {

    private static final Integer NO_TIMEOUT = 0;

    @Test
    public void exercise() {
        DataSource ds = getDataSource();
        try (Connection connection = ds.getConnection()) {
            System.out.println("Yay, we got our connection to the " +
                    "database through a datasource!");
            assertTrue(connection.isValid(NO_TIMEOUT));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // datasource is an interface and can have many different
    // implementations
    // one of the h2 implementation is the JdbcDataSource (which offers
    // no pooling or distributed transactions)
    private DataSource getDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");
        /* this would register it with a JNDI service. ignore for now
        Context ctx = new InitialContext();
        ctx.bind("jdbc/datasource", ds);*/
        return ds;
    }

}
