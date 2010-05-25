package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.rdf.model.Model;

public abstract class Serializer {
	public abstract String getContentType();
	abstract public void serializeModel(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
