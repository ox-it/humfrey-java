package uk.ac.ox.oucs.humfrey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.util.StoreUtils;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;

public class HumfreyModelFactory {
	static private Map<String,Model> models = new HashMap<String,Model>();
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
