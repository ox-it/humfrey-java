package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.rdf.model.Model;

abstract public class JenaSerializer extends Serializer {
	protected abstract String getSerialization();
	protected abstract String getContentType();

	@Override
	public void serializeModel(Model model, Query query, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		resp.setContentType(getContentType());
		Writer writer = resp.getWriter();
		model.write(writer, getSerialization());
	}

}
