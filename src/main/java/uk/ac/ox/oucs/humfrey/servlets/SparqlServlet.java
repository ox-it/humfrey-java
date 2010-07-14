package uk.ac.ox.oucs.humfrey.servlets;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

public class SparqlServlet extends ModelServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		uk.ac.ox.oucs.humfrey.Query query = getQuery(req, resp);
		if (query == null)
			return;
		
		Model model = namedGraphSet.asJenaModel("");
		String queryString = req.getParameter("query");

		
		VelocityContext context = new VelocityContext();
		
		if (queryString != null) {
			try {
				Query sparqlQuery = QueryFactory.create(queryString);
				
				QueryExecution qexec = null;
				try {
					qexec = QueryExecutionFactory.create(sparqlQuery, model);
					executeQuery(qexec, sparqlQuery.getQueryType(), context);
				} catch (QueryExecException e) {
					context.put("error", e.getMessage());
				} finally {
					if (qexec != null)
						qexec.close();
				}
				
			} catch (QueryParseException e) {
				context.put("error", e.getMessage());
			}

		} else if (!query.getFormat().equals("html")) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		context.put("query", queryString);
		
		templater.render(resp, "sparql.vm", context);
	}

	private static final long serialVersionUID = -8371160059122150837L;
	
	private void executeQuery(QueryExecution qexec, int queryType, VelocityContext context) {
		switch (queryType) {
		case Query.QueryTypeAsk:
			break;
		case Query.QueryTypeConstruct:
			executeConstructQuery(qexec, context);
			break;
		case Query.QueryTypeDescribe:
			executeDescribeQuery(qexec, context);
			break;
		case Query.QueryTypeSelect:
			executeSelectQuery(qexec, context);
			break;
		case Query.QueryTypeUnknown:
			break;
		}
	}
	
	private void executeSelectQuery(QueryExecution qexec, VelocityContext context) {
		ResultSet resultset = qexec.execSelect();
		List<List<Object>> results = new LinkedList<List<Object>>();;
		List<String> bindings = resultset.getResultVars();;
		
		while (resultset.hasNext()) {
			QuerySolution soln = resultset.next();
			List<Object> result = new LinkedList<Object>();
			for (String binding : bindings) {
				RDFNode node = soln.get(binding);
				if (node.isResource())
					result.add(VelocityResource.create((Resource) node, homeURIRegex));
				else
					result.add(((Literal) node).getValue());
			}
			results.add(result);
		}
		
		context.put("results", results);
		context.put("bindings", bindings);
	}
	
	private void executeDescribeQuery(QueryExecution qexec, VelocityContext context) {
		Model model = qexec.execDescribe();
		List<VelocityResource> resources = new LinkedList<VelocityResource>();
		
		ResIterator subjects = model.listSubjects();
		while (subjects.hasNext())
			resources.add(VelocityResource.create(subjects.next(), homeURIRegex));
		
		context.put("model", model);
		context.put("resources", resources);
	}
	
	private void executeConstructQuery(QueryExecution qexec, VelocityContext context) {
		Model model = qexec.execConstruct();
		List<VelocityResource> resources = new LinkedList<VelocityResource>();
		
		ResIterator subjects = model.listSubjects();
		while (subjects.hasNext())
			resources.add(VelocityResource.create(subjects.next(), homeURIRegex));
		
		context.put("model", model);
		context.put("resources", resources);
	}
	
	
	

}
