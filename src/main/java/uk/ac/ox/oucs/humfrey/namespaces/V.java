package uk.ac.ox.oucs.humfrey.namespaces;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

public class V {
	public static Model ontModel = ModelFactory.createOntologyModel();
	public static String ns = "http://www.w3.org/2006/vcard/ns#";

	public static Property tel = ontModel.createProperty(ns + "tel");
}
