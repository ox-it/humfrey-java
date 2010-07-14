package uk.ac.ox.oucs.humfrey.namespaces;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class CS {
	static Model ontModel = ModelFactory.createOntologyModel();
	static String ns = "http://purl.org/vocab/changeset/schema#";

	public static Resource ChangeSet = ontModel.createResource(ns + "ChangeSet");
	public static Property subjectOfChange = ontModel.createProperty(ns + "subjectOfChange");
}
