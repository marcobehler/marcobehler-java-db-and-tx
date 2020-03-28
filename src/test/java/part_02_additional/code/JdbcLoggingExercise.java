package part_02_additional.code;

import org.h2.jdbcx.JdbcDataSource;
import org.jdbcdslog.DataSourceProxy;
import org.jdbcdslog.JDBCDSLogException;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Marco Behler <marco@marcobehler.com>
 */
public class JdbcLoggingExercise {

    @Test
    public void exercise() throws JDBCDSLogException {
        DataSource ds = getDataSource();
        try (Connection conn = ds.getConnection()) {
            conn.createStatement().execute("select 1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DataSource getDataSource() throws JDBCDSLogException {
        JdbcDataSource originalDS = new JdbcDataSource();
        originalDS.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
        originalDS.setUser("sa");
        originalDS.setPassword("sa");

        // this is only _one_ way of creating a logging datasource wrapper
        // for the other ones, check the homepage for the other ways
        DataSourceProxy ds = new DataSourceProxy();
        ds.setTargetDSDirect(originalDS);
        return ds;
    }
}
