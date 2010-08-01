package uk.ac.ox.oucs.humfrey.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.FormatPreferences;
import uk.ac.ox.oucs.humfrey.serializers.AbstractSerializer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

public class SparqlServlet extends ModelServlet {
	private enum SerializationType {
		ST_NOQUERY,
		ST_EXCEPTION,
		ST_RESULTSET,
		ST_RESOURCE,
		ST_RESOURCELIST,
		ST_BOOLEAN,
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Query sparqlQuery;
		FormatPreferences acceptFormats;

		String queryString = req.getParameter("query");
		
		if (queryString == null) {
			acceptFormats = new FormatPreferences("html", "html", "html");
			serialize(req, resp, acceptFormats, SerializationType.ST_NOQUERY, null);
			return;
		} else {
			try {
				sparqlQuery = QueryFactory.create(queryString, Syntax.syntaxARQ);
				acceptFormats = getAcceptableFormats(sparqlQuery.getQueryType());
				serialize(req, resp, acceptFormats, getSerializationType(sparqlQuery.getQueryType()), sparqlQuery);
				return;
			} catch (QueryParseException e) {
				acceptFormats = new FormatPreferences("txt", "html", serializer.getSerializers(AbstractSerializer.SerializationType.ST_EXCEPTION));
				serialize(req, resp, acceptFormats, SerializationType.ST_EXCEPTION, e);
				return;
			}
		}
	}
	
	private void serialize(HttpServletRequest req, HttpServletResponse resp, FormatPreferences acceptFormats, SerializationType serializationType, Object arg) {
		uk.ac.ox.oucs.humfrey.Query query = getQuery(req, resp, acceptFormats, FormatPreferences.noFormats);
		if (query == null)
			return;
		
		switch (serializationType) {
		case ST_NOQUERY:
			templater.render(resp, "sparql.vm", new VelocityContext());
			return;
		case ST_EXCEPTION:
			serializer.serializeSparqlError(((Exception) arg).getMessage(), query, req, resp);
			return;
		case ST_RESULTSET:
		case ST_RESOURCELIST:
		case ST_BOOLEAN:
			QueryExecution qexec = null;
			Model model = namedGraphSet.asJenaModel("");
			Query sparqlQuery = (Query) arg;
			try {
				qexec = QueryExecutionFactory.create(sparqlQuery, model);
				executeQuery(qexec, sparqlQuery.getQueryType(), query, req, resp);
			} catch (QueryExecException e) {
				acceptFormats = new FormatPreferences("txt", "html", serializer.getSerializers(AbstractSerializer.SerializationType.ST_EXCEPTION));
				serialize(req, resp, acceptFormats, SerializationType.ST_EXCEPTION, e);
			} finally {
				if (qexec != null)
					qexec.close();
			}
			
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
		serializer.serializeResourceList(model, query, req, resp);
	}
	
	private void executeConstructQuery(uk.ac.ox.oucs.humfrey.Query query, QueryExecution qexec, HttpServletRequest req, HttpServletResponse resp) {
		Model model = qexec.execConstruct();
		serializer.serializeResourceList(model, query, req, resp);
	}

	private FormatPreferences getAcceptableFormats(int queryType) {
		switch (queryType) {
		case Query.QueryTypeAsk:
			return new FormatPreferences("srx", "html", serializer.getSerializers(AbstractSerializer.SerializationType.ST_BOOLEAN));
		case Query.QueryTypeConstruct:
		case Query.QueryTypeDescribe:
			return new FormatPreferences("rdf", "html", serializer.getSerializers(AbstractSerializer.SerializationType.ST_RESOURCELIST));
		case Query.QueryTypeSelect:
			return new FormatPreferences("srx", "html", serializer.getSerializers(AbstractSerializer.SerializationType.ST_RESULTSET));
		default:
			throw new RuntimeException("Unexpected query type: " + queryType);
		}
	}
	
	private SerializationType getSerializationType(int queryType) {
		switch (queryType) {
		case Query.QueryTypeAsk:
			return SerializationType.ST_BOOLEAN;
		case Query.QueryTypeConstruct:
		case Query.QueryTypeDescribe:
			return SerializationType.ST_RESOURCELIST;
		case Query.QueryTypeSelect:
			return SerializationType.ST_RESULTSET;
		default:
			throw new RuntimeException("Unexpected query type: " + queryType);
		}
		
	}
}
