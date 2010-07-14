package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public abstract class AbstractSerializer {
	Map<String,AbstractSerializer> serializers;
	
	public abstract String getContentType();
	public abstract String getName();
	
	public void serializeModel(Model model, Model fullModel, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		throw new NotImplementedException();
	}
	public void serializeResultSet(ResultSet resultset, Query query, HttpServletRequest req, HttpServletResponse resp)  throws IOException {
		throw new NotImplementedException();
	}
	public void serializeSparqlError(String message, Query query, HttpServletRequest req, HttpServletResponse resp)  throws IOException {
		throw new NotImplementedException();
	}
	public void serializeResources(Model model, Model fullModel, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serializeModel(model, fullModel, query, req, resp);
	}
	
}
