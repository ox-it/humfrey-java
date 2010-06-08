package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.rdf.model.Model;

public abstract class AbstractSerializer {
	Map<String,AbstractSerializer> serializers;
	
	public abstract String getContentType();
	public abstract String getName();
	abstract public void serializeModel(Model model, Model fullModel, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
