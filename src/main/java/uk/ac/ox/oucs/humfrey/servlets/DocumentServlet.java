package uk.ac.ox.oucs.humfrey.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.DocumentTransformer;
import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class DocumentServlet extends DocServlet {

	private String documentationGraphName;
	private static final long serialVersionUID = 1024447964397369108L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		
		DocumentTransformer documentTransformer = new DocumentTransformer();
		
		Model model = ModelFactory.createModelForGraph(namedGraphSet.getGraph(documentationGraphName));
		Resource resource = model.createResource(query.getURI());
		if (!query.getAccept().equals("html") || !model.containsResource(resource) || !documentTransformer.isTransformable(resource)) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		VelocityContext context = new VelocityContext();
		
		context.put("resource", VelocityResource.create(resource, homeURIRegex));
		resp.setContentType("text/html");
		templater.render(resp, "document.vm", context);

	}

	@Override
	public void init() throws ServletException {
		super.init();
		
		ServletContext servletContext = getServletContext();
		documentationGraphName = servletContext.getInitParameter("humfrey.documentationGraphName");
	}
}
