/**
 * 
 */
package uk.ac.ox.oucs.humfrey.resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.velocity.tools.generic.EscapeTool;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.serializers.Serializer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class VelocityResource implements Comparable<VelocityResource> {
	protected static final String labelProperties[] = {"rdfs_label", "skos_prefLabel", "dct_title", "dc_title", "rdf_value"};
	protected static final Map<String,Class<? extends VelocityResource>> classMap = getClassMap();
	protected static EscapeTool escapeTool = new EscapeTool(); 
	Resource resource;
	Model fullModel;
	Model model;
	
	public static VelocityResource create(Resource resource) {
		return create(resource, resource.getModel());
	}
	
	public static VelocityResource create(Resource resource, Model model) {
		StmtIterator rdfTypes = resource.listProperties(p(model, "rdf:type"));
		while (rdfTypes.hasNext()) {
			RDFNode node = rdfTypes.next().getObject();
			if (!node.isURIResource())
				continue;
			String abbreviated = Namespaces.abbreviate((Resource) node);
			if (classMap.containsKey(abbreviated))
				try {
					return classMap.get(abbreviated).getConstructor(Resource.class, Model.class).newInstance(resource, model);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
		}
		return new VelocityResource(resource, model);
	}
	
	protected VelocityResource(Resource resource, Model model) {
		this.resource = resource;
		this.model = resource.getModel();
		this.fullModel = model;
	}
	
	protected static Property p(Model model, String s) {
		return Namespaces.p(model, s);
	}
	protected static Resource r(Model model, String s) {
		return Namespaces.r(model, s);
	}
	
	public Object get(String key) {
		Iterator<Object> resources = getAll(key);
		if (resources.hasNext())
			return resources.next();
		else
			return null;
	}
	public boolean has(String key) {
		return resource.hasProperty(Namespaces.p(fullModel, key));
	}
	
	public Iterator<Object> getAll(String key) {
		final StmtIterator statements = resource.listProperties(Namespaces.p(fullModel, key));
		return new Iterator<Object>() {
			@Override
			public boolean hasNext() {
				return statements.hasNext();
			}

			@Override
			public Object next() {
				RDFNode object = statements.next().getObject();
				if (object.isLiteral() && ((Literal) object).getDatatypeURI() == null)
					return ((Literal) object).getString();
				else if (object.isLiteral())
					return ((Literal) object).getValue();
				else
					return VelocityResource.create((Resource) object, fullModel);
			}

			@Override
			public void remove() {
				throw new NotImplementedException();
			}
			
		};
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
		if (resource.isURIResource()) {
			String link = "<a href=\""+escapeTool.xml(getURI())+"\">"+escapeTool.html(getLabel())+"</a>";
			if (isForeign() && fullModel.listStatements(resource, (Property) null, (RDFNode) null).hasNext())
				link += " <a href=\"/doc/?uri=" + escapeTool.url(getURI()) + "\">&#8962;</a>";
			return link;
		} else
			return escapeTool.html(getLabel());
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
	
	private static final Map<String,Class<? extends VelocityResource>> getClassMap() {
		Map<String,Class<? extends VelocityResource>> map = new HashMap<String,Class<? extends VelocityResource>>();
		map.put("v:Address", Address.class);
		
		String[] telTypes = "BBS,Car,Cell,Fax,ISDN,Modem,Msg,PCS,Tel,Video,Voice".split(",");
		for (String telType : telTypes)
			map.put("v:"+telType, Tel.class);
		return map;
	}
}