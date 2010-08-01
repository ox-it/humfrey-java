package uk.ac.ox.oucs.humfrey.serializers;

class TurtleSerializer extends JenaSerializer {
	public TurtleSerializer(Serializer serializer, String homeURIRegex) {
		super(serializer, homeURIRegex);
	}
	public String getSerialization() {
		return "TURTLE";
	}
	public String getContentType() {
		return "text/turtle";
	}
	
	@Override
	public String getName() {
		return "Turtle";
	}

	
}
