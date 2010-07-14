package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
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
		resp.setContentType(getContentType());
		
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
	
	@Override
	public void serializeResultSet(ResultSet resultset, Query query, HttpServletRequest req, HttpServletResponse resp)  throws IOException {
		boolean notFirst = false;
		resp.setContentType(getContentType());
		List<String> bindings = resultset.getResultVars();
		
		Writer writer = resp.getWriter();

		if (asJavaScript && req.getParameter("callback") != null)
			writer.write(req.getParameter("callback")+"(");
		writer.write("{bindings: [");
		for (String binding : bindings) {
			if (notFirst)
				writer.write(", ");
			else
				notFirst = true;
			writer.write("\"" + StringEscapeUtils.escapeJavaScript(binding) + "\"");
		}
		writer.write("], results: [");
		while (resultset.hasNext()) {
			writer.write("{");
			QuerySolution soln = resultset.next();
			notFirst = false;
			for (String binding : bindings) {
				if (notFirst)
					writer.write(", ");
				else
					notFirst = true;
				writer.write("\"" + StringEscapeUtils.escapeXml(binding) + "\": {");
				
				RDFNode node = soln.get(binding);
				if (node.isLiteral()) {
					Literal literal = (Literal) node;
					writer.write("type: \"literal\", ");
					if (literal.getDatatypeURI() != null)
						writer.write("datatype: \"" + literal.getDatatypeURI() + "\", ");
					if (literal.getLanguage() != null)
						writer.write("datatype: \"" + StringEscapeUtils.escapeJavaScript(literal.getLanguage()) + "\", ");
					writer.write("value: \"" + StringEscapeUtils.escapeJavaScript(literal.getLexicalForm()) + "\"");
				} else if (node.isAnon()) {
					writer.write("type: \"bnode\", ");
					writer.write("label: \"" + StringEscapeUtils.escapeJavaScript(VelocityResource.create((Resource) node, null).getLabel()) + "\", ");
					writer.write("id: \"" + ((Resource) node).getId() + "\"");
				} else if (node.isURIResource()) {
					writer.write("type: \"uri\", ");
					writer.write("label: \"" + StringEscapeUtils.escapeJavaScript(VelocityResource.create((Resource) node, null).getLabel()) + "\", ");
					writer.write("uri: \"" + StringEscapeUtils.escapeJavaScript(((Resource) node).getURI()) + "\"");
				}
				writer.write("}");
			}
			writer.write("}");
			if (resultset.hasNext())
				writer.write(", ");
		}
		writer.write("}}");
		if (asJavaScript && req.getParameter("callback") != null)
			writer.write(");");
		writer.write("\n");
		
	}
	
	private String _(String s) {
		return "\"" + s + "\"";
	}

	private String _(Resource res) {
		return _(res.getURI());
	}
}
