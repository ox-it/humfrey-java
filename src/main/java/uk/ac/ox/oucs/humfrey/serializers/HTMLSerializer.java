package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Templater;
import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.rdf.model.Model;

class HTMLSerializer extends AbstractSerializer {
	
	Templater templater;
	Map<String,AbstractSerializer> serializers;
	String[][] groups = {
			{"Naming", "rdfs_label", "skos_prefLabel", "skos_altLabel", "dc_title"},
			{"Overview", "dct_publisher", "dct_issued", "dct_license", "foaf_homepage"},
			{"Location", "v_adr", "geo_lat", "geo_long"},
			{"Contact", "v_tel", "foaf_mbox", "foaf_phone"},
	};
	
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
		
		context.put("resource", VelocityResource.create(fullModel.getResource(query.getURI()), fullModel));
		context.put("query", query);
		context.put("serializers", serializers);
		context.put("model", fullModel);
		
		templater.render(resp.getWriter(), "doc.vm", context);
	}

}
