package uk.ac.ox.oucs.humfrey;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class DocServlet extends ModelServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 640907447304234785L;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Model model = getModel();
		URL url = query.getURL();
		
		if (containsQuerySubject(model, query)) {
			Model resourceModel = buildModelForResource(model.createResource(url.toString()));
			serializeModel(resourceModel, query, req, resp);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	private Model buildModelForResource(Resource resource) {
		Model model = ModelFactory.createDefaultModel();
		buildModelForResource(model, new HashSet<RDFNode>(), resource);
		return model;
	}
	
	public void buildModelForResource(Model model, Set<RDFNode> seen, Resource resource) {
		seen.add(resource);
		StmtIterator stmts = resource.listProperties();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			model.add(stmt);
			if (stmt.getObject() instanceof Resource && !seen.contains(stmt.getObject()))
				buildModelForResource(model, seen, resource);
		}
	}

}
