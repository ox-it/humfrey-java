package uk.ac.ox.oucs.humfrey.servlets;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.servlets.ModelServlet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;

import de.fuberlin.wiwiss.ng4j.NamedGraph;

public class IndexServlet extends ModelServlet {
	private static final long serialVersionUID = 5028351085414583181L;
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		Model model = namedGraphSet.asJenaModel("");
		
		VelocityContext context = new VelocityContext();
		context.put("size", model.size());
		
		ResIterator datasetIterator = model.listSubjectsWithProperty(Namespaces.rdf.p(model, "type"), Namespaces.dcat.r(model, "Dataset"));
		int datasetCount = 0;
		while (datasetIterator.hasNext()) {
			datasetIterator.next();
			datasetCount += 1;
		}
		context.put("datasetCount", datasetCount);
		
		int graphCount = 0;
		Iterator<NamedGraph> graphIterator = namedGraphSet.listGraphs();
		while (graphIterator.hasNext()) {
			graphIterator.next();
			graphCount += 1;
		}
		context.put("graphCount", graphCount);
		
		templater.render(resp, "index.vm", context);
		resp.setContentType("text/html");
	}

}
