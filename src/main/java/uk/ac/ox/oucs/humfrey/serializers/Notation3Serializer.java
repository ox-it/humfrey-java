package uk.ac.ox.oucs.humfrey.serializers;

public class Notation3Serializer extends JenaSerializer {
	protected String getSerialization() {
		return "N3";
	}
	protected String getContentType() {
		return "text/n3";
	}
}
