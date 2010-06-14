/**
 * 
 */
package uk.ac.ox.oucs.humfrey.resources;

import uk.ac.ox.oucs.humfrey.Namespaces;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

class Address extends VelocityResource {
	String[] addressProperties = {
		"v:street-address", "v:extended-address", "v:locality",
		"v:region", "v:postal-code", "v:country-name",
	};

	public Address(Resource resource, Model model) {
		super(resource, model);
	}

	@Override
	public String toString() {
		boolean notFirst = false;
		String out = "<div typeof=\"";
		StmtIterator rdfTypes = resource.listProperties(Namespaces.p(model, "rdf:type"));
		while (rdfTypes.hasNext()) {
			RDFNode rdfType = rdfTypes.next().getObject();
			if (rdfType.isURIResource()) {
				if (notFirst)
					out += " ";
				else
					notFirst = true;
				out += Namespaces.abbreviate((Resource) rdfType);

			}
		}
		out += "\">";
		notFirst = false;
		for (String addressProperty : addressProperties) {
			Statement stmt = resource.getProperty(Namespaces.p(model, addressProperty));
			if (stmt != null && stmt.getObject().isLiteral()) {
				if (notFirst)
					out += "<br/>";
				else
					notFirst = true;
				out += "<span rel=\"" + addressProperty + "\">";
				out += escapeTool.html(((Literal) stmt.getObject()).getValue().toString());
				out += "</span>";
			}
		}
		out += "</div>";
		return out;
	}

}