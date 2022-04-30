package org.icatproject.core.manager.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.Set;

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
import javax.persistence.EntityManager;

import org.apache.http.client.ClientProtocolException;
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
import org.icatproject.core.manager.Rest;
import org.icatproject.utils.IcatUnits;
import org.icatproject.utils.IcatUnits.SystemValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpensearchApi extends SearchApi {

	private static enum ModificationType {
		CREATE, UPDATE, DELETE
	};

	private static enum RelationType {
		CHILD, NESTED_CHILD, NESTED_GRANDCHILD
	};

	private static class ParentRelation {
		public RelationType relationType;
		public String parentName;
		public String joinField;
		public Set<String> fields;

		public ParentRelation(RelationType relationType, String parentName, String joinField, Set<String> fields) {
			this.relationType = relationType;
			this.parentName = parentName;
			this.joinField = joinField;
			this.fields = fields;
		}
	}

	private IcatUnits icatUnits;
	protected static final Logger logger = LoggerFactory.getLogger(OpensearchApi.class);
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
							.add("type", "stemmer").add("langauge", "possessive_english"))))
			.build();
	private static Map<String, List<ParentRelation>> relations = new HashMap<>();

	static {
		// Non-nested children have a one to one relationship with an indexed entity and
		// so do not form an array, and update specific fields by query
		relations.put("datafileformat", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "datafile", "datafileFormat", DatafileFormat.docFields)));
		relations.put("datasettype", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "dataset", "type", DatasetType.docFields)));
		relations.put("investigationtype", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "investigation", "type", InvestigationType.docFields)));
		relations.put("facility", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "investigation", "facility", Facility.docFields)));

		// Nested children are indexed as an array of objects on their parent entity,
		// and know their parent's id (N.B. InvestigationUsers are also mapped to
		// Datasets and Datafiles, but using the investigation.id field)
		relations.put("datafileparameter", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "datafile", null)));
		relations.put("datasetparameter", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "dataset", null)));
		relations.put("investigationparameter", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null)));
		relations.put("investigationuser", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "investigation", null)));
		relations.put("sample", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null)));

		// Nested grandchildren are entities that are related to one of the nested
		// children, but do not have a direct reference to one of the indexed entities,
		// and so must be updated by query - they also only affect a subset of the
		// nested fields, rather than an entire nested object
		relations.put("parametertype", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "investigationparameter",
						ParameterType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "datasetparameter",
						ParameterType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "datafile", "datafileparameter",
						ParameterType.docFields)));
		relations.put("user", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "investigationuser",
						User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "investigationuser", User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "datafile", "investigationuser", User.docFields)));
		relations.put("sampleType", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "sample", SampleType.docFields)));
	}

	public OpensearchApi(URI server) {
		super(server);
		icatUnits = new IcatUnits();
	}

	public OpensearchApi(URI server, String unitAliasOptions) {
		super(server);
		icatUnits = new IcatUnits(unitAliasOptions);
	}

	private static String buildCreateScript(String target) {
		String source = "ctx._source." + target + " = params.doc";
		JsonObjectBuilder builder = Json.createObjectBuilder().add("lang", "painless").add("source", source);
		return Json.createObjectBuilder().add("script", builder).build().toString();
	}

	private static String buildChildScript(Set<String> docFields, boolean update) {
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

	private static String buildNestedChildScript(String target, boolean update) {
		String source = "if (ctx._source." + target + " != null) {List ids = new ArrayList(); ctx._source." + target
				+ ".forEach(t -> ids.add(t.id)); if (ids.contains(params.id)) {ctx._source." + target
				+ ".remove(ids.indexOf(params.id))}}";
		if (update) {
			source += "if (ctx._source." + target + " != null) {ctx._source." + target
					+ ".addAll(params.doc);} else {ctx._source." + target + " = params.doc;}";
		}
		JsonObjectBuilder builder = Json.createObjectBuilder().add("lang", "painless").add("source", source);
		return Json.createObjectBuilder().add("script", builder).build().toString();
	}

	private static String buildNestedGrandchildScript(String target, Set<String> docFields, boolean update) {
		String source = "int listIndex; if (ctx._source." + target
				+ " != null) {List ids = new ArrayList(); ctx._source." + target
				+ ".forEach(t -> ids.add(t.id)); if (ids.contains(params.id)) {listIndex = ids.indexOf(params.id)}}";
		String childSource = "ctx._source." + target + ".get(listIndex)";
		for (String field : docFields) {
			if (update) {
				if (field.equals("numericValueSI")) {
					source += "if (" + childSource
							+ ".numericValue != null && params.containsKey('conversionFactor')) {" + childSource
							+ ".numericValueSI = params.conversionFactor * " + childSource + ".numericValue;} else {"
							+ childSource + ".remove('numericValueSI');}";
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

	private static JsonObject buildMappings(String index) {
		JsonObject typeLong = Json.createObjectBuilder().add("type", "long").build();
		JsonObjectBuilder propertiesBuilder = Json.createObjectBuilder()
				.add("id", typeLong)
				.add("investigationuser", buildNestedMapping("investigation.id", "user.id"));
		if (index.equals("investigation")) {
			propertiesBuilder
					.add("type.id", typeLong)
					.add("facility.id", typeLong)
					.add("sample", buildNestedMapping("investigation.id", "type.id"))
					.add("investigationparameter", buildNestedMapping("investigation.id", "type.id"));
		} else if (index.equals("dataset")) {
			propertiesBuilder
					.add("investigation.id", typeLong)
					.add("type.id", typeLong)
					.add("sample.id", typeLong)
					.add("datasetparameter", buildNestedMapping("dataset.id", "type.id"));
		} else if (index.equals("datafile")) {
			propertiesBuilder
					.add("investigation.id", typeLong)
					.add("datafileFormat.id", typeLong)
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

	private static Long parseDate(JsonObject jsonObject, String key, int offset, Long defaultValue)
			throws IcatException {
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

	@Override
	public void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor)
			throws IcatException, IOException, URISyntaxException {
		// getBeanDocExecutor is not used for this implementation, but is
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

	@Override
	public void clear() throws IcatException {
		commit();
		String body = QueryBuilder.addQuery(QueryBuilder.buildMatchAllQuery()).build().toString();
		post("/_all/_delete_by_query", body);
	}

	@Override
	public void commit() throws IcatException {
		post("/_refresh");		
	}

	@Override
	public List<FacetDimension> facetSearch(String target, JsonObject facetQuery, Integer maxResults,
			Integer maxLabels) throws IcatException {
		List<FacetDimension> results = new ArrayList<>();
		if (!facetQuery.containsKey("dimensions")) {
			// If no dimensions were specified, return early
			return results;
		}
		String dimensionPrefix = null;
		String index = target.toLowerCase();
		if (relations.containsKey(index)) {
			// If we're attempting to facet a nested entity, use the parent index
			dimensionPrefix = index;
			index = relations.get(index).get(0).parentName;
		}

		JsonObject queryObject = facetQuery.getJsonObject("query");
		JsonArray dimensions = facetQuery.getJsonArray("dimensions");
		JsonObjectBuilder bodyBuilder = parseQuery(Json.createObjectBuilder(), queryObject, index);
		bodyBuilder = parseFacets(bodyBuilder, dimensions, maxLabels, dimensionPrefix);
		String body = bodyBuilder.build().toString();

		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("size", maxResults.toString());

		JsonObject postResponse = postResponse("/" + index + "/_search", body, parameterMap);
	
		JsonObject aggregations = postResponse.getJsonObject("aggregations");
		if (dimensionPrefix != null) {
			aggregations = aggregations.getJsonObject(dimensionPrefix);
		}
		for (String dimension : aggregations.keySet()) {
			parseFacetsResponse(results, target, dimension, aggregations);
		}
		return results;
	}

	private JsonObjectBuilder parseFacets(JsonObjectBuilder bodyBuilder, JsonArray dimensions, int maxLabels,
			String dimensionPrefix) {
		JsonObjectBuilder aggsBuilder = Json.createObjectBuilder();
		for (JsonObject dimensionObject : dimensions.getValuesAs(JsonObject.class)) {
			String dimensionString = dimensionObject.getString("dimension");
			String field = dimensionPrefix == null ? dimensionString : dimensionPrefix + "." + dimensionString;
			if (dimensionObject.containsKey("ranges")) {
				JsonArray ranges = dimensionObject.getJsonArray("ranges");
				aggsBuilder.add(dimensionString, QueryBuilder.buildRangeFacet(field, ranges));
			} else {
				aggsBuilder.add(dimensionString, QueryBuilder.buildStringFacet(field, maxLabels));
			}
		}
		if (dimensionPrefix == null) {
			bodyBuilder.add("aggs", aggsBuilder);
		} else {
			bodyBuilder.add("aggs", Json.createObjectBuilder()
					.add(dimensionPrefix, Json.createObjectBuilder()
							.add("nested", Json.createObjectBuilder().add("path", dimensionPrefix))
							.add("aggs", aggsBuilder)));
		}
		return bodyBuilder;
	}

	@Override
	public SearchResult getResults(JsonObject query, JsonValue searchAfter, Integer blockSize, String sort,
			List<String> requestedFields) throws IcatException {
		String index = query.containsKey("target") ? query.getString("target").toLowerCase() : "_all";

		JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
		bodyBuilder = parseSort(bodyBuilder, sort);
		bodyBuilder = parseSearchAfter(bodyBuilder, searchAfter);
		bodyBuilder = parseQuery(bodyBuilder, query, index);
		String body = bodyBuilder.build().toString();

		Map<String, String> parameterMap = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		requestedFields.forEach(f -> sb.append(f).append(","));
		parameterMap.put("_source", sb.toString());
		parameterMap.put("size", blockSize.toString());

		JsonObject postResponse = postResponse("/" + index + "/_search", body, parameterMap);

		SearchResult result = new SearchResult();
		List<ScoredEntityBaseBean> entities = result.getResults();
		JsonArray hits = postResponse.getJsonObject("hits").getJsonArray("hits");
		for (JsonObject hit : hits.getValuesAs(JsonObject.class)) {
			Float score = Float.NaN;
			if (!hit.isNull("_score")) {
				score = hit.getJsonNumber("_score").bigDecimalValue().floatValue();
			}
			Integer id = new Integer(hit.getString("_id"));
			entities.add(new ScoredEntityBaseBean(id, score, hit.getJsonObject("_source")));
		}

		// If we're returning as many results as were asked for, setSearchAfter so
		// subsequent searches can continue from the last result
		if (hits.size() == blockSize) {
			JsonObject lastHit = hits.getJsonObject(blockSize - 1);
			if (lastHit.containsKey("sort")) {
				result.setSearchAfter(lastHit.getJsonArray("sort"));
			} else {
				ScoredEntityBaseBean lastEntity = entities.get(blockSize - 1);
				long id = lastEntity.getEntityBaseBeanId();
				float score = lastEntity.getScore();
				result.setSearchAfter(Json.createArrayBuilder().add(score).add(id).build());
			}
		}
		
		return result;
	}

	private JsonObjectBuilder parseSort(JsonObjectBuilder builder, String sort) {
		if (sort == null || sort.equals("")) {
			return builder.add("sort", Json.createArrayBuilder()
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
			return builder.add("sort", sortArrayBuilder.add(Json.createObjectBuilder().add("id", "asc")).build());
		}
	}

	private JsonObjectBuilder parseSearchAfter(JsonObjectBuilder builder, JsonValue searchAfter) {
		if (searchAfter == null) {
			return builder;
		} else {
			return builder.add("search_after", searchAfter);
		}
	}

	private JsonObjectBuilder parseQuery(JsonObjectBuilder builder, JsonObject query, String index)
			throws IcatException {
		// In general, we use a boolean query to compound queries on individual fields
		JsonObjectBuilder queryBuilder = Json.createObjectBuilder();
		JsonObjectBuilder boolBuilder = Json.createObjectBuilder();

		if (query.containsKey("text")) {
			// The free text is the only element we perform scoring on, so "must" occur
			JsonArrayBuilder mustBuilder = Json.createArrayBuilder();
			mustBuilder.add(QueryBuilder.buildStringQuery(query.getString("text")));
			boolBuilder.add("must", mustBuilder);
		}

		// Non-scored elements are added to the "filter"
		JsonArrayBuilder filterBuilder = Json.createArrayBuilder();

		Long lowerTime = parseDate(query, "lower", 0, Long.MIN_VALUE);
		Long upperTime = parseDate(query, "upper", 59999, Long.MAX_VALUE);
		if (lowerTime != Long.MIN_VALUE || upperTime != Long.MAX_VALUE) {
			if (index.equals("datafile")) {
				// datafile has only one date field
				filterBuilder.add(QueryBuilder.buildLongRangeQuery("date", lowerTime, upperTime));
			} else {
				filterBuilder.add(QueryBuilder.buildLongRangeQuery("startDate", lowerTime, upperTime));
				filterBuilder.add(QueryBuilder.buildLongRangeQuery("endDate", lowerTime, upperTime));
			}
		}

		List<JsonObject> investigationUserQueries = new ArrayList<>();
		if (query.containsKey("user")) {
			String name = query.getString("user");
			JsonObject nameQuery = QueryBuilder.buildMatchQuery("investigationuser.user.name", name);
			investigationUserQueries.add(nameQuery);
		}
		if (query.containsKey("userFullName")) {
			String fullName = query.getString("userFullName");
			JsonObject fullNameQuery = QueryBuilder.buildStringQuery(fullName, "investigationuser.user.fullName");
			investigationUserQueries.add(fullNameQuery);
		}
		if (investigationUserQueries.size() > 0) {
			JsonObject[] array = investigationUserQueries.toArray(new JsonObject[0]);
			filterBuilder.add(QueryBuilder.buildNestedQuery("investigationuser", array));
		}

		if (query.containsKey("samples")) {
			JsonArray samples = query.getJsonArray("samples");
			for (int i = 0; i < samples.size(); i++) {
				String sample = samples.getString(i);
				JsonObject stringQuery = QueryBuilder.buildStringQuery(sample, "sample.name", "sample.type.name");
				filterBuilder.add(QueryBuilder.buildNestedQuery("sample", stringQuery));
			}
		}

		if (query.containsKey("parameters")) {
			for (JsonObject parameterObject : query.getJsonArray("parameters").getValuesAs(JsonObject.class)) {
				String path = index + "parameter";
				List<JsonObject> parameterQueries = new ArrayList<>();
				if (parameterObject.containsKey("name")) {
					String name = parameterObject.getString("name");
					parameterQueries.add(QueryBuilder.buildMatchQuery(path + ".type.name", name));
				}
				if (parameterObject.containsKey("units")) {
					String units = parameterObject.getString("units");
					parameterQueries.add(QueryBuilder.buildMatchQuery(path + ".type.units", units));
				}
				if (parameterObject.containsKey("stringValue")) {
					String stringValue = parameterObject.getString("stringValue");
					parameterQueries.add(QueryBuilder.buildMatchQuery(path + ".stringValue", stringValue));
				} else if (parameterObject.containsKey("lowerDateValue")
						&& parameterObject.containsKey("upperDateValue")) {
					Long lower = parseDate(parameterObject, "lowerDateValue", 0, Long.MIN_VALUE);
					Long upper = parseDate(parameterObject, "upperDateValue", 59999, Long.MAX_VALUE);
					parameterQueries.add(QueryBuilder.buildLongRangeQuery(path + ".dateTimeValue", lower, upper));
				} else if (parameterObject.containsKey("lowerNumericValue")
						&& parameterObject.containsKey("upperNumericValue")) {
					JsonNumber lower = parameterObject.getJsonNumber("lowerNumericValue");
					JsonNumber upper = parameterObject.getJsonNumber("upperNumericValue");
					parameterQueries.add(QueryBuilder.buildRangeQuery(path + ".numericValue", lower, upper));
				}
				filterBuilder.add(QueryBuilder.buildNestedQuery(path, parameterQueries.toArray(new JsonObject[0])));
			}
		}

		if (query.containsKey("id")) {
			filterBuilder.add(QueryBuilder.buildTermsQuery("id", query.getJsonArray("id")));
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
				URI uri = new URIBuilder(server).setPath("/" + index).build();
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
				URI uri = new URIBuilder(server).setPath("/" + index).build();
				HttpPut httpPut = new HttpPut(uri);
				JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
				bodyBuilder.add("settings", indexSettings).add("mappings", buildMappings(index));
				String body = bodyBuilder.build().toString();
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
		for (Entry<String, List<ParentRelation>> entry : relations.entrySet()) {
			String childName = entry.getKey();
			ParentRelation relation = entry.getValue().get(0);
			switch (relation.relationType) {
				case CHILD:
					post("/_scripts/update_" + childName, buildChildScript(relation.fields, true));
					post("/_scripts/delete_" + childName, buildChildScript(relation.fields, false));
					break;
				case NESTED_CHILD:
					post("/_scripts/create_" + childName, buildCreateScript(childName));
					post("/_scripts/update_" + childName, buildNestedChildScript(childName, true));
					post("/_scripts/delete" + childName, buildNestedChildScript(childName, false));
					break;
				case NESTED_GRANDCHILD:
					if (childName.equals("parametertype")) {
						// Special case, as parametertype applies to investigationparameter,
						// datasetparameter, datafileparameter
						for (String index : indices) {
							String target = index + "parameter";
							String updateScript = buildNestedGrandchildScript(target, relation.fields, true);
							String deleteScript = buildNestedGrandchildScript(target, relation.fields, false);
							post("/_scripts/update_" + index + childName, updateScript);
							post("/_scripts/delete_" + index + childName, deleteScript);
						}
					} else {
						String updateScript = buildNestedGrandchildScript(relation.joinField, relation.fields, true);
						String deleteScript = buildNestedGrandchildScript(relation.joinField, relation.fields, false);
						post("/_scripts/update_" + childName, updateScript);
						post("/_scripts/delete_" + childName, deleteScript);
					}
					break;
			}
		}
	}

	public void modify(String json) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			List<HttpPost> updatesByQuery = new ArrayList<>();
			Set<String> investigationIds = new HashSet<>();
			StringBuilder sb = new StringBuilder();
			JsonReader jsonReader = Json.createReader(new StringReader(json));
			JsonArray outerArray = jsonReader.readArray();
			for (JsonObject operation : outerArray.getValuesAs(JsonObject.class)) {
				Set<String> operationKeys = operation.keySet();
				if (operationKeys.size() != 1) {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"Operation should only have one key, but it had " + operationKeys);
				}
				String operationKey = operationKeys.toArray(new String[1])[0];
				ModificationType modificationType = ModificationType.valueOf(operationKey.toUpperCase());
				JsonObject innerOperation = operation.getJsonObject(modificationType.toString().toLowerCase());
				String index = innerOperation.getString("_index").toLowerCase();
				String id = innerOperation.getString("_id");
				JsonObject document = innerOperation.containsKey("doc") ? innerOperation.getJsonObject("doc") : null;

				if (relations.containsKey(index)) {
					// Entities without an index will have one or more parent indices that need to
					// be updated with their information
					for (ParentRelation relation : relations.get(index)) {
						modifyNestedEntity(sb, updatesByQuery, id, index, document, modificationType, relation);
					}
				} else {
					// Otherwise we are dealing with an indexed entity
					modifyEntity(sb, investigationIds, id, index, document, modificationType);
				}
			}

			if (sb.toString().length() > 0) {
				// Perform simple bulk modifications
				URI uri = new URIBuilder(server).setPath("/_bulk").build();
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setEntity(new StringEntity(sb.toString(), ContentType.APPLICATION_JSON));
				// logger.trace("Making call {} with body {}", uri, sb.toString());
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			}

			if (updatesByQuery.size() > 0) {
				// Ensure bulk changes are committed before performing updatesByQuery
				commit();
				for (HttpPost updateByQuery : updatesByQuery) {
					try (CloseableHttpResponse response = httpclient.execute(updateByQuery)) {
						Rest.checkStatus(response, IcatExceptionType.INTERNAL);
					}
				}
			}

			if (investigationIds.size() > 0) {
				// Ensure bulk changes are committed before checking for InvestigationUsers
				commit();
				for (String investigationId : investigationIds) {
					URI uriGet = new URIBuilder(server).setPath("/investigation/_source/" + investigationId)
							.build();
					HttpGet httpGet = new HttpGet(uriGet);
					try (CloseableHttpResponse responseGet = httpclient.execute(httpGet)) {
						if (responseGet.getStatusLine().getStatusCode() == 200) {
							extractFromInvestigation(httpclient, investigationId, responseGet);
						}
					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private void extractFromInvestigation(CloseableHttpClient httpclient, String investigationId,
			CloseableHttpResponse responseGet)
			throws IOException, URISyntaxException, IcatException, ClientProtocolException {
		JsonObject responseObject = Json.createReader(responseGet.getEntity().getContent()).readObject();
		if (responseObject.containsKey("investigationuser")) {
			JsonArray jsonArray = responseObject.getJsonArray("investigationuser");
			for (String index : new String[] { "datafile", "dataset" }) {
				URI uri = new URIBuilder(server).setPath("/" + index + "/_update_by_query").build();
				HttpPost httpPost = new HttpPost(uri);
				JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("doc", jsonArray);
				JsonObjectBuilder scriptBuilder = Json.createObjectBuilder();
				scriptBuilder.add("id", "create_investigationuser").add("params", paramsBuilder);
				JsonObject queryObject = QueryBuilder.buildTermQuery("investigation.id", investigationId);
				JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
				String body = bodyBuilder.add("query", queryObject).add("script", scriptBuilder).build().toString();
				httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
				// logger.trace("Making call {} with body {}", uri, body);
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			}
		}
	}

	private void modifyNestedEntity(StringBuilder sb, List<HttpPost> updatesByQuery, String id, String index,
			JsonObject document, ModificationType modificationType, ParentRelation relation)
			throws URISyntaxException {

		switch (modificationType) {
			case CREATE:
				if (relation.parentName.equals(relation.joinField)) {
					// If the target parent is the same as the joining field, we're appending the
					// nested child to a list of objects which can be sent as a bulk update request
					document = convertDocumentUnits(document);
					createNestedEntity(sb, id, index, document, relation);
				} else if (index.equals("sampletype")) {
					// Otherwise, in most cases we don't need to update, as User and ParameterType
					// cannot be null on their parent InvestigationUser or InvestigationParameter
					// when that parent is created so the information is captured. However, since
					// SampleType can be null upon creation of a Sample, need to account for the
					// creation of a SampleType at a later date.
					updateNestedEntityByQuery(updatesByQuery, id, index, document, relation, true);
				}
				break;
			case UPDATE:
				updateNestedEntityByQuery(updatesByQuery, id, index, document, relation, true);
				break;
			case DELETE:
				updateNestedEntityByQuery(updatesByQuery, id, index, document, relation, false);
				break;
		}
	}

	private static void createNestedEntity(StringBuilder sb, String id, String index, JsonObject document,
			ParentRelation relation) {

		String parentId = document.getString(relation.parentName + ".id");
		JsonObjectBuilder innerBuilder = Json.createObjectBuilder()
				.add("_id", parentId).add("_index", relation.parentName);
		// For nested 0:* relationships, wrap single documents in an array
		JsonArray docArray = Json.createArrayBuilder().add(document).build();
		JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("id", id).add("doc", docArray);
		// ParameterType is a special case where script needs to include the parentName
		String scriptId = index.equals("parametertype") ? "update_" + relation.parentName + index : "update_" + index;
		JsonObjectBuilder scriptBuilder = Json.createObjectBuilder().add("id", scriptId).add("params", paramsBuilder);
		JsonObjectBuilder upsertBuilder = Json.createObjectBuilder().add(index, docArray);
		JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
				.add("upsert", upsertBuilder).add("script", scriptBuilder);
		sb.append(Json.createObjectBuilder().add("update", innerBuilder).build().toString()).append("\n");
		sb.append(payloadBuilder.build().toString()).append("\n");
	}

	private void updateNestedEntityByQuery(List<HttpPost> updatesByQuery, String id, String index, JsonObject document,
			ParentRelation relation, boolean update) throws URISyntaxException {
		String path = "/" + relation.parentName + "/_update_by_query";
		URI uri = new URIBuilder(server).setPath(path).build();
		HttpPost httpPost = new HttpPost(uri);

		String scriptId = update ? "update_" : "delete_";
		scriptId += index.equals("parametertype") ? relation.parentName + index : index;
		JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("id", id);
		if (update) {
			if (relation.fields == null) {
				// Update affects all of the nested fields, so can add the entire document
				document = convertDocumentUnits(document);
				paramsBuilder.add("doc", Json.createArrayBuilder().add(document));
			} else {
				// Need to update individual nested fields
				paramsBuilder = convertScriptUnits(paramsBuilder, document, relation.fields);
			}
		}
		JsonObjectBuilder scriptBuilder = Json.createObjectBuilder().add("id", scriptId).add("params", paramsBuilder);
		JsonObject queryObject;
		String idField = relation.joinField.equals(relation.parentName) ? "id" : relation.joinField + ".id";
		if (relation.relationType.equals(RelationType.NESTED_GRANDCHILD)) {
			queryObject = QueryBuilder.buildNestedQuery(relation.joinField, QueryBuilder.buildTermQuery(idField, id));
		} else {
			queryObject = QueryBuilder.buildTermQuery(idField, id);
		}
		JsonObject bodyJson = Json.createObjectBuilder().add("query", queryObject).add("script", scriptBuilder).build();
		// logger.trace("updateByQuery script: {}", bodyJson.toString());
		httpPost.setEntity(new StringEntity(bodyJson.toString(), ContentType.APPLICATION_JSON));
		updatesByQuery.add(httpPost);
	}

	private void convertUnits(JsonObject document, JsonObjectBuilder rebuilder, String valueString,
			Double numericValue) {
		String unitString = document.getString("type.units");
		SystemValue systemValue = icatUnits.new SystemValue(numericValue, unitString);
		if (systemValue.units != null) {
			rebuilder.add("type.unitsSI", systemValue.units);
		}
		if (systemValue.value != null) {
			rebuilder.add(valueString, systemValue.value);
		}
	}

	private JsonObject convertDocumentUnits(JsonObject document) {
		if (!document.containsKey("type.units")) {
			return document;
		}
		// Need to rebuild the document...
		JsonObjectBuilder rebuilder = Json.createObjectBuilder();
		for (String key : document.keySet()) {
			rebuilder.add(key, document.get(key));
		}
		Double numericValue = document.containsKey("numericValue")
				? document.getJsonNumber("numericValue").doubleValue()
				: null;
		convertUnits(document, rebuilder, "numericValueSI", numericValue);
		document = rebuilder.build();
		return document;
	}

	private JsonObjectBuilder convertScriptUnits(JsonObjectBuilder paramsBuilder, JsonObject document,
			Set<String> fields) {
		for (String field : fields) {
			if (field.equals("type.unitsSI")) {
				convertUnits(document, paramsBuilder, "conversionFactor", 1.);
			} else if (field.equals("numericValueSI")) {
				continue;
			} else {
				paramsBuilder.add(field, document.get(field));
			}
		}
		return paramsBuilder;
	}

	private static void modifyEntity(StringBuilder sb, Set<String> investigationIds, String id, String index,
			JsonObject document, ModificationType modificationType) {

		JsonObject targetObject = Json.createObjectBuilder().add("_id", new Long(id)).add("_index", index).build();
		JsonObject update = Json.createObjectBuilder().add("update", targetObject).build();
		JsonObject docAsUpsert;
		switch (modificationType) {
			case CREATE:
				docAsUpsert = Json.createObjectBuilder().add("doc", document).add("doc_as_upsert", true).build();
				sb.append(update.toString()).append("\n").append(docAsUpsert.toString()).append("\n");
				if (!index.equals("investigation")) {
					// In principle a Dataset/Datafile could be created after InvestigationUser
					// entities are attached to an Investigation, so need to check for those
					investigationIds.add(document.getString("investigation.id"));
				}
				break;
			case UPDATE:
				docAsUpsert = Json.createObjectBuilder().add("doc", document).add("doc_as_upsert", true).build();
				sb.append(update.toString()).append("\n").append(docAsUpsert.toString()).append("\n");
				break;
			case DELETE:
				sb.append(Json.createObjectBuilder().add("delete", targetObject).build().toString()).append("\n");
				break;
		}
	}
}
