package org.icatproject.core;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocGenerator {

	private static Logger logger = LoggerFactory.getLogger(DocGenerator.class);

	public static void main(String[] args) throws Exception {

		logger.debug("DocGenerator starting");

		File dir = new File(args[0]);
		PrintWriter out = new PrintWriter(new File(dir, "src/site/resources/schema.html"));
		out.print(
				"<!DOCTYPE HTML><html><head><style type=\"text/css\">h1,h2,h3 {color:sienna;} table { border-collapse:collapse; } td, th { border:1px solid sienna; padding:4px; font-weight:normal; text-align:left} th { color:sienna; }</style><title>ICAT Schema</title><link rel=\"icon\" href=\"http://www.icatproject.org/favicon.ico\"/></head><body><h1>ICAT Schema</h1>");
		List<String> cnames = EntityInfoHandler.getEntityNamesList();

		out.print("<p style=\"max-width:50em;\">");
		boolean first = true;
		for (String cname : cnames) {
			if (first) {
				first = false;
			} else {
				out.print(", ");
			}
			out.print("<a href = \"#" + cname + "\">" + cname + "</a>");
		}
		out.print("</p>");

		for (String cname : cnames) {
			out.print("<hr/><h2 id=\"" + cname + "\">" + cname + "</h2>");
			Class<? extends EntityBaseBean> eklass = EntityInfoHandler.getClass(cname);
			String classComment = EntityInfoHandler.getClassComment(eklass);
			if (classComment == null) {
				System.out.println(cname + " has no comment");
			} else {
				out.println("<p>" + classComment + "</p>");
			}

			Set<Field> fields = new HashSet<Field>(EntityInfoHandler.getGetters(eklass).keySet());
			Map<Field, String> fieldComments = EntityInfoHandler.getFieldComments(eklass);
			Set<Field> notnullables = new HashSet<Field>(EntityInfoHandler.getNotNullableFields(eklass));
			Map<Field, Integer> stringFields = EntityInfoHandler.getStringFields(eklass);

			Iterator<Field> iter = fields.iterator();
			while (iter.hasNext()) {
				Field f = iter.next();
				if (f.getName().equals("id")) {
					iter.remove();
					break;
				}
			}

			List<Field> constraint = EntityInfoHandler.getConstraintFields(eklass);
			if (!constraint.isEmpty()) {
				out.print("<p><b>Uniqueness constraint</b> ");
				first = true;
				for (Field f : constraint) {
					if (first) {
						first = false;
					} else {
						out.print(", ");
					}
					out.print(f.getName());
				}
				out.println("</p>");
			}

			out.println(
					"<h3>Relationships</h3><table><tr><th>Card</th><th>Class</th><th>Field</th><th>Cascaded</th><th>Description</th></tr>");
			for (Relationship r : EntityInfoHandler.getRelatedEntities(eklass)) {
				Field f = r.getField();
				boolean notnullable = notnullables.contains(f);
				boolean many = r.isCollection();
				String beanName = r.getDestinationBean().getSimpleName();
				String card = (notnullable ? "1" : "0") + "," + (many ? "*" : "1");
				String cascaded = (r.isCollection() ? "Yes" : "");
				out.print("<tr><td> " + card + "</td>");
				out.print("<td><a href = \"#" + beanName + "\">" + beanName + "</a></td><td>" + f.getName()
						+ "</td><td>" + cascaded + "</td>");
				String comments = fieldComments.get(f);
				out.println("<td>" + ((comments == null) ? "" : comments) + "</td></tr>");
				fields.remove(f);
			}
			out.println("</table>");

			if (!fields.isEmpty()) {
				out.println("<h3>Other fields</h3>");
				out.println("<table><tr><th>Field</th><th>Type</th><th>Description</th></tr>");
				for (Field f : fields) {
					String type = f.getType().getSimpleName();
					Integer length = stringFields.get(f);
					if (length != null) {
						type = type + " [" + length + "]";
					}
					if (notnullables.contains(f)) {
						type = type + " NOT NULL";
					}
					String comments = fieldComments.get(f);
					out.print("<tr><td>" + f.getName() + "</td><td>" + type + "</td>");
					out.println("<td>" + ((comments == null) ? "" : comments) + "</td></tr>");
				}
				out.println("</table>");
			}

		}
		out.print("</body></html>");
		out.close();

	}
}
