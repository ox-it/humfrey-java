package uk.ac.ox.oucs.humfrey.serializers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ox.oucs.humfrey.Query;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * Implements the SPARQL results JSON syntax described at
 * <http://www.w3.org/TR/rdf-sparql-json-res/>.
 * 
 * @author Alexander Dutton <alexander.dutton@oucs.ox.ac.uk>
 *
 */
public class SparqlJSONSerializer extends AbstractSerializer {

	@Override
	public void serializeBoolean(boolean value, Query query,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType(getContentType());
		ResultSetFormatter.outputAsJSON(resp.getOutputStream(), value);
	}

	@Override
	public String getContentType() {
		return "application/sparql-results+json";
	}

	@Override
	public String getName() {
		return "SPARQL results JSON";
	}

	@Override
	public void serializeResultSet(ResultSet resultset, Query query,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType(getContentType());
		
		ResultSetFormatter.outputAsJSON(resp.getOutputStream(), resultset);
	}
	
	@Override
	public boolean canSerialize(SerializationType serializationType) {
		switch (serializationType) {
		case ST_RESULTSET:
		case ST_BOOLEAN:
			return true;
		default:
			return false;
		}
	}

}
