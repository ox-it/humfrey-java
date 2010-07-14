package uk.ac.ox.oucs.humfrey.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
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
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Templater;
import uk.ac.ox.oucs.humfrey.serializers.*;

public class ModelServlet extends HttpServlet {
	static NamedGraphSet namedGraphSet;
	static Model configModel;
	Serializer serializer;
	Templater templater;
	String homeURIRegex;
	/**
	 * 
	 */
	private static final long serialVersionUID = 2653035836289316041L;

	@Override
	public void init() throws ServletException {
		super.init();
		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}
		
		ServletContext context = getServletContext();
		Connection connection;
		try {
			connection = DriverManager.getConnection(
					context.getInitParameter("humfrey.databaseURL"),
					context.getInitParameter("humfrey.databaseUser"),
					context.getInitParameter("humfrey.databasePassword"));
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		namedGraphSet = new NamedGraphSetDB(connection);
		templater = new Templater(getServletContext());
		serializer = new Serializer(namedGraphSet.asJenaModel(""), templater, homeURIRegex);
		homeURIRegex = context.getInitParameter("humfrey.homeURIRegex");

		configModel = ModelFactory.createDefaultModel();
		
		InputStream is;
		try {
			File file = new File(context.getInitParameter("humfrey.configPath"));
			is = new FileInputStream(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		configModel.read(is, null, "N3");
	}
	
	public void serializeGraph(Graph graph, Query query, HttpServletRequest req, HttpServletResponse resp) {
		Model model = ModelFactory.createModelForGraph(graph);
		serializeModel(model, query, req, resp);
	}
	
	public void serializeModel(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) {
		serializer.serializeModel(model, query, req, resp);
	}
	
	protected Query getQuery(HttpServletRequest req, HttpServletResponse resp) {
		ServletContext context = getServletContext();
		try {
			return new Query(context.getInitParameter("humfrey.accountPrefix"), configModel, serializer, req);
		} catch (Query.InvalidFormatException e) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		} catch (Query.InvalidCredentialsException e) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			resp.addHeader("WWW-Authenticate", context.getInitParameter("humfrey.wwwAuthenticateHeader"));
			return null;
		} catch (Query.UnknownQueryException e) {
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
