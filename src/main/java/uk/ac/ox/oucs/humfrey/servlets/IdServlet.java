package uk.ac.ox.oucs.humfrey.servlets;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.FormatPreferences;
import uk.ac.ox.oucs.humfrey.Query;
import uk.ac.ox.oucs.humfrey.serializers.AbstractSerializer;

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
		
		URL url = query.getURL();
		
		if (datasetContains(query.getURI())) {
			resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
			URL location = Query.buildURL(url.getProtocol(), url.getHost(), url.getPort(), "/doc/" + url.getPath().substring(4) + "." + query.getAccept());
			resp.addHeader("Location", location.toString());
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	@Override
	protected FormatPreferences getAcceptFormats() {
		return new FormatPreferences("rdf", "html", serializer.getSerializers(AbstractSerializer.SerializationType.ST_RESOURCE));
	}

}
