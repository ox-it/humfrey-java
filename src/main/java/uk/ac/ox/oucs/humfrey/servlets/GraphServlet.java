package uk.ac.ox.oucs.humfrey.servlets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;

public class GraphServlet extends ModelServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8868243256357826456L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Node uri = query.getNode();
		
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
		updateGraph(req, resp, new GraphUpdater(){
			public void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node) {
				NamedGraph namedGraph = namedGraphSet.createGraph(node);
				namedGraph.getBulkUpdateHandler().add(graph);
			}
		});
	}
	
	protected void doUnion(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		updateGraph(req, resp, new GraphUpdater(){
			public void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node) {
				NamedGraph namedGraph = namedGraphSet.getGraph(node);
				namedGraph.getBulkUpdateHandler().add(graph);
			}
		});
	}

	protected void doIntersection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		updateGraph(req, resp, new GraphUpdater(){
			public void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node) {
				NamedGraph namedGraph = namedGraphSet.getGraph(node);
				ExtendedIterator<Triple> triples = namedGraph.find(null, null, null);
				List<Triple> triplesToRemove = new LinkedList<Triple>();
				while (triples.hasNext()) {
					Triple triple = triples.next();
					if (!graph.contains(triple))
						triplesToRemove.add(triple);
				}
				namedGraph.getBulkUpdateHandler().delete(triplesToRemove);
			}
		});
	}
	
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Node node = query.getNode();
		if (namedGraphSet.containsGraph(node)) {
			namedGraphSet.removeGraph(node);
			resp.setStatus(200);
		} else
			resp.setStatus(404);
		
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
	
	private void updateGraph(HttpServletRequest req, HttpServletResponse resp,
			GraphUpdater graphUpdater) throws ServletException, IOException {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Node node = query.getNode();
		
		Model model = ModelFactory.createDefaultModel();
		model.read(req.getReader(), query.getSerialization());
		
		graphUpdater.updateGraph(namedGraphSet, model.getGraph(), node);
		resp.setStatus(200);
	}
	
	interface GraphUpdater {
		void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node);
	}

}
