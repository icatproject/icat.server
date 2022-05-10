package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.icatproject.core.IcatException.IcatExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

	private static Logger logger = LoggerFactory.getLogger(ThrowableExceptionMapper.class);

	@Override
	public Response toResponse(Throwable e) {
		logger.info("Processing: " + e.getClass() + " " + e.getMessage(), e);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("code", IcatExceptionType.INTERNAL.name())
				.write("message", e.getClass() + " " + e.getMessage()).writeEnd().close();

		return Response.status(Status.INTERNAL_SERVER_ERROR).entity(baos.toString()).build();
	}
}