package org.icatproject.exposed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
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
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.CreateResponse;
import org.icatproject.core.manager.EntityBeanManager;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.Lucene.ParameterPOJO;
import org.icatproject.core.manager.Porter;
import org.icatproject.core.manager.PropertyHandler;
import org.icatproject.core.manager.Transmitter;

@Path("/")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ICATRest {

	private void checkRoot(String sessionId) throws IcatException {
		String userId = beanManager.getUserName(sessionId, manager);
		if (!rootUserNames.contains(userId)) {
			throw new IcatException(IcatExceptionType.INSUFFICIENT_PRIVILEGES,
					"user must be in rootUserNames");
		}
	}

	public class EventChecker {

		private JsonParser parser;

		public EventChecker(JsonParser parser) {
			this.parser = parser;
		}

		public Event get(Event... events) {
			Event event = parser.next();
			if (event == Event.KEY_NAME || event == Event.VALUE_STRING) {
				logger.debug(event + ": " + parser.getString());
			} else {
				logger.debug(event);
			}
			for (Event e : events) {
				if (event == e) {
					return event;
				}
			}
			StringBuilder sb = new StringBuilder("event " + event + " is not of expected type [");
			boolean first = true;
			for (Event e : events) {
				if (!first) {
					sb.append(", ");
				} else {
					first = false;
				}
				sb.append(e);
			}
			sb.append(']');
			throw new JsonException(sb.toString());
		}
	}

	private static Logger logger = Logger.getLogger(ICATRest.class);

	private final static DateFormat df8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	private Map<String, Authenticator> authPlugins;

	@EJB
	EntityBeanManager beanManager;

	@EJB
	GateKeeper gatekeeper;

	private int lifetimeMinutes;

	@PersistenceContext(unitName = "icat")
	private EntityManager manager;

	@EJB
	Porter porter;

	@EJB
	Transmitter transmitter;

	@EJB
	PropertyHandler propertyHandler;

	@Resource
	private UserTransaction userTransaction;

	private Set<String> rootUserNames;

	@GET
	@Path("port")
	@Produces(MediaType.TEXT_PLAIN)
	public Response exportData(@QueryParam("json") String jsonString) throws IcatException {
		return porter.exportData(jsonString, manager, userTransaction);
	}

	@GET
	@Path("session/{sessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSession(@PathParam("sessionId") String sessionId) throws IcatException {

		String userName = beanManager.getUserName(sessionId, manager);
		double remainingMinutes = beanManager.getRemainingMinutes(sessionId, manager);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("userName", userName)
				.write("remainingMinutes", remainingMinutes).writeEnd();
		gen.close();
		return baos.toString();
	}

	@GET
	@Path("jpql")
	@Produces(MediaType.TEXT_PLAIN)
	public String getJpql(@QueryParam("sessionId") String sessionId,
			@QueryParam("query") String query) throws IcatException {
		checkRoot(sessionId);
		if (query == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "query is not set");
		}
		int nMax = 5;
		List<Object> os = manager.createQuery(query, Object.class).setMaxResults(nMax)
				.getResultList();
		StringBuilder sb = new StringBuilder();
		if (os.size() == nMax) {
			sb.append("Count at least 5");
		} else {
			sb.append("Count " + os.size());
		}
		boolean first = true;
		for (Object o : os) {
			if (!first) {
				sb.append(", ");
			} else {
				sb.append(": ");
				first = false;
			}
			sb.append(o);
		}
		return sb.toString();
	}

	@GET
	@Path("version")
	@Produces(MediaType.APPLICATION_JSON)
	public String getVersion() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("version", Constants.API_VERSION).writeEnd();
		gen.close();
		return baos.toString();
	}

	@POST
	@Path("entityManager")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String create(@FormParam("sessionId") String sessionId,
			@FormParam("entities") String json) throws IcatException {
		List<EntityBaseBean> entities = new ArrayList<>();

		try (JsonParser parser = Json.createParser(new ByteArrayInputStream(json.getBytes()))) {
			EventChecker checker = new EventChecker(parser);
			checker.get(Event.START_ARRAY);
			while (true) {
				Event event = checker.get(Event.START_OBJECT, Event.END_ARRAY);
				if (event == Event.END_ARRAY) {
					break;
				}
				checker.get(Event.KEY_NAME);
				String entityName = parser.getString();
				checker.get(Event.START_OBJECT);
				entities.add(parseEntity(checker, entityName));
				checker.get(Event.END_OBJECT);
			}
		} catch (JsonException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		}
		String userName = beanManager.getUserName(sessionId, manager);

		List<CreateResponse> createResponses = beanManager.createMany(userName, entities, manager,
				userTransaction);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (CreateResponse createResponse : createResponses) {
				transmitter.processMessage(createResponse.getNotificationMessage());
				gen.write(createResponse.getPk());
			}
			gen.writeEnd();
		} catch (JMSException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " "
					+ e.getMessage());
		}
		return baos.toString();
	}

	// Note that the START_OBJECT has already been swallowed
	private EntityBaseBean parseEntity(EventChecker checker, String beanName) throws IcatException {
		Class<EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		Map<Field, Method> setters = eiHandler.getSetters(klass);
		Set<Field> atts = eiHandler.getAttributes(klass);
		Set<Field> updaters = eiHandler.getSettersForUpdate(klass).keySet();
		Map<String, Field> fieldsByName = eiHandler.getFieldsByName(klass);
		EntityBaseBean bean = null;
		JsonParser parser = checker.parser;
		try {
			bean = klass.newInstance();
			while (true) {
				Event event = checker.get(Event.KEY_NAME, Event.END_OBJECT);
				if (event == Event.END_OBJECT) {
					break;
				}
				Field field = fieldsByName.get(parser.getString());
				if (field == null) {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Field "
							+ parser.getString() + " not found in " + beanName);
				} else if (field.getName().equals("id")) {
					checker.get(Event.VALUE_NUMBER);
					bean.setId(Long.parseLong(parser.getString()));
				} else if (atts.contains(field)) {
					checker.get(Event.VALUE_NUMBER, Event.VALUE_STRING);
					String type = field.getType().getSimpleName();
					Object arg;
					if (type.equals("String")) {
						arg = parser.getString();
					} else if (type.equals("Integer")) {
						arg = Integer.parseInt(parser.getString());
					} else if (type.equals("Double")) {
						arg = Double.parseDouble(parser.getString());
					} else if (type.equals("Long")) {
						arg = Long.parseLong(parser.getString());
					} else if (type.equals("boolean")) {
						arg = Boolean.parseBoolean(parser.getString());
						// } else if (field.getType().isEnum()) {
						// arg =
						// gen.write(field.getName(), value.toString());
					} else if (type.equals("Date")) {
						synchronized (df8601) {
							try {
								arg = df8601.parse(parser.getString());
							} catch (ParseException e) {
								throw new IcatException(IcatExceptionType.BAD_PARAMETER,
										"Badly formatted date " + parser.getString());
							}
						}
					} else {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER,
								"Don't know how to parseEntity field of type " + type);
					}
					setters.get(field).invoke(bean, arg);
				} else if (updaters.contains(field)) {
					event = checker.get(Event.START_OBJECT);
					EntityBaseBean arg = parseEntity(checker, field.getType().getSimpleName());
					setters.get(field).invoke(bean, arg);
				} else {
					checker.get(Event.START_ARRAY);
					@SuppressWarnings("unchecked")
					List<EntityBaseBean> col = (List<EntityBaseBean>) getters.get(field).invoke(
							bean);
					while (true) {
						event = checker.get(Event.START_OBJECT, Event.END_ARRAY);
						if (event == Event.END_ARRAY) {
							break;
						}
						EntityBaseBean arg = parseEntity(checker, field.getName());
						col.add(arg);
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | JsonException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getClass() + " "
					+ e.getMessage() + " at " + parser.getLocation().getStreamOffset() + " in json");
		}
		return bean;
	}

	@GET
	@Path("lucene")
	@Produces(MediaType.APPLICATION_JSON)
	public String lucene(@QueryParam("sessionId") String sessionId,
			@QueryParam("query") String query, @QueryParam("maxCount") int maxCount)
			throws IcatException {
		if (query == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "query is not set");
		}
		String userName = beanManager.getUserName(sessionId, manager);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonReader jr = Json.createReader(new ByteArrayInputStream(query.getBytes()))) {
			JsonObject jo = jr.readObject();
			String target = jo.getString("target", null);
			String user = jo.getString("user", null);
			String text = jo.getString("text", null);
			String lower = jo.getString("lower", null);
			String upper = jo.getString("upper", null);
			List<ParameterPOJO> parms = new ArrayList<>();
			if (jo.containsKey("parameters")) {
				for (JsonValue val : jo.getJsonArray("parameters")) {
					JsonObject parm = (JsonObject) val;
					String name = parm.getString("name");
					String units = parm.getString("units");
					if (parm.containsKey("stringValue")) {
						parms.add(new ParameterPOJO(name, units, parm.getString("stringValue")));
					} else if (parm.containsKey("lowerDateValue")
							&& parm.containsKey("upperDateValue")) {
						parms.add(new ParameterPOJO(name, units, parm.getString("lowerDateValue"),
								parm.getString("upperDateValue")));
					} else if (parm.containsKey("lowerNumericValue")
							&& parm.containsKey("upperNumericValue")) {
						parms.add(new ParameterPOJO(name, units, parm.getJsonNumber(
								"lowerNumericValue").doubleValue(), parm.getJsonNumber(
								"upperNumericValue").doubleValue()));
					} else {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, parm.toString());
					}
				}
			}
			List<EntityBaseBean> objects;
			if (target.equals("Investigation")) {

				List<String> samples = new ArrayList<>();
				if (jo.containsKey("samples")) {
					for (JsonValue val : jo.getJsonArray("samples")) {
						JsonString samp = (JsonString) val;
						samples.add(samp.getString());
					}
				}
				String userFullName = jo.getString("userFullName", null);

				objects = beanManager.luceneInvestigations(userName, user, text, lower, upper,
						parms, samples, userFullName, maxCount, manager, userTransaction);

			} else if (target.equals("Dataset")) {
				objects = beanManager.luceneDatasets(userName, user, text, lower, upper, parms,
						maxCount, manager, userTransaction);

			} else {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, "target:" + target
						+ " is not expected");
			}
			JsonGenerator gen = Json.createGenerator(baos);
			gen.writeStartArray();
			for (EntityBaseBean bean : objects) {
				gen.writeStartObject();
				gen.writeStartObject(bean.getClass().getSimpleName());
				jsonise(bean, gen);
				gen.writeEnd();
				gen.writeEnd();
			}
			gen.writeEnd();
			gen.close();
			return baos.toString();
		}
	}

	@GET
	@Path("entityManager")
	@Produces(MediaType.APPLICATION_JSON)
	public String search(@QueryParam("sessionId") String sessionId,
			@QueryParam("query") String query, @QueryParam("id") Long id) throws IcatException {

		if (query == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "query is not set");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);

		String userName = beanManager.getUserName(sessionId, manager);
		if (id == null) {
			gen.writeStartArray();

			for (Object result : beanManager.search(userName, query, manager, userTransaction)) {
				if (result == null) {
					gen.writeNull();
				} else if (result instanceof EntityBaseBean) {
					gen.writeStartObject();
					gen.writeStartObject(result.getClass().getSimpleName());
					jsonise((EntityBaseBean) result, gen);
					gen.writeEnd();
					gen.writeEnd();
				} else if (result instanceof Long) {
					gen.write((Long) result);
				} else if (result instanceof Double) {
					if (Double.isNaN((double) result)) {
						gen.writeNull();
					} else {
						gen.write((Double) result);
					}
				} else if (result instanceof String) {
					gen.write((String) result);
				} else if (result instanceof Boolean) {
					gen.write((Boolean) result);
				} else {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
							"Don't know how to jsonise " + result.getClass());
				}
			}

			gen.writeEnd();
		} else {
			EntityBaseBean result = beanManager.get(userName, query, id, manager, userTransaction);
			gen.writeStartObject();
			gen.writeStartObject(result.getClass().getSimpleName());
			jsonise(result, gen);
			gen.writeEnd();
			gen.writeEnd();
		}

		gen.close();
		return baos.toString();
	}

	private void jsonise(EntityBaseBean bean, JsonGenerator gen) throws IcatException {

		gen.write("id", bean.getId()).write("createId", bean.getCreateId())
				.write("createTime", bean.getCreateTime().toString())
				.write("modId", bean.getModId()).write("modTime", bean.getModTime().toString());

		Class<? extends EntityBaseBean> klass = bean.getClass();
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		Set<Field> atts = eiHandler.getAttributes(klass);
		Set<Field> updaters = eiHandler.getSettersForUpdate(klass).keySet();

		for (Field field : eiHandler.getFields(klass)) {
			Object value = null;
			try {
				value = getters.get(field).invoke(bean);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " "
						+ e.getMessage());
			}
			if (value == null) {
				// Ignore null values
			} else if (atts.contains(field)) {
				String type = field.getType().getSimpleName();
				if (type.equals("String")) {
					gen.write(field.getName(), (String) value);
				} else if (type.equals("Integer")) {
					gen.write(field.getName(), value.toString());
				} else if (type.equals("Double")) {
					gen.write(field.getName(), value.toString());
				} else if (type.equals("Long")) {
					gen.write(field.getName(), value.toString());
				} else if (type.equals("boolean")) {
					gen.write(field.getName(), value.toString());
				} else if (field.getType().isEnum()) {
					gen.write(field.getName(), value.toString());
				} else if (type.equals("Date")) {
					synchronized (df8601) {
						gen.write(field.getName(), df8601.format((Date) value));
					}
				} else {
					throw new IcatException(IcatExceptionType.INTERNAL,
							"Don't know how to jsonise field of type " + type);
				}
			} else if (updaters.contains(field)) {
				gen.writeStartObject(field.getName());
				jsonise((EntityBaseBean) value, gen);
				gen.writeEnd();
			} else {
				gen.writeStartArray(field.getName());
				@SuppressWarnings("unchecked")
				List<EntityBaseBean> beans = (List<EntityBaseBean>) value;
				for (EntityBaseBean b : beans) {
					gen.writeStartObject();
					jsonise(b, gen);
					gen.writeEnd();
				}
				gen.writeEnd();
			}
		}
	}

	@POST
	@Path("port")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void importData(@Context HttpServletRequest request) throws IcatException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Multipart content expected");
		}

		ServletFileUpload upload = new ServletFileUpload();
		String jsonString = null;
		String name = null;

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
					porter.importData(jsonString, stream, manager, userTransaction);
				}
			}
		} catch (FileUploadException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@PostConstruct
	private void init() {
		authPlugins = propertyHandler.getAuthPlugins();
		lifetimeMinutes = propertyHandler.getLifetimeMinutes();
		rootUserNames = propertyHandler.getRootUserNames();
	}

	@POST
	@Path("session")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@Context HttpServletRequest request, @FormParam("json") String jsonString)
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
		return baos.toString();

	}

	@DELETE
	@Path("session/{sessionId}")
	public void logout(@PathParam("sessionId") String sessionId) throws IcatException {
		beanManager.logout(sessionId, manager, userTransaction);
	}

	@PUT
	@Path("session/{sessionId}")
	public void refresh(@PathParam("sessionId") String sessionId) throws IcatException {
		beanManager.refresh(sessionId, lifetimeMinutes, manager, userTransaction);
	}
}