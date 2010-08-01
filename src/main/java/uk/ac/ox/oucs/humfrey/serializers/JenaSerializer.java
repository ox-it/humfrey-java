package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class JenaSerializer extends AbstractSerializer {
	public abstract String getSerialization();
	
	@Override
	public void serializeModel(Model model, Model fullModel, Query query, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		resp.setContentType(getContentType());
		Writer writer = resp.getWriter();
		model.write(writer, getSerialization());
	}

	@Override
	public boolean canSerialize(SerializationType serializationType) {
		switch (serializationType) {
		case ST_MODEL:
		case ST_RESOURCE:
			return true;
		default:
			return false;
		}
	}
}
