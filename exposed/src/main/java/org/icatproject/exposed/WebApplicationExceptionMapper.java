package org.icatproject.exposed;

import java.io.StringWriter;
import java.net.HttpURLConnection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

	private static Logger logger = Logger.getLogger(WebApplicationExceptionMapper.class);

	@Override
	public Response toResponse(WebApplicationException e) {

		logger.info("Processing: " + e.getClass() + " " + e.getMessage());

		JsonObject om = Json.createObjectBuilder().add("code", "InternalException")
				.add("message", e.getClass() + " " + e.getMessage()).build();
		StringWriter stWriter = new StringWriter();
		try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
			jsonWriter.writeObject(om);
		}
		return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(stWriter.toString())
				.build();

	}
}