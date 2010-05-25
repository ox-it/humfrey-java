package uk.ac.ox.oucs.humfrey;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class Namespaces {
	static Namespace aiiso = new Namespace("aiiso", "http://purl.org/vocab/aiiso/schema#");
	static Namespace cc = new Namespace("cc", "http://web.resource.org/cc/");
	static Namespace owl = new Namespace("owl", "http://www.w3.org/2002/07/owl#");
	static Namespace dc = new Namespace("dc", "http://purl.org/dc/elements/1.1/");
	static Namespace dct = new Namespace("dct", "http://purl.org/dc/terms/");
	static Namespace dctype = new Namespace("dctype", "http://purl.org/dc/dcmitype/");
	static Namespace event = new Namespace("event", "http://purl.org/NET/c4dm/event.owl#");
	static Namespace foaf = new Namespace("foaf", "http://xmlns.com/foaf/0.1/");
	static Namespace geo = new Namespace("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
	static Namespace sioc = new Namespace("sioc", "http://rdfs.org/sioc/ns#");
	static Namespace skos = new Namespace("skos", "http://www.w3.org/2004/02/skos/core#");
	static Namespace rdf = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	static Namespace rdfs = new Namespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	static Namespace vann = new Namespace("vann", "http://purl.org/vocab/vann/");
	static Namespace vs = new Namespace("vs", "http://www.w3.org/2003/06/sw-vocab-status/ns#");
	static Namespace xsd = new Namespace("xsd", "http://www.w3.org/2001/XMLSchema#");
	
	static Map<String,String> getPrefixMapping() {
		return Namespace.register;
	}
	
	public static class Namespace {
		String prefix, ns;
		static Map<String,String> register = new HashMap<String,String>();
		
		public Namespace(String prefix, String ns) {
			this.prefix = prefix;
			this.ns = ns;
			register.put(prefix, ns);
		}
		public Node _(String local) {
			return Node.createURI(ns + local);
		}
		public Property p(Model model, String local) {
			return model.createProperty(ns, local);
		}
		public Resource r(Model model, String local) {
			return model.createResource(ns+local);
		}
	}
}
