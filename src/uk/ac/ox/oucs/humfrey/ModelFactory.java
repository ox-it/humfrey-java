package uk.ac.ox.oucs.humfrey;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;

public class ModelFactory {
	static private Map<String,Model> models = new HashMap<String,Model>();

	static Model getModel(String filename) {
		if (models.containsKey(filename)) {
			return models.get(filename);
		} else {
			Store store = SDBFactory.connectStore(filename);
			try {
				if (!StoreUtils.isFormatted(store))
					store.getTableFormatter().create();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			Model model = SDBFactory.connectDefaultModel(store);
			models.put(filename, model);
			return model;
		}
	}
}
