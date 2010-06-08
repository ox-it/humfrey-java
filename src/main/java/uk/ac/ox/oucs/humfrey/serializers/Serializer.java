package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Templater;

public class Serializer {
	private Map<String,AbstractSerializer> serializers = new HashMap<String,AbstractSerializer>();
	Model fullModel;
	
	public Serializer(Model fullModel, Templater templater) {
		this.fullModel = fullModel;
		
		serializers.put("rdf", new RDFXMLSerializer());
		serializers.put("n3", new Notation3Serializer());
		serializers.put("nt", new NTripleSerializer());
		serializers.put("ttl", new TurtleSerializer());
		serializers.put("js", new JSONSerializer(true));
		serializers.put("json", new JSONSerializer(false));
		serializers.put("html", new HTMLSerializer(templater, serializers));
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
		AbstractSerializer serializer = serializers.get(query.getFormat());
		try {
			serializer.serializeModel(model, fullModel, query, req, resp);
		} catch (IOException e) {
			throw new RuntimeException(e);
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
}
