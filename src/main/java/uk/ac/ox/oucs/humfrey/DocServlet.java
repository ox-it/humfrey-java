package uk.ac.ox.oucs.humfrey;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.serializers.Serializer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class DocServlet extends ModelServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 640907447304234785L;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Model model = getModel();
		URL url = query.getURL();
		
		if (containsQuerySubject(model, query)) {
			Model resourceModel = buildModelForResource(model.createResource(url.toString()));
			addFormatInformation(resourceModel, query);
			serializeModel(resourceModel, query, req, resp);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	private Model buildModelForResource(Resource resource) {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefixes(getPrefixMapping());
		buildModelForResource(model, new HashSet<RDFNode>(), resource);
		return model;
	}
	
	public void buildModelForResource(Model model, Set<RDFNode> seen, Resource resource) {
		seen.add(resource);
		StmtIterator stmts = resource.listProperties();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			model.add(stmt);
			if (stmt.getObject() instanceof Resource && !seen.contains(stmt.getObject()))
				buildModelForResource(model, seen, resource);
		}
	}
	
	private void addFormatInformation(Model model, Query query) {
		Property dct_isFormatOf = Namespaces.dct.p(model, "isFormatOf");
		Property dct_format = Namespaces.dct.p(model, "format");
		Property rdf_type = Namespaces.rdf.p(model, "type");
		Property foaf_primaryTopic = Namespaces.foaf.p(model, "primaryTopic");
		RDFNode docURI = model.createResource(query.getDocRoot().toString());
		
		for (Entry<String, Serializer> entry : serializers.entrySet()) {
			Resource docFormatURI = model.createResource(query.getDocRoot()+"."+entry.getKey());
			docFormatURI
				.addProperty(dct_isFormatOf, docURI)
				.addProperty(rdf_type, Namespaces.dctype.r(model, "Text"))
				.addProperty(rdf_type, Namespaces.foaf.r(model, "Document"))
				.addProperty(foaf_primaryTopic, model.createResource(query.getURI().toString()))
				.addProperty(dct_format, entry.getValue().getContentType());
		}
	}

}