package uk.ac.ox.oucs.humfrey.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.FormatPreferences;
import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.serializers.AbstractSerializer;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class DocServlet extends ModelServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 640907447304234785L;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		
		if (datasetContains(query.getURI())) {
			QueryExecution qexec = QueryExecutionFactory.create("DESCRIBE <"+query.getURI()+">");
			Model model = qexec.execDescribe();
			Resource resource = model.getResource(query.getURI());
			serializer.serializeResource(resource, query, req, resp);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	@Override
	protected FormatPreferences getAcceptFormats() {
		return new FormatPreferences("rdf", "html", serializer.getSerializers(AbstractSerializer.SerializationType.ST_RESOURCE));
	}

}
