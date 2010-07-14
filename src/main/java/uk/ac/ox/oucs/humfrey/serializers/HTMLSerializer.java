package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Templater;
import uk.ac.ox.oucs.humfrey.namespaces.DCAT;
import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

class HTMLSerializer extends AbstractSerializer {
	
	Templater templater;
	Map<String,AbstractSerializer> serializers;
	String homeURIRegex;
	String[][] groups = {
			{"Naming", "rdfs_label", "skos_prefLabel", "skos_altLabel", "dc_title"},
			{"Overview", "dct_publisher", "dct_issued", "dct_license", "foaf_homepage"},
			{"Location", "v_adr", "geo_lat", "geo_long"},
			{"Contact", "v_tel", "foaf_mbox", "foaf_phone"},
	};
	
	public HTMLSerializer(Templater templater, Map<String,AbstractSerializer> serializers, String homeURIRegex) {
		this.templater = templater;
		this.serializers = serializers;
		this.homeURIRegex = homeURIRegex;
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
		Resource resource = fullModel.getResource(query.getURI());
		
		context.put("resource", VelocityResource.create(resource, homeURIRegex, fullModel));
		context.put("query", query);
		context.put("serializers", serializers);
		context.put("model", fullModel);
		
		resp.setContentType(getContentType());
		
		if (resource.hasProperty(RDF.type, DCAT.Dataset))
			templater.render(resp.getWriter(), "dataset.vm", context);
		else
			templater.render(resp.getWriter(), "doc.vm", context);
	}

}
