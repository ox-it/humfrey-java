package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Templater;
import uk.ac.ox.oucs.humfrey.VelocityResource;

import com.hp.hpl.jena.rdf.model.Model;

class HTMLSerializer extends AbstractSerializer {
	
	Templater templater;
	Map<String,AbstractSerializer> serializers;
	
	public HTMLSerializer(Templater templater, Map<String,AbstractSerializer> serializers) {
		this.templater = templater;
		this.serializers = serializers;
	}

	@Override
	public String getContentType() {
		return "text/html";
	}
	
	@Override
	public String getName() {
		return "HTML";
	}

	@Override
	public void serializeModel(Model model, Model fullModel, Query query,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		VelocityContext context = new VelocityContext();
		
		context.put("resource", new VelocityResource(fullModel.getResource(query.getURI()), model));
		context.put("query", query);
		context.put("serializers", serializers);
		context.put("model", fullModel);
		
		templater.render(resp.getWriter(), "doc.vm", context);
	}

}
