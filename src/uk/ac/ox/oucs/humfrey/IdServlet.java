package uk.ac.ox.oucs.humfrey;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Resource;

public class IdServlet extends ModelServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4531468862625965870L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String uri = req.getRequestURL().toString();
		
		Resource resource = model.getResource(uri);
		if (model.containsResource(resource)) {
			resp.setStatus(303); // See Other
			String format = getFormat(req.getHeader("Accept"));
			
			resp.addHeader("Location", "");
		} else {
			resp.setStatus(501);
		}
		
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	private String getFormat(String accept) {
		if (accept == null)
			return "html";
		String[] mimeTypes = accept.split(",");
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
}
