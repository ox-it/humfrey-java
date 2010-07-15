package uk.ac.ox.oucs.humfrey.serializers;

class RDFXMLSerializer extends JenaSerializer {
	public String getSerialization() {
		return "RDF/XML-ABBREV";
	}
	public String getContentType() {
		return "application/rdf+xml";
	}
	
	@Override
	public String getName() {
		return "RDF/XML";
	}

	
}
