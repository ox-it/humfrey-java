package uk.ac.ox.oucs.humfrey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

import uk.ac.ox.oucs.humfrey.serializers.*;

public class ModelServlet extends HttpServlet {
	static NamedGraphSet namedGraphSet = null;
	static Map<String,Serializer> serializers = getSerializers();
	/**
	 * 
	 */
	private static final long serialVersionUID = 2653035836289316041L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		namedGraphSet = new NamedGraphSetImpl();
/*		ServletContext context = getServletContext();
		namedGraphSet = HumfreyModelFactory.getNamedGraphSet(
			context.getInitParameter("databaseURL"),
			context.getInitParameter("databaseUser"),
			context.getInitParameter("databasePassword")); */
		super.init(config);
	}
	
	public void serializeGraph(Graph graph, Query query, HttpServletRequest req, HttpServletResponse resp) {
		Model model = ModelFactory.createModelForGraph(graph);
		serializeModel(model, query, req, resp);
	}
	
	public void serializeModel(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) {
		if (serializers.containsKey(query.getFormat())) {
			try {
				serializers.get(query.getFormat()).serializeModel(model, query, req, resp);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	protected Query getQuery(HttpServletRequest req, HttpServletResponse resp) {
		try {
			return new Query(req);
		} catch (InvalidFormatException e) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
	}
	
	public String parseAcceptHeader(String acceptHeader) {
		String[] parts = acceptHeader.split(" *; *");
		for (String part : parts) {
			part = part.split(",")[0];
			if (part.equals("application/rdf+xml"))
				return "rdf";
			else if (part.equals("text/n3"))
				return "n3";
			else if (part.equals("text/plain"))
				return "nt";
			else if (part.equals("text/turtle"))
				return "ttl";
		}
		return null;
	}
	
	protected Model getModel() {
		return namedGraphSet.asJenaModel("");
	}
	
	protected boolean containsQuerySubject(Model model, Query query) {
		return model.containsResource(model.createResource(query.getURL().toString()));
	}
	
	private static Map<String,Serializer> getSerializers() {
		Map<String,Serializer> serializers = new HashMap<String,Serializer>();
		serializers.put("rdf", new RDFXMLSerializer());
		serializers.put("n3", new Notation3Serializer());
		serializers.put("nt", new NTripleSerializer());
		serializers.put("ttl", new TurtleSerializer());
		return serializers;
	}
	
	protected Map<String,String> getPrefixMapping() {
		Graph graph = namedGraphSet.asJenaGraph(Node.createURI(""));
		Map<String,String> prefixMap = new HashMap<String,String>();
		ExtendedIterator<Triple> triples = graph.find(
				null,
				Namespaces.rdf._("type"),
				Namespaces.owl._("Ontology"));
		while (triples.hasNext()) {
			Node ontology = triples.next().getSubject();
			
			ExtendedIterator<Triple> uris = graph.find(
					ontology, Namespaces.vann._("preferredNamespaceUri"), null);
			ExtendedIterator<Triple> prefixes = graph.find(
					ontology, Namespaces.vann._("preferredNamespacePrefix"), null);
			
			if (!uris.hasNext() || !prefixes.hasNext())
				continue;
			
			prefixMap.put(uris.next().getObject().toString(),
						 prefixes.next().getObject().toString());
		}
		prefixMap.putAll(Namespaces.getPrefixMapping());
		return prefixMap;
	}

}
