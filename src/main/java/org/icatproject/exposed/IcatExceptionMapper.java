package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
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

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("code", e.getType().name()).write("message", e.getMessage());
		if (e.getOffset() >= 0) {
			gen.write("offset", e.getOffset());
		}
		gen.writeEnd().close();
		return Response.status(e.getType().getStatus()).entity(baos.toString()).build();

	}
}