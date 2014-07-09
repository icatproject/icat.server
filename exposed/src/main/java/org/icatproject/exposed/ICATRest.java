package org.icatproject.exposed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.manager.EntityBeanManager;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.PropertyHandler;

@Path("/")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ICATRest {

	// TODO Need to check that all modes (CHECK, IGNORE etc) work properly for import and that the
	// system attributes can only be set by somebody in root user names

	// TODO get rid of javax.ws.rs

	// TODO avoid processing IDS one by one for export

	// TODO avoid duplicate code in two modes of export

	@EJB
	EntityBeanManager beanManager;

	@EJB
	RestfulBeanManager restfulBeanManager;

	private static Logger logger = Logger.getLogger(ICATRest.class);

	@EJB
	GateKeeper gatekeeper;

	private int lifetimeMinutes;

	@PersistenceContext(unitName = "icat")
	private EntityManager manager;

	@EJB
	PropertyHandler propertyHandler;

	private Set<String> rootUserNames;

	private Map<String, Authenticator> authPlugins;

	@Resource
	private UserTransaction userTransaction;

	@GET
	@Path("version")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVersion() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("version", Constants.API_VERSION).writeEnd();
		gen.close();
		return Response.ok(baos.toString()).build();
	}

	@POST
	@Path("session")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpServletRequest request, @FormParam("json") String jsonString)
			throws IcatException {
		logger.debug(jsonString);
		if (jsonString == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "json must not be null");
		}
		String plugin = null;
		Map<String, String> credentials = new HashMap<>();
		try (JsonParser parser = Json.createParser(new ByteArrayInputStream(jsonString.getBytes()))) {
			String key = null;
			boolean inCredentials = false;

			while (parser.hasNext()) {
				JsonParser.Event event = parser.next();
				if (event == Event.KEY_NAME) {
					key = parser.getString();
				} else if (event == Event.VALUE_STRING) {
					if (inCredentials) {
						credentials.put(key, parser.getString());
					} else {
						if (key.equals("plugin")) {
							plugin = parser.getString();
						}
					}
				} else if (event == Event.START_ARRAY && key.equals("credentials")) {
					inCredentials = true;
				} else if (event == Event.END_ARRAY) {
					inCredentials = false;
				}
			}
		}

		Authenticator authenticator = authPlugins.get(plugin);
		if (authenticator == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Authenticator mnemonic " + plugin + " not recognised");
		}
		logger.debug("Using " + plugin + " to authenticate");

		String userName = authenticator.authenticate(credentials, request.getRemoteAddr())
				.getUserName();
		String sessionId = beanManager.login(userName, lifetimeMinutes, manager, userTransaction);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("sessionId", sessionId).writeEnd();
		gen.close();
		return Response.ok(baos.toString()).build();

	}

	@GET
	@Path("session/{sessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSession(@PathParam("sessionId") String sessionId) throws IcatException {

		String userName = beanManager.getUserName(sessionId, manager);
		double remainingMinutes = beanManager.getRemainingMinutes(sessionId, manager);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("userName", userName)
				.write("remainingMinutes", remainingMinutes).writeEnd();
		gen.close();
		return Response.ok(baos.toString()).build();

	}

	@PUT
	@Path("session/{sessionId}")
	public void refresh(@PathParam("sessionId") String sessionId) throws IcatException {
		beanManager.refresh(sessionId, lifetimeMinutes, manager, userTransaction);
	}

	@DELETE
	@Path("session/{sessionId}")
	public void logout(@PathParam("sessionId") String sessionId) throws IcatException {
		beanManager.logout(sessionId, manager, userTransaction);
	}

	@POST
	@Path("port")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response importData(@Context HttpServletRequest request) throws IcatException,
			IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Multipart content expected");
		}

		ServletFileUpload upload = new ServletFileUpload();
		String jsonString = null;
		String name = null;
		Response result = null;

		// Parse the request
		try {
			FileItemIterator iter = upload.getItemIterator(request);
			while (iter.hasNext()) {
				FileItemStream item = iter.next();
				String fieldName = item.getFieldName();
				InputStream stream = item.openStream();
				if (item.isFormField()) {
					String value = Streams.asString(stream);
					if (fieldName.equals("json")) {
						jsonString = value;

					} else {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Form field "
								+ fieldName + "is not recognised");
					}
				} else {
					if (name == null) {
						name = item.getName();
					}
					result = restfulBeanManager.importData(jsonString, stream, manager,
							userTransaction);
				}
			}
			return result;
		} catch (FileUploadException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@GET
	@Path("port")
	@Produces(MediaType.TEXT_PLAIN)
	public Response exportData(@QueryParam("json") String jsonString) throws IcatException {
		return restfulBeanManager.exportData(jsonString, manager, userTransaction);
	}

	@PostConstruct
	private void init() {
		authPlugins = propertyHandler.getAuthPlugins();
		lifetimeMinutes = propertyHandler.getLifetimeMinutes();
		rootUserNames = gatekeeper.getRootUserNames();
	}
}