package uk.ac.ox.oucs.humfrey.serializers;

public class NTripleSerializer extends JenaSerializer {
	protected String getSerialization() {
		return "N-TRIPLE";
	}
	public String getContentType() {
		return "text/plain";
	}
}
