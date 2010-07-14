package uk.ac.ox.oucs.humfrey.resources;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class Tel extends VelocityResource {
	static String[] telTypes = {
		"v:Pref", "v:Voice", "v:ISDN", "v:Modem", "v:Msg", "v:Pager", "v:BBS",
		"v:Cell", "v:Video", "v:Fax", "v:Car", "v:PCS", "v:Home", "v:Work",
		"v:Dom"};
	static String[] telTypeNames = {
		"preferred", "voice", "ISDN", "modem", "voicemail", "pager",
		"bulletin board system", "mobile", "video", "fax", "car phone",
		"personal communications service", "home", "work", "domestic"};

	public Tel(Resource resource, String homeURIRegex, Model model) {
		super(resource, homeURIRegex, model);
	}
	
	@Override
	public String toString() {
		String types = "";
		boolean notFirst = false;
		Property rdfType = p(model, "rdf:type");
		for (int i = 0; i < telTypes.length; i++)
			if (resource.hasProperty(rdfType, r(model, telTypes[i]))) {
				if (notFirst)
					types += " ";
				else
					notFirst = true;
				types += telTypeNames[i];
			}
		if (types.length() > 0)
			types = " (" + types + ")";
		
		Statement stmt = resource.getProperty(p(model, "rdf:value"));
		RDFNode node = stmt.getObject();
		if (!node.isLiteral())
			return escapeTool.html("<unknown");
		String value = ((Literal) node).getValue().toString();
		return "<a href=\"tel:" + escapeTool.xml(value.replaceAll("[^+\\d]", "")) + "\">" + escapeTool.html(value + types) + "</a>";
	}

}
