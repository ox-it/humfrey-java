package uk.ac.ox.oucs.humfrey;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.graph.Node;

public class Query {
	static final String[] formats = {"rdf", "html", "n3", "ttl", "nt"};
	
	Node uri = null;
	URL url = null;
	String format = null;
	String serialization = null;
	String contentType = null;
	
	public Query(HttpServletRequest req) throws InvalidFormatException {
		try {
			url = new URL(req.getRequestURL().toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		String path = url.getPath();
		if (path.startsWith("/id/")) {
			uri = Node.createURI(url.toString());
			setFormat(negotiateContent(req.getHeader("Accept")));
		} else if (path.startsWith("/graph/")
				|| path.startsWith("/ontology/")
				|| path.startsWith("/doc/")) {
			if (path.startsWith("/doc/"))
				path = "/id/" + path.substring(5);
			for (String format : formats) {
				if (path.endsWith("."+format)) {
					setFormat(format);
					path = path.substring(0, path.lastIndexOf('.'));
				}
			}
			if (format == null)
				throw new InvalidFormatException();
			url = buildURL(url.getProtocol(), url.getHost(), url.getPort(), path);
			uri = Node.createURI(url.toString());
		}
		
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
	
	private void setFormat(String format) {
		this.format = format;
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
		}
		return "html";
	}
	
	public Node getURI() {
		return uri;
	}
	public URL getURL() {
		return url;
	}
	public String getFormat() {
		return format;
	}
	public String getContentType() {
		return contentType;
	}
	public String getSerialization() {
		return serialization;
	}
	
	
}
