package uk.ac.ox.oucs.humfrey;

import java.net.MalformedURLException;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.tools.generic.EscapeTool;

import uk.ac.ox.oucs.humfrey.namespaces.PERM;

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

	static List<MediaType> mediaTypes = new LinkedList<MediaType>();
	static {
		Query q = new Query();
		mediaTypes.add(q.new MediaType("application/xhtml+xml", "html"));
		mediaTypes.add(q.new MediaType("text/html", "html"));
		mediaTypes.add(q.new MediaType("application/rdf+xml", "rdf"));
		mediaTypes.add(q.new MediaType("text/javascript", "js"));
		mediaTypes.add(q.new MediaType("application/json", "json"));
		mediaTypes.add(q.new MediaType("application/javascript", "js"));
		mediaTypes.add(q.new MediaType("text/n3", "n3"));
		mediaTypes.add(q.new MediaType("application/sparql-results+xml", "srx"));
	}
	
	private Query() {}
	
	public Query(String accountPrefix, Model configModel,
			HttpServletRequest req, FormatPreferences acceptFormats, FormatPreferences contentTypeFormats)
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
			String uriString = req.getParameter("uri");
			if (uriString == null)
				throw new UnknownQueryException();
			uri = Node.createURI(uriString);
			try {
				url = new URL(uriString);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			String format = req.getParameter("format");
			foreignResource = true;
			if (format == null)
				setAccept(negotiateContent(req.getHeader("Accept"), acceptFormats));
			else {
				if (acceptFormats.formatAvailable(format)) {
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
			for (String format : acceptFormats.union(contentTypeFormats)) {
				if (path.endsWith("."+format)) {
					if (acceptFormats.formatAvailable(format)) {
						setAccept(format);
						negotiatedAccept = false;
					}
					if (contentTypeFormats.formatAvailable(format)) {
						setContentType(format);
						negotiatedContentType = false;
					}
					path = path.substring(0, path.lastIndexOf('.'));
				}
			}
			if (accept == null)
				setAccept(negotiateContent(req.getHeader("Accept"), acceptFormats));
			if (contentType == null)
				setContentType(negotiateContent(req.getHeader("Content-Type"), contentTypeFormats));
			url = buildURL(url.getProtocol(), url.getHost(), url.getPort(), path);
			uri = Node.createURI(url.toString());
		} else if (path.equals("/sparql/")) {
			String format = req.getParameter("format");
			foreignResource = true;
			if (format == null)
				setAccept(negotiateContent(req.getHeader("Accept"), acceptFormats));
			else {
				if (acceptFormats.formatAvailable(format)) {
					setAccept(format);
					negotiatedAccept = false;
				} else
					throw new InvalidFormatException();
			}
		} else {
			uri = Node.createURI(url.toString());
			setAccept(negotiateContent(req.getHeader("Accept"), acceptFormats));
			
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
	
	private String negotiateContent(String header, FormatPreferences formats) {
		if (header == null)
			return formats.getDefaultNotProvided();
		String[] mimeTypes = header.split(",");
		
		for (String mimeType : mimeTypes) {
			mimeType = mimeType.split(";")[0].trim();
			
			for (MediaType mediaType : mediaTypes)
				if (mimeType.equals(mediaType.mediaType) && formats.formatAvailable(mediaType.format))
					return mediaType.format;
		}
		return formats.getDefaultNotFound();
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
	public boolean negotiatedAccept() {
		return negotiatedAccept;
	}
	public String getContentType() {
		return contentType;
	}
	public boolean negotiatedContentType() {
		return negotiatedContentType;
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
	
	private class MediaType {
		public String mediaType, format;
		public MediaType(String mediaType, String format) {
			this.mediaType = mediaType; this.format = format;
		}
		
	}
	

}
