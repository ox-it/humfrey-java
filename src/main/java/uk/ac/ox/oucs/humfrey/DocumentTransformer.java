package uk.ac.ox.oucs.humfrey;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.nuiton.jrst.JRSTGenerator;
import org.nuiton.jrst.JRSTReader;
import org.nuiton.jrst.JRSTGenerator.DocumentResolver;

import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

public class DocumentTransformer {
	protected Map<String,AbstractTransformer> transformers;
	{
		transformers = new HashMap<String,AbstractTransformer>();
		transformers.put("text/prs.fallenstein.rst", new RSTTransformer());
	}
	
	public boolean isTransformable(VelocityResource resource) {
		return isTransformable(resource.getResource());
	}
	
	public boolean isTransformable(Resource resource) {
		Statement stmt = resource.getProperty(DCTerms.format);
		if (stmt == null)
			return false;
		RDFNode format = stmt.getObject();
		if (!format.isLiteral() || !transformers.containsKey(((Literal) format).getLexicalForm()))
			return false;
		return (resource.getProperty(RDF.value) != null);
		
	}
	
	public String transform(VelocityResource resource) {
		return transform(resource.getResource());
	}
	public String transform(Resource resource) {
		String format = resource.getProperty(DCTerms.format).getString();
		String value = resource.getProperty(RDF.value).getString();
		AbstractTransformer transformer = transformers.get(format);
		return transformer.transform(value);
	}
	
	interface AbstractTransformer {
		public String transform(String input);
	}
	
	class RSTTransformer implements AbstractTransformer {
		final URL stylesheet = org.nuiton.util.Resource.getURL("xsl/docbook2xhtml.xsl");
		
		public String transform(String input) {
			JRSTReader jrst = new JRSTReader();
			Document doc;
			StringWriter out = new StringWriter();
			try {
				doc = jrst.read(new StringReader(input));

				TransformerFactory factory = TransformerFactory.newInstance();
				factory.setURIResolver(new DocumentResolver(stylesheet));
				Transformer transformer = factory.newTransformer(new StreamSource(
						stylesheet.openStream()));
				DocumentSource source = new DocumentSource(doc);
				Result result = new StreamResult(out);
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.METHOD, "html");

				transformer.transform(source, result);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			

			return out.toString();
		}
	}
}
