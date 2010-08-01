package uk.ac.ox.oucs.humfrey.serializers;

class Notation3Serializer extends JenaSerializer {
	public Notation3Serializer(Serializer serializer, String homeURIRegex) {
		super(serializer, homeURIRegex);
	}
	public String getSerialization() {
		return "N3";
	}
	public String getContentType() {
		return "text/n3";
	}
	@Override
	public String getName() {
		return "N3";
	}

	
}
