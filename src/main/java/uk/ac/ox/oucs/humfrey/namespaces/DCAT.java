package uk.ac.ox.oucs.humfrey.namespaces;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public final class DCAT {
	static Model ontModel = ModelFactory.createOntologyModel();
	static String ns = "http://vocab.deri.ie/dcat#";
	
	public static Resource Dataset = ontModel.createResource(ns + "Dataset");
	
}
