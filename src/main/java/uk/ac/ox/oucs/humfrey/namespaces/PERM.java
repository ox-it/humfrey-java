package uk.ac.ox.oucs.humfrey.namespaces;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class PERM {
	static Model ontModel = ModelFactory.createOntologyModel();
	static String ns = "http://vocab.ox.ac.uk/perm#";

	public static Resource Public = ontModel.createResource(ns + "Public");
	public static Resource ResourceMatch = ontModel.createResource(ns + "ResourceMatch");
	public static Property matchExpression = ontModel.createProperty(ns + "matchExpression");
	
	public static Property password = ontModel.createProperty(ns + "password");
	
	public static Property mayAdminister = ontModel.createProperty(ns + "mayAdminister");
	public static Property mayAugment = ontModel.createProperty(ns + "mayAugment");
	public static Property mayCreateChildrenOf = ontModel.createProperty(ns + "mayCreateChildrenOf");
	public static Property mayRead = ontModel.createProperty(ns + "mayRead");
	public static Property mayUpdate = ontModel.createProperty(ns + "mayUpdate");
	public static Property mayDelete = ontModel.createProperty(ns + "mayDelete");
}
