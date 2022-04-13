package org.icatproject.core.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO see what functionality can live here, and possibly convert from abstract to a fully generic API
public abstract class SearchApi {

	protected static final Logger logger = LoggerFactory.getLogger(SearchApi.class);
	protected static SimpleDateFormat df;
	protected static String basePath = "";
	protected static String matchAllQuery;

	protected URI server;

	static {
		df = new SimpleDateFormat("yyyyMMddHHmm");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
	}

	static {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().writeStartObject("query").writeStartObject("match_all")
					.writeEnd().writeEnd().writeEnd();
		}
		matchAllQuery = baos.toString();
	}

	/**
	 * Converts String into Date object.
	 * 
	 * @param value String representing a Date in the format "yyyyMMddHHmm".
	 * @return Date object, or null if value was null.
	 * @throws java.text.ParseException
	 */
	protected static Date decodeDate(String value) throws java.text.ParseException {
		if (value == null) {
			return null;
		} else {
			synchronized (df) {
				return df.parse(value);
			}
		}
	}

	/**
	 * Converts String into number of ms since epoch.
	 * 
	 * @param value String representing a Date in the format "yyyyMMddHHmm".
	 * @return Number of ms since epoch, or null if value was null
	 * @throws java.text.ParseException
	 */
	protected static Long decodeTime(String value) throws java.text.ParseException {
		if (value == null) {
			return null;
		} else {
			synchronized (df) {
				return df.parse(value).getTime();
			}
		}
	}

	/**
	 * Converts Date object into String format.
	 * 
	 * @param dateValue Date object to be converted.
	 * @return String representing a Date in the format "yyyyMMddHHmm".
	 */
	protected static String encodeDate(Date dateValue) {
		if (dateValue == null) {
			return null;
		} else {
			synchronized (df) {
				return df.format(dateValue);
			}
		}
	}

	public static String encodeDeletion(EntityBaseBean bean) {
		String entityName = bean.getClass().getSimpleName();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().writeStartObject("delete");
			gen.write("_index", entityName).write("_id", bean.getId().toString());
			gen.writeEnd().writeEnd();
		}
		return baos.toString();
	}

	public static void encodeDouble(JsonGenerator gen, String name, Double value) {
		gen.write(name, value);
	}

	public static void encodeLong(JsonGenerator gen, String name, Date value) {
		gen.write(name, value.getTime());
	}

	public static String encodeOperation(String operation, EntityBaseBean bean) throws IcatException {
		Long icatId = bean.getId();
		if (icatId == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, bean.toString() + " had null id");
		}
		String entityName = bean.getClass().getSimpleName();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().writeStartObject(operation);
			gen.write("_index", entityName).write("_id", icatId.toString());
			gen.writeStartObject("doc");
			bean.getDoc(gen);
			gen.writeEnd().writeEnd().writeEnd();
		}
		return baos.toString();
	}

	public static void encodeString(JsonGenerator gen, String name, Long value) {
		gen.write(name, Long.toString(value));
	}

	public static void encodeString(JsonGenerator gen, String name, String value) {
		gen.write(name, value);
	}

	public static void encodeText(JsonGenerator gen, String name, String value) {
		if (value != null) {
			gen.write(name, value);
		}
	}

	public void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor)
			throws IcatException, IOException, URISyntaxException {
		// getBeanDocExecutor is not used for all implementations, but is
		// required for the @Override
		// TODO Change this string building fake JSON by hand
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Long id : ids) {
			EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);
			if (bean != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (JsonGenerator gen = Json.createGenerator(baos)) {
					gen.writeStartObject().writeStartObject("create");
					gen.write("_index", entityName).write("_id", bean.getId().toString());
					gen.writeStartObject("doc");
					bean.getDoc(gen);
					gen.writeEnd().writeEnd().writeEnd();
				}
				if (sb.length() != 1) {
					sb.append(',');
				}
				sb.append(baos.toString());
			}
		}
		sb.append("]");
		modify(sb.toString());
	}

	public String buildSearchAfter(ScoredEntityBaseBean lastBean, String sort) throws IcatException {
		if (sort != null && !sort.equals("")) {
			try (JsonReader reader = Json.createReader(new ByteArrayInputStream(sort.getBytes()))) {
				JsonObject object = reader.readObject();
				JsonArrayBuilder builder = Json.createArrayBuilder();
				for (String key : object.keySet()) {
					if (!lastBean.getSource().keySet().contains(key)) {
						throw new IcatException(IcatExceptionType.INTERNAL,
								"Cannot build searchAfter document from source as sorted field " + key + " missing.");
					}
					String value = lastBean.getSource().getString(key);
					builder.add(value);
				}
				return builder.build().toString();
			}
		} else {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			if (Float.isNaN(lastBean.getScore())) {
				throw new IcatException(IcatExceptionType.INTERNAL,
						"Cannot build searchAfter document from source as score was NaN.");
			}
			builder.add(lastBean.getScore());
			builder.add(lastBean.getEntityBaseBeanId());
			return builder.build().toString();
		}
	}

	public void clear() throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/_delete_by_query").build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(matchAllQuery));
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public void commit() throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/_refresh").build();
			logger.trace("Making call {}", uri);
			HttpPost httpPost = new HttpPost(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public void freeSearcher(String uid) throws IcatException {
		logger.info("Manually freeing searcher not supported, no request sent");
	}

	public abstract List<FacetDimension> facetSearch(String target, JsonObject facetQuery, int maxResults, int maxLabels)
			throws IcatException;

	public SearchResult getResults(JsonObject query, int maxResults) throws IcatException {
		return getResults(query, null, maxResults, null, Arrays.asList("id"));
	}

	public SearchResult getResults(JsonObject query, int maxResults, String sort) throws IcatException {
		return getResults(query, null, maxResults, sort, Arrays.asList("id"));
	}

	public SearchResult getResults(JsonObject query, String searchAfter, int blockSize, String sort,
			List<String> fields) throws IcatException {

		// return getResults(uid.toString(), query, blockSize);
		// TODO
		return null;
	}

	private SearchResult getResults(String uid, JsonObject query, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String index;
			Set<String> fields = query.keySet();
			if (fields.contains("target")) {
				index = query.getString("target");
			} else {
				index = query.getString("_all");
			}
			URI uri = new URIBuilder(server).setPath(basePath + "/" + index + "/_search").build();
			logger.trace("Making call {}", uri);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos)) {
				// TODO refactor some of this into re-usable building blocks
				gen.writeStartObject().writeStartObject("query").writeStartObject("bool");
				if (fields.contains("text")) {
					// TODO consider default field: this would need to be set by index, but the
					// default of * makes more sense to me...
					String text = query.getString("text");
					gen.writeStartObject("must").writeStartObject("simple_query_string").write("query", text).writeEnd()
							.writeEnd();
				}
				gen.writeStartArray("filter");
				Long lowerTime = null;
				Long upperTime = null;
				if (fields.contains("lower")) {
					lowerTime = decodeTime(query.getString("lower"));
				}
				if (fields.contains("upper")) {
					upperTime = decodeTime(query.getString("upper"));
				}
				if (lowerTime != null || upperTime != null) {
					gen.writeStartObject().writeStartObject("bool").writeStartArray("should");
					if (lowerTime != null) {
						gen.writeStartObject().writeStartObject("range").writeStartObject("date")
								.write("gte", lowerTime).writeEnd().writeEnd().writeEnd();
						gen.writeStartObject().writeStartObject("range").writeStartObject("startDate")
								.write("gte", lowerTime).writeEnd().writeEnd().writeEnd();
					}
					if (upperTime != null) {
						gen.writeStartObject().writeStartObject("range").writeStartObject("date")
								.write("lte", upperTime).writeEnd().writeEnd().writeEnd();
						gen.writeStartObject().writeStartObject("range").writeStartObject("endDate")
								.write("lte", upperTime).writeEnd().writeEnd().writeEnd();
					}
					gen.writeEnd().writeEnd().writeEnd();
				}
				if (fields.contains("user")) {
					String user = query.getString("user");
					gen.writeStartObject().writeStartObject("match").writeStartObject("userName").write("query", user)
							.write("operator", "and").writeEnd().writeEnd().writeEnd();
				}
				if (fields.contains("userFullName")) {
					String userFullName = query.getString("userFullName");
					gen.writeStartObject().writeStartObject("simple_query_string").write("query", userFullName)
							.writeStartArray("fields").write("userFullName").writeEnd().writeEnd().writeEnd();
				}
				if (fields.contains("samples")) {
					JsonArray samples = query.getJsonArray("samples");
					for (int i = 0; i < samples.size(); i++) {
						String sample = samples.getString(i);
						gen.writeStartObject().writeStartObject("simple_query_string").write("query", sample)
								.writeStartArray("fields").write("sampleText").writeEnd().writeEnd().writeEnd();
					}
				}
				if (fields.contains("parameters")) {
					for (JsonValue parameterValue : query.getJsonArray("parameters")) {
						JsonObject parameterObject = (JsonObject) parameterValue;
						String name = parameterObject.getString("name", null);
						String units = parameterObject.getString("units", null);
						String stringValue = parameterObject.getString("stringValue", null);
						Long lowerDate = decodeTime(parameterObject.getString("lowerDateValue", null));
						Long upperDate = decodeTime(parameterObject.getString("upperDateValue", null));
						JsonNumber lowerNumeric = parameterObject.getJsonNumber("lowerNumericValue");
						JsonNumber upperNumeric = parameterObject.getJsonNumber("upperNumericValue");
						gen.writeStartObject().writeStartObject("bool").writeStartArray("must");
						if (name != null) {
							gen.writeStartObject().writeStartObject("match").writeStartObject("parameterName")
									.write("query", name).write("operator", "and").writeEnd().writeEnd().writeEnd();
						}
						if (units != null) {
							gen.writeStartObject().writeStartObject("match").writeStartObject("parameterUnits")
									.write("query", units).write("operator", "and").writeEnd().writeEnd().writeEnd();
						}
						if (stringValue != null) {
							gen.writeStartObject().writeStartObject("match").writeStartObject("parameterStringValue")
									.write("query", stringValue).write("operator", "and").writeEnd().writeEnd()
									.writeEnd();
						} else if (lowerDate != null && upperDate != null) {
							gen.writeStartObject().writeStartObject("range").writeStartObject("parameterDateValue")
									.write("gte", lowerDate).write("lte", upperDate).writeEnd().writeEnd().writeEnd();
						} else if (lowerNumeric != null && upperNumeric != null) {
							gen.writeStartObject().writeStartObject("range").writeStartObject("parameterNumericValue")
									.write("gte", lowerNumeric).write("lte", upperNumeric).writeEnd().writeEnd()
									.writeEnd();
						}
						gen.writeEnd().writeEnd().writeEnd();
					}
				}
				gen.writeEnd().writeEnd().writeEnd().writeEnd();
			}
			// TODO build returned results
			SearchResult result = new SearchResult();
			List<ScoredEntityBaseBean> entities = result.getResults();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(baos.toString()));
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				JsonReader jsonReader = Json.createReader(response.getEntity().getContent());
				JsonObject jsonObject = jsonReader.readObject();
				JsonArray hits = jsonObject.getJsonObject("hits").getJsonArray("hits");
				for (JsonObject hit : hits.getValuesAs(JsonObject.class)) {
					entities.add(new ScoredEntityBaseBean(hit.getInt("_id"),
							hit.getJsonNumber("_score").bigDecimalValue().floatValue(), null)); // TODO
				}
			}
			return result;
		} catch (IOException | URISyntaxException | ParseException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public void lock(String entityName) throws IcatException {
		logger.info("Manually locking index not supported, no request sent");
	}

	public void unlock(String entityName) throws IcatException {
		logger.info("Manually unlocking index not supported, no request sent");
	}

	public void modify(String json) throws IcatException {
		// TODO replace other places with this format
		// TODO this assumes simple update/create with no relation
		// Format should be [{"index": "investigation", "id": "123", "document": {}},
		// ...]
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			logger.debug("modify: {}", json);
			StringBuilder sb = new StringBuilder();
			JsonReader jsonReader = Json.createReader(new StringReader(json));
			JsonArray outerArray = jsonReader.readArray();
			for (JsonObject operation : outerArray.getValuesAs(JsonObject.class)) {
				if (operation.containsKey("doc")) {
					JsonObject document = operation.getJsonObject("doc");
					operation.remove("doc");
					sb.append(operation.toString()).append("\n");
					sb.append(document.toString()).append("\n");
				} else {
					sb.append(operation.toString()).append("\n");
				}
			}
			URI uri = new URIBuilder(server).setPath(basePath + "/_bulk").build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(sb.toString()));
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	// TODO Remove?
	/**
	 * Legacy function for building a Query from individual arguments
	 * 
	 * @param target
	 * @param user
	 * @param text
	 * @param lower
	 * @param upper
	 * @param parameters
	 * @param samples
	 * @param userFullName
	 * @return
	 */
	public static JsonObject buildQuery(String target, String user, String text, Date lower, Date upper,
			List<ParameterPOJO> parameters, List<String> samples, String userFullName) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		if (target != null) {
			builder.add("target", target);
		}
		if (user != null) {
			builder.add("user", user);
		}
		if (text != null) {
			builder.add("text", text);
		}
		if (lower != null) {
			builder.add("lower", lower.getTime());
		}
		if (upper != null) {
			builder.add("upper", upper.getTime());
		}
		if (parameters != null && !parameters.isEmpty()) {
			JsonArrayBuilder parametersBuilder = Json.createArrayBuilder();
			for (ParameterPOJO parameter : parameters) {
				JsonObjectBuilder parameterBuilder = Json.createObjectBuilder();
				if (parameter.name != null) {
					parameterBuilder.add("name", parameter.name);
				}
				if (parameter.units != null) {
					parameterBuilder.add("units", parameter.units);
				}
				if (parameter.stringValue != null) {
					parameterBuilder.add("stringValue", parameter.stringValue);
				}
				if (parameter.lowerDateValue != null) {
					parameterBuilder.add("lowerDateValue", parameter.lowerDateValue.getTime());
				}
				if (parameter.upperDateValue != null) {
					parameterBuilder.add("upperDateValue", parameter.upperDateValue.getTime());
				}
				if (parameter.lowerNumericValue != null) {
					parameterBuilder.add("lowerNumericValue", parameter.lowerNumericValue);
				}
				if (parameter.upperNumericValue != null) {
					parameterBuilder.add("upperNumericValue", parameter.upperNumericValue);
				}
				parametersBuilder.add(parameterBuilder);
			}
			builder.add("parameters", parametersBuilder);
		}
		if (samples != null && !samples.isEmpty()) {
			JsonArrayBuilder samplesBuilder = Json.createArrayBuilder();
			for (String sample : samples) {
				samplesBuilder.add(sample);
			}
			builder.add("samples", samplesBuilder);
		}
		if (userFullName != null) {
			builder.add("userFullName", userFullName);
		}
		return builder.build();
	}
}
