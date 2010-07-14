package uk.ac.ox.oucs.humfrey.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

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
					executeQuery(qexec, sparqlQuery.getQueryType(), query, req, resp);
				} catch (QueryExecException e) {
					serializeSparqlError(e.getMessage(), query, req, resp);
				} finally {
					if (qexec != null)
						qexec.close();
				}
				
			} catch (QueryParseException e) {
				serializeSparqlError(e.getMessage(), query, req, resp);
			}

		} else {
			if (!query.getFormat().equals("html")) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			context.put("query", queryString);
			templater.render(resp, "sparql.vm", context);
		}
	}

	private static final long serialVersionUID = -8371160059122150837L;
	
	private void executeQuery(QueryExecution qexec, int queryType, uk.ac.ox.oucs.humfrey.Query query, HttpServletRequest req, HttpServletResponse resp) {
		switch (queryType) {
		case Query.QueryTypeAsk:
			break;
		case Query.QueryTypeConstruct:
			executeConstructQuery(query, qexec, req, resp);
			break;
		case Query.QueryTypeDescribe:
			executeDescribeQuery(query, qexec, req, resp);
			break;
		case Query.QueryTypeSelect:
			executeSelectQuery(query, qexec, req, resp);
			break;
		case Query.QueryTypeUnknown:
			break;
		}
	}
	
	private void executeSelectQuery(uk.ac.ox.oucs.humfrey.Query query, QueryExecution qexec, HttpServletRequest req, HttpServletResponse resp) {
		ResultSet resultset = qexec.execSelect();
		serializer.serializeResultSet(resultset, query, req, resp);
	}
	
	private void executeDescribeQuery(uk.ac.ox.oucs.humfrey.Query query, QueryExecution qexec, HttpServletRequest req, HttpServletResponse resp) {
		Model model = qexec.execDescribe();
		serializer.serializeResources(model, query, req, resp);
	}
	
	private void executeConstructQuery(uk.ac.ox.oucs.humfrey.Query query, QueryExecution qexec, HttpServletRequest req, HttpServletResponse resp) {
		Model model = qexec.execConstruct();
		serializer.serializeResources(model, query, req, resp);
	}
	
	private void serializeSparqlError(String message, uk.ac.ox.oucs.humfrey.Query query, HttpServletRequest req, HttpServletResponse resp) {
		serializer.serializeSparqlError(message, query, req, resp);
	}

}
