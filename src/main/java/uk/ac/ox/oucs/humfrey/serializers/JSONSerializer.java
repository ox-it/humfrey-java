package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

class JSONSerializer extends AbstractSerializer {
	boolean asJavaScript;
	
	public JSONSerializer(boolean asJavaScript) {
		this.asJavaScript = asJavaScript;
	}

	@Override
	public String getContentType() {
		if (asJavaScript)
			return "text/javascript";
		else
			return "application/json";
	}

	@Override
	public String getName() {
		if (asJavaScript)
			return "JavaScript";
		else
			return "JSON";
	}

	@Override
	public void serializeModel(Model model, Model fullModel, Query query,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		Writer writer = resp.getWriter();
		
		if (asJavaScript && req.getParameter("callback") != null)
			writer.write(req.getParameter("callback")+"(");
		
		writer.write("{");
		
		ResIterator subjects = model.listSubjects();
		while (subjects.hasNext()) {
			Resource subject = subjects.next();
			if (subject.isURIResource())
				serializeResource(writer, subject);
		}
		
		writer.write("}");
		
		if (req.getParameter("callback") != null)
			writer.write(");");
		
		writer.write("\n");
	}
	
	private void serializeResource(Writer writer, Resource resource) throws IOException {
		writer.write(_(resource) + ": {");
		
		Map<Property,Set<RDFNode>> propertyMap = Serializer.getPropertyMap(resource);
		
		boolean notFirstA = false;
		for (Property predicate : propertyMap.keySet()) {
			if (notFirstA)
				writer.write(", ");
			else
				notFirstA = true;
			writer.write(_(predicate) + ": [");
			boolean notFirstB = false;
			for (RDFNode node : propertyMap.get(predicate)) {
				if (notFirstB)
					writer.write(", ");
				else
					notFirstB = true;
				if (node.isAnon())
					serializeResource(writer, (Resource) node);
				else
					writer.write(_(node.toString()));
			}
			writer.write("]");
		}
		
		writer.write("}");
	}
	
	private String _(String s) {
		return "\"" + s + "\"";
	}

	private String _(Resource res) {
		return _(res.getURI());
	}
}
