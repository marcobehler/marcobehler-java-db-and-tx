package part_01_jdbc.code;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("CallToDriverManagerGetConnection")
public final class OpenConnectionExerciseJava7Test {
	@Test
	public void open_dbc_connection_java_7() {
		try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:excercise_db;DB_CLOSE_DELAY=-1"); //NON-NLS
		     final Statement statement = connection.createStatement()) {
			final boolean isConnectionValid = connection.isValid(0);
			final String message = "Are we connected to the database? : %s%n"; //NON-NLS
			System.out.printf(message, isConnectionValid);
			statement.execute("CREATE TABLE bids (id IDENTITY, user VARCHAR, time TIMESTAMP, amount NUMBER, currency VARCHAR)");
			statement.execute("CREATE TABLE winning_bids (bid_id NUMBER, item_id NUMBER)");
			assertTrue(true);
		} catch (SQLException e) {
			final String message = "An SQL error occured : %s"; //NON-NLS
			fail(String.format(message, e));
		}
	}
}
