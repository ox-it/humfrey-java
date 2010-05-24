package uk.ac.ox.oucs.humfrey.serializers;

public class RDFXMLSerializer extends JenaSerializer {
	protected String getSerialization() {
		return "RDF/XML-ABBREV";
	}
	protected String getContentType() {
		return "application/rdf+xml";
	}
}
