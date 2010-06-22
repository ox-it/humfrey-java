package uk.ac.ox.oucs.humfrey.namespaces;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class VOID {
	static Model ontModel = ModelFactory.createOntologyModel();
	static String ns = "http://rdfs.org/ns/void#";

	public static Resource Dataset = ontModel.createResource(ns + "date");
}
