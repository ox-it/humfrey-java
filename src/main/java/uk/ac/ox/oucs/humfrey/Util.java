package uk.ac.ox.oucs.humfrey;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class Util {
	static public Set<Resource> getTypes(Resource resource) {
		Set<Resource> types = new HashSet<Resource>();
		
		StmtIterator stmts = resource.listProperties(RDF.type);
		while (stmts.hasNext()) {
			RDFNode node = stmts.next().getObject();
			if (node.isURIResource())
				types.add((Resource) node);
		}
		return types;
	}
}
