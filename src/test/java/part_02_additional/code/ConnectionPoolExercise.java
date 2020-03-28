package part_02_additional.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Marco Behler <marco@marcobehler.com>
 */
public class ConnectionPoolExercise {

    private static final Integer NO_TIMEOUT = 0;

    @Test
    public void exercise() {
        DataSource ds = getDataSource();
        try (Connection connection = ds.getConnection()) {
            System.out.println("Yay, we got our pooled connection to the" +
                    " database!");
            assertTrue(connection.isValid(NO_TIMEOUT));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // it is recommended to .close() a HikariDataSource once you
            // are done using it. Other datasources might have different
            // shutdown methods
          if (ds instanceof  HikariDataSource) {
              ((HikariDataSource) ds).close();
          }
        }
    }

    // hikari is a (great) connection pool. use it or Vibur DBCP instead of
    // all other variants out there , like dbcp
    private DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("sa");
        return new HikariDataSource(config);
    }
}
