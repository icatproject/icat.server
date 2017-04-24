package org.icatproject.exposed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
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
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.ParameterValueType;
import org.icatproject.core.manager.AccessType;
import org.icatproject.core.manager.EntityBeanManager;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.ParameterPOJO;
import org.icatproject.core.manager.Porter;
import org.icatproject.core.manager.PropertyHandler;
import org.icatproject.core.manager.PropertyHandler.ExtendedAuthenticator;
import org.icatproject.core.manager.ScoredEntityBaseBean;
import org.icatproject.utils.ContainerGetter.ContainerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ICATRest {

	private static Logger logger = LoggerFactory.getLogger(ICATRest.class);

	private final static DateFormat df8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

	private static SimpleDateFormat df;

	static {
		df = new SimpleDateFormat("yyyyMMddHHmm");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
	}

	private static Date dec(String value) throws java.text.ParseException {
		if (value == null) {
			return null;
		} else {
			synchronized (df) {
				return df.parse(value);
			}
		}
	}

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
	PropertyHandler propertyHandler;

	@Resource
	private UserTransaction userTransaction;

	private Set<String> rootUserNames;

	private int maxEntities;

	private ContainerType containerType;

	private Map<String, String> cluster;

	private void checkRoot(String sessionId) throws IcatException {
		String userId = beanManager.getUserName(sessionId, manager);
		if (!rootUserNames.contains(userId)) {
			throw new IcatException(IcatExceptionType.INSUFFICIENT_PRIVILEGES, "user must be in rootUserNames");
		}
	}

	/**
	 * Create one or more entities
	 * 
	 * @summary Write
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @param json
	 *            description of entities to create which takes the form
	 *            <code>[{"InvestigationType":{"facility":{"id":12042},"name":"ztype"}},{"Facility":{"name":"another
	 * 			fred"}}]</code> . It is a list of objects where each object has
	 *            a name which is the type of the entity and a value which is an
	 *            object with name value pairs where these names are the names
	 *            of the attributes and the values are either simple or they may
	 *            be objects themselves. In this case two entities are being
	 *            created an InvestigationType and a Facility with a name of
	 *            "another fred". The InvestigationType being created will
	 *            reference an existing facility with an id of 12042 and will
	 *            have a name of "ztype". For references to existing objects
	 *            only the "id" value need be set otherwise if child objects are
	 *            to be created at the same time then the "id" should not be set
	 *            but the other desired attributes should.
	 * 
	 *            This call can also perform updates as any object included
	 *            which has an id value provided has any other specified fields
	 *            updated.
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
	public String write(@Context HttpServletRequest request, @FormParam("sessionId") String sessionId,
			@FormParam("entities") String json) throws IcatException {

		String userName = beanManager.getUserName(sessionId, manager);

		List<Long> beanIds = beanManager.write(userName, json, manager, userTransaction, request.getRemoteAddr());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (Long id : beanIds) {
				gen.write(id);
			}
			gen.writeEnd();
		}
		return baos.toString();
	}

	/**
	 * Clone an entity
	 * 
	 * @summary Clone
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @param name
	 *            name of type of entity such as "Investigation"
	 * @param id
	 *            id of entity to be cloned
	 * @param keys
	 *            json string with keys to identify the clone which takes the
	 *            form <code>{"name":"anInvName", "visitId":"v42"]</code>. If
	 *            the entity type has more than one field to identify it then
	 *            any value not supplied in the map represented by the json
	 *            string will be taken from the object being cloned.
	 * 
	 * @return id of clone as a json string of the form <samp>{"id":126}</samp>
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@POST
	@Path("cloner")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String cloneEntity(@Context HttpServletRequest request, @FormParam("sessionId") String sessionId,
			@FormParam("name") String name, @FormParam("id") long id, @FormParam("keys") String keys)
			throws IcatException {

		String userName = beanManager.getUserName(sessionId, manager);

		long beanId = beanManager.cloneEntity(userName, name, id, keys, manager, userTransaction,
				request.getRemoteAddr());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("id", beanId).writeEnd();
		gen.close();
		return baos.toString();
	}

	/**
	 * Delete entities as a json string.
	 * 
	 * @summary delete
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @param json
	 *            specifies what to delete as a single entity or as an array of
	 *            entities such as <code>{"Facility": {"id" : 42}}</code> where
	 *            the id must be specified and no other attributes.
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@DELETE
	@Path("entityManager")
	@Produces(MediaType.APPLICATION_JSON)
	public void delete(@Context HttpServletRequest request, @QueryParam("sessionId") String sessionId,
			@QueryParam("entities") String json) throws IcatException {

		if (json == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "entities is not set");
		}

		List<EntityBaseBean> beans = new ArrayList<>();
		try (JsonReader reader = Json.createReader(new ByteArrayInputStream(json.getBytes()))) {
			JsonStructure top = reader.read();
			int offset = 0;
			if (top.getValueType() == ValueType.ARRAY) {
				for (JsonValue obj : (JsonArray) top) {
					beans.add(getOne((JsonObject) obj, offset++));
				}
			} else {
				beans.add(getOne((JsonObject) top, 0));
			}
		} catch (JsonException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getMessage() + " in json " + json);
		}
		String userName = beanManager.getUserName(sessionId, manager);
		beanManager.delete(userName, beans, manager, userTransaction, request.getRemoteAddr());
	}

	private EntityBaseBean getOne(JsonObject entity, int offset) throws IcatException {
		logger.debug("Get one {} for delete", entity);

		if (entity.size() != 1) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"entity must have one keyword followed by its values in json " + entity, offset);
		}
		Entry<String, JsonValue> entry = entity.entrySet().iterator().next();
		String beanName = entry.getKey();
		Class<EntityBaseBean> klass = EntityInfoHandler.getClass(beanName);
		JsonObject contents = (JsonObject) entry.getValue();

		EntityBaseBean bean = null;
		try {
			bean = klass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, "failed to instantiate " + beanName, offset);
		}

		for (Entry<String, JsonValue> pair : contents.entrySet()) {
			if (pair.getKey().equals("id")) {
				bean.setId(((JsonNumber) pair.getValue()).longValueExact());
			} else {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"entity must have only the id value specified in json " + entity, offset);
			}
		}

		if (bean.getId() == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"entity must have the id value specified in json " + entity, offset);
		}
		logger.trace("Got {} for delete", bean);
		return bean;
	}

	/**
	 * Export data from ICAT
	 * 
	 * @summary Export Metadata
	 * 
	 * @param jsonString
	 *            what to export which takes the form
	 *            <code>{"sessionId":"0d9a3706-80d4-4d29-9ff3-4d65d4308a24","query":"Facility",
	 * 			  "attributes":"ALL"}</code> where query if specified is a
	 *            normal ICAT query which may have an INCLUDE clause. This is
	 *            used to define the metadata to export. If not present then the
	 *            whole ICAT will be exported.
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

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public String list(@QueryParam("sessionId") String sessionId, @QueryParam("path") String wholePath)
			throws IcatException {

		List<String> files = null;
		List<String> dirs = null;
		if (wholePath.isEmpty()) {
			files = manager
					.createQuery("SELECT p.name FROM Path p where p.parent is null and p.file = true", String.class)
					.getResultList();

			dirs = manager
					.createQuery("SELECT p.name FROM Path p where p.parent is null and p.file = false", String.class)
					.getResultList();
		} else {
			org.icatproject.core.entity.Path path = null;
			for (java.nio.file.Path name : (new File(wholePath)).toPath()) {
				List<org.icatproject.core.entity.Path> paths;
				if (path == null) {
					paths = manager
							.createQuery("SELECT p FROM Path p where p.parent is null and p.name = :name",
									org.icatproject.core.entity.Path.class)
							.setParameter("name", name.toString()).getResultList();
				} else {
					paths = manager
							.createQuery("SELECT p FROM Path p where p.parent =:parent and p.name = :name",
									org.icatproject.core.entity.Path.class)
							.setParameter("parent", path).setParameter("name", name.toString()).getResultList();
				}
				if (paths.size() == 0) {
					throw new IcatException(IcatExceptionType.NO_SUCH_OBJECT_FOUND, "path not found");
				} else {
					path = paths.get(0);
				}
			}
			if (path.isFile()) {
				throw new IcatException(IcatExceptionType.NO_SUCH_OBJECT_FOUND, "path not found");
			}
			files = manager
					.createQuery("SELECT p.name FROM Path p where p.parent = :parent and p.file = true", String.class)
					.setParameter("parent", path).getResultList();
			dirs = manager
					.createQuery("SELECT p.name FROM Path p where p.parent = :parent and p.file = false", String.class)
					.setParameter("parent", path).getResultList();
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);

		gen.writeStartObject();
		gen.writeStartArray("files");
		String userName = beanManager.getUserName(sessionId, manager);
		java.nio.file.Path basePath = (new File(wholePath)).toPath();

		for (String file : files) {
			try {
				EntityBaseBean bean = manager
						.createQuery("SELECT df from Datafile df where df.location = :location", Datafile.class)
						.setParameter("location", basePath.resolve(file)).getSingleResult();
				gatekeeper.performAuthorisation(userName, bean, AccessType.READ, manager);
			} catch (Exception e) {
				// May fail because not there or not readable
			}
			gen.write(file);
		}
		gen.writeEnd();
		gen.writeStartArray("directories");
		for (String dir : dirs) {
			gen.write(dir);
		}
		gen.writeEnd();

		gen.writeEnd();

		gen.close();
		return baos.toString();

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
	 * @param max
	 *            if specified changes the number of entries to return from 5
	 * 
	 * @return the first entities that match the query as simple text for
	 *         testing
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("jpql")
	@Produces(MediaType.TEXT_PLAIN)
	public String getJpql(@QueryParam("sessionId") String sessionId, @QueryParam("query") String query,
			@QueryParam("max") Integer max) throws IcatException {
		checkRoot(sessionId);
		if (query == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "query is not set");
		}
		int nMax = 5;
		if (max != null) {
			nMax = max;
		}
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
			if (o.getClass().isArray()) {
				boolean firstInArray = true;
				sb.append('[');
				for (Object z : (Object[]) o) {
					if (!firstInArray) {
						sb.append(", ");
					} else {
						firstInArray = false;
					}
					sb.append(z);
				}
				sb.append(']');
			} else {
				sb.append(o);
			}
		}
		return sb.toString();
	}

	/**
	 * Return all that can be returned when not authenticated
	 * 
	 * @summary Properties
	 * 
	 * @return a json string
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("properties")
	@Produces(MediaType.APPLICATION_JSON)
	public String getProperties() throws IcatException {

		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		JsonArrayBuilder authenticatorArrayBuilder = Json.createArrayBuilder();

		for (Entry<String, ExtendedAuthenticator> entry : authPlugins.entrySet()) {
			ExtendedAuthenticator extendedAuthenticator = entry.getValue();
			JsonObjectBuilder authenticatorBuilder = Json.createObjectBuilder();
			authenticatorBuilder.add("mnemonic", entry.getKey());
			JsonReader jsonReader = Json
					.createReader(new StringReader(extendedAuthenticator.getAuthenticator().getDescription()));
			JsonObject description = jsonReader.readObject();
			jsonReader.close();
			authenticatorBuilder.add("keys", description.get("keys"));
			if (extendedAuthenticator.isAdmin()) {
				authenticatorBuilder.add("admin", true);
			}
			if (extendedAuthenticator.getFriendly() != null) {
				authenticatorBuilder.add("friendly", extendedAuthenticator.getFriendly());
			}
			authenticatorArrayBuilder.add(authenticatorBuilder);
		}

		jsonBuilder.add("maxEntities", maxEntities).add("lifetimeMinutes", lifetimeMinutes)
				.add("authenticators", authenticatorArrayBuilder).add("containerType", containerType.name());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonWriter writer = Json.createWriter(baos);
		writer.writeObject(jsonBuilder.build());
		writer.close();
		return baos.toString();
	}

	/**
	 * Return information about a session
	 * 
	 * @summary Session
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * 
	 * @return a json string with userName and remainingMinutes of the form
	 *         <samp> {"userName":"db/root","remainingMinutes":117.
	 *         87021666666666} </samp>
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
	 * Return whether or not a given user is logged in - i.e. has at least one
	 * unexpired session. This call should be used for a user logged in using an
	 * authentication plugin configured to not return the mnemonic.
	 * 
	 * @summary LoggedIn
	 * 
	 * @param userName
	 *            the name of the user (without mnemonic)
	 * 
	 * @return json string of the form: <samp>{"isLoggedIn":true}</samp>
	 */
	@GET
	@Path("user/{userName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String isLoggedIn1(@PathParam("userName") String userName) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().write("loggedIn", beanManager.isLoggedIn(userName, manager)).writeEnd();
		}
		return baos.toString();
	}

	/**
	 * Returns after specified number of seconds - returning elapsed time in
	 * milliseconds
	 * 
	 * @summary Sleep
	 * 
	 * @param seconds
	 *            how many seconds to wait before returning
	 * 
	 * @return json string of the form: <samp>{"slept": 20000}</samp>
	 */
	@GET
	@Path("sleep/{seconds}")
	@Produces(MediaType.APPLICATION_JSON)
	public String sleep(@PathParam("seconds") Long seconds) {
		long time = System.currentTimeMillis();
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// Do nothing
		}
		time = (System.currentTimeMillis() - time);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().write("slept", time).writeEnd();
		}
		return baos.toString();
	}

	/**
	 * Return whether or not a given user is logged in - i.e. has at least one
	 * unexpired session. This call should be used for a user logged in using an
	 * authentication plugin configured to return the mnemonic.
	 * 
	 * @summary LoggedIn
	 * 
	 * @param mnemonic
	 *            the mnemomnic used to identify the authentication plugin
	 * @param userName
	 *            the name of the user (without mnemonic)
	 * 
	 * @return json string of the form: <samp>{"isLoggedIn":true}</samp>
	 */
	@GET
	@Path("user/{mnemonic}/{userName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String isLoggedIn2(@PathParam("mnemonic") String mnemonic, @PathParam("userName") String userName) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().write("loggedIn", beanManager.isLoggedIn(mnemonic + "/" + userName, manager))
					.writeEnd();
		}
		return baos.toString();
	}

	/**
	 * return the version of the icat server
	 * 
	 * @summary Version
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
	 * <code>"{sessionId":"0d9a3706-80d4-4d29-9ff3-4d65d4308a24",
			"duplicate":"THROW", "attributes":"USER"}</code> . The value
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
	 * available to those specified in the rootUserNames in the run.properties
	 * file.</dd>
	 * </dl>
	 * 
	 * @summary import metadata
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
						throw new IcatException(IcatExceptionType.BAD_PARAMETER,
								"Form field '" + fieldName + "' is not recognised");
					}
				} else {
					if (name == null) {
						name = item.getName();
					}
					porter.importData(jsonString, stream, manager, userTransaction, request.getRemoteAddr());
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
		containerType = propertyHandler.getContainerType();
		cluster = propertyHandler.getCluster();
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
				} else if (type.equals("ParameterValueType")) {
					gen.write(field.getName(), ((ParameterValueType) value).name());
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

	private void jsonise(Object result, JsonGenerator gen) throws IcatException {
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
		} else if (result instanceof ParameterValueType) {
			gen.write(((ParameterValueType) result).name());
		} else if (result instanceof Date) {
			synchronized (df8601) {
				gen.write(df8601.format((Date) result));
			}
		} else {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL,
					"Don't know how to jsonise " + result.getClass());
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
	 *            <code>{"plugin":"db", "credentials:[{"username":"root"},
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
	public String login(@Context HttpServletRequest request, @FormParam("json") String jsonString)
			throws IcatException {
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

		ExtendedAuthenticator extendedAuthenticator = authPlugins.get(plugin);
		if (extendedAuthenticator == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Authenticator mnemonic " + plugin + " not recognised");
		}
		Authenticator authenticator = extendedAuthenticator.getAuthenticator();
		logger.debug("Using " + plugin + " to authenticate");

		String userName = authenticator.authenticate(credentials, request.getRemoteAddr()).getUserName();
		String sessionId = beanManager.login(userName, lifetimeMinutes, manager, userTransaction,
				request.getRemoteAddr());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("sessionId", sessionId).writeEnd();
		gen.close();
		return baos.toString();

	}

	/**
	 * Logout from a session
	 * 
	 * @summary Logout
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
	public void logout(@Context HttpServletRequest request, @PathParam("sessionId") String sessionId)
			throws IcatException {
		beanManager.logout(sessionId, manager, userTransaction, request.getRemoteAddr());
	}

	/**
	 * perform a lucene search
	 * 
	 * @summary lucene search
	 * 
	 * @param sessionId
	 *            a sessionId of a user which takes the form
	 *            <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @param query
	 *            json encoded query object. One of the fields is "target" which
	 *            must be "Investigation", "Dataset" or "Datafile". The other
	 *            fields are all optional:
	 *            <dl>
	 *            <dt>user</dt>
	 *            <dd>name of user as in the User table which may include a
	 *            prefix</dd>
	 *            <dt>text</dt>
	 *            <dd>some text occurring somewhere in the entity. This is
	 *            understood by the <a href=
	 *            "https://lucene.apache.org/core/4_10_2/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description"
	 *            >lucene parser</a> but avoid trying to use fields.</dd>
	 *            <dt>lower</dt>
	 *            <dd>earliest date to search for in the form
	 *            <code>201509030842</code> i.e. yyyyMMddHHmm using UTC as
	 *            timezone. In the case of an investigation or data set search
	 *            the date is compared with the start date and in the case of a
	 *            data file the date field is used.</dd>
	 *            <dt>upper</dt>
	 *            <dd>latest date to search for in the form
	 *            <code>201509030842</code> i.e. yyyyMMddHHmm using UTC as
	 *            timezone. In the case of an investigation or data set search
	 *            the date is compared with the end date and in the case of a
	 *            data file the date field is used.</dd>
	 *            <dt>parameters</dt>
	 *            <dd>this holds a list of json parameter objects all of which
	 *            must match. Parameters have the following fields, all of which
	 *            are optional:
	 *            <dl>
	 *            <dt>name</dt>
	 *            <dd>A wildcard search for a parameter with this name.
	 *            Supported wildcards are <code>*</code>, which matches any
	 *            character sequence (including the empty one), and
	 *            <code>?</code>, which matches any single character.
	 *            <code>\</code> is the escape character. Note this query can be
	 *            slow, as it needs to iterate over many terms. In order to
	 *            prevent extremely slow queries, a name should not start with
	 *            the wildcard <code>*</code></dd>
	 *            <dt>units</dt>
	 *            <dd>A wildcard search for a parameter with these units.
	 *            Supported wildcards are <code>*</code>, which matches any
	 *            character sequence (including the empty one), and
	 *            <code>?</code>, which matches any single character.
	 *            <code>\</code> is the escape character. Note this query can be
	 *            slow, as it needs to iterate over many terms. In order to
	 *            prevent extremely slow queries, units should not start with
	 *            the wildcard <code>*</code></dd>
	 *            <dt>stringValue</dt>
	 *            <dd>A wildcard search for a parameter stringValue. Supported
	 *            wildcards are <code>*</code>, which matches any character
	 *            sequence (including the empty one), and <code>?</code>, which
	 *            matches any single character. <code>\</code> is the escape
	 *            character. Note this query can be slow, as it needs to iterate
	 *            over many terms. In order to prevent extremely slow queries,
	 *            requested stringValues should not start with the wildcard
	 *            <code>*</code></dd>
	 *            <dt>lowerDateValue and upperDateValue</dt>
	 *            <dd>latest and highest date to search for in the form
	 *            <code>201509030842</code> i.e. yyyyMMddHHmm using UTC as
	 *            timezone. This should be used to search on parameters having a
	 *            dateValue. If only one bound is set the restriction has not
	 *            effect.</dd>
	 *            <dt>lowerNumericValue and upperNumericValue</dt>
	 *            <dd>This should be used to search on parameters having a
	 *            numericValue. If only one bound is set the restriction has not
	 *            effect.</dd>
	 *            </dl>
	 *            </dd>
	 *            <dt>samples</dt>
	 *            <dd>A json array of strings each of which must match text
	 *            found in a sample. This is understood by the <a href=
	 *            "https://lucene.apache.org/core/4_10_2/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description"
	 *            >lucene parser</a> but avoid trying to use fields. This is
	 *            only respected in the case of an investigation search.</dd>
	 *            <dt>userFullName</dt>
	 *            <dd>Full name of user in the User table which may contain
	 *            titles etc. Matching is done by the <a href=
	 *            "https://lucene.apache.org/core/4_10_2/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description"
	 *            >lucene parser</a> but avoid trying to use fields. This is
	 *            only respected in the case of an investigation search.</dd>
	 *            </dl>
	 * 
	 * @param maxCount
	 *            maximum number of entities to return
	 * 
	 * @return set of entities encoded as json
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("lucene/data")
	@Produces(MediaType.APPLICATION_JSON)
	public String lucene(@Context HttpServletRequest request, @QueryParam("sessionId") String sessionId,
			@QueryParam("query") String query, @QueryParam("maxCount") int maxCount) throws IcatException {
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
					String name = parm.getString("name", null);
					if (name == null) {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, "name not set in one of parameters");
					}
					String units = parm.getString("units", null);
					if (units == null) {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER,
								"units not set in parameter '" + name + "'");
					}
					if (parm.containsKey("stringValue")) {
						parms.add(new ParameterPOJO(name, units, parm.getString("stringValue")));
					} else if (parm.containsKey("lowerDateValue") && parm.containsKey("upperDateValue")) {
						synchronized (df) {
							parms.add(new ParameterPOJO(name, units, df.parse(parm.getString("lowerDateValue")),
									df.parse(parm.getString("upperDateValue"))));
						}
					} else if (parm.containsKey("lowerNumericValue") && parm.containsKey("upperNumericValue")) {
						parms.add(new ParameterPOJO(name, units, parm.getJsonNumber("lowerNumericValue").doubleValue(),
								parm.getJsonNumber("upperNumericValue").doubleValue()));
					} else {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, parm.toString());
					}
				}
			}
			List<ScoredEntityBaseBean> objects;

			if (target.equals("Investigation")) {
				List<String> samples = new ArrayList<>();
				if (jo.containsKey("samples")) {
					for (JsonValue val : jo.getJsonArray("samples")) {
						JsonString samp = (JsonString) val;
						samples.add(samp.getString());
					}
				}
				String userFullName = jo.getString("userFullName", null);
				objects = beanManager.luceneInvestigations(userName, user, text, dec(lower), dec(upper), parms, samples,
						userFullName, maxCount, manager, request.getRemoteAddr());

			} else if (target.equals("Dataset")) {
				objects = beanManager.luceneDatasets(userName, user, text, dec(lower), dec(upper), parms, maxCount,
						manager, request.getRemoteAddr());

			} else if (target.equals("Datafile")) {
				objects = beanManager.luceneDatafiles(userName, user, text, dec(lower), dec(upper), parms, maxCount,
						manager, request.getRemoteAddr());

			} else {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, "target:" + target + " is not expected");
			}
			JsonGenerator gen = Json.createGenerator(baos);
			gen.writeStartArray();
			for (ScoredEntityBaseBean sb : objects) {
				gen.writeStartObject();
				gen.write("id", sb.getEntityBaseBeanId());
				gen.write("score", sb.getScore());
				gen.writeEnd();
			}
			gen.writeEnd();
			gen.close();
			return baos.toString();
		} catch (JsonException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "JsonException " + e.getMessage());
		} catch (ParseException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "ParserException " + e.getMessage());
		}
	}

	/**
	 * This is an internal call made by one icat instance to another in the same
	 * cluster
	 * 
	 * @summary markPublicTablesStale
	 */
	@POST
	@Path("gatekeeper/markPublicTablesStale")
	public void gatekeeperMarkPublicTablesStale(@Context HttpServletRequest request) {
		logger.debug("Call to gatekeeper/markPublicTablesStale requested from {}", request.getRemoteAddr());
		if (!cluster.containsKey(request.getRemoteAddr())) {
			logger.warn("Call to gatekeeper/markPublicTablesStale made from {} is not allowed",
					request.getRemoteAddr());
		}
		gatekeeper.markPublicTablesStale();

	}

	/**
	 * This is an internal call made by one icat instance to another in the same
	 * cluster
	 * 
	 * @summary markPublicTablesStale
	 */
	@POST
	@Path("gatekeeper/markPublicStepsStale")
	public void gatekeeperMarkPublicStepsStale(@Context HttpServletRequest request) {
		logger.debug("Call to gatekeeper/markPublicStepsStale requested from {}", request.getRemoteAddr());
		if (!cluster.containsKey(request.getRemoteAddr())) {
			logger.warn("Call to gatekeeper/markPublicStepsStale made from {} is not allowed", request.getRemoteAddr());
		}
		gatekeeper.markPublicStepsStale();
	}

	/**
	 * Stop population of the lucene database if it is running.
	 * 
	 * @summary Lucene Clear
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
	 * @summary Lucene Commit
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
	 * @summary lucene GetPopulating
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
	 * Call for testing only. The call will take the time specified and then
	 * returns.
	 * 
	 * @summary wait
	 * 
	 * @param sessionId
	 *            a sessionId of a user listed in rootUserNames
	 * @throws IcatException
	 *             when something is wrong
	 */
	@POST
	@Path("waitMillis")
	public void waitMillis(@FormParam("sessionId") String sessionId, @FormParam("ms") long ms) throws IcatException {
		checkRoot(sessionId);
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	/**
	 * Clear and repopulate lucene documents for the specified entityName
	 * 
	 * @summary Lucene Populate
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
	@Path("lucene/db/{entityName}/{minid}")
	public void lucenePopulate(@FormParam("sessionId") String sessionId, @PathParam("entityName") String entityName,
			@PathParam("minid") long minid) throws IcatException {
		checkRoot(sessionId);
		beanManager.lucenePopulate(entityName, minid, manager);
	}

	/**
	 * Refresh session
	 * 
	 * @summary Refresh
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
	public void refresh(@Context HttpServletRequest request, @PathParam("sessionId") String sessionId)
			throws IcatException {
		beanManager.refresh(sessionId, lifetimeMinutes, manager, userTransaction, request.getRemoteAddr());
	}

	/**
	 * Return entities or selected values in tabular format as a json string.
	 * This includes the functionality of both search and get calls in the SOAP
	 * web service.
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
	 *            be as described in the ICAT Soap manual</a>.
	 * 
	 * @return entities or arrays of values as a json string. The query
	 *         <code>SELECT f FROM Facility f</code> might return
	 *         <samp>[{"Facility":{"id":126, "name":"another fred"
	 *         }},{"Facility":{"id":185, "name":"a fred"}} ]</samp> and is a
	 *         list of the objects returned and takes the same form as the data
	 *         passed in for create. The objects are fully self describing. If
	 *         more than one quantity is listed in the select clause then
	 *         instead of a single value being returned for each result an array
	 *         of values is returned. For example
	 *         <code>SELECT f.id, f.name FROM Facility f</code> might return:
	 *         <samp> [[126, "another fred"],[185, "a fred"]]</samp>. If an id
	 *         value is specified then only one object can be returned so
	 *         <em>the outer square brackets are omitted.</em>.
	 * 
	 * @throws IcatException
	 *             when something is wrong
	 */
	@GET
	@Path("entityManager")
	@Produces(MediaType.APPLICATION_JSON)
	public String search(@Context HttpServletRequest request, @QueryParam("sessionId") String sessionId,
			@QueryParam("query") String query, @QueryParam("id") Long id) throws IcatException {

		if (query == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, "query is not set");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);

		String userName = beanManager.getUserName(sessionId, manager);
		if (id == null) {
			gen.writeStartArray();
			for (Object result : beanManager.search(userName, query, manager, request.getRemoteAddr())) {
				if (result == null) {
					gen.writeNull();
				} else if (result.getClass().isArray()) {
					gen.writeStartArray();
					for (Object field : (Object[]) result) {
						jsonise(field, gen);
					}
					gen.writeEnd();
				} else {
					jsonise(result, gen);
				}

			}

			gen.writeEnd();
		} else {
			EntityBaseBean result = beanManager.get(userName, query, id, manager, request.getRemoteAddr());
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