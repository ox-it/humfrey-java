package uk.ac.ox.oucs.humfrey.serializers;

public class TurtleSerializer extends JenaSerializer {
	protected String getSerialization() {
		return "TURTLE";
	}
	protected String getContentType() {
		return "text/turtle";
	}
}
