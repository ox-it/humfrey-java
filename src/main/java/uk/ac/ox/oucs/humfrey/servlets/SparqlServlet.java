package uk.ac.ox.oucs.humfrey.servlets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SparqlServlet extends ModelServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Model model = namedGraphSet.asJenaModel("");
		String query = req.getParameter("query");
		List<List<Object>> results = null;
		List<String> bindings = null;
		
		if (query != null) {
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			try {
				ResultSet resultset = qexec.execSelect();
				bindings = resultset.getResultVars();
				results = new LinkedList<List<Object>>();
				while (resultset.hasNext()) {
					QuerySolution soln = resultset.next();
					List<Object> result = new LinkedList<Object>();
					for (String binding : bindings) {
						RDFNode node = soln.get(binding);
						if (node.isResource())
							result.add(VelocityResource.create((Resource) node));
						else
							result.add(((Literal) node).getValue());
					}
					results.add(result);
				}
			} finally {
				qexec.close();
			}

		}
		
		VelocityContext context = new VelocityContext();
		context.put("query", query);
		context.put("results", results);
		context.put("bindings", bindings);
		
		templater.render(resp, "sparql.vm", context);
	}

	private static final long serialVersionUID = -8371160059122150837L;
	
	

}
