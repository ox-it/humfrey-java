package uk.ac.ox.oucs.humfrey;

import java.net.MalformedURLException;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.tools.generic.EscapeTool;

import uk.ac.ox.oucs.humfrey.namespaces.PERM;
import uk.ac.ox.oucs.humfrey.serializers.Serializer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class Query {
	Node uri = null;
	URL url = null;
	
	String accept = null;
	boolean negotiatedAccept = true;

	String contentType = null;
	boolean negotiatedContentType = true;
	
	String serverHostPart = null;
	String username = null;
	Resource user = null;
	boolean foreignResource = false;
	private static EscapeTool escapeTool = new EscapeTool(); 
	
	public Query(String accountPrefix, Model configModel,
			Serializer serializer, HttpServletRequest req)
	throws InvalidFormatException, InvalidCredentialsException, UnknownQueryException {
		try {
			if (req.getHeader("X-Request-URL") != null)
				url = new URL(req.getHeader("X-Request-URL"));
			else
				url = new URL(req.getRequestURL().toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		serverHostPart = buildURL(url.getProtocol(), url.getHost(), url.getPort(), "/").toString();
		String path = url.getPath();
		if (path.equals("/doc/")) {
			uri = Node.createURI(req.getParameter("uri"));
			try {
				url = new URL(req.getParameter("uri"));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			String format = req.getParameter("format");
			foreignResource = true;
			if (format == null)
				setAccept(negotiateContent(req.getHeader("Accept")));
			else {
				if (serializer.hasFormat(format)) {
					setAccept(format);
					negotiatedAccept = false;
				} else
					throw new InvalidFormatException();
			}
		} else if (path.startsWith("/graph/")
				|| path.startsWith("/ontology/")
				|| path.startsWith("/doc/")) {
			if (path.startsWith("/doc/"))
				path = "/id/" + path.substring(5);
			for (String format : serializer.getFormats()) {
				if (path.endsWith("."+format)) {
					setAccept(format);
					setContentType(format);
					negotiatedAccept = negotiatedContentType = false;
					path = path.substring(0, path.lastIndexOf('.'));
				}
			}
			if (accept == null)
				setAccept(negotiateContent(req.getHeader("Accept")));
			if (contentType == null)
				setContentType(negotiateContent(req.getHeader("Content-Type")));
			url = buildURL(url.getProtocol(), url.getHost(), url.getPort(), path);
			uri = Node.createURI(url.toString());
		} else if (path.equals("/sparql/")) {
			String format = req.getParameter("format");
			foreignResource = true;
			if (format == null)
				setAccept(negotiateContent(req.getHeader("Accept")));
			else {
				if (serializer.hasFormat(format)) {
					setAccept(format);
					negotiatedAccept = false;
				} else
					throw new InvalidFormatException();
			}
		} else {
			uri = Node.createURI(url.toString());
			setAccept(negotiateContent(req.getHeader("Accept")));
			
		}

		performAuthentication(accountPrefix, configModel, req);
	}
	
	private void performAuthentication(String accountPrefix, Model configModel, HttpServletRequest req) throws InvalidCredentialsException {	
		String authorization = req.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("Basic ")) {
			authorization = authorization.substring(6);
			Base64 base64 = new Base64();
			String[] credentials = (new String(base64.decode(authorization.getBytes()))).split(":", 2);


			if (credentials.length != 2)
				throw new InvalidCredentialsException();

			String username = credentials[0], password = credentials[1];
			String[] passwordHash;
			Statement pwStmt = configModel.getProperty(configModel.createResource(accountPrefix + username), PERM.password);
			passwordHash = ((Literal) pwStmt.getObject()).getString().split("\\$");
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			md.update(passwordHash[0].getBytes());
			md.update(password.getBytes());

			Formatter formatter = new Formatter();
			byte[] digest = md.digest();
			for (byte b : digest)
				formatter.format("%02x", b);
			if (formatter.toString().equals(passwordHash[1]))
				this.username = username;
				this.user = configModel.createResource(accountPrefix + username);
		}
		if (authorization != null && username == null)
			throw new InvalidCredentialsException();
	}

	public URL getDocRoot() {
		if (!url.getPath().startsWith("/id/"))
			throw new AssertionError();
		return buildURL(url.getProtocol(), url.getHost(), url.getPort(), "/doc/"+url.getPath().substring(4));
	}
	
	static public URL buildURL(String protocol, String host, int port, String path) {
		try {
			if (port == 80)
				return new URL(protocol, host, path);
			else
				return new URL(protocol, host, port, path);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void setAccept(String format) {
		this.accept = format;
	}
	private void setContentType(String format) {
		this.contentType = format;
	}
	
	public String getDocURL() {
		return getDocURL(null);
	}
	
	public String getDocURL(String format) {
		String docURL;
		if (foreignResource) {
			docURL = serverHostPart + "doc/?uri=" + escapeTool.url(uri.toString());
			if (format != null)
				docURL += "&format=" + format;
		} else {
			docURL = getDocRoot().toString();
			if (format != null)
				docURL += "." + format;
		}
		return docURL;
	}
	
	private String negotiateContent(String header) {
		if (header == null)
			return "rdf";
		String[] mimeTypes = header.split(",");
		for (String mimeType : mimeTypes) {
			mimeType = mimeType.split(";")[0].trim();
			if (mimeType.equals("application/xhtml+xml"))
				return "html";
			else if (mimeType.equals("text/html"))
				return "html";
			else if (mimeType.equals("application/rdf+xml"))
				return "rdf";
			else if (mimeType.equals("text/n3"))
				return "n3";
			else if (mimeType.equals("application/json"))
				return "json";
			else if (mimeType.equals("text/javascript"))
				return "js";
			else if (mimeType.equals("application/javascript"))
				return "js";
		}
		return "html";
	}
	
	public Node getNode() {
		return uri;
	}
	public URL getURL() {
		return url;
	}
	public String getURI() {
		return url.toString();
	}
	public String getAccept() {
		return accept;
	}
	public String getContentType() {
		return contentType;
	}
	public boolean isAuthenticated() {
		return username != null;
	}
	
	public String getUsername() {
		return username;
	}
	public Resource getUser() {
		return user;
	}
	
	public class InvalidFormatException extends Exception {
		private static final long serialVersionUID = -3887601882049480796L;
	}
	public class InvalidCredentialsException extends Exception {
		private static final long serialVersionUID = -3887601882049480797L;
	}
	public class UnknownQueryException extends Exception {
		private static final long serialVersionUID = -3887601882049480798L;
	}
	

}
