package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public abstract class AbstractSerializer {
	public enum SerializationType {
		ST_NOQUERY,
		ST_EXCEPTION,
		ST_MODEL,
		ST_RESULTSET,
		ST_RESOURCE,
		ST_RESOURCELIST,
		ST_BOOLEAN,
	}

	Map<String,AbstractSerializer> serializers;

	public abstract String getContentType();
	public abstract String getName();

	public void serializeSparqlError(String message, Query query, HttpServletRequest req, HttpServletResponse resp)  throws IOException {
		resp.setContentType("text/plain");
		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		resp.getWriter().write("There was an error processing your query:\n\n" + message + "\n");
	}
	public void serializeModel(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		throw new NotImplementedException();
	}
	public void serializeResultSet(ResultSet resultset, Query query, HttpServletRequest req, HttpServletResponse resp)  throws IOException {
		throw new NotImplementedException();
	}
	public void serializeResource(Resource resource, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		throw new NotImplementedException();
	}
	public void serializeResourceList(List<Resource> resources, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		throw new NotImplementedException();
	}
	public void serializeBoolean(List<Resource> resources, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		throw new NotImplementedException();
	}
	
	public boolean canSerialize(SerializationType serializationType) {
		return false;
	}
	
	void serializeResourceList(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		List<Resource> resources = new LinkedList<Resource>();
		ResIterator resIterator = model.listSubjects();
		while (resIterator.hasNext())
			resources.add(resIterator.next());
		serializeResourceList(resources, query, req, resp);
	}
	
	public Model buildModelForResource(Resource resource) {
		Model model = ModelFactory.createDefaultModel();
		buildModelForResource(resource, model, new HashSet<Resource>());
		return model;
	}
	public void buildModelForResource(Resource resource, Model model) {
		buildModelForResource(resource, model, new HashSet<Resource>());
	}
	public void buildModelForResource(Resource resource, Model model, Set<Resource> seen) {
		seen.add(resource);
		StmtIterator stmts = resource.listProperties();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			model.add(stmt);
			if (stmt.getObject().isAnon() && !seen.contains(resource))
				buildModelForResource(resource, model, seen);
		}
	}
	
	protected static void addFormatInformation(Model model, Query query, Serializer serializer) {
		Property dct_isFormatOf = Namespaces.dct.p("isFormatOf");
		Property dct_format = Namespaces.dct.p("format");
		Property rdf_type = Namespaces.rdf.p("type");
		Property foaf_primaryTopic = Namespaces.foaf.p("primaryTopic");
		RDFNode docURI = model.createResource(query.getDocURL());
		
		for (Entry<String, AbstractSerializer> entry : serializer.getSerializers(SerializationType.ST_RESOURCE).entrySet()) {
			Resource docFormatURI = model.createResource(query.getDocURL(entry.getKey()));
			docFormatURI
				.addProperty(dct_isFormatOf, docURI)
				.addProperty(rdf_type, Namespaces.dctype.r("Text"))
				.addProperty(rdf_type, Namespaces.foaf.r("Document"))
				.addProperty(foaf_primaryTopic, model.createResource(query.getNode().toString()))
				.addProperty(dct_format, entry.getValue().getContentType());
		}
	}
}
