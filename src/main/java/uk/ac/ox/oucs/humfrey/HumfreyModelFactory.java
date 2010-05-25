package uk.ac.ox.oucs.humfrey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;

public class HumfreyModelFactory {
	static private NamedGraphSet namedGraphSet = null;
	
	static NamedGraphSet getNamedGraphSet(String url, String user, String password) {
		if (namedGraphSet != null)
			return namedGraphSet;
		else {
			Connection connection;
			try {
				connection = DriverManager.getConnection(url, user, password);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			namedGraphSet = new NamedGraphSetDB(connection);
			return namedGraphSet;
		}
	}
}
