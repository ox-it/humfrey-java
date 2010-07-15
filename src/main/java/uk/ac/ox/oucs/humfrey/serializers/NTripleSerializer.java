package uk.ac.ox.oucs.humfrey.serializers;

class NTripleSerializer extends JenaSerializer {
	public String getSerialization() {
		return "N-TRIPLE";
	}
	public String getContentType() {
		return "text/plain";
	}
	
	@Override
	public String getName() {
		return "N-Triples";
	}

	
}
