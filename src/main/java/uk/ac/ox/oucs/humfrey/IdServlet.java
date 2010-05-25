package uk.ac.ox.oucs.humfrey;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;

public class IdServlet extends ModelServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4531468862625965870L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Query query = getQuery(req, resp);
		if (query == null)
			return;
		Model model = getModel();
		URL url = query.getURL();
		
		if (containsQuerySubject(model, query)) {
			resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
			URL location = Query.buildURL(url.getProtocol(), url.getHost(), url.getPort(), "/doc/" + url.getPath().substring(4) + "." + query.getFormat());
			resp.addHeader("Location", location.toString());
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

}
