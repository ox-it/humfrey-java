package uk.ac.ox.oucs.humfrey.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.FormatPreferences;
import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.servlets.ModelServlet;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;

public class IndexServlet extends ModelServlet {
	private static final long serialVersionUID = 5028351085414583181L;
	private static FormatPreferences formatPreferences = new FormatPreferences("html", "html", "html");
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		VelocityContext context = new VelocityContext();
		
		QueryExecution qexec = QueryExecutionFactory.create("SELECT (COUNT(?d) as ?count) WHERE { ?d a <"+Namespaces.dcat._("Dataset")+"> }", dataset);
		ResultSet results = qexec.execSelect();
		context.put("datasetCount", ((Literal) results.next().get("count")).getValue());
		
		context.put("graphCount", dataset.asDatasetGraph().size());
		
		templater.render(resp, "index.vm", context);
		resp.setContentType("text/html");
	}
	
	@Override
	public FormatPreferences getAcceptFormats() {
		return formatPreferences;
	}
	@Override
	public FormatPreferences getContentTypeFormats() {
		return FormatPreferences.noFormats;
	}
	

}
