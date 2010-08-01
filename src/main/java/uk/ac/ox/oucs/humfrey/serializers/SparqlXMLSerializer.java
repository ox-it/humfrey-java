package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Implements the SPARQL results XML syntax described at
 * <http://www.w3.org/TR/rdf-sparql-XMLres/>.
 * 
 * @author Alexander Dutton <alexander.dutton@oucs.ox.ac.uk>
 *
 */
public class SparqlXMLSerializer extends AbstractSerializer {

	@Override
	public String getContentType() {
		return "application/sparql-results+xml";
	}

	@Override
	public String getName() {
		return "SPARQL results XML";
	}

	@Override
	public void serializeResultSet(ResultSet resultset, Query query,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		List<String> bindings = resultset.getResultVars();
		
		resp.setContentType(getContentType());
		Writer writer = resp.getWriter();
		writer.write("<?xml version=\"1.0\"?>\n");
		writer.write("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n");
		writer.write("  <head>\n");
		for (String binding : bindings)
			writer.write("    <variable name=\"" + StringEscapeUtils.escapeXml(binding) + "\"/>\n");
		writer.write("  </head>\n");
		writer.write("  <results>\n");
		while (resultset.hasNext()) {
			writer.write("    <result>\n");
			QuerySolution soln = resultset.next();
			for (String binding : bindings) {
				writer.write("      <binding name=\"" + StringEscapeUtils.escapeXml(binding) + "\">\n");
				
				RDFNode node = soln.get(binding);
				if (node.isLiteral()) {
					Literal literal = (Literal) node;
					writer.write("        <literal");
					if (literal.getDatatypeURI() != null)
						writer.write(" datatype=\"" + literal.getDatatypeURI() + "\"");
					if (literal.getLanguage() != null)
						writer.write(" xml:lang=\"" + StringEscapeUtils.escapeXml(literal.getLanguage()) + "\"");
					writer.write(">" + StringEscapeUtils.escapeXml(literal.getLexicalForm()) + "</literal>\n");
				} else if (node.isAnon()) {
					writer.write("        <bnode>" + ((Resource) node).getId() + "</bnode>\n");
				} else if (node.isURIResource()) {
					writer.write("        <uri>" + StringEscapeUtils.escapeXml(((Resource) node).getURI()) + "</uri>\n");
				}
				writer.write("      </binding>\n");
			}
			writer.write("    </result>\n");
		}
		writer.write("  </results>\n");
		writer.write("</sparql>\n");
	}
	
	@Override
	public boolean canSerialize(SerializationType serializationType) {
		switch (serializationType) {
		case ST_RESULTSET:
			return true;
		default:
			return false;
		}
	}

}
