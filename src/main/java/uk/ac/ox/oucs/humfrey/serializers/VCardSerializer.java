package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.Util;
import uk.ac.ox.oucs.humfrey.namespaces.V;
import uk.ac.ox.oucs.humfrey.resources.Tel;
import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;

public class VCardSerializer extends AbstractSerializer {

	@Override
	public String getContentType() {
		return "text/x-vcard";
	}

	@Override
	public String getName() {
		return "vCard";
	}
	
	@Override
	public void serializeModel(Model model, Model fullModel, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Resource resource = fullModel.getResource(query.getURI());
		StmtIterator stmts;
		VelocityResource vr = VelocityResource.create(resource, null);

		resp.setContentType(getContentType());
		//resp.setContentType("text/plain");
		Writer writer = resp.getWriter();
		
		writer.write("BEGIN:VCARD\n");
		writer.write("VERSION:3.0\n");
		encode(writer, "FN", vr.getLabel());
		encode(writer, "N", "Dutton;Alexander;Simon;Mr.;MComp.");
		
		stmts = resource.listProperties(V.tel);
		while (stmts.hasNext())
			encodeTel(writer, stmts.next().getObject());
		
		encodeEmail(writer, resource.getProperty(FOAF.mbox));
		encodeURI(writer, "PHOTO", resource.getProperty(FOAF.img));
		encodeURI(writer, "LOGO", resource.getProperty(FOAF.logo));
		
		writer.write("END:VCARD\n");
		
	}
	
	private void encode(Writer writer, String type, String value) throws IOException {
		value = type + ":" + value.replace("\n", "\\n");
		
		int offset = 0;
		while (offset < value.length()) {
			if (offset != 0)
				writer.write("\n ");
			if (offset + 78 > value.length())
				writer.write(value.substring(offset));
			else
				writer.write(value.substring(offset, offset + 78));
			offset += 78;
		}
		writer.write("\n");
	}
	
	private boolean encodeURI(Writer writer, String type, Statement stmt) throws IOException {
		Resource resource = getURIResource(stmt);
		if (resource == null)
			return false;
		encode(writer, type + ";VALUE=uri", resource.getURI());
		return true;
	}
	
	private boolean encodeEmail(Writer writer, Statement stmt) throws IOException {
		Resource resource = getURIResource(stmt);
		if (resource == null)
			return false;
		if (!resource.getURI().startsWith("mailto:"))
			return false;
		encode(writer, "EMAIL;TYPE=internet", resource.getURI().substring(7));
		return true;
	}
	
	private boolean encodeTel(Writer writer, RDFNode node) throws IOException {
		String value;
		if (!node.isResource())
			return false;
		Resource resource = (Resource) node;
		try {
			value = resource.getProperty(RDF.value).getString();
		} catch (Exception e) {
			return false;
		}
		Set<Resource> types = Util.getTypes(resource);
		Set<String> typeNames = new HashSet<String>();
		
		for (String type : Tel.telTypes) {
			Resource t = V.ontModel.createResource(V.ns + type.substring(2));
			if (types.contains(t))
				typeNames.add(type.substring(2).toLowerCase());
		}
		
		String type = list(typeNames);
		if (type.length() > 0)
			type = ";TYPE=" + type;
		
		encode(writer, "TEL" + type, value);
		return true;
	}
	
	public Resource getURIResource(Statement stmt) {
		try{
			Resource resource = stmt.getResource();
			if (resource.isURIResource())
				return resource;
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	private String list(Iterable<String> strings) {
		return list(strings, ";");
	}
	private String list(Iterable<String> strings, String sep) {
		String o = "";
		int i = 0;
		for (String string : strings) {
			if (i > 0)
				o += sep;
			o += string.replace(sep, "\\" + sep);
			i += 1;
		}
		return o;
	}
	private String map(Map<String,String> s) {
		String o = "";
		int i = 0;
		for (Entry<String,String> e : s.entrySet()) {
			if (i > 0)
				o += ";";
			o += e.getKey().replace(";", "\\;") + ";" + e.getValue().replace(";", "\\;");
			i += 1;
		}
		return o;
	}
	private String listmap(Map<String,Iterable<String>> s) {
		Map<String,String> o = new HashMap<String,String>();
		for (Entry<String,Iterable<String>> e : s.entrySet())
			o.put(e.getKey(), list(e.getValue(), ","));
		return map(o);
	}
	private String listlist(Iterable<Iterable<String>> s) {
		List<String> o = new LinkedList<String>();
		for (Iterable<String> i : s)
			o.add(list(i, ","));
		return list(o, ";");
	}

	public boolean canSerializeResource(Resource resource, Set<Resource> types) {
		VelocityResource vr = VelocityResource.create(resource, null);
		return !vr.getLabel().equals("<unnamed>");
	}
}
