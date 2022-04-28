package org.icatproject.core.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.measure.IncommensurableException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.format.MeasurementParseException;
import javax.persistence.EntityManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.SampleType;
import org.icatproject.core.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.unit.Units;

// TODO see what functionality can live here, and possibly convert from abstract to a fully generic API
public class SearchApi {
	// TODO this is a duplicate of icat.lucene code (for now...?)
	private static class ParentRelationship {
		public String parentName;
		public String joinName;
		public List<String> fields;

		public ParentRelationship(String parentName, String joinName, List<String> fields) {
			this.parentName = parentName;
			this.joinName = joinName;
			this.fields = fields;
		}
	}

	private static final SimpleUnitFormat unitFormat = SimpleUnitFormat.getInstance();
	protected static final Logger logger = LoggerFactory.getLogger(SearchApi.class);
	protected static SimpleDateFormat df;
	protected static String basePath = "";
	protected static JsonObject matchAllQuery = Json.createObjectBuilder().add("query", Json.createObjectBuilder()
			.add("match_all", Json.createObjectBuilder())).build();
	 // TODO synonym filter in the default_search ONLY
	private static JsonObject indexSettings = Json.createObjectBuilder().add("analysis", Json.createObjectBuilder()
			.add("analyzer", Json.createObjectBuilder()
					.add("default", Json.createObjectBuilder()
							.add("tokenizer", "classic").add("filter", Json.createArrayBuilder()
									.add("possessive_english").add("lowercase").add("porter_stem")))
					.add("default_search", Json.createObjectBuilder()
							.add("tokenizer", "classic").add("filter", Json.createArrayBuilder()
									.add("possessive_english").add("lowercase").add("porter_stem").add("synonym"))))
			.add("filter", Json.createObjectBuilder()
					.add("synonym", Json.createObjectBuilder()
							.add("type", "synonym").add("synonyms_path", "synonym.txt"))
					.add("possessive_english", Json.createObjectBuilder()
							.add("type", "stemmer").add("langauge", "possessive_english")))).build();
	protected static Set<String> indices = new HashSet<>();
	protected static Map<String, String> scripts = new HashMap<>();
	private static Map<String, List<ParentRelationship>> relationships = new HashMap<>();

	protected URI server;

	static {
		df = new SimpleDateFormat("yyyyMMddHHmm");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);

		unitFormat.alias(Units.CELSIUS, "celsius"); // TODO this should be generalised with the units we need

		indices.addAll(Arrays.asList("datafile", "dataset", "investigation"));

		scripts.put("delete_datafileparameter", buildChildrenScript("datafileparameter", false));
		scripts.put("update_datafileparameter", buildChildrenScript("datafileparameter", true));
		scripts.put("delete_datasetparameter", buildChildrenScript("datasetparameter", false));
		scripts.put("update_datasetparameter", buildChildrenScript("datasetparameter", true));
		scripts.put("delete_investigationparameter", buildChildrenScript("investigationparameter", false));
		scripts.put("update_investigationparameter", buildChildrenScript("investigationparameter", true));
		scripts.put("delete_investigationuser", buildChildrenScript("investigationuser", false));
		scripts.put("update_investigationuser", buildChildrenScript("investigationuser", true));
		scripts.put("create_investigationuser", buildCreateScript("investigationuser"));
		scripts.put("delete_sample", buildChildrenScript("sample", false));
		scripts.put("update_sample", buildChildrenScript("sample", true));
		scripts.put("delete_datafileparametertype", buildChildrenScript("datafileparameter", ParameterType.docFields, false));
		scripts.put("update_datafileparametertype", buildChildrenScript("datafileparameter", ParameterType.docFields, true));
		scripts.put("delete_datasetparametertype", buildChildrenScript("datasetparameter", ParameterType.docFields, false));
		scripts.put("update_datasetparametertype", buildChildrenScript("datasetparameter", ParameterType.docFields, true));
		scripts.put("delete_investigationparametertype", buildChildrenScript("investigationparameter", ParameterType.docFields, false));
		scripts.put("update_investigationparametertype", buildChildrenScript("investigationparameter", ParameterType.docFields, true));
		scripts.put("delete_user", buildChildrenScript("investigationuser", User.docFields, false));
		scripts.put("update_user", buildChildrenScript("investigationuser", User.docFields, true));
		scripts.put("delete_sampletype", buildChildrenScript("sample", SampleType.docFields, false));
		scripts.put("update_sampletype", buildChildrenScript("sample", SampleType.docFields, true));

		scripts.put("delete_datafileformat", buildChildScript("datafileformat", DatafileFormat.docFields, false));
		scripts.put("update_datafileformat", buildChildScript("datafileformat", DatafileFormat.docFields, true));
		scripts.put("delete_datasettype", buildChildScript("datasettype", DatasetType.docFields, false));
		scripts.put("update_datasettype", buildChildScript("datasettype", DatasetType.docFields, true));
		scripts.put("delete_investigationtype", buildChildScript("investigationtype", InvestigationType.docFields, false));
		scripts.put("update_investigationtype", buildChildScript("investigationtype", InvestigationType.docFields, true));
		scripts.put("delete_facility", buildChildScript("facility", Facility.docFields, false));
		scripts.put("update_facility", buildChildScript("facility", Facility.docFields, true));

		relationships.put("datafileparameter", Arrays.asList(
				new ParentRelationship("datafile", "datafile", new ArrayList<>())));
		relationships.put("datasetparameter", Arrays.asList(
				new ParentRelationship("dataset", "dataset", new ArrayList<>())));
		relationships.put("investigationparameter", Arrays.asList(
				new ParentRelationship("investigation", "investigation", new ArrayList<>())));
		relationships.put("investigationuser", Arrays.asList(
				new ParentRelationship("investigation", "investigation", new ArrayList<>())));
		relationships.put("sample", Arrays.asList(
				new ParentRelationship("investigation", "investigation", new ArrayList<>())));

		relationships.put("parametertype", Arrays.asList(
				new ParentRelationship("investigation", "investigationparameter", ParameterType.docFields),
				new ParentRelationship("dataset", "datasetparameter", ParameterType.docFields),
				new ParentRelationship("datafile", "datafileparameter", ParameterType.docFields)
				));
		relationships.put("user", Arrays.asList(
				new ParentRelationship("investigation", "investigationuser", User.docFields)));
		relationships.put("sampleType", Arrays.asList(
				new ParentRelationship("investigation", "sample", SampleType.docFields)));

		relationships.put("datafileformat", Arrays.asList(
				new ParentRelationship("datafile", "datafileFormat", DatafileFormat.docFields)));
		relationships.put("datasettype", Arrays.asList(
				new ParentRelationship("dataset", "type", DatasetType.docFields)));
		relationships.put("investigationtype", Arrays.asList(
				new ParentRelationship("investigation", "type", InvestigationType.docFields)));
		relationships.put("facility", Arrays.asList(
				new ParentRelationship("investigation", "facility", Facility.docFields)));
	}

	public SearchApi(URI server) {
		this.server = server;
	}

	private static String buildCreateScript(String target) {
		String source = "ctx._source." + target + " = params.doc";
		JsonObjectBuilder builder = Json.createObjectBuilder().add("lang", "painless").add("source", source);
		return Json.createObjectBuilder().add("script", builder).build().toString();
	}

	private static String buildChildrenScript(String target, boolean update) {
		String source = "if (ctx._source." + target + " != null) {List ids = new ArrayList(); ctx._source." + target + ".forEach(t -> ids.add(t.id)); if (ids.contains(params.id)) {ctx._source." + target + ".remove(ids.indexOf(params.id))}}";
		if (update) {
			source += "if (ctx._source." + target + " != null) {ctx._source." + target + ".addAll(params.doc);} else {ctx._source." + target + " = params.doc;}";
		}
		JsonObjectBuilder builder = Json.createObjectBuilder().add("lang", "painless").add("source", source);
		return Json.createObjectBuilder().add("script", builder).build().toString();
	}

	private static String buildChildrenScript(String target, List<String> docFields, boolean update) {
		String source = "int listIndex; if (ctx._source." + target + " != null) {List ids = new ArrayList(); ctx._source." + target + ".forEach(t -> ids.add(t.id)); if (ids.contains(params.id)) {listIndex = ids.indexOf(params.id)}}";
		String childSource = "ctx._source." + target + ".get(listIndex)";
		for (String field : docFields) {
			if (update) {
				if (field.equals("numericValueSI")) {
					source += "if ("+ childSource + ".numericValue != null && params.containsKey('conversionFactor')) {" + childSource + ".numericValueSI = params.conversionFactor * "+ childSource + ".numericValue;} else {" + childSource + ".remove('numericValueSI');}";
				} else {
					source += childSource + "['" + field + "']" + " = params['" + field + "']; ";
				}
			} else {
				source += childSource + ".remove('" + field + "'); ";
			}
		}
		JsonObjectBuilder builder = Json.createObjectBuilder().add("lang", "painless").add("source", source);
		return Json.createObjectBuilder().add("script", builder).build().toString();
	}

	private static String buildChildScript(String target, List<String> docFields, boolean update) {
		String source = "";
		for (String field : docFields) {
			if (update) {
				source += "ctx._source['" + field + "'] = params['" + field + "']; ";
			} else {
				source += "ctx._source.remove('" + field + "'); ";
			}
		}
		JsonObjectBuilder builder = Json.createObjectBuilder().add("lang", "painless").add("source", source);
		return Json.createObjectBuilder().add("script", builder).build().toString();
	}

	private static JsonObject buildMappings(String index) {
		JsonObject typeLong = Json.createObjectBuilder().add("type", "long").build();
		JsonObjectBuilder propertiesBuilder = Json.createObjectBuilder()
				.add("id", typeLong).add("investigationuser", buildNestedMapping("investigation.id", "user.id"));
		if (index.equals("investigation")) {
			propertiesBuilder.add("type.id", typeLong).add("facility.id", typeLong)
					.add("sample", buildNestedMapping("investigation.id", "type.id"))
					.add("investigationparameter", buildNestedMapping("investigation.id", "type.id"));
		} else if (index.equals("dataset")) {
			propertiesBuilder.add("investigation.id", typeLong).add("type.id", typeLong).add("sample.id", typeLong)
					.add("datasetparameter", buildNestedMapping("dataset.id", "type.id"));
		} else if (index.equals("datafile")) {
			propertiesBuilder.add("investigation.id", typeLong).add("datafileFormat.id", typeLong)
					.add("datafileparameter", buildNestedMapping("datafile.id", "type.id"));
		} 
		return Json.createObjectBuilder().add("properties", propertiesBuilder).build();
	}

	private static JsonObject buildNestedMapping(String... idFields) {
		JsonObject typeLong = Json.createObjectBuilder().add("type", "long").build();
		JsonObjectBuilder propertiesBuilder = Json.createObjectBuilder().add("id", typeLong);
		for (String idField : idFields) {
			propertiesBuilder.add(idField, typeLong);
		}
 		return Json.createObjectBuilder().add("type", "nested").add("properties", propertiesBuilder).build();
	}

	private static JsonObject buildMatchQuery(String field, String value) {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("query", value).add("operator", "and");
		JsonObjectBuilder matchBuilder = Json.createObjectBuilder().add(field + ".keyword", fieldBuilder);
		return Json.createObjectBuilder().add("match", matchBuilder).build();
	}

	private static JsonObject buildNestedQuery(String path, JsonObject... queryObjects) {
		JsonObject builtQueries = null;
		if (queryObjects.length == 0) {
			builtQueries = matchAllQuery;
		} else if (queryObjects.length == 1) {
			builtQueries = queryObjects[0];
		} else {
			JsonArrayBuilder filterBuilder = Json.createArrayBuilder();
			for (JsonObject queryObject : queryObjects) {
				filterBuilder.add(queryObject);
			}
			JsonObjectBuilder boolBuilder = Json.createObjectBuilder().add("filter", filterBuilder);
			builtQueries = Json.createObjectBuilder().add("bool", boolBuilder).build();
		}
		JsonObjectBuilder nestedBuilder = Json.createObjectBuilder().add("path", path).add("query", builtQueries);
		return Json.createObjectBuilder().add("nested", nestedBuilder).build();
	}

	private static JsonObject buildStringQuery(String value, String... fields) {
		JsonObjectBuilder queryStringBuilder = Json.createObjectBuilder().add("query", value);
		if (fields.length > 0) {
			JsonArrayBuilder fieldsBuilder = Json.createArrayBuilder();
			for (String field : fields) {
				fieldsBuilder.add(field);
			}
			queryStringBuilder.add("fields", fieldsBuilder);
		}
		return Json.createObjectBuilder().add("query_string", queryStringBuilder).build();
	}

	private static JsonObject buildTermQuery(String field, String value) {
		return Json.createObjectBuilder().add("term", Json.createObjectBuilder().add(field, value)).build();
	}

	private static JsonObject buildTermsQuery(String field, JsonArray values) {
		return Json.createObjectBuilder().add("terms", Json.createObjectBuilder().add(field, values)).build();
	}

	private static JsonObject buildLongRangeQuery(String field, Long lowerValue, Long upperValue) {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("gte", lowerValue).add("lte", upperValue);
		JsonObjectBuilder rangeBuilder = Json.createObjectBuilder().add(field, fieldBuilder);
		return Json.createObjectBuilder().add("range", rangeBuilder).build();
	}

	private static JsonObject buildRangeQuery(String field, JsonNumber lowerValue, JsonNumber upperValue) {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("gte", lowerValue).add("lte", upperValue);
		JsonObjectBuilder rangeBuilder = Json.createObjectBuilder().add(field, fieldBuilder);
		return Json.createObjectBuilder().add("range", rangeBuilder).build();
	}

	// TODO (mostly) duplicated code from icat.lucene...
	private static Long parseDate(JsonObject jsonObject, String key, int offset, Long defaultValue) throws IcatException {
		if (jsonObject.containsKey(key)) {
			ValueType valueType = jsonObject.get(key).getValueType();
			switch (valueType) {
				case STRING:
					String dateString = jsonObject.getString(key);
					try {
						return decodeTime(dateString) + offset;
					} catch (Exception e) {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER, 
								"Could not parse date " + dateString + " using expected format yyyyMMddHHmm");
					}
				case NUMBER:
					return jsonObject.getJsonNumber(key).longValueExact();
				default:
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, 
							"Dates should be represented by a NUMBER or STRING JsonValue, but got " + valueType);
			}
		}
		return defaultValue;
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (Long id : ids) {
				EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);
				if (bean != null) {
					gen.writeStartObject().writeStartObject("create");
					gen.write("_index", entityName).write("_id", bean.getId().toString());
					gen.writeStartObject("doc");
					bean.getDoc(gen);
					gen.writeEnd().writeEnd().writeEnd();
				}
			}
			gen.writeEnd();
		}
		modify(baos.toString());
	}

	public JsonValue buildSearchAfter(ScoredEntityBaseBean lastBean, String sort) throws IcatException {
		if (sort != null && !sort.equals("")) {
			try (JsonReader reader = Json.createReader(new StringReader(sort))) {
				JsonObject object = reader.readObject();
				JsonArrayBuilder builder = Json.createArrayBuilder();
				for (String key : object.keySet()) {
					if (!lastBean.getSource().keySet().contains(key)) {
						throw new IcatException(IcatExceptionType.INTERNAL,
								"Cannot build searchAfter document from source as sorted field " + key + " missing.");
					}
					JsonValue value = lastBean.getSource().get(key);
					builder.add(value);
				}
				builder.add(lastBean.getEntityBaseBeanId());
				return builder.build();
			}
		} else {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			if (Float.isNaN(lastBean.getScore())) {
				throw new IcatException(IcatExceptionType.INTERNAL,
						"Cannot build searchAfter document from source as score was NaN.");
			}
			builder.add(lastBean.getScore());
			builder.add(lastBean.getEntityBaseBeanId());
			return builder.build();
		}
	}

	public void clear() throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/_all/_delete_by_query").build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(matchAllQuery.toString(), ContentType.APPLICATION_JSON));
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

	public List<FacetDimension> facetSearch(String target, JsonObject facetQuery, Integer maxResults,
			int maxLabels) throws IcatException {
		List<FacetDimension> results = new ArrayList<>();
		if (!facetQuery.containsKey("dimensions")) {
			return results;
		}
		String dimensionPrefix = "";
		String index = target.toLowerCase();
		if (relationships.containsKey(index)) {
			dimensionPrefix = index + ".";
			index = relationships.get(index).get(0).parentName;
		}
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URIBuilder builder = new URIBuilder(server).setPath(basePath + "/" + index + "/_search");
			builder.addParameter("size", maxResults.toString());
			URI uri = builder.build();
			logger.trace("Making call {}", uri);
			JsonObject queryObject = facetQuery.getJsonObject("query");
			JsonObjectBuilder bodyBuilder = parseQuery(queryObject, null, null, index, queryObject.keySet());

			JsonObjectBuilder aggsBuilder = Json.createObjectBuilder();
			for (JsonObject dimension : facetQuery.getJsonArray("dimensions").getValuesAs(JsonObject.class)) {
				String dimensionString = dimension.getString("dimension");
				if (dimension.containsKey("ranges")) {
					aggsBuilder.add(dimensionString, Json.createObjectBuilder().add("range", Json.createObjectBuilder()
							.add("field", dimensionPrefix + dimensionString).add("keyed", true).add("ranges", dimension.getJsonArray("ranges"))));
				} else {
					aggsBuilder.add(dimensionString, Json.createObjectBuilder().add("terms", Json.createObjectBuilder()
							.add("field", dimensionPrefix + dimensionString + ".keyword").add("size", maxLabels)));
				}
			}
			if (dimensionPrefix.equals("")) {
				bodyBuilder.add("aggs", aggsBuilder);
			} else {
				bodyBuilder.add("aggs", Json.createObjectBuilder()
						.add(dimensionPrefix.substring(0, dimensionPrefix.length() - 1), Json.createObjectBuilder()
								.add("nested", Json.createObjectBuilder()
										.add("path", dimensionPrefix.substring(0, dimensionPrefix.length() - 1)))
								.add("aggs", aggsBuilder)));
			}
			String body = bodyBuilder.build().toString();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			logger.trace("Body: {}", body);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				JsonReader jsonReader = Json.createReader(response.getEntity().getContent());
				JsonObject jsonObject = jsonReader.readObject();
				logger.trace("facet response: {}", jsonObject);
				JsonObject aggregations = jsonObject.getJsonObject("aggregations");
				
				if (!dimensionPrefix.equals("")) {
					aggregations = aggregations.getJsonObject(dimensionPrefix.substring(0, dimensionPrefix.length() - 1));
				}

				for (String dimension : aggregations.keySet()) {
					if (dimension.equals("doc_count")) {
						continue;
					}
					FacetDimension facetDimension = new FacetDimension(target, dimension);
					List<FacetLabel> facets = facetDimension.getFacets();
					JsonObject aggregation = aggregations.getJsonObject(dimension);
					JsonValue bucketsValue = aggregation.get("buckets");
					switch (bucketsValue.getValueType()) {
						case ARRAY:
							List<JsonObject> buckets = aggregation.getJsonArray("buckets").getValuesAs(JsonObject.class);
							if (buckets.size() == 0) {
								continue;
							}
							for (JsonObject bucket : buckets) {
								long docCount = bucket.getJsonNumber("doc_count").longValueExact();
								facets.add(new FacetLabel(bucket.getString("key"), docCount));
							}
							break;
						case OBJECT:
							JsonObject bucketsObject = aggregation.getJsonObject("buckets");
							Set<String> keySet = bucketsObject.keySet();
							if (keySet.size() == 0) {
								continue;
							}
							for (String key : keySet) {
								JsonObject bucket = bucketsObject.getJsonObject(key);
								long docCount = bucket.getJsonNumber("doc_count").longValueExact();
								facets.add(new FacetLabel(key, docCount));
							}
							break;
						default:
							String msg = "Excpeted 'buckets' to have ARRAY or OBJECT type, but it was "
									+ bucketsValue.getValueType();
							throw new IcatException(IcatExceptionType.INTERNAL, msg);
					}
					results.add(facetDimension);
				}
			}
			return results;
		} catch (IOException | URISyntaxException | ParseException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public SearchResult getResults(JsonObject query, int maxResults) throws IcatException {
		return getResults(query, null, maxResults, null, Arrays.asList("id"));
	}

	public SearchResult getResults(JsonObject query, int maxResults, String sort) throws IcatException {
		return getResults(query, null, maxResults, sort, Arrays.asList("id"));
	}

	public SearchResult getResults(JsonObject query, JsonValue searchAfter, Integer blockSize, String sort,
			List<String> requestedFields) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String index;
			Set<String> queryFields = query.keySet();
			if (queryFields.contains("target")) {
				index = query.getString("target").toLowerCase();
			} else {
				index = query.getString("_all");
			}
			URIBuilder builder = new URIBuilder(server).setPath(basePath + "/" + index + "/_search");
			StringBuilder sb = new StringBuilder();
			requestedFields.forEach(f -> sb.append(f).append(","));
			builder.addParameter("_source", sb.toString());
			builder.addParameter("size", blockSize.toString());
			URI uri = builder.build();
			logger.trace("Making call {}", uri);

			String body = parseQuery(query, searchAfter, sort, index, queryFields).build().toString();
			SearchResult result = new SearchResult();
			List<ScoredEntityBaseBean> entities = result.getResults();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			logger.trace("Body: {}", body);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				JsonReader jsonReader = Json.createReader(response.getEntity().getContent());
				JsonObject jsonObject = jsonReader.readObject();
				JsonArray hits = jsonObject.getJsonObject("hits").getJsonArray("hits");
				for (JsonObject hit : hits.getValuesAs(JsonObject.class)) {
					logger.trace("Hit: {}", hit.toString());
					Float score = Float.NaN;
					if (!hit.isNull("_score")) {
						score = hit.getJsonNumber("_score").bigDecimalValue().floatValue();
					}
					Integer id = new Integer(hit.getString("_id"));
					entities.add(new ScoredEntityBaseBean(id, score, hit.getJsonObject("_source")));
				}
				if (hits.size() == blockSize) {
					JsonObject lastHit = hits.getJsonObject(blockSize - 1);
					logger.trace("Building searchAfter from {}", lastHit.toString());
					if (lastHit.containsKey("sort")) {
						result.setSearchAfter(lastHit.getJsonArray("sort"));
					} else {
						ScoredEntityBaseBean lastEntity = entities.get(blockSize - 1);
						result.setSearchAfter(Json.createArrayBuilder().add(lastEntity.getScore())
								.add(lastEntity.getEntityBaseBeanId()).build());
					}
				}
			}
			return result;
		} catch (IOException | URISyntaxException | ParseException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private JsonObjectBuilder parseQuery(JsonObject query, JsonValue searchAfter, String sort, String index,
			Set<String> queryFields) throws IcatException, ParseException {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		if (sort == null || sort.equals("")) {
			builder.add("sort", Json.createArrayBuilder()
					.add(Json.createObjectBuilder().add("_score", "desc"))
					.add(Json.createObjectBuilder().add("id", "asc")).build());
		} else {
			JsonObject sortObject = Json.createReader(new StringReader(sort)).readObject();
			JsonArrayBuilder sortArrayBuilder = Json.createArrayBuilder();
			for (String key : sortObject.keySet()) {
				if (key.toLowerCase().contains("date")) {
					sortArrayBuilder.add(Json.createObjectBuilder().add(key, sortObject.getString(key)));
				} else {
					sortArrayBuilder.add(Json.createObjectBuilder()
							.add(key + ".keyword", sortObject.getString(key)));
				}
			}
			builder.add("sort", sortArrayBuilder.add(Json.createObjectBuilder().add("id", "asc")).build());
		}
		if (searchAfter != null) {
			builder.add("search_after", searchAfter);
		}
		JsonObjectBuilder queryBuilder = Json.createObjectBuilder();
		JsonObjectBuilder boolBuilder = Json.createObjectBuilder();
		JsonArrayBuilder filterBuilder = Json.createArrayBuilder();
		if (queryFields.contains("text")) {
			JsonArrayBuilder mustBuilder = Json.createArrayBuilder();
			mustBuilder.add(buildStringQuery(query.getString("text")));
			boolBuilder.add("must", mustBuilder);
		}
		Long lowerTime = parseDate(query, "lower", 0, Long.MIN_VALUE);
		Long upperTime = parseDate(query, "upper", 59999, Long.MAX_VALUE);
		if (lowerTime != Long.MIN_VALUE || upperTime != Long.MAX_VALUE) {
			if (index.equals("datafile")) {
				// datafile has only one date field
				filterBuilder.add(buildLongRangeQuery("date", lowerTime, upperTime));
			} else {
				filterBuilder.add(buildLongRangeQuery("startDate", lowerTime, upperTime));
				filterBuilder.add(buildLongRangeQuery("endDate", lowerTime, upperTime));
			}
		}

		List<JsonObject> investigationUserQueries = new ArrayList<>();
		if (queryFields.contains("user")) {
			investigationUserQueries.add(buildMatchQuery("investigationuser.user.name", query.getString("user")));
		}
		if (queryFields.contains("userFullName")) {
			investigationUserQueries.add(buildStringQuery(query.getString("userFullName"), "investigationuser.user.fullName"));
		}
		if (investigationUserQueries.size() > 0) {
			filterBuilder.add(buildNestedQuery("investigationuser", investigationUserQueries.toArray(new JsonObject[0])));
		}

		if (queryFields.contains("samples")) {
			JsonArray samples = query.getJsonArray("samples");
			for (int i = 0; i < samples.size(); i++) {
				String sample = samples.getString(i);
				filterBuilder.add(buildNestedQuery("sample", buildStringQuery(sample, "sample.name", "sample.type.name")));
			}
		}
		if (queryFields.contains("parameters")) {
			for (JsonValue parameterValue : query.getJsonArray("parameters")) {
				JsonObject parameterObject = (JsonObject) parameterValue;
				String name = parameterObject.getString("name", null);
				String units = parameterObject.getString("units", null);
				String stringValue = parameterObject.getString("stringValue", null);
				Long lowerDate = parseDate(parameterObject, "lowerDateValue", 0, null);
				Long upperDate = parseDate(parameterObject, "upperDateValue", 59999, null);
				JsonNumber lowerNumeric = parameterObject.getJsonNumber("lowerNumericValue");
				JsonNumber upperNumeric = parameterObject.getJsonNumber("upperNumericValue");

				String path = index + "parameter";
				List<JsonObject> parameterQueries = new ArrayList<>();
				if (name != null) {
					parameterQueries.add(buildMatchQuery(path + ".type.name", name));
				}
				if (units != null) {
					parameterQueries.add(buildMatchQuery(path + ".type.units", units));
				}
				if (stringValue != null) {
					parameterQueries.add(buildMatchQuery(path + ".stringValue", stringValue));
				} else if (lowerDate != null && upperDate != null) {
					parameterQueries.add(buildLongRangeQuery(path + ".dateTimeValue", lowerDate, upperDate));
				} else if (lowerNumeric != null && upperNumeric != null) {
					parameterQueries.add(buildRangeQuery(path + ".numericValue", lowerNumeric, upperNumeric));
				}
				filterBuilder.add(buildNestedQuery(path, parameterQueries.toArray(new JsonObject[0])));
			}
		}

		if (queryFields.contains("id")) {
			filterBuilder.add(buildTermsQuery("id", query.getJsonArray("id")));
		}

		JsonArray filterArray = filterBuilder.build();
		if (filterArray.size() > 0) {
			boolBuilder.add("filter", filterArray);
		}
		return builder.add("query", queryBuilder.add("bool", boolBuilder));
	}

	public void initMappings() throws IcatException {
		for (String index : indices) {
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				URI uri = new URIBuilder(server).setPath(basePath + "/" + index).build();
				logger.trace("Making call {}", uri);
				HttpHead httpHead = new HttpHead(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpHead)) {
					int statusCode = response.getStatusLine().getStatusCode();
					// If the index isn't present, we should get 404 and create the index
					if (statusCode == 200) {
						// If the index already exists (200), do not attempt to create it
						logger.trace("{} index already exists, continue", index);
						continue;
					} else if (statusCode != 404) {
						// If the code isn't 200 or 404, something has gone wrong
						Rest.checkStatus(response, IcatExceptionType.INTERNAL);
					}
				}
			} catch (URISyntaxException | IOException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}

			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				URI uri = new URIBuilder(server).setPath(basePath + "/" + index).build();
				HttpPut httpPut = new HttpPut(uri);
				String body = Json.createObjectBuilder()
						.add("settings", indexSettings).add("mappings", buildMappings(index)).build().toString();
				logger.trace("Making call {} with body {}", uri, body);
				httpPut.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
				try (CloseableHttpResponse response = httpclient.execute(httpPut)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			} catch (URISyntaxException | IOException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		}
	}

	public void initScripts() throws IcatException {
		for (String scriptKey : scripts.keySet()) {
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				URI uri = new URIBuilder(server).setPath(basePath + "/_scripts/" + scriptKey).build();
				logger.trace("Making call {}", uri);
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setEntity(new StringEntity(scripts.get(scriptKey), ContentType.APPLICATION_JSON));
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			} catch (URISyntaxException | IOException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
			}
		}
	}

	public void lock(String entityName) throws IcatException {
		logger.info("Manually locking index not supported, no request sent");
	}

	public void unlock(String entityName) throws IcatException {
		logger.info("Manually unlocking index not supported, no request sent");
	}

	public void modify(String json) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			logger.debug("modify: {}", json);
			List<HttpPost> updatesByQuery = new ArrayList<>();
			Set<String> investigationIds = new HashSet<>();
			StringBuilder sb = new StringBuilder();
			JsonReader jsonReader = Json.createReader(new StringReader(json));
			JsonArray outerArray = jsonReader.readArray();
			for (JsonObject operation : outerArray.getValuesAs(JsonObject.class)) {
				String operationKey;
				if (operation.containsKey("create")) {
					operationKey = "create";
				} else if (operation.containsKey("update")) {
					operationKey = "update";
				} else if (operation.containsKey("delete")) {
					operationKey = "delete";
				} else {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"Operation type should be 'create', 'update' or 'delete'");
				}
				JsonObject innerOperation = operation.getJsonObject(operationKey);
				String index = innerOperation.getString("_index").toLowerCase();
				String id = innerOperation.getString("_id");
				if (relationships.containsKey(index)) {
					for (ParentRelationship relation : relationships.get(index)) {
						if (operationKey.equals("create") && relation.parentName.equals(relation.joinName)) {
							// Don't need it for children of a relative
							// Do need it for 0:* relatives, in which case it's just appending to a list of nested objects
							JsonObject document = innerOperation.getJsonObject("doc");
							// TODO if the document has type.unit (it's a parameter) then we need to convert units here. Advantage being we (might) have the value
							if (document.containsKey("type.units")) {
								// Need to rebuild the document...
								JsonObjectBuilder rebuilder = Json.createObjectBuilder();
								for (String key : document.keySet()) {
									rebuilder.add(key, document.get(key));
								}
								String unitString = document.getString("type.units");
								try {
									Unit<?> unit = unitFormat.parse(unitString);
									Unit<?> systemUnit = unit.getSystemUnit();
									rebuilder.add("type.unitsSI", systemUnit.getName());
									if (document.containsKey("numericValue")) {
										double numericValue = document.getJsonNumber("numericValue").doubleValue();
										UnitConverter converter = unit.getConverterToAny(systemUnit);
										rebuilder.add("numericValueSI", converter.convert(numericValue));
									}
								} catch (IncommensurableException | MeasurementParseException e) {
									logger.error("Unable to convert 'type.units' of {} due to {}", unitString, e.getMessage());
								}
								document = rebuilder.build();
							}
							String parentId = document.getString(relation.parentName + ".id");
							JsonObjectBuilder innerBuilder = Json.createObjectBuilder().add("_id",
									parentId).add("_index", relation.parentName);
							JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("id", id).add("doc", Json.createArrayBuilder().add(document));
							String scriptId = (index.equals("parametertype")) ? "update_" + relation.parentName + index : "update_" + index;
							JsonObjectBuilder scriptBuilder = Json.createObjectBuilder().add("id", scriptId)
									.add("params", paramsBuilder);
							sb.append(Json.createObjectBuilder().add("update",
									innerBuilder).build().toString()).append("\n");
							sb.append(Json.createObjectBuilder()
									.add("upsert", Json.createObjectBuilder().add(index, Json.createArrayBuilder().add(document)))
									.add("script", scriptBuilder).build().toString()).append("\n");
						} else if (!operationKey.equals("delete")) {
							JsonObject document = innerOperation.getJsonObject("doc");
							URI uri = new URIBuilder(server)
									.setPath(basePath + "/" + relation.parentName + "/_update_by_query").build();
							HttpPost httpPost = new HttpPost(uri);
							JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("id", id);
							if (relation.fields.size() == 0) {
								// TODO duplicated
								if (document.containsKey("type.units")) {
									// Need to rebuild the document...
									JsonObjectBuilder rebuilder = Json.createObjectBuilder();
									for (String key : document.keySet()) {
										rebuilder.add(key, document.get(key));
									}
									String unitString = document.getString("type.units");
									try {
										Unit<?> unit = unitFormat.parse(unitString);
										Unit<?> systemUnit = unit.getSystemUnit();
										rebuilder.add("type.unitsSI", systemUnit.getName());
										if (document.containsKey("numericValue")) {
											double numericValue = document.getJsonNumber("numericValue").doubleValue();
											UnitConverter converter = unit.getConverterToAny(systemUnit);
											rebuilder.add("numericValueSI", converter.convert(numericValue));
										}
									} catch (IncommensurableException | MeasurementParseException e) {
										logger.error("Unable to convert 'type.units' of {} due to {}", unitString, e.getMessage());
									}
									document = rebuilder.build();
								}
								paramsBuilder.add("doc", Json.createArrayBuilder().add(document));
							} else {
								UnitConverter converter = null;
								for (String field : relation.fields) {
									if (field.equals("type.unitsSI")) {
										String unitString = document.getString("type.units");
										try {
											Unit<?> unit = unitFormat.parse(unitString);
											Unit<?> systemUnit = unit.getSystemUnit();
											converter = unit.getConverterToAny(systemUnit);
											paramsBuilder.add(field, systemUnit.getName());
										} catch (IncommensurableException | MeasurementParseException e) {
											logger.error("Unable to convert 'type.units' of {} due to {}", unitString, e.getMessage());
										}
									} else if (field.equals("numericValueSI")) {
										if (converter != null) {
											// If we convert 1, we then have the necessary factor and can do multiplication by script...
											paramsBuilder.add("conversionFactor", converter.convert(1.));
										}
									} else {
										paramsBuilder.add(field, document.get(field));
									}
								}
							}
							String scriptId = (index.equals("parametertype")) ? "update_" + relation.parentName + index : "update_" + index;
							JsonObjectBuilder scriptBuilder = Json.createObjectBuilder().add("id", scriptId)
									.add("params", paramsBuilder);
							JsonObject queryObject;
							String idField = (relation.joinName.equals(relation.parentName)) ? "id" : relation.joinName + ".id";
							if (!Arrays.asList("parametertype", "sampletype", "user").contains(index)) {
								queryObject = buildTermQuery(idField, id);
							} else {
								queryObject = buildNestedQuery(relation.joinName, buildTermQuery(idField, id));
							}
							JsonObject bodyJson = Json.createObjectBuilder().add("query", queryObject)
									.add("script", scriptBuilder).build();
							logger.trace("update script: {}", bodyJson.toString());
							httpPost.setEntity(new StringEntity(bodyJson.toString(), ContentType.APPLICATION_JSON));
							updatesByQuery.add(httpPost);
						} else {
							URI uri = new URIBuilder(server)
									.setPath(basePath + "/" + relation.parentName + "/_update_by_query").build();
							HttpPost httpPost = new HttpPost(uri);
							JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("id", id);
							String scriptId = (index.equals("parametertype")) ? "delete_" + relation.parentName + index : "update_" + index;
							JsonObjectBuilder scriptBuilder = Json.createObjectBuilder().add("id", scriptId)
									.add("params", paramsBuilder);
							JsonObject queryObject;
							String idField = (relation.joinName.equals(relation.parentName)) ? "id" : relation.joinName + ".id";
							if (!Arrays.asList("parametertype", "sampletype", "user").contains(index)) {
								queryObject = buildTermQuery(idField, id);
							} else {
								queryObject = buildNestedQuery(relation.joinName, buildTermQuery(idField, id));
							}
							JsonObject bodyJson = Json.createObjectBuilder().add("query", queryObject)
									.add("script", scriptBuilder).build();
							logger.trace("delete script: {}", bodyJson.toString());
							httpPost.setEntity(new StringEntity(bodyJson.toString(), ContentType.APPLICATION_JSON));
							updatesByQuery.add(httpPost);
						}
					}
				} else {
					JsonObjectBuilder innerBuilder = Json.createObjectBuilder().add("_id", new Long(id)).add("_index",
							index);
					if (operationKey.equals("delete")) {
						sb.append(Json.createObjectBuilder().add(operationKey, innerBuilder).build().toString()).append("\n");
					} else {
						JsonObject document = innerOperation.getJsonObject("doc");
						sb.append(Json.createObjectBuilder().add("update", innerBuilder).build().toString())
								.append("\n");
						sb.append(Json.createObjectBuilder().add("doc", document).add("doc_as_upsert", true).build()
								.toString()).append("\n");
						
						if (!index.equals("investigation")) {
							// TODO Nightmare user lookup
							investigationIds.add(document.getString("investigation.id"));
						}
					}
				}
			}
			logger.debug("bulk string: {}", sb.toString());
			if (sb.toString().length() > 0) {
				URI uri = new URIBuilder(server).setPath(basePath + "/_bulk").build();
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setEntity(new StringEntity(sb.toString(), ContentType.APPLICATION_JSON));
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			}
			if (updatesByQuery.size() > 0) {
				commit();
			}
			logger.trace("updatesByQuery: {}", updatesByQuery.toString());
			for (HttpPost updateByQuery : updatesByQuery) {
				try (CloseableHttpResponse response = httpclient.execute(updateByQuery)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			}
			if (investigationIds.size() > 0) {
				commit();
			}
			logger.trace("investigationIds: {}", investigationIds.toString());
			for (String investigationId : investigationIds) {
				URI uriGet = new URIBuilder(server).setPath(basePath + "/investigation/_source/" + investigationId)
						.build();
				HttpGet httpGet = new HttpGet(uriGet);
				try (CloseableHttpResponse responseGet = httpclient.execute(httpGet)) {
					if (responseGet.getStatusLine().getStatusCode() == 200) {
						// It's possible that the an investigation/investigationUser has not yet been indexed, in which case we cannot update the dataset/file with the user metadata
						Rest.checkStatus(responseGet, IcatExceptionType.INTERNAL);
						JsonObject responseObject = Json.createReader(responseGet.getEntity().getContent()).readObject();
						logger.trace("GET investigation {} response: {}", investigationId, responseObject);
						if (responseObject.containsKey("investigationuser")) {
							JsonArray jsonArray = responseObject.getJsonArray("investigationuser");
							for (String index : new String[] {"datafile", "dataset"}) {
								URI uri = new URIBuilder(server).setPath(basePath + "/" + index + "/_update_by_query").build();
								HttpPost httpPost = new HttpPost(uri);
								JsonObjectBuilder queryBuilder = Json.createObjectBuilder()
										.add("term", Json.createObjectBuilder()
												.add("investigation.id", investigationId));
								JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("doc", jsonArray);
								JsonObject bodyJson = Json.createObjectBuilder()
										.add("query", queryBuilder)
										.add("script", Json.createObjectBuilder()
												.add("id", "create_investigationuser").add("params", paramsBuilder))
										.build();
								httpPost.setEntity(new StringEntity(bodyJson.toString(), ContentType.APPLICATION_JSON));
								try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
									Rest.checkStatus(response, IcatExceptionType.INTERNAL);
								}
							}
						}

					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

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
