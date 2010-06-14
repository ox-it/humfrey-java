package uk.ac.ox.oucs.humfrey;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.tools.generic.EscapeTool;

import uk.ac.ox.oucs.humfrey.serializers.Serializer;

import com.hp.hpl.jena.graph.Node;

public class Query {
	Node uri = null;
	URL url = null;
	String format = null;
	String serialization = null;
	String contentType = null;
	String serverHostPart = null;
	boolean foreignResource = false;
	private static EscapeTool escapeTool = new EscapeTool(); 
	
	public Query(Serializer serializer, HttpServletRequest req) throws InvalidFormatException {
		try {
			url = new URL(req.getRequestURL().toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		serverHostPart = buildURL(url.getProtocol(), url.getHost(), url.getPort(), "/").toString();
		String path = url.getPath();
		if (path.startsWith("/id/")) {
			uri = Node.createURI(url.toString());
			setFormat(negotiateContent(req.getHeader("Accept")));
			
		} else if (path.equals("/doc/")) {
			uri = Node.createURI(req.getParameter("uri"));
			try {
				url = new URL(req.getParameter("uri"));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			String format = req.getParameter("format");
			foreignResource = true;
			if (format == null)
				setFormat(negotiateContent(req.getHeader("Accept")));
			else {
				if (serializer.hasFormat(format))
					setFormat(format);
				else
					throw new InvalidFormatException();
			}
		} else if (path.startsWith("/graph/")
				|| path.startsWith("/ontology/")
				|| path.startsWith("/doc/")) {
			if (path.startsWith("/doc/"))
				path = "/id/" + path.substring(5);
			for (String format : serializer.getFormats()) {
				if (path.endsWith("."+format)) {
					setFormat(format);
					path = path.substring(0, path.lastIndexOf('.'));
				}
			}
			if (format == null)
				setFormat(negotiateContent(req.getHeader("Accept")));
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
