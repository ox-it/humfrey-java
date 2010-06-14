/**
 * 
 */
package uk.ac.ox.oucs.humfrey.resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.tools.generic.EscapeTool;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.serializers.Serializer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class VelocityResource implements Comparable<VelocityResource> {
	protected static final String labelProperties[] = {"rdfs_label", "skos_prefLabel", "dc_title"};
	protected static EscapeTool escapeTool = new EscapeTool(); 
	Resource resource;
	Model fullModel;
	Model model;
	
	public static VelocityResource create(Resource resource) {
		return create(resource, resource.getModel());
	}
	
	public static VelocityResource create(Resource resource, Model model) {
		Property rdfType = p(model, "rdf:type");
		
		if (resource.hasProperty(rdfType, r(model, "v:Address")))
			return new Address(resource, model);
		else
			return new VelocityResource(resource, model);
	}
	
	protected VelocityResource(Resource resource, Model model) {
		this.resource = resource;
		this.model = resource.getModel();
		this.fullModel = model;
	}
	
	private static Property p(Model model, String s) {
		return Namespaces.p(model, s);
	}
	private static Resource r(Model model, String s) {
		return Namespaces.r(model, s);
	}
	
	public Object get(String key) {
		Statement statement = resource.getProperty(Namespaces.p(fullModel, key));
		if (statement == null)
			return null;
		RDFNode object = statement.getObject();
		if (object.isLiteral() && ((Literal) object).getDatatypeURI() == null)
			return ((Literal) object).getString();
		else if (object.isLiteral())
			return ((Literal) object).getValue();
		else
			return VelocityResource.create((Resource) object, fullModel);
	}
	
	public String getURI() {
		if (resource.isURIResource())
			return resource.getURI();
		else
			return null;
	}
	
	public String getLabel() {
		for (String labelProperty : labelProperties) {
			Object label = get(labelProperty);
			if (label != null && label instanceof String)
				return (String) label;
		}
		if (resource.isURIResource()) {
			String prefix = model.getNsURIPrefix(resource.getNameSpace());
			if (prefix == null)
				prefix = Namespaces.getPrefix(resource.getNameSpace());
			if (prefix != null)
				return prefix + ":" + resource.getLocalName();
			else
				return resource.getURI();
		}
		else
			return "<unnamed>";
	}
	
	public String getLink() {
		String link = "<a href=\""+escapeTool.xml(getURI())+"\">"+escapeTool.html(getLabel())+"</a>";
		if (isForeign() && fullModel.listStatements(resource, (Property) null, (RDFNode) null).hasNext())
			link += " <a href=\"/doc/?uri=" + escapeTool.url(getURI()) + "\">&#8962;</a>";
		return link;
	}
	
	public String toString() {
		return getLink();
	}
	
	public boolean isForeign() {
		return resource.isURIResource() && !resource.getURI().matches("^http://([a-z\\-]+\\.)?data.ox.ac.uk/id/");
	}
	
	public Map<VelocityResource,Set<Object>> getPropertyMap() {
		Map<Property,Set<RDFNode>> oldPropertyMap = Serializer.getPropertyMap(resource);
		Map<VelocityResource,Set<Object>> newPropertyMap = new HashMap<VelocityResource,Set<Object>>();
		for (Property property : oldPropertyMap.keySet()) {
			Set<Object> propertySet = new HashSet<Object>();
			newPropertyMap.put(VelocityResource.create(property, fullModel), propertySet);
			for (RDFNode node : oldPropertyMap.get(property)) {
				if (node.isLiteral())
					propertySet.add(node);
				else
					propertySet.add(VelocityResource.create((Resource) node, fullModel));
			}
		}
		return newPropertyMap;
	}

	@Override
	public int compareTo(VelocityResource other) {
		return this.getLabel().compareTo(other.getLabel());
	}
	
	public String getHTML() {
		return getLink();
	}
}