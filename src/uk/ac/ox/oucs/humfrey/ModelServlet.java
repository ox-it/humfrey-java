package uk.ac.ox.oucs.humfrey;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelServlet extends HttpServlet {
	Model model = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 2653035836289316041L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		model = ModelFactory.getModel(config.getInitParameter("storeDescription"));
		super.init(config);
	}

}
