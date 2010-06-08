/**
 * 
 */
package uk.ac.ox.oucs.humfrey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.oucs.humfrey.serializers.Serializer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class VelocityResource implements Comparable<VelocityResource> {
	protected static final String labelProperties[] = {"rdfs_label", "skos_prefLabel", "dc_title"};
	Resource resource;
	Model fullModel;
	Model model;
	
	public VelocityResource(Resource resource) {
		this(resource, resource.getModel());
	}
	
	public VelocityResource(Resource resource, Model model) {
		this.resource = resource;
		this.model = resource.getModel();
		this.fullModel = model;
	}
	
	public Object get(String key) {
		String parts[] = key.split("_", 2);
		String prefix = Namespaces.getURI(parts[0]), local = parts[1];
		Statement statement = resource.getProperty(fullModel.createProperty(prefix, local));
		if (statement == null)
			return null;
		RDFNode object = statement.getObject();
		if (object.isLiteral())
			return ((Literal) object).getValue();
		else
			return new VelocityResource((Resource) object, fullModel);
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
		return "<a href=\""+getURI()+"\">"+getLabel()+"</a>";
	}
	
	public String toString() {
		return getLink();
	}
	
	public Map<VelocityResource,Set<Object>> getPropertyMap() {
		Map<Property,Set<RDFNode>> oldPropertyMap = Serializer.getPropertyMap(resource);
		Map<VelocityResource,Set<Object>> newPropertyMap = new HashMap<VelocityResource,Set<Object>>();
		for (Property property : oldPropertyMap.keySet()) {
			Set<Object> propertySet = new HashSet<Object>();
			newPropertyMap.put(new VelocityResource(property, fullModel), propertySet);
			for (RDFNode node : oldPropertyMap.get(property)) {
				if (node.isLiteral())
					propertySet.add(node.toString());
				else
					propertySet.add(new VelocityResource((Resource) node, fullModel));
			}
		}
		return newPropertyMap;
	}

	@Override
	public int compareTo(VelocityResource other) {
		return this.getLabel().compareTo(other.getLabel());
	}
}