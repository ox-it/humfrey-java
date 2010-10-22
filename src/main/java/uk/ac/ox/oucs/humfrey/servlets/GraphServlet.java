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
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import uk.ac.ox.oucs.humfrey.Namespaces;
import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.namespaces.CS;
import uk.ac.ox.oucs.humfrey.namespaces.PERM;
import uk.ac.ox.oucs.humfrey.serializers.AbstractSerializer;
import uk.ac.ox.oucs.humfrey.serializers.JenaSerializer;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

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
		String uri = query.getURI();

		if (!dataset.containsNamedModel(uri)) {
			resp.setStatus(404);
			return;
		}

		serializeModel(dataset.getNamedModel(uri), query, req, resp);

	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate};
		updateModel(req, resp, permissions, new ModelUpdater() {
			public void updateModel(Model model, String uri) {
				Model m = dataset.getNamedModel(uri);
				m.removeAll();
				m.add(model);
			}
		});
	}

	protected void doUnion(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate, PERM.mayAugment};
		updateModel(req, resp, permissions, new ModelUpdater(){
			public void updateModel(Model model, String uri) {
				dataset.getNamedModel(uri).add(model);
			}
		});
	}

	protected void doIntersection(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate};
		updateModel(req, resp, permissions, new ModelUpdater(){
			public void updateModel(Model model, String uri) {
				Model target = dataset.getNamedModel(uri);
				StmtIterator stmts = target.listStatements();
				List<Statement> stmtsToRemove = new LinkedList<Statement>();
				while (stmts.hasNext()) {
					Statement stmt = stmts.next();
					if (!model.contains(stmt))
						stmtsToRemove.add(stmt);
				}
				target.remove(stmtsToRemove);
			}
		});
	}

	protected void doSubtract(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		Property[] permissions = {PERM.mayAdminister, PERM.mayUpdate};
		updateModel(req, resp, permissions, new ModelUpdater(){
			public void updateModel(Model model, String uri) {
				dataset.getNamedModel(uri).remove(model);
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
		String uri = query.getURI();
		if (dataset.containsNamedModel(uri)) {
			dataset.getNamedModel(uri).removeAll();
			resp.setStatus(200);
		} else
			resp.setStatus(404);

	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
		Set<String> methods = new HashSet<String>();
		Query query = getQuery(req, resp);
		if (query == null)
			return;

		String accountPrefix = getServletContext().getInitParameter("humfrey.accountPrefix");
		Set<Property> permissions = getPermissions(accountPrefix, query.getUsername(), query.getURI());
		
		methods.add("OPTIONS");
		
		if (dataset.containsNamedModel(query.getURI())) {
			methods.add("HEAD"); methods.add("GET");
		}
		
		if (permissions.contains(PERM.mayAdminister) || permissions.contains(PERM.mayUpdate)) {
			methods.add("PUT"); methods.add("INTERSECTION"); 
			methods.add("UNION"); methods.add("SUBTRACT");
			methods.add("DELETE");
		}
		if (permissions.contains(PERM.mayAugment))
			methods.add("UNION");
		if (permissions.contains(PERM.mayDelete))
			methods.add("DELETE");

		resp.addHeader("Allow", StringUtils.join(methods, ", "));
		
		Map<String,AbstractSerializer> serializers = serializer.getSerializers();
		
		if (query.negotiatedAccept()) {
			Set<String> contentTypes = new HashSet<String>();
			for (AbstractSerializer serializer : serializers.values())
				if (serializer.canSerialize(AbstractSerializer.SerializationType.ST_MODEL))
					contentTypes.add(serializer.getContentType());
			resp.addHeader("X-Allow-Accept", StringUtils.join(contentTypes, ", "));
		}
		if (query.negotiatedContentType()) {
			Set<String> contentTypes = new HashSet<String>();
			for (AbstractSerializer serializer : serializers.values())
				if (serializer instanceof JenaSerializer)
					contentTypes.add(serializer.getContentType());
			resp.addHeader("X-Allow-Content-Type", StringUtils.join(contentTypes, ", "));
		}
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		if (req.getMethod().equals("UNION"))
			doUnion(req, resp);
		else if (req.getMethod().equals("INTERSECTION"))
			doIntersection(req, resp);
		else if (req.getMethod().equals("SUBTRACT"))
			doSubtract(req, resp);
		else
			super.service(req, resp);
	}

	private void updateModel(HttpServletRequest req, HttpServletResponse resp, Property[] permissions,
			ModelUpdater modelUpdater) throws ServletException, IOException {
		Query query = getQuery(req, resp);
		ServletContext context = getServletContext();
		if (query == null)
			return;

		if (performPermissionsCheck(context, query, resp, permissions))
			return;

		String uri = query.getURI();

		AbstractSerializer as = serializer.get(query.getContentType());
		if (as == null || !(as instanceof JenaSerializer)) {
			resp.setStatus(query.negotiatedContentType() ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Model model = ModelFactory.createDefaultModel();
		model.read(req.getInputStream(), query.getURI(), ((JenaSerializer) as).getSerialization());

		Model target = dataset.getNamedModel(uri);
		Set<Statement> before = target.listStatements().toSet();

		modelUpdater.updateModel(model, uri);

		Set<Statement> after = target.listStatements().toSet();

		Set<Statement> intersection = new HashSet<Statement>(before);
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

		for (Statement statement : before)
			changeset.addProperty(Namespaces.cs.p("removal"),
					statement.createReifiedStatement());
		for (Statement statement : after)
			changeset.addProperty(Namespaces.cs.p("addition"),
					statement.createReifiedStatement());


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
	
	static private Set<Property> getPermissions(String userPrefix, String username, String uri) {
		Set<Property> permissions = new HashSet<Property>();
		Resource resource = configModel.createResource(uri);
		Resource user;
		if (username == null)
			user = PERM.Public;
		else
			user = configModel.createResource(userPrefix + username);
		
		StmtIterator stmts = configModel.listStatements(user, null, resource);
		while (stmts.hasNext())
			permissions.add(stmts.next().getPredicate());
		
		String queryString = "";
		queryString += "PREFIX perm: <http://vocab.ox.ac.uk/perm#>\n";
		queryString += "SELECT ?perm ?regex WHERE {\n";
		queryString += "  <"+user.getURI()+"> ?perm ?rm .";
		queryString += "  ?rm a perm:ResourceMatch ;";
		queryString += "      perm:matchExpression ?regex }";
		
		com.hp.hpl.jena.query.Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, configModel);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution sol = results.next();
			if (uri.matches(((Literal) sol.get("regex")).getLexicalForm()))
				permissions.add(configModel.createProperty(((Resource) sol.get("perm")).getURI()));
		}
		
		if (username != null)
			permissions.addAll(getPermissions(userPrefix, null, uri));
		return permissions;
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

	interface ModelUpdater {
		void updateModel(Model model, String uri);
	}

	@Override
	public void init() throws ServletException {
		super.init();

		ServletContext context = getServletContext();
		logDirectory = new File(context.getInitParameter("humfrey.logDirectory"));
	}

}
