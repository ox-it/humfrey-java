package uk.ac.ox.oucs.humfrey.servlets;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;

public class DatasetServlet extends ModelServlet {
	private static final long serialVersionUID = 7796801302636827658L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		QueryExecution qexec = QueryExecutionFactory.create("DESCRIBE ?d WHERE { ?d a <"+Namespaces.dcat._("Dataset")+"> }");
		Model model = qexec.execDescribe();

		List<VelocityResource> datasets = new LinkedList<VelocityResource>();
		ResIterator datasetIterator = model.listSubjectsWithProperty(Namespaces.rdf.p("type"), Namespaces.dcat.r("Dataset"));
		while (datasetIterator.hasNext())
			datasets.add(VelocityResource.create(datasetIterator.next(), homeURIRegex));
		Collections.sort(datasets);

		VelocityContext context = new VelocityContext();
		context.put("datasets", datasets);
		
		templater.render(resp, "datasets.vm", context);
	}
}
