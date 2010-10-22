package uk.ac.ox.oucs.humfrey.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

import uk.ac.ox.oucs.humfrey.FormatPreferences;
import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Templater;
import uk.ac.ox.oucs.humfrey.serializers.*;

public abstract class ModelServlet extends HttpServlet {
	static Dataset dataset;
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
		
		TDB.getContext().set(TDB.symUnionDefaultGraph, true);
		dataset = TDBFactory.createDataset("/tmp/tdb/");
		
		ServletContext context = getServletContext();
		
		templater = new Templater(getServletContext());
		serializer = new Serializer(dataset, templater, homeURIRegex);
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
		return getQuery(req, resp, getAcceptFormats(), getContentTypeFormats());
		
	}
	
	protected Query getQuery(HttpServletRequest req, HttpServletResponse resp, FormatPreferences acceptFormats, FormatPreferences contentTypeFormats) {
		ServletContext context = getServletContext();
		try {
			return new Query(context.getInitParameter("humfrey.accountPrefix"), configModel, req, acceptFormats, contentTypeFormats);
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
	
	protected boolean containsQuerySubject(Model model, Query query) {
		return model.containsResource(model.createResource(query.getURL().toString()));
	}
	
	protected Map<String,String> getPrefixMapping() {
		Map<String,String> prefixMap = new HashMap<String,String>();
		/*Graph graph = namedGraphSet.asJenaGraph(Node.createURI(""));
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
		}*/
		prefixMap.putAll(Namespaces.getPrefixMapping());
		return prefixMap;
	}
	
	protected boolean datasetContains(String uri) {
		QueryExecution qexec = QueryExecutionFactory.create("ASK WHERE {<"+uri+"> ?p ?o}", dataset);
		return qexec.execAsk();
	}
	
	protected FormatPreferences getAcceptFormats() {
		throw new NotImplementedException();
	}
	protected FormatPreferences getContentTypeFormats() {
		return FormatPreferences.noFormats;
	}

}
