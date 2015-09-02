package org.icatproject.exposed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;
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
import org.icatproject.core.manager.PropertyHandler.ExtendedAuthenticator;
import org.icatproject.core.manager.Transmitter;

@Path("/")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ICATRest {

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

	private Map<String, ExtendedAuthenticator> authPlugins;

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

	private int maxEntities;

	private void checkRoot(String sessionId) throws IcatException {
		String userId = beanManager.getUserName(sessionId, manager);
		if (!rootUserNames.contains(userId)) {
			throw new IcatException(IcatExceptionType.INSUFFICIENT_PRIVILEGES, "user must be in rootUserNames");
		}
	}

	/**
	 * Create one or more entities
	 * 
	 * @summary Create
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @param json
	 *            description of entities to create which takes the form
	 *            <code>[{"InvestigationType":{"facility":{"id":12042},"name":"ztype"}},{"Facility":{"name":"another
	 * 			fred"}}]</code> . It is a list of objects where each object has a name
	 *            which is the type of the entity and a value which is an object
	 *            with name value pairs where these names are the names of the
	 *            attributes and the values are either simple or they may be
	 *            objects themselves. In this case two entities are being
	 *            created an InvestigationType and a Facility with a name of
	 *            "another fred". The InvestigationType being created will
	 *            reference an existing facility with an id of 12042 and will
	 *            have a name of "ztype". For references to existing objects
	 *            only the "id" value need be set otherwise if child objects are
	 *            to be created at the same time then the "id" should not be set
	 *            but the other desired attributes should.
	 * 
	 * @return ids of created entities as a json string of the form <samp>[125,
	 *         126]</samp>
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@POST
	@Path("entityManager")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String create(@FormParam("sessionId") String sessionId, @FormParam("entities") String json)
			throws IcatException {
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

		List<CreateResponse> createResponses = beanManager.createMany(userName, entities, manager, userTransaction);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (CreateResponse createResponse : createResponses) {
				transmitter.processMessage(createResponse.getNotificationMessage());
				gen.write(createResponse.getPk());
			}
			gen.writeEnd();
		} catch (JMSException e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
		return baos.toString();
	}

	/**
	 * Export data from ICAT
	 * 
	 * @summary Export
	 * 
	 * @param jsonString
	 *            what to export which takes the form
	 *            <code>{"sessionId":"0d9a3706-80d4-4d29-9ff3-4d65d4308a24","query":"Facility",
	 * 			, "attributes":"ALL"</code> where query if specified is a normal ICAT
	 *            query which may have an INCLUDE clause. This is used to define
	 *            the metadata to export. If not present then the whole ICAT
	 *            will be exported.
	 *            <p>
	 *            The value "attributes" if not specified defaults to "USER". It
	 *            is not case sensitive and it defines which attributes to
	 *            consider:
	 *            </p>
	 *            <dl>
	 *            <dt>USER</dt>
	 *            <dd>values for modId, createId, modDate and createDate will
	 *            not appear in the output.</dd>
	 * 
	 *            <dt>ALL</dt>
	 *            <dd>all field values will be output.</dd>
	 *            </dl>
	 * 
	 * @return plain text in ICAT dump format
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("port")
	@Produces(MediaType.TEXT_PLAIN)
	public Response exportData(@QueryParam("json") String jsonString) throws IcatException {
		return porter.exportData(jsonString, manager, userTransaction);
	}

	/**
	 * This call is primarily for testing. Authorization is not done so you must
	 * be listed in rootUserNames to use this call.
	 * 
	 * @summary Execute line of jpql
	 * 
	 * @param sessionId
	 *            a sessionId of a user listed in rootUserNames
	 * @param query
	 *            the jpql
	 * 
	 * @return the first 5 entities that match the query as simple text for
	 *         testing
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("jpql")
	@Produces(MediaType.TEXT_PLAIN)
	public String getJpql(@QueryParam("sessionId") String sessionId, @QueryParam("query") String query)
			throws IcatException {
		checkRoot(sessionId);
		if (query == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "query is not set");
		}
		int nMax = 5;
		List<Object> os = manager.createQuery(query, Object.class).setMaxResults(nMax).getResultList();
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

	/**
	 * Return all that can be returned when not authenticated
	 * 
	 * @return a json string
	 */
	@GET
	@Path("properties")
	@Produces(MediaType.APPLICATION_JSON)
	public String getProperties() {

		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		JsonArrayBuilder authenticatorArrayBuilder = Json.createArrayBuilder();

		for (Entry<String, ExtendedAuthenticator> entry : authPlugins.entrySet()) {
			ExtendedAuthenticator extendedAuthenticator = entry.getValue();
			JsonObjectBuilder authenticatorBuilder = Json.createObjectBuilder();
			authenticatorBuilder.add("mnemonic", entry.getKey());
			JsonReader jsonReader = Json.createReader(new StringReader(extendedAuthenticator.getAuthenticator()
					.getDescription()));
			JsonObject description = jsonReader.readObject();
			jsonReader.close();
			authenticatorBuilder.add("description", description);
			if (extendedAuthenticator.isAdmin()) {
				authenticatorBuilder.add("admin", true);
			}
			if (extendedAuthenticator.getFriendly() != null) {
				authenticatorBuilder.add("friendly", extendedAuthenticator.getFriendly());
			}
			authenticatorArrayBuilder.add(authenticatorBuilder);
		}

		jsonBuilder.add("maxEntities", maxEntities).add("lifetimeMinutes", lifetimeMinutes)
				.add("authenticators", authenticatorArrayBuilder);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonWriter writer = Json.createWriter(baos);
		writer.writeObject(jsonBuilder.build());
		writer.close();
		return baos.toString();
	}

	/**
	 * Return information about a session
	 * 
	 * @summary getSession
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * 
	 * @return a json string with userName and remainingMinutes of the form
	 *         <samp>
	 *         {"userName":"db/root","remainingMinutes":117.87021666666666}
	 *         </samp>
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("session/{sessionId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSession(@PathParam("sessionId") String sessionId) throws IcatException {

		String userName = beanManager.getUserName(sessionId, manager);
		double remainingMinutes = beanManager.getRemainingMinutes(sessionId, manager);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("userName", userName).write("remainingMinutes", remainingMinutes).writeEnd();
		gen.close();
		return baos.toString();
	}

	/**
	 * return the version of the icat server
	 * 
	 * @summary getVersion
	 * 
	 * @return json string of the form: <samp>{"version":"4.4.0"}</samp>
	 */
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

	/**
	 * Import data
	 * 
	 * <p>
	 * The multipart form data starts with a form parameter with a ContentType
	 * of TEXT_PLAIN and a name of "json" and is followed by the data to be
	 * imported with a ContentType of APPLICATION_OCTET_STREAM.
	 * </p>
	 * 
	 * <p>
	 * The json takes the form:
	 * <code>"{sessionId", "0d9a3706-80d4-4d29-9ff3-4d65d4308a24",
			"duplicate", "THROW", "attributes", "USER"}</code> . The value
	 * "duplicate" if not specified defaults to "THROW". It is not case
	 * sensitive and it defines the action to be taken if a duplicate is found:
	 * </p>
	 * <dl>
	 * <dt>THROW</dt>
	 * <dd>throw an exception</dd>
	 * 
	 * <dt>IGNORE</dt>
	 * <dd>go to the next row</dd>
	 * 
	 * <dt>CHECK</dt>
	 * <dd>check that new data matches the old - and throw exception if it does
	 * not.</dd>
	 * 
	 * <dt>OVERWRITE</dt>
	 * <dd>replace old data with new</dd>
	 * </dl>
	 * 
	 * The value "attributes" if not specified defaults to "USER". It is not
	 * case sensitive and it defines which attributes to consider:
	 * 
	 * <dl>
	 * <dt>USER</dt>
	 * <dd>values for modId, createId, modDate and createDate provided in the
	 * input will be ignored.</dd>
	 * 
	 * <dt>ALL</dt>
	 * <dd>all field values specified will be respected. This option is only
	 * available to those specified in the rootUserNames in the icat.properties
	 * file.</dd>
	 * </dl>
	 * 
	 * 
	 * @summary importData
	 *
	 * @throws IcatException
	 *             when something is wrong
	 */
	@POST
	@Path("port")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void importData(@Context HttpServletRequest request) throws IcatException {
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
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Form field " + fieldName
								+ "is not recognised");
					}
				} else {
					if (name == null) {
						name = item.getName();
					}
					porter.importData(jsonString, stream, manager, userTransaction);
				}
			}
		} catch (FileUploadException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@PostConstruct
	private void init() {
		authPlugins = propertyHandler.getAuthPlugins();
		lifetimeMinutes = propertyHandler.getLifetimeMinutes();
		rootUserNames = propertyHandler.getRootUserNames();
		maxEntities = propertyHandler.getMaxEntities();
	}

	private void jsonise(EntityBaseBean bean, JsonGenerator gen) throws IcatException {
		synchronized (df8601) {
			gen.write("id", bean.getId()).write("createId", bean.getCreateId())
					.write("createTime", df8601.format(bean.getCreateTime())).write("modId", bean.getModId())
					.write("modTime", df8601.format(bean.getModTime()));
		}

		Class<? extends EntityBaseBean> klass = bean.getClass();
		Map<Field, Method> getters = eiHandler.getGetters(klass);
		Set<Field> atts = eiHandler.getAttributes(klass);
		Set<Field> updaters = eiHandler.getSettersForUpdate(klass).keySet();

		for (Field field : eiHandler.getFields(klass)) {
			Object value = null;
			try {
				value = getters.get(field).invoke(bean);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
			if (value == null) {
				// Ignore null values
			} else if (atts.contains(field)) {
				String type = field.getType().getSimpleName();
				if (type.equals("String")) {
					gen.write(field.getName(), (String) value);
				} else if (type.equals("Integer")) {
					gen.write(field.getName(), (Integer) value);
				} else if (type.equals("Double")) {
					gen.write(field.getName(), (Double) value);
				} else if (type.equals("Long")) {
					gen.write(field.getName(), (Long) value);
				} else if (type.equals("boolean")) {
					gen.write(field.getName(), (Boolean) value);
				} else if (field.getType().isEnum()) {
					gen.write(field.getName(), value.toString());
				} else if (type.equals("Date")) {
					synchronized (df8601) {
						gen.write(field.getName(), df8601.format((Date) value));
					}
				} else {
					throw new IcatException(IcatExceptionType.INTERNAL, "Don't know how to jsonise field of type "
							+ type);
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

	/**
	 * Login to create a session
	 * 
	 * @summary Login
	 * 
	 * @param request
	 * @param jsonString
	 *            with plugin and credentials which takes the form
	 *            <code>{"plugin":"db", "credentials[{"username":"root"},
				{"password":"guess"}]}</code>
	 * 
	 * @return json with sessionId of the form
	 *         <samp>{"sessionId","0d9a3706-80d4-4d29-9ff3-4d65d4308a24"}</samp>
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@POST
	@Path("session")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@Context HttpServletRequest request, @FormParam("json") String jsonString) throws IcatException {
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

		Authenticator authenticator = authPlugins.get(plugin).getAuthenticator();
		if (authenticator == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION, "Authenticator mnemonic " + plugin
					+ " not recognised");
		}
		logger.debug("Using " + plugin + " to authenticate");

		String userName = authenticator.authenticate(credentials, request.getRemoteAddr()).getUserName();
		String sessionId = beanManager.login(userName, lifetimeMinutes, manager, userTransaction);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("sessionId", sessionId).writeEnd();
		gen.close();
		return baos.toString();

	}

	/**
	 * Logout from a session
	 * 
	 * @summary logout
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@DELETE
	@Path("session/{sessionId}")
	public void logout(@PathParam("sessionId") String sessionId) throws IcatException {
		beanManager.logout(sessionId, manager, userTransaction);
	}

	/**
	 * perform a lucene search
	 * 
	 * @summary lucene
	 * 
	 * @param sessionId
	 *            a sessionId of a user
	 * @param query
	 *            json encoded query. One of the fields is "target" which may be
	 *            Investigation, Dataset or Datafile.
	 * @param maxCount
	 *            maximum number of entities to return
	 * 
	 * @return set of entitites encoded as json
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("lucene/data")
	@Produces(MediaType.APPLICATION_JSON)
	public String lucene(@QueryParam("sessionId") String sessionId, @QueryParam("query") String query,
			@QueryParam("maxCount") int maxCount) throws IcatException {
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
					} else if (parm.containsKey("lowerDateValue") && parm.containsKey("upperDateValue")) {
						parms.add(new ParameterPOJO(name, units, parm.getString("lowerDateValue"), parm
								.getString("upperDateValue")));
					} else if (parm.containsKey("lowerNumericValue") && parm.containsKey("upperNumericValue")) {
						parms.add(new ParameterPOJO(name, units, parm.getJsonNumber("lowerNumericValue").doubleValue(),
								parm.getJsonNumber("upperNumericValue").doubleValue()));
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

				objects = beanManager.luceneInvestigations(userName, user, text, lower, upper, parms, samples,
						userFullName, maxCount, manager, userTransaction);

			} else if (target.equals("Dataset")) {
				objects = beanManager.luceneDatasets(userName, user, text, lower, upper, parms, maxCount, manager,
						userTransaction);

			} else if (target.equals("Datafile")) {
				objects = beanManager.luceneDatafiles(userName, user, text, lower, upper, parms, maxCount, manager,
						userTransaction);

			} else {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, "target:" + target + " is not expected");
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

	/**
	 * Clear the lucene database
	 * 
	 * @summary luceneClear
	 * 
	 * @param sessionId
	 *            a sessionId of a user listed in rootUserNames
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@DELETE
	@Path("lucene/db")
	public void luceneClear(@QueryParam("sessionId") String sessionId) throws IcatException {
		checkRoot(sessionId);
		beanManager.luceneClear();
	}

	/**
	 * Forces a commit of the lucene database
	 * 
	 * @summary commit
	 * 
	 * @param sessionId
	 *            a sessionId of a user listed in rootUserNames
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@POST
	@Path("lucene/db")
	public void luceneCommit(@FormParam("sessionId") String sessionId) throws IcatException {
		checkRoot(sessionId);
		beanManager.luceneCommit();
	}

	/**
	 * Return a list of class names for which population is going on
	 * 
	 * @summary luceneGetPopulating
	 * 
	 * @param sessionId
	 *            a sessionId of a user listed in rootUserNames
	 * @return list of class names
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("lucene/db")
	@Produces(MediaType.APPLICATION_JSON)
	public String luceneGetPopulating(@QueryParam("sessionId") String sessionId) throws IcatException {
		checkRoot(sessionId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartArray();
		for (String name : beanManager.luceneGetPopulating()) {
			gen.write(name);
		}
		gen.writeEnd().close();
		return baos.toString();
	}

	/**
	 * Clear and repopulate lucene documents for the specified entityName
	 * 
	 * @summary lucenePopulate
	 * 
	 * @param sessionId
	 *            a sessionId of a user listed in rootUserNames
	 * @param entityName
	 *            the name of the entity
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@POST
	@Path("lucene/db/{entityName}")
	public void lucenePopulate(@FormParam("sessionId") String sessionId, @PathParam("entityName") String entityName)
			throws IcatException {
		checkRoot(sessionId);
		beanManager.lucenePopulate(entityName, manager);
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
					throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Field " + parser.getString()
							+ " not found in " + beanName);
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
								throw new IcatException(IcatExceptionType.BAD_PARAMETER, "Badly formatted date "
										+ parser.getString());
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
					List<EntityBaseBean> col = (List<EntityBaseBean>) getters.get(field).invoke(bean);
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
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| JsonException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getClass() + " " + e.getMessage() + " at "
					+ parser.getLocation().getStreamOffset() + " in json");
		}
		return bean;
	}

	/**
	 * Refresh session
	 * 
	 * @summary refresh
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@PUT
	@Path("session/{sessionId}")
	public void refresh(@PathParam("sessionId") String sessionId) throws IcatException {
		beanManager.refresh(sessionId, lifetimeMinutes, manager, userTransaction);
	}

	/**
	 * Return entities as a json string. This includes the functionality of both
	 * search and get calls in the SOAP web service.
	 * 
	 * @summary search/get
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @param query
	 *            specifies what to search for such as
	 *            <code>SELECT f FROM Facility f</code>
	 * @param id
	 *            it takes the form <code>732</code> and is used when the
	 *            functionality of get is required in which case the query must
	 *            be as described in the ICAT Java Client manual.
	 * 
	 * @return entities as a json string of the form
	 *         <samp>[{"Facility":{"id":126, "name":"another fred"}}]</samp> and
	 *         is a list of the objects returned and takes the same form as the
	 *         data passed in for create. If an id value is specified then only
	 *         one object can be returned so <em>the outer square brackets are
	 *         omitted</em>.
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("entityManager")
	@Produces(MediaType.APPLICATION_JSON)
	public String search(@QueryParam("sessionId") String sessionId, @QueryParam("query") String query,
			@QueryParam("id") Long id) throws IcatException {

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
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "Don't know how to jsonise "
							+ result.getClass());
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

}