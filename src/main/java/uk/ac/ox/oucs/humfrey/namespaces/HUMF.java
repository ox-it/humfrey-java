package uk.ac.ox.oucs.humfrey.namespaces;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class HUMF {
	static Model ontModel = ModelFactory.createOntologyModel();
	static String ns = "http://vocab.ox.ac.uk/humfrey/";

	public static Resource User = ontModel.createResource(ns + "User");
	public static Property mayModify = ontModel.createProperty(ns + "mayModel");
}
