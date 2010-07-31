package uk.ac.ox.oucs.humfrey.servlets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.namespaces.CS;
import uk.ac.ox.oucs.humfrey.namespaces.PERM;
import uk.ac.ox.oucs.humfrey.serializers.AbstractSerializer;
import uk.ac.ox.oucs.humfrey.serializers.JenaSerializer;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;

public class GraphServlet extends ModelServlet {
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
	File logDirectory;

	private static final long serialVersionUID = -8868243256357826456L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Node uri = query.getNode();
		
		if (!namedGraphSet.containsGraph(uri)) {
			resp.setStatus(404);
			return;
		}
		
		Graph graph = namedGraphSet.getGraph(uri);
		serializeGraph(graph, query, req, resp);
		resp.setStatus(200);
		
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate};
		updateGraph(req, resp, permissions, new GraphUpdater() {
			public void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node) {
				NamedGraph namedGraph = namedGraphSet.createGraph(node);
				namedGraph.getBulkUpdateHandler().add(graph);
			}
		});
	}
	
	protected void doUnion(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate, PERM.mayAugment};
		updateGraph(req, resp, permissions, new GraphUpdater(){
			public void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node) {
				NamedGraph namedGraph = namedGraphSet.getGraph(node);
				namedGraph.getBulkUpdateHandler().add(graph);
			}
		});
	}

	protected void doIntersection(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate};
		updateGraph(req, resp, permissions, new GraphUpdater(){
			public void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node) {
				NamedGraph namedGraph = namedGraphSet.getGraph(node);
				ExtendedIterator<Triple> triples = namedGraph.find(null, null, null);
				List<Triple> triplesToRemove = new LinkedList<Triple>();
				while (triples.hasNext()) {
					Triple triple = triples.next();
					if (!graph.contains(triple))
						triplesToRemove.add(triple);
				}
				namedGraph.getBulkUpdateHandler().delete(triplesToRemove);
			}
		});
	}
	
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate, PERM.mayDelete};
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		if (performPermissionsCheck(getServletContext(), query, resp, permissions))
			return;
		Node node = query.getNode();
		if (namedGraphSet.containsGraph(node)) {
			namedGraphSet.removeGraph(node);
			resp.setStatus(200);
		} else
			resp.setStatus(404);
		
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getMethod().equals("UNION"))
			doUnion(req, resp);
		else if (req.getMethod().equals("INTERSECTION"))
			doIntersection(req, resp);
		else
			super.service(req, resp);
	}
	
	private void updateGraph(HttpServletRequest req, HttpServletResponse resp, Property[] permissions,
			GraphUpdater graphUpdater) throws ServletException, IOException {
		Query query = getQuery(req, resp);
		ServletContext context = getServletContext();
		if (query == null)
			return;
		
		if (performPermissionsCheck(context, query, resp, permissions))
			return;
		
		Node node = query.getNode();
		
		AbstractSerializer as = serializer.get(query.getAccept());
		if (!(as instanceof JenaSerializer)) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		Model model = ModelFactory.createDefaultModel();
		model.read(req.getInputStream(), query.getURI(), ((JenaSerializer) as).getSerialization());
		
		Set<Triple> before;
		if (namedGraphSet.containsGraph(node)) {
			Graph graph = namedGraphSet.getGraph(node);
			before = graph.find(null, null, null).toSet();
		} else
			before = new HashSet<Triple>();
		
		graphUpdater.updateGraph(namedGraphSet, model.getGraph(), node);
		
		Set<Triple> after = namedGraphSet.getGraph(node).find(null, null, null).toSet();
		
		Set<Triple> intersection = new HashSet<Triple>(before);
		intersection.retainAll(after);
		
		before.removeAll(intersection); // All those removed
		after.removeAll(intersection); // All those added

		Model changesetModel = ModelFactory.createDefaultModel();
		
		Resource changeset = changesetModel.createResource();
		Literal date = changesetModel.createTypedLiteral(
				dateFormat.format(new Date()), new BaseDatatype(XSD.date.getURI()));
		
		changeset.addProperty(Namespaces.rdf.p("type"),
							  CS.ChangeSet)
				 .addProperty(Namespaces.dc.p("date"),
						 	  date)
				 .addProperty(CS.subjectOfChange,
						 	  changesetModel.createResource(query.getURI()))
				 .addProperty(DC.creator,
						      query.getUser());
		
		for (Triple triple : before)
			changeset.addProperty(Namespaces.cs.p("removal"),
								  changesetModel.asStatement(triple).createReifiedStatement());
		for (Triple triple : after)
			changeset.addProperty(Namespaces.cs.p("addition"),
								  changesetModel.asStatement(triple).createReifiedStatement());

		
		String filename = query.getURL().getPath() + "/" + date.getLexicalForm() + ".n3";
		File file = new File(logDirectory, filename);
		file.getParentFile().mkdirs();
		FileWriter ow = new FileWriter(new File(logDirectory.getPath(), filename));
		changesetModel.write(ow, "N3");
		ow.close();

		resp.setStatus(200);
	}
	
	static private boolean performPermissionsCheck(ServletContext context, Query query, HttpServletResponse resp, Property[] permissions) {
		if (!hasPermission(context.getInitParameter("humfrey.accountPrefix"), query.getUsername(), query.getURI(), permissions)) {
			if (query.isAuthenticated()) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return true;
			} else {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				resp.addHeader("WWW-Authenticate", context.getInitParameter("humfrey.wwwAuthenticateHeader"));
				return true;
			}
		}
		return false;		
	}
	
	static private boolean hasPermission(String userPrefix, String username, String uri, Property[] permissions) {
		Set<Property> permissionSet = new HashSet<Property>();
		Resource resource = configModel.createResource(uri);
		Resource user;
		
		if (username == null)
			user = PERM.Public;
		else
			user = configModel.createResource(userPrefix + username);
		
		for (Property permission : permissions) {
			if (configModel.contains(user, permission, resource))
				return true;
			permissionSet.add(permission);
		}
		
		StmtIterator stmts = user.listProperties();
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			if (!permissionSet.contains(stmt.getPredicate()))
				continue;
			RDFNode resourceMatch = stmt.getObject();
			if (!(resourceMatch.isResource() && configModel.contains((Resource) resourceMatch, RDF.type, PERM.ResourceMatch)))
				continue;
			
			RDFNode matchRegex = ((Resource) resourceMatch).getProperty(PERM.matchExpression).getObject();
			if (matchRegex == null || !matchRegex.isLiteral())
				continue;
			
			try {
				if (uri.matches(((Literal) matchRegex).getString()))
					return true;
			} catch (PatternSyntaxException e) {}
		}
		
		if (username != null)
			return hasPermission(userPrefix, null, uri, permissions);
		else
			return false;
	}
	
	interface GraphUpdater {
		void updateGraph(NamedGraphSet namedGraphSet, Graph graph, Node node);
	}

	@Override
	public void init() throws ServletException {
		super.init();
		
		ServletContext context = getServletContext();
		logDirectory = new File(context.getInitParameter("humfrey.logDirectory"));
	}

}
