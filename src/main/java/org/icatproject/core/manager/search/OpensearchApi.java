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
	private static Map<String, List<String>> defaultFieldsMap = new HashMap<>();
	protected static final Set<String> indices = new HashSet<>(
			Arrays.asList("datafile", "dataset", "investigation", "instrumentscientist"));

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
		relations.put("investigation", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "dataset", "investigation",
						new HashSet<>(Arrays.asList("investigation.name", "investigation.id", "investigation.startDate",
								"investigation.title"))),
				new ParentRelation(RelationType.CHILD, "datafile", "investigation",
						new HashSet<>(Arrays.asList("investigation.name", "investigation.id")))));
		relations.put("dataset", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "datafile", "dataset",
						new HashSet<>(Arrays.asList("dataset.name", "dataset.id")))));
		relations.put("user", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "instrumentscientist", "user", User.docFields)));

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
		relations.put("investigationinstrument", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "investigation", null)));
		relations.put("sample", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null)));

		// Grandchildren are entities that are related to one of the nested
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
		relations.put("instrument", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "investigationinstrument",
						User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "investigationinstrument",
						User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "datafile", "investigationinstrument",
						User.docFields)));
		relations.put("sampleType", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "sample", SampleType.docFields)));

		defaultFieldsMap.put("_all", new ArrayList<>());
		defaultFieldsMap.put("datafile",
				Arrays.asList("name", "description", "doi", "location", "datafileFormat.name"));
		defaultFieldsMap.put("dataset",
				Arrays.asList("name", "description", "doi", "sample.name", "sample.type.name", "type.name"));
		defaultFieldsMap.put("investigation",
				Arrays.asList("name", "visitId", "title", "summary", "doi", "facility.name"));
	}

	public OpensearchApi(URI server) throws IcatException {
		super(server);
		icatUnits = new IcatUnits();
		initMappings();
		initScripts();
	}

	public OpensearchApi(URI server, String unitAliasOptions) throws IcatException {
		super(server);
		icatUnits = new IcatUnits(unitAliasOptions);
		initMappings();
		initScripts();
	}

	private static JsonObject buildMappings(String index) {
		JsonObject typeLong = Json.createObjectBuilder().add("type", "long").build();
		JsonObjectBuilder propertiesBuilder = Json.createObjectBuilder()
				.add("id", typeLong);
		if (index.equals("investigation")) {
			propertiesBuilder
					.add("type.id", typeLong)
					.add("facility.id", typeLong)
					.add("sample", buildNestedMapping("investigation.id", "type.id"))
					.add("investigationparameter", buildNestedMapping("investigation.id", "type.id"))
					.add("investigationuser", buildNestedMapping("investigation.id", "user.id"))
					.add("investigationinstrument", buildNestedMapping("investigation.id", "instrument.id"));
		} else if (index.equals("dataset")) {
			propertiesBuilder
					.add("investigation.id", typeLong)
					.add("type.id", typeLong)
					.add("sample.id", typeLong)
					.add("datasetparameter", buildNestedMapping("dataset.id", "type.id"))
					.add("investigationuser", buildNestedMapping("investigation.id", "user.id"))
					.add("investigationinstrument", buildNestedMapping("investigation.id", "instrument.id"));
		} else if (index.equals("datafile")) {
			propertiesBuilder
					.add("investigation.id", typeLong)
					.add("datafileFormat.id", typeLong)
					.add("datafileparameter", buildNestedMapping("datafile.id", "type.id"))
					.add("investigationuser", buildNestedMapping("investigation.id", "user.id"))
					.add("investigationinstrument", buildNestedMapping("investigation.id", "instrument.id"));
		} else if (index.equals("instrumentscientist")) {
			propertiesBuilder
					.add("instrument.id", typeLong)
					.add("user.id", typeLong);
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
		if (!indices.contains(index) && relations.containsKey(index)) {
			// If we're attempting to facet a nested entity, use the parent index
			dimensionPrefix = index;
			index = relations.get(index).get(0).parentName;
		}

		JsonObject queryObject = facetQuery.getJsonObject("query");
		JsonArray dimensions = facetQuery.getJsonArray("dimensions");
		List<String> defaultFields = defaultFieldsMap.get(index);
		JsonObjectBuilder bodyBuilder = parseQuery(Json.createObjectBuilder(), queryObject, index, defaultFields);
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
		List<String> defaultFields = defaultFieldsMap.get(index);

		JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
		bodyBuilder = parseSort(bodyBuilder, sort);
		bodyBuilder = parseSearchAfter(bodyBuilder, searchAfter);
		bodyBuilder = parseQuery(bodyBuilder, query, index, defaultFields);
		String body = bodyBuilder.build().toString();

		Map<String, String> parameterMap = new HashMap<>();
		Map<String, Set<String>> joinedFields = new HashMap<>();
		buildParameterMap(blockSize, requestedFields, parameterMap, joinedFields);

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
			JsonObject source = hit.getJsonObject("_source");
			// If there are fields requested from another index, join them to the source
			for (String joinedEntityName : joinedFields.keySet()) {
				String joinedIndex = joinedEntityName.toLowerCase();
				Set<String> requestedJoinedFields = joinedFields.get(joinedEntityName);
				Map<String, String> joinedParameterMap = new HashMap<>();
				String fld;
				String parentId;
				if (joinedIndex.contains("investigation")) {
					// Special case to allow datafiles and datasets join via their investigation.id
					// field
					fld = "investigation.id";
					if (index.equals("investigation")) {
						parentId = source.getString("id");
					} else {
						parentId = source.getString("investigation.id");
					}
				} else {
					fld = joinedIndex + ".id";
					parentId = source.getString("id");
				}
				// Search for joined entities matching the id
				JsonObject termQuery = QueryBuilder.buildTermQuery(fld, parentId);
				String joinedBody = Json.createObjectBuilder().add("query", termQuery).build().toString();
				buildParameterMap(blockSize, requestedJoinedFields, joinedParameterMap, null);
				JsonObject joinedResponse = postResponse("/" + joinedIndex + "/_search", joinedBody,
						joinedParameterMap);
				// Parse the joined source and integrate it into the main source Json
				JsonArray joinedHits = joinedResponse.getJsonObject("hits").getJsonArray("hits");
				JsonObjectBuilder sourceBuilder = Json.createObjectBuilder();
				source.entrySet().forEach(entry -> sourceBuilder.add(entry.getKey(), entry.getValue()));
				JsonArrayBuilder joinedSourceBuilder = Json.createArrayBuilder();
				for (JsonValue joinedHit : joinedHits) {
					JsonObject joinedHitObject = (JsonObject) joinedHit;
					joinedSourceBuilder.add(joinedHitObject.getJsonObject("_source"));
				}
				source = sourceBuilder.add(joinedIndex, joinedSourceBuilder).build();
			}
			entities.add(new ScoredEntityBaseBean(id, -1, score, source));
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

	/**
	 * Parses fields from requestedFields and set them in Map for the url
	 * parameters.
	 * 
	 * @param blockSize       The maximum number of results to return from a single
	 *                        search.
	 * @param requestedFields Fields that should be returned as part of the source
	 * @param parameterMap    Map of key value pairs to be included in the url.
	 * @param joinedFields    Map of indices to fields which should be returned that
	 *                        are NOT part of the main index/entity being searched.
	 * @throws IcatException if the field cannot be parsed.
	 */
	private void buildParameterMap(Integer blockSize, Iterable<String> requestedFields,
			Map<String, String> parameterMap, Map<String, Set<String>> joinedFields) throws IcatException {
		StringBuilder sb = new StringBuilder();
		for (String field : requestedFields) {
			String[] splitString = field.split(" ");
			if (splitString.length == 1) {
				sb.append(splitString[0] + ",");
			} else if (splitString.length == 2) {
				if (joinedFields != null && indices.contains(splitString[0].toLowerCase())) {
					if (joinedFields.containsKey(splitString[0])) {
						joinedFields.get(splitString[0]).add(splitString[1]);
					} else {
						joinedFields.putIfAbsent(splitString[0],
								new HashSet<String>(Arrays.asList(splitString[1])));
					}
				} else {
					sb.append(splitString[0].toLowerCase() + ",");
				}
			} else {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"Could not parse field: " + field);
			}
		}
		parameterMap.put("_source", sb.toString());
		parameterMap.put("size", blockSize.toString());
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

	/**
	 * Parses the search query from the incoming queryRequest into Json that the
	 * search cluster can understand.
	 * 
	 * @param builder       The JsonObjectBuilder being used to create the body for
	 *                      the POST request to the cluster.
	 * @param queryRequest  The Json object containing the information on the
	 *                      requested query, NOT formatted for the search cluster.
	 * @param index         The index to search.
	 * @param defaultFields Default fields to apply parsed string queries to.
	 * @return The JsonObjectBuilder initially passed with the "query" added to it.
	 * @throws IcatException If the query cannot be parsed.
	 */
	private JsonObjectBuilder parseQuery(JsonObjectBuilder builder, JsonObject queryRequest, String index,
			List<String> defaultFields) throws IcatException {
		// In general, we use a boolean query to compound queries on individual fields
		JsonObjectBuilder queryBuilder = Json.createObjectBuilder();
		JsonObjectBuilder boolBuilder = Json.createObjectBuilder();

		if (queryRequest.containsKey("text")) {
			// The free text is the only element we perform scoring on, so "must" occur
			JsonArrayBuilder mustBuilder = Json.createArrayBuilder();
			mustBuilder
					.add(QueryBuilder.buildStringQuery(queryRequest.getString("text"),
							defaultFields.toArray(new String[0])));
			boolBuilder.add("must", mustBuilder);
		}

		// Non-scored elements are added to the "filter"
		JsonArrayBuilder filterBuilder = Json.createArrayBuilder();

		Long lowerTime = parseDate(queryRequest, "lower", 0, Long.MIN_VALUE);
		Long upperTime = parseDate(queryRequest, "upper", 59999, Long.MAX_VALUE);
		if (lowerTime != Long.MIN_VALUE || upperTime != Long.MAX_VALUE) {
			if (index.equals("datafile")) {
				// datafile has only one date field
				filterBuilder.add(QueryBuilder.buildLongRangeQuery("date", lowerTime, upperTime));
			} else {
				filterBuilder.add(QueryBuilder.buildLongRangeQuery("startDate", lowerTime, upperTime));
				filterBuilder.add(QueryBuilder.buildLongRangeQuery("endDate", lowerTime, upperTime));
			}
		}

		if (queryRequest.containsKey("user")) {
			String name = queryRequest.getString("user");
			// Because InstrumentScientist is on a separate index, we need to explicitly
			// perform a search here
			JsonObject termQuery = QueryBuilder.buildTermQuery("user.name.keyword", name);
			String body = Json.createObjectBuilder().add("query", termQuery).build().toString();
			Map<String, String> parameterMap = new HashMap<>();
			parameterMap.put("_source", "instrument.id");
			JsonObject postResponse = postResponse("/instrumentscientist/_search", body, parameterMap);
			JsonArray hits = postResponse.getJsonObject("hits").getJsonArray("hits");
			JsonArrayBuilder instrumentIdsBuilder = Json.createArrayBuilder();
			for (JsonObject hit : hits.getValuesAs(JsonObject.class)) {
				String instrumentId = hit.getJsonObject("_source").getString("instrument.id");
				instrumentIdsBuilder.add(instrumentId);
			}
			JsonObject instrumentQuery = QueryBuilder.buildTermsQuery("investigationinstrument.instrument.id",
					instrumentIdsBuilder.build());
			JsonObject nestedInstrumentQuery = QueryBuilder.buildNestedQuery("investigationinstrument",
					instrumentQuery);
			// InvestigationUser should be a nested field on the main Document
			JsonObject investigationUserQuery = QueryBuilder.buildMatchQuery("investigationuser.user.name", name);
			JsonObject nestedUserQuery = QueryBuilder.buildNestedQuery("investigationuser", investigationUserQuery);
			// At least one of being an InstrumentScientist or an InvestigationUser is
			// necessary
			JsonArrayBuilder array = Json.createArrayBuilder().add(nestedInstrumentQuery).add(nestedUserQuery);
			filterBuilder.add(Json.createObjectBuilder().add("bool", Json.createObjectBuilder().add("should", array)));
		}
		if (queryRequest.containsKey("userFullName")) {
			String fullName = queryRequest.getString("userFullName");
			JsonObject fullNameQuery = QueryBuilder.buildStringQuery(fullName, "investigationuser.user.fullName");
			filterBuilder.add(QueryBuilder.buildNestedQuery("investigationuser", fullNameQuery));
		}

		if (queryRequest.containsKey("samples")) {
			JsonArray samples = queryRequest.getJsonArray("samples");
			for (int i = 0; i < samples.size(); i++) {
				String sample = samples.getString(i);
				JsonObject stringQuery = QueryBuilder.buildStringQuery(sample, "sample.name", "sample.type.name");
				filterBuilder.add(QueryBuilder.buildNestedQuery("sample", stringQuery));
			}
		}

		if (queryRequest.containsKey("parameters")) {
			for (JsonObject parameterObject : queryRequest.getJsonArray("parameters").getValuesAs(JsonObject.class)) {
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

		if (queryRequest.containsKey("id")) {
			filterBuilder.add(QueryBuilder.buildTermsQuery("id", queryRequest.getJsonArray("id")));
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
				logger.debug("Making call {}", uri);
				HttpHead httpHead = new HttpHead(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpHead)) {
					int statusCode = response.getStatusLine().getStatusCode();
					// If the index isn't present, we should get 404 and create the index
					if (statusCode == 200) {
						// If the index already exists (200), do not attempt to create it
						logger.debug("{} index already exists, continue", index);
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
				logger.debug("Making call {} with body {}", uri, body);
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
			String key = entry.getKey();
			ParentRelation relation = entry.getValue().get(0);
			String updateScript = "";
			String deleteScript = "";
			// Each type of relation needs a different script to update
			switch (relation.relationType) {
				case CHILD:
					updateScript = OpensearchScriptBuilder.buildChildScript(relation.fields, true);
					deleteScript = OpensearchScriptBuilder.buildChildScript(relation.fields, false);
					break;
				case NESTED_CHILD:
					updateScript = OpensearchScriptBuilder.buildNestedChildScript(key, true);
					deleteScript = OpensearchScriptBuilder.buildNestedChildScript(key, false);
					String createScript = OpensearchScriptBuilder.buildCreateNestedChildScript(key);
					post("/_scripts/create_" + key, createScript);
					break;
				case NESTED_GRANDCHILD:
					if (key.equals("parametertype")) {
						// Special case, as parametertype applies to investigationparameter,
						// datasetparameter, datafileparameter
						for (String index : indices) {
							String target = index + "parameter";
							updateScript = OpensearchScriptBuilder.buildGrandchildScript(target, relation.fields,
									true);
							deleteScript = OpensearchScriptBuilder.buildGrandchildScript(target, relation.fields,
									false);
						}
					} else {
						updateScript = OpensearchScriptBuilder.buildGrandchildScript(relation.joinField,
								relation.fields, true);
						deleteScript = OpensearchScriptBuilder.buildGrandchildScript(relation.joinField,
								relation.fields, false);
					}
					break;
			}
			post("/_scripts/update_" + key, updateScript);
			post("/_scripts/delete_" + key, deleteScript);
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
				logger.trace("{} {} with id {}", operationKey, index, id);

				if (relations.containsKey(index)) {
					// Related entities (with or without an index) will have one or more other
					// indices that need to
					// be updated with their information
					for (ParentRelation relation : relations.get(index)) {
						modifyNestedEntity(sb, updatesByQuery, id, index, document, modificationType, relation);
					}
				}
				if (indices.contains(index)) {
					// Also modify any main, indexable entities
					modifyEntity(sb, investigationIds, id, index, document, modificationType);
				}
			}

			if (sb.toString().length() > 0) {
				// Perform simple bulk modifications
				URI uri = new URIBuilder(server).setPath("/_bulk").build();
				HttpPost httpPost = new HttpPost(uri);
				httpPost.setEntity(new StringEntity(sb.toString(), ContentType.APPLICATION_JSON));
				logger.trace("Making call {} with body {}", uri, sb.toString());
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

	/**
	 * For cases when Datasets and Datafiles are created after an Investigation,
	 * some nested fields such as InvestigationUser and InvestigationInstrument may
	 * have already been indexed on the Investigation but not the Dataset/file as
	 * the latter did not yet exist. This method retrieves these arrays from the
	 * Investigation index ensuring that all information is available on all indices
	 * at the time of creation.
	 * 
	 * @param httpclient      The client being used to send HTTP
	 * @param investigationId Id of an investigation which may contain relevant
	 *                        information.
	 * @param responseGet     The response from a GET request using the
	 *                        investigationId, which may or may not contain relevant
	 *                        information in the returned _source Json.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws IcatException
	 * @throws ClientProtocolException
	 */
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
				logger.trace("Making call {} with body {}", uri, body);
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
					commit();
				}
			}
		}
		if (responseObject.containsKey("investigationinstrument")) {
			JsonArray jsonArray = responseObject.getJsonArray("investigationinstrument");
			for (String index : new String[] { "datafile", "dataset" }) {
				URI uri = new URIBuilder(server).setPath("/" + index + "/_update_by_query").build();
				HttpPost httpPost = new HttpPost(uri);
				JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("doc", jsonArray);
				JsonObjectBuilder scriptBuilder = Json.createObjectBuilder();
				scriptBuilder.add("id", "create_investigationinstrument").add("params", paramsBuilder);
				JsonObject queryObject = QueryBuilder.buildTermQuery("investigation.id", investigationId);
				JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
				String body = bodyBuilder.add("query", queryObject).add("script", scriptBuilder).build().toString();
				logger.trace("Making call {} with body {}", uri, body);
				httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
					commit();
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
					// since we have the parent id
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

		// Determine the Id of the painless script to use
		String scriptId = update ? "update_" : "delete_";
		scriptId += index.equals("parametertype") ? relation.parentName + index : index;

		// All updates/deletes require the entityId
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
		logger.trace("Making call {} with body {}", path, bodyJson.toString());
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
			if (document.containsKey(field)) {
				if (field.equals("type.unitsSI")) {
					convertUnits(document, paramsBuilder, "conversionFactor", 1.);
				} else if (field.equals("numericValueSI")) {
					continue;
				} else {
					paramsBuilder.add(field, document.get(field));
				}
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
				if (document.containsKey("investigation.id")) {
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
