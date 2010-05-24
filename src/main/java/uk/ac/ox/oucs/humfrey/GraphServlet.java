package uk.ac.ox.oucs.humfrey;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

import de.fuberlin.wiwiss.ng4j.NamedGraph;

public class GraphServlet extends ModelServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8868243256357826456L;

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		//super.doDelete(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Node uri = query.getURI();
		
		if (!namedGraphSet.containsGraph(uri)) {
			resp.setStatus(404);
			return;
		}
		
		Graph graph = namedGraphSet.getGraph(uri);
		serializeGraph(graph, query, req, resp);
		resp.setStatus(200);
		
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Node uri = query.getURI();
		
		Model model = ModelFactory.createDefaultModel();
		
		try {
			model.read(req.getReader(), query.getSerialization());
		} catch (JenaException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		NamedGraph namedGraph = namedGraphSet.createGraph(uri);
		namedGraph.getBulkUpdateHandler().add(model.getGraph());
		resp.setStatus(200);
	}
	
	void doUnion(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Node uri = query.getURI();
		
		Model model = ModelFactory.createDefaultModel();
		model.read(req.getReader(), query.getSerialization());
		
		NamedGraph namedGraph = namedGraphSet.getGraph(uri);
		namedGraph.getBulkUpdateHandler().add(model.getGraph());
		resp.setStatus(200);
	}

	void doIntersection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getMethod().equals("UNION"))
			doUnion(req, resp);
		else if (req.getMethod().equals("INTERSECTION"))
			doIntersection(req, resp);
		else
			super.service(req, resp);
	}

}
