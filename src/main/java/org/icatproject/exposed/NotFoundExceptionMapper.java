package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException.IcatExceptionType;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	private static Logger logger = Logger.getLogger(NotFoundExceptionMapper.class);

	@Override
	public Response toResponse(NotFoundException e) {
		logger.info("Processing: " + e.getClass() + " " + e.getMessage(), e);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("code", IcatExceptionType.NOT_IMPLEMENTED.name())
				.write("message", "Operation not implemented by this ICAT server.").writeEnd().close();
		return Response.status(IcatExceptionType.NOT_IMPLEMENTED.getStatus()).entity(baos.toString()).build();
	}
}