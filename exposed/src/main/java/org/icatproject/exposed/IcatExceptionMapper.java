package org.icatproject.exposed;

import java.io.StringWriter;
import java.net.HttpURLConnection;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Provider
public class IcatExceptionMapper implements ExceptionMapper<IcatException> {

	private static Logger logger = Logger.getLogger(IcatExceptionMapper.class);

	@Override
	public Response toResponse(IcatException e) {

		logger.info("Processing: " + e.getType() + " " + e.getMessage());

		JsonObjectBuilder omb = Json.createObjectBuilder().add("code", e.getType().name())
				.add("message", e.getMessage());
		if (e.getOffset() >= 0) {
			omb.add("offset", e.getOffset());
		}
		StringWriter stWriter = new StringWriter();
		try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
			jsonWriter.writeObject(omb.build());
		}
		int code = 0;
		switch (e.getType()) {
		case BAD_PARAMETER:
			code = HttpURLConnection.HTTP_BAD_REQUEST;
			break;
		case INSUFFICIENT_PRIVILEGES:
			code = HttpURLConnection.HTTP_FORBIDDEN;
			break;
		case NO_SUCH_OBJECT_FOUND:
			code = HttpURLConnection.HTTP_NOT_FOUND;
			break;
		case OBJECT_ALREADY_EXISTS:
			code = HttpURLConnection.HTTP_BAD_REQUEST;
			break;
		case SESSION:
			code = HttpURLConnection.HTTP_FORBIDDEN;
			break;
		case VALIDATION:
			code = HttpURLConnection.HTTP_BAD_REQUEST;
			break;
		default:
			code = HttpURLConnection.HTTP_INTERNAL_ERROR;
		}
		return Response.status(code).entity(stWriter.toString()).build();

	}
}