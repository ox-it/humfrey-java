package uk.ac.ox.oucs.humfrey;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;

import uk.ac.ox.oucs.humfrey.resources.VelocityResource;

import com.hp.hpl.jena.rdf.model.Literal;

public class Templater {
	private VelocityEngine ve;
	private EscapeTool escapeTool;
	
	public Templater(ServletContext servletContext) {
		escapeTool = new HumfreyEscapeTool();
		ve = new VelocityEngine();
		try {
			ve.setApplicationAttribute("javax.servlet.ServletContext", servletContext);
			ve.setProperty("resource.loader", "webapp");
			ve.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.WebappResourceLoader");
			ve.setProperty("webapp.resource.loader.path", "/WEB-INF/templates/");
			ve.setProperty("runtime.references.strict", true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void render(HttpServletResponse resp, String templateName, VelocityContext context) {
		Writer writer;
		try {
			writer = resp.getWriter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		render(writer, templateName, context);
	}
	
	public void render(Writer writer, String templateName, VelocityContext context) {
		Template template = getTemplate(templateName);
		context.put("esc", escapeTool);
		try {
			template.merge(context, writer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Template getTemplate(String name) {
		try {
			return ve.getTemplate(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}
	
	class HumfreyEscapeTool extends EscapeTool {

		@Override
		public String html(Object string) {
			if (string instanceof VelocityResource)
				return ((VelocityResource) string).toString();
			else if (string instanceof Literal)
				return super.html(((Literal) string).getValue().toString());
			else
				return super.html(string);
		}
		
	}
}
