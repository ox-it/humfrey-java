package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Templater;
import uk.ac.ox.oucs.humfrey.Util;

/**
 * An interface to all available serializers.
 * 
 * @author Alexander Dutton <alexander.dutton@oucs.ox.ac.uk>
 */
public class Serializer {
	private Map<String,AbstractSerializer> serializers = new HashMap<String,AbstractSerializer>();
	Model fullModel;
	
	public Serializer(Model fullModel, Templater templater, String homeURIRegex) {
		this.fullModel = fullModel;
		
		serializers.put("rdf", new RDFXMLSerializer());
		serializers.put("n3", new Notation3Serializer());
		serializers.put("nt", new NTripleSerializer());
		serializers.put("ttl", new TurtleSerializer());
		serializers.put("js", new JSONSerializer(true));
		serializers.put("json", new JSONSerializer(false));
		serializers.put("srx", new SparqlXMLSerializer());
		serializers.put("html", new HTMLSerializer(templater, this, homeURIRegex));
	}
	
	public AbstractSerializer get(String format) {
		return serializers.get(format);
	}
	
	public boolean hasFormat(String format) {
		return serializers.containsKey(format);
	}
	
	public Collection<String> getFormats() {
		return serializers.keySet();
	}
	
	public Map<String,AbstractSerializer> getSerializers() {
		return serializers;
	}
	
	public void serializeModel(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) {
		AbstractSerializer serializer = serializers.get(query.getAccept());
		try {
			serializer.serializeModel(model, fullModel, query, req, resp);
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NotImplementedException e) {
			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public void serializeResultSet(ResultSet resultset, Query query, HttpServletRequest req, HttpServletResponse resp) {
		AbstractSerializer serializer = serializers.get(query.getAccept());
		try {
			serializer.serializeResultSet(resultset, query, req, resp);
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NotImplementedException e) {
			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public void serializeSparqlError(String message, Query query, HttpServletRequest req, HttpServletResponse resp) {
		AbstractSerializer serializer = serializers.get(query.getAccept());
		try {
			serializer.serializeSparqlError(message, query, req, resp);
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NotImplementedException e) {
			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public void serializeResources(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) {
		AbstractSerializer serializer = serializers.get(query.getAccept());
		try {
			serializer.serializeResources(model, fullModel, query, req, resp);
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NotImplementedException e) {
			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public static Map<Property,Set<RDFNode>> getPropertyMap(Resource resource) {
		StmtIterator stmts = resource.listProperties();
		Map<Property,Set<RDFNode>> properties = new HashMap<Property,Set<RDFNode>>();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			if (!properties.containsKey(stmt.getPredicate()))
				properties.put(stmt.getPredicate(), new HashSet<RDFNode>());
			properties.get(stmt.getPredicate()).add(stmt.getObject());
		}
		return properties;
	}
	
	public Map<String,AbstractSerializer> getResourceSerializers(Resource resource) {
		Map<String,AbstractSerializer> matches = new HashMap<String,AbstractSerializer>();
		Set<Resource> types = Util.getTypes(resource);
		for (String format : serializers.keySet()) {
			AbstractSerializer serializer = serializers.get(format);
			if (serializer.canSerializeResource(resource, types))
				matches.put(format, serializer);
		}
		return matches;
	}
	
	public Map<String,AbstractSerializer> getResourceListSerializers() {
		Map<String,AbstractSerializer> matches = new HashMap<String,AbstractSerializer>();
		for (String format : serializers.keySet()) {
			AbstractSerializer serializer = serializers.get(format);
			if (serializer.canSerializeResourceList())
				matches.put(format, serializer);
		}
		return matches;
	}
	
	public Map<String,AbstractSerializer> getModelSerializers() {
		Map<String,AbstractSerializer> matches = new HashMap<String,AbstractSerializer>();
		for (String format : serializers.keySet()) {
			AbstractSerializer serializer = serializers.get(format);
			if (serializer.canSerializeModel())
				matches.put(format, serializer);
		}
		return matches;
	}
	
	public Map<String,AbstractSerializer> getResultSetSerializers() {
		Map<String,AbstractSerializer> matches = new HashMap<String,AbstractSerializer>();
		for (String format : serializers.keySet()) {
			AbstractSerializer serializer = serializers.get(format);
			if (serializer.canSerializeResultSet())
				matches.put(format, serializer);
		}
		return matches;
	}
	
	
}
