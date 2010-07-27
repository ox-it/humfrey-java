package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

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
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		resp.getWriter().write("There was an error processing your query:\n\n" + message + "\n");
	}
	public void serializeResources(Model model, Model fullModel, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serializeModel(model, fullModel, query, req, resp);
	}
	
	public boolean canSerializeResultSet() {
		return false;
	}
	
	public boolean canSerializeResource(Resource resource, Set<Resource> types) {
		return false;
	}
	
	public boolean canSerializeResourceList() {
		return false;
	}
	
	public boolean canSerializeModel() {
		return false;
	}
}
