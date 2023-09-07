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

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.EntityManager;

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
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleType;
import org.icatproject.core.entity.Technique;
import org.icatproject.core.entity.User;
import org.icatproject.core.manager.Rest;
import org.icatproject.utils.IcatUnits;
import org.icatproject.utils.IcatUnits.SystemValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface to Opensearch/Elasticsearch clusters is currently considered to
 * be experimental. For the more widely used and extensively tested Lucene based
 * engine, see {@link LuceneApi}.
 */
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

	private boolean aggregateFiles = false;
	public IcatUnits icatUnits;
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
	private static Map<String, List<String>> defaultFacetsMap = new HashMap<>();
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
						new HashSet<>(Arrays.asList("dataset.name", "dataset.id", "sample.id")))));
		relations.put("user", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "instrumentscientist", "user", User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "investigationuser",
						User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "investigationuser", User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "datafile", "investigationuser", User.docFields)));
		relations.put("sample", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "dataset", "sample", Sample.docFields),
				new ParentRelation(RelationType.CHILD, "datafile", "sample", Sample.docFields),
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null)));
		relations.put("sampletype", Arrays.asList(
				new ParentRelation(RelationType.CHILD, "dataset", "sample.type", SampleType.docFields),
				new ParentRelation(RelationType.CHILD, "datafile", "sample.type", SampleType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "sample", SampleType.docFields)));

		// Nested children are indexed as an array of objects on their parent entity,
		// and know their parent's id (N.B. InvestigationUsers are also mapped to
		// Datasets and Datafiles, but using the investigation.id field)
		relations.put("datafileparameter", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "datafile", null)));
		relations.put("datasetparameter", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "dataset", null)));
		relations.put("datasettechnique", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "dataset", null)));
		relations.put("investigationparameter", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null)));
		relations.put("sampleparameter", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "sample", null), // Must be first
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "sample", null),
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "sample", null)));
		relations.put("investigationuser", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "investigation", null)));
		relations.put("investigationinstrument", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "investigation", null)));
		relations.put("investigationfacilitycycle", Arrays.asList(
				new ParentRelation(RelationType.NESTED_CHILD, "investigation", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "dataset", "investigation", null),
				new ParentRelation(RelationType.NESTED_CHILD, "datafile", "investigation", null)));

		// Grandchildren are entities that are related to one of the nested
		// children, but do not have a direct reference to one of the indexed entities,
		// and so must be updated by query - they also only affect a subset of the
		// nested fields, rather than an entire nested object
		relations.put("parametertype", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "investigationparameter",
						ParameterType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "sampleparameter",
						ParameterType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "datasetparameter",
						ParameterType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "sampleparameter",
						ParameterType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "datafile", "datafileparameter",
						ParameterType.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "datafile", "sampleparameter",
						ParameterType.docFields)));
		relations.put("technique", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "datasettechnique",
						Technique.docFields)));
		relations.put("instrument", Arrays.asList(
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "investigation", "investigationinstrument",
						User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "dataset", "investigationinstrument",
						User.docFields),
				new ParentRelation(RelationType.NESTED_GRANDCHILD, "datafile", "investigationinstrument",
						User.docFields)));

		defaultFieldsMap.put("_all", new ArrayList<>());
		defaultFieldsMap.put("datafile",
				Arrays.asList("name", "description", "doi", "location", "datafileFormat.name", "sample.name"));
		defaultFieldsMap.put("dataset",
				Arrays.asList("name", "description", "doi", "sample.name", "sample.type.name", "type.name"));
		defaultFieldsMap.put("investigation",
				Arrays.asList("name", "visitId", "title", "summary", "doi", "facility.name"));

		defaultFacetsMap.put("datafile", Arrays.asList("datafileFormat.name"));
		defaultFacetsMap.put("dataset", Arrays.asList("type.name"));
		defaultFacetsMap.put("investigation", Arrays.asList("type.name"));
	}

	public OpensearchApi(URI server) throws IcatException {
		super(server);
		icatUnits = new IcatUnits();
		initMappings();
		initScripts();
	}

	public OpensearchApi(URI server, String unitAliasOptions, boolean aggregateFiles) throws IcatException {
		super(server);
		icatUnits = new IcatUnits(unitAliasOptions);
		this.aggregateFiles = aggregateFiles;
		initMappings();
		initScripts();
	}

	/**
	 * Builds a JsonObject representation of the mapping of fields to their type.
	 * The default behaviour is for a field to be treated as text with a string
	 * field automatically generated with the suffix ".keyword". Therefore only
	 * nested and long fields need to be explicitly accounted for.
	 * 
	 * @param index Index to build the mapping for.
	 * @return JsonObject of the document mapping.
	 */
	private static JsonObject buildMappings(String index) {
		JsonObject typeLong = Json.createObjectBuilder().add("type", "long").build();
		JsonObjectBuilder propertiesBuilder = Json.createObjectBuilder().add("id", typeLong);
		switch (index) {
			case "investigation":
				propertiesBuilder
						.add("type.id", typeLong)
						.add("facility.id", typeLong)
						.add("fileSize", typeLong)
						.add("fileCount", typeLong)
						.add("sample", buildNestedMapping("investigation.id", "type.id"))
						.add("sampleparameter", buildNestedMapping("sample.id", "type.id"))
						.add("investigationparameter", buildNestedMapping("investigation.id", "type.id"))
						.add("investigationuser", buildNestedMapping("investigation.id", "user.id"))
						.add("investigationinstrument", buildNestedMapping("investigation.id", "instrument.id"))
						.add("investigationfacilitycycle", buildNestedMapping("investigation.id", "facilityCycle.id"));
				break;

			case "dataset":
				propertiesBuilder
						.add("investigation.id", typeLong)
						.add("type.id", typeLong)
						.add("sample.id", typeLong)
						.add("sample.investigaion.id", typeLong)
						.add("sample.type.id", typeLong)
						.add("fileSize", typeLong)
						.add("fileCount", typeLong)
						.add("datasetparameter", buildNestedMapping("dataset.id", "type.id"))
						.add("datasettechnique", buildNestedMapping("dataset.id", "technique.id"))
						.add("investigationuser", buildNestedMapping("investigation.id", "user.id"))
						.add("investigationinstrument", buildNestedMapping("investigation.id", "instrument.id"))
						.add("investigationfacilitycycle", buildNestedMapping("investigation.id", "facilityCycle.id"))
						.add("sampleparameter", buildNestedMapping("sample.id", "type.id"));
				break;

			case "datafile":
				propertiesBuilder
						.add("investigation.id", typeLong)
						.add("datafileFormat.id", typeLong)
						.add("sample.investigaion.id", typeLong)
						.add("sample.type.id", typeLong)
						.add("fileSize", typeLong)
						.add("fileCount", typeLong)
						.add("datafileparameter", buildNestedMapping("datafile.id", "type.id"))
						.add("investigationuser", buildNestedMapping("investigation.id", "user.id"))
						.add("investigationinstrument", buildNestedMapping("investigation.id", "instrument.id"))
						.add("investigationfacilitycycle", buildNestedMapping("investigation.id", "facilityCycle.id"))
						.add("sampleparameter", buildNestedMapping("sample.id", "type.id"));
				break;

			case "instrumentscientist":
				propertiesBuilder
						.add("instrument.id", typeLong)
						.add("user.id", typeLong);
				break;

		}
		return Json.createObjectBuilder().add("properties", propertiesBuilder).build();
	}

	/**
	 * Builds a JsonObject representation of the fields on a nested object.
	 * 
	 * @param idFields Id fields on the nested object which require the long type
	 *                 mapping.
	 * @return JsonObjectBuilder for the nested object.
	 */
	private static JsonObjectBuilder buildNestedMapping(String... idFields) {
		JsonObjectBuilder propertiesBuilder = propertiesBuilder(idFields);
		return buildNestedMapping(propertiesBuilder);
	}

	private static JsonObjectBuilder buildNestedMapping(JsonObjectBuilder propertiesBuilder) {
		return Json.createObjectBuilder().add("type", "nested").add("properties", propertiesBuilder);
	}

	private static JsonObjectBuilder propertiesBuilder(String... idFields) {
		JsonObject typeLong = Json.createObjectBuilder().add("type", "long").build();
		JsonObjectBuilder propertiesBuilder = Json.createObjectBuilder().add("id", typeLong);
		for (String idField : idFields) {
			propertiesBuilder.add(idField, typeLong);
		}
		return propertiesBuilder;
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
			for (long id : ids) {
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
		String body = OpensearchQuery.matchAllQuery.toString();
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
		String dimensionPrefix = null;
		String index = target.toLowerCase();
		if (!indices.contains(index) && relations.containsKey(index)) {
			// If we're attempting to facet a nested entity, use the parent index
			dimensionPrefix = index;
			index = relations.get(index).get(0).parentName;
		}

		JsonObject queryObject = facetQuery.getJsonObject("query");
		List<String> defaultFields = defaultFieldsMap.get(index);
		OpensearchQuery opensearchQuery = new OpensearchQuery(this);
		opensearchQuery.parseQuery(queryObject, index, dimensionPrefix, defaultFields);
		if (facetQuery.containsKey("dimensions")) {
			JsonArray dimensions = facetQuery.getJsonArray("dimensions");
			opensearchQuery.parseFacets(dimensions, maxLabels, dimensionPrefix);
		} else {
			List<String> dimensions = defaultFacetsMap.get(index);
			opensearchQuery.parseFacets(dimensions, maxLabels, dimensionPrefix);
		}
		String body = opensearchQuery.body();

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

	@Override
	public SearchResult getResults(JsonObject query, JsonValue searchAfter, Integer blockSize, String sort,
			List<String> requestedFields) throws IcatException {
		String index = query.containsKey("target") ? query.getString("target").toLowerCase() : "_all";
		List<String> defaultFields = defaultFieldsMap.get(index);

		OpensearchQuery opensearchQuery = new OpensearchQuery(this);
		opensearchQuery.parseQuery(query, index, null, defaultFields);
		opensearchQuery.parseSort(sort);
		opensearchQuery.parseSearchAfter(searchAfter);
		String body = opensearchQuery.body();

		Map<String, String> parameterMap = new HashMap<>();
		Map<String, Set<String>> joinedFields = new HashMap<>();
		buildParameterMap(blockSize, requestedFields, parameterMap, joinedFields);

		JsonObject postResponse = postResponse("/" + index + "/_search", body, parameterMap);

		SearchResult result = new SearchResult();
		List<ScoredEntityBaseBean> entities = result.getResults();
		JsonArray hits = postResponse.getJsonObject("hits").getJsonArray("hits");
		for (JsonObject hit : hits.getValuesAs(JsonObject.class)) {
			float score = Float.NaN;
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
				JsonObject termQuery = OpensearchQuery.buildTermQuery(fld, parentId);
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
				long id = lastEntity.getId();
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
						joinedFields.putIfAbsent(splitString[0], new HashSet<>(Arrays.asList(splitString[1])));
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

	/**
	 * Create mappings for indices that do not already have them.
	 * 
	 * @throws IcatException
	 */
	public void initMappings() throws IcatException {
		for (String index : indices) {
			if (!indexExists(index)) {
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
	}

	/**
	 * @param index Name of an index (entity) to check the existence of
	 * @return Whether index exists on the cluster or not
	 * @throws IcatException
	 */
	private boolean indexExists(String index) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath("/" + index).build();
			logger.debug("Making call {}", uri);
			HttpHead httpHead = new HttpHead(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpHead)) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 404) {
					// If the index isn't present, we should get 404
					logger.debug("{} index does not exist", index);
					return false;
				} else {
					// checkStatus will throw unless the code is 200 (index exists)
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
					logger.debug("{} index already exists", index);
					return true;
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	/**
	 * Create scripts for indices that do not already have them.
	 * 
	 * @throws IcatException
	 */
	public void initScripts() throws IcatException {
		for (Entry<String, List<ParentRelation>> entry : relations.entrySet()) {
			String key = entry.getKey();
			ParentRelation relation = entry.getValue().get(0);
			// Special cases
			switch (key) {
				case "parametertype":
					// ParameterType can apply to 4 different nested objects
					post("/_scripts/update_parametertype",
							OpensearchScriptBuilder.buildParameterTypesScript(ParameterType.docFields, true));
					post("/_scripts/delete_parametertype",
							OpensearchScriptBuilder.buildParameterTypesScript(ParameterType.docFields, false));
					continue;

				case "sample":
					// Sample is a child of Datafile and Dataset...
					post("/_scripts/update_sample", OpensearchScriptBuilder.buildChildScript(Sample.docFields, true));
					post("/_scripts/delete_sample", OpensearchScriptBuilder.buildChildScript(Sample.docFields, false));
					// ...but a nested child of Investigations
					post("/_scripts/update_nestedsample", OpensearchScriptBuilder.buildNestedChildScript(key, true));
					post("/_scripts/delete_nestedsample", OpensearchScriptBuilder.buildNestedChildScript(key, false));
					String createScript = OpensearchScriptBuilder.buildCreateNestedChildScript(key);
					post("/_scripts/create_" + key, createScript);
					continue;

				case "sampletype":
					// SampleType is a child of Datafile and Dataset...
					post("/_scripts/update_sampletype",
							OpensearchScriptBuilder.buildChildScript(SampleType.docFields, true));
					post("/_scripts/delete_sampletype",
							OpensearchScriptBuilder.buildChildScript(SampleType.docFields, false));
					// ...but a nested grandchild of Investigations
					post("/_scripts/update_nestedsampletype",
							OpensearchScriptBuilder.buildGrandchildScript("sample", SampleType.docFields, true));
					post("/_scripts/delete_nestedsampletype",
							OpensearchScriptBuilder.buildGrandchildScript("sample", SampleType.docFields, false));
					continue;

			}
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
					updateScript = OpensearchScriptBuilder.buildGrandchildScript(relation.joinField,
							relation.fields, true);
					deleteScript = OpensearchScriptBuilder.buildGrandchildScript(relation.joinField,
							relation.fields, false);
					break;
			}
			post("/_scripts/update_" + key, updateScript);
			post("/_scripts/delete_" + key, deleteScript);
		}
		post("/_scripts/fileSize", OpensearchScriptBuilder.buildFileSizeScript());
	}

	public void modify(String json) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			OpensearchBulk bulk = new OpensearchBulk();
			JsonReader jsonReader = Json.createReader(new StringReader(json));
			JsonArray outerArray = jsonReader.readArray();
			for (JsonObject operation : outerArray.getValuesAs(JsonObject.class)) {
				parseModification(httpclient, bulk, operation);
			}

			postModify("/_bulk", bulk.bulkBody());

			if (bulk.updatesMap.size() > 0) {
				for (String path : bulk.updatesMap.keySet()) {
					for (String body : bulk.updatesMap.get(path)) {
						postModify(path, body);
					}
				}
			}

			if (bulk.investigationIds.size() > 0) {
				// Ensure bulk changes are committed before checking for InvestigationUsers
				commit();
				for (String investigationId : bulk.investigationIds) {
					String path = "/investigation/_source/" + investigationId;
					URI uriGet = new URIBuilder(server).setPath(path).build();
					HttpGet httpGet = new HttpGet(uriGet);
					try (CloseableHttpResponse responseGet = httpclient.execute(httpGet)) {
						if (responseGet.getStatusLine().getStatusCode() == 200) {
							extractFromInvestigation(httpclient, investigationId, responseGet);
						}
					}
				}
			}

			buildFileSizeUpdates("investigation", bulk.investigationAggregations, bulk.fileAggregationBuilder);
			buildFileSizeUpdates("dataset", bulk.datasetAggregations, bulk.fileAggregationBuilder);
			postModify("/_bulk", bulk.fileAggregationBody());

			postModify("/_bulk", bulk.deletedBody());
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	/**
	 * Parses a modification from operation, and adds it to bulk.
	 * 
	 * @param httpclient The client being used to send HTTP
	 * @param bulk       OpensearchBulk object recording the requests for updates
	 * @param operation  JsonObject representing the operation to be performed as
	 *                   part of the bulk modification
	 * @throws IcatException
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void parseModification(CloseableHttpClient httpclient, OpensearchBulk bulk, JsonObject operation)
			throws IcatException, URISyntaxException, ClientProtocolException, IOException {
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
			// indices that need to be updated with their information
			for (ParentRelation relation : relations.get(index)) {
				modifyNestedEntity(bulk, id, index, document, modificationType, relation);
			}
		}
		if (indices.contains(index)) {
			// Also modify any main, indexable entities
			modifyEntity(httpclient, bulk, id, index, document, modificationType);
		}
	}

	/**
	 * Commits to ensure index is up to date, then sends a POST request for
	 * modification. This may be bulk, a single update, update by query etc.
	 * 
	 * @param path Path on the search engine to POST to
	 * @param body String of Json to send as the request body
	 * @throws IcatException
	 */
	private void postModify(String path, String body) throws IcatException {
		if (body.length() > 0) {
			commit();
			post(path, body);
		}
	}

	/**
	 * Builds commands for updating the fileSizes of the entities keyed in
	 * aggregations.
	 * 
	 * @param entity                Name of the entity/index to be updated.
	 * @param aggregations          Map of aggregated fileSize changes with the
	 *                              entity ids as keys.
	 * @param fileSizeStringBuilder StringBuilder for constructing the bulk updates.
	 */
	private void buildFileSizeUpdates(String entity, Map<String, long[]> aggregations,
			StringBuilder fileSizeStringBuilder) {
		if (aggregations.size() > 0) {
			for (String id : aggregations.keySet()) {
				JsonObject targetObject = Json.createObjectBuilder().add("_id", new Long(id)).add("_index", entity)
						.build();
				JsonObject update = Json.createObjectBuilder().add("update", targetObject).build();
				long deltaFileSize = aggregations.get(id)[0];
				long deltaFileCount = aggregations.get(id)[1];
				JsonObjectBuilder paramsBuilder = Json.createObjectBuilder();
				JsonObjectBuilder scriptBuilder = Json.createObjectBuilder();
				paramsBuilder.add("deltaFileSize", deltaFileSize).add("deltaFileCount", deltaFileCount);
				scriptBuilder.add("id", "fileSize").add("params", paramsBuilder);
				JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
				String body = bodyBuilder.add("script", scriptBuilder).build().toString();
				fileSizeStringBuilder.append(update.toString()).append("\n").append(body).append("\n");
			}
		}
	}

	/**
	 * Gets the source of a Datafile and returns it.
	 * 
	 * @param httpclient The client being used to send HTTP
	 * @param id         ICAT entity id of the Datafile.
	 * @return The Datafile source.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 */
	private JsonObject extractSource(CloseableHttpClient httpclient, String id)
			throws IOException, URISyntaxException, ClientProtocolException {
		URI uriGet = new URIBuilder(server).setPath("/datafile/_source/" + id)
				.build();
		HttpGet httpGet = new HttpGet(uriGet);
		try (CloseableHttpResponse responseGet = httpclient.execute(httpGet)) {
			if (responseGet.getStatusLine().getStatusCode() == 200) {
				return Json.createReader(responseGet.getEntity().getContent()).readObject();
			}
		}
		return null;
	}

	/**
	 * For cases when Datasets and Datafiles are created after an Investigation,
	 * some nested fields such as InvestigationUser and InvestigationInstrument may
	 * have already been indexed on the Investigation but not the Dataset/file as
	 * the latter did not yet exist.
	 * 
	 * This method retrieves these arrays from the Investigation index ensuring that
	 * all information is available on all indices at the time of creation.
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
			extractEntity(httpclient, investigationId, responseObject, "investigationuser", false);
		}
		if (responseObject.containsKey("investigationinstrument")) {
			extractEntity(httpclient, investigationId, responseObject, "investigationinstrument", false);
		}
		if (responseObject.containsKey("investigationfacilitycycle")) {
			extractEntity(httpclient, investigationId, responseObject, "investigationfacilitycycle", false);
		}
		if (responseObject.containsKey("sample")) {
			extractEntity(httpclient, investigationId, responseObject, "sample", true);
		}
	}

	/**
	 * For cases when Datasets and Datafiles are created after an Investigation,
	 * some nested fields such as InvestigationUser and InvestigationInstrument may
	 * have already been indexed on the Investigation but not the Dataset/file as
	 * the latter did not yet exist.
	 * 
	 * This method extracts a single entity and uses it to update the
	 * dataset/datafile indices.
	 *
	 * @param httpclient      The client being used to send HTTP
	 * @param investigationId Id of an investigation which may contain relevant
	 *                        information.
	 * @param responseObject  JsonObject to extract the entity from
	 * @param entityName      Name of the entity being extracted
	 * @param addFields       Whether to add individual fields (true) or the entire
	 *                        entity as one "doc" (false)
	 * @throws URISyntaxException
	 * @throws IcatException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private void extractEntity(CloseableHttpClient httpclient, String investigationId, JsonObject responseObject,
			String entityName, boolean addFields)
			throws URISyntaxException, IcatException, IOException, ClientProtocolException {
		JsonArray jsonArray = responseObject.getJsonArray(entityName);
		for (String index : new String[] { "datafile", "dataset" }) {
			URI uri = new URIBuilder(server).setPath("/" + index + "/_update_by_query").build();
			HttpPost httpPost = new HttpPost(uri);
			if (addFields) {
				for (JsonObject document : jsonArray.getValuesAs(JsonObject.class)) {
					String documentId = document.getString("id");
					JsonObject queryObject = OpensearchQuery.buildTermQuery(entityName + ".id", documentId);
					JsonObjectBuilder paramsBuilder = Json.createObjectBuilder();
					JsonObjectBuilder scriptBuilder = Json.createObjectBuilder();
					for (String field : document.keySet()) {
						paramsBuilder.add(entityName + "." + field, document.get(field));
					}
					scriptBuilder.add("id", "update_" + entityName).add("params", paramsBuilder);

					updateWithExtractedEntity(httpclient, uri, httpPost, queryObject, scriptBuilder);
				}
			} else {
				JsonObject queryObject = OpensearchQuery.buildTermQuery("investigation.id", investigationId);
				JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("doc", jsonArray);
				JsonObjectBuilder scriptBuilder = Json.createObjectBuilder();
				scriptBuilder.add("id", "create_" + entityName).add("params", paramsBuilder);

				updateWithExtractedEntity(httpclient, uri, httpPost, queryObject, scriptBuilder);
			}
		}
	}

	/**
	 * For cases when Datasets and Datafiles are created after an Investigation,
	 * some nested fields such as InvestigationUser and InvestigationInstrument may
	 * have already been indexed on the Investigation but not the Dataset/file as
	 * the latter did not yet exist.
	 * 
	 * This updates an index with the result of the extraction.
	 * 
	 * @param httpclient    The client being used to send HTTP
	 * @param uri           URI for the relevant _update_by_query path
	 * @param httpPost      HttpPost to be sent
	 * @param queryObject   JsonObject determining which entities should be updated
	 * @param scriptBuilder JsonObjectBuilder for the script used to perform the
	 *                      update
	 * @throws IcatException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private void updateWithExtractedEntity(CloseableHttpClient httpclient, URI uri, HttpPost httpPost,
			JsonObject queryObject, JsonObjectBuilder scriptBuilder)
			throws IcatException, IOException, ClientProtocolException {
		JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
		String body = bodyBuilder.add("query", queryObject).add("script", scriptBuilder).build().toString();
		httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
		logger.trace("Making call {} with body {}", uri, body);
		try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
			Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			commit();
		}
	}

	/**
	 * Performs more complex update of an entity nested to a parent, for example
	 * parameters.
	 * 
	 * @param bulk             OpensearchBulk object recording the requests for
	 *                         updates by query
	 * @param id               Id of the entity.
	 * @param index            Index of the entity.
	 * @param document         JsonObject containing the key value pairs of the
	 *                         document fields.
	 * @param modificationType The type of operation to be performed.
	 * @param relation         The relation between the nested entity and its
	 *                         parent.
	 * @throws URISyntaxException
	 * @throws IcatException
	 */
	private void modifyNestedEntity(OpensearchBulk bulk, String id, String index, JsonObject document,
			ModificationType modificationType, ParentRelation relation) throws URISyntaxException, IcatException {

		switch (modificationType) {
			case CREATE:
				if (relation.parentName.equals(relation.joinField)) {
					// If the target parent is the same as the joining field, we're appending the
					// nested child to a list of objects which can be sent as a bulk update request
					// since we have the parent id
					document = convertDocumentUnits(document);
					if (index.equals("sample")) {
						// In order to make searching for sample information seamless between
						// Investigations and Datasets/files, need to ensure that when nesting fields
						// like "sample.name" under a "sample" object, we do not end up with
						// "sample.sample.name"
						JsonObjectBuilder documentBuilder = Json.createObjectBuilder();
						for (Entry<String, JsonValue> entry : document.entrySet()) {
							documentBuilder.add(entry.getKey().replace("sample.", ""), entry.getValue());
						}
						createNestedEntity(bulk, id, index, documentBuilder.build(), relation);
					} else {
						createNestedEntity(bulk, id, index, document, relation);
					}
				} else if (index.equals("sampletype")) {
					// Otherwise, in most cases we don't need to update, as User and ParameterType
					// cannot be null on their parent InvestigationUser or InvestigationParameter
					// when that parent is created so the information is captured. However, since
					// SampleType can be null upon creation of a Sample, need to account for the
					// creation of a SampleType at a later date.
					updateNestedEntityByQuery(bulk, id, index, document, relation, true);
				} else if (index.equals("sampleparameter")) {
					// SampleParameter requires specific logic, as the join is performed using the
					// Sample id rather than the SampleParameter id or the parent id.
					if (document.containsKey("sample.id")) {
						String sampleId = document.getString("sample.id");
						updateNestedEntityByQuery(bulk, sampleId, index, document, relation, true);
					}
				}
				break;
			case UPDATE:
				updateNestedEntityByQuery(bulk, id, index, document, relation, true);
				break;
			case DELETE:
				updateNestedEntityByQuery(bulk, id, index, document, relation, false);
				break;
		}
	}

	/**
	 * Create a new nested entity in an array on its parent.
	 * 
	 * @param bulk     OpensearchBulk object recording the requests for single
	 *                 updates
	 * @param id       Id of the entity.
	 * @param index    Index of the entity.
	 * @param document JsonObject containing the key value pairs of the document
	 *                 fields.
	 * @param relation The relation between the nested entity and its parent.
	 * @throws IcatException      If parentId is missing from document.
	 * @throws URISyntaxException
	 */
	private void createNestedEntity(OpensearchBulk bulk, String id, String index, JsonObject document,
			ParentRelation relation) throws IcatException, URISyntaxException {

		if (!document.containsKey(relation.joinField + ".id")) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					relation.joinField + ".id not found in " + document);
		}

		String parentId = document.getString(relation.joinField + ".id");
		String path = "/" + relation.parentName + "/_update/" + parentId;

		// For nested 0:* relationships, wrap single documents in an array
		JsonArray docArray = Json.createArrayBuilder().add(document).build();
		JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("id", id).add("doc", docArray);
		String scriptId;
		if (index.equals("sample") || index.equals("sampletype") && relation.parentName.equals("investigation")) {
			scriptId = "update_nested" + index;
		} else {
			scriptId = "update_" + index;
		}

		JsonObjectBuilder scriptBuilder = Json.createObjectBuilder().add("id", scriptId).add("params", paramsBuilder);
		JsonObjectBuilder upsertBuilder = Json.createObjectBuilder().add(index, docArray);
		JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
				.add("upsert", upsertBuilder).add("script", scriptBuilder);
		bulk.addUpdate(path, payloadBuilder.build().toString());
	}

	/**
	 * For existing nested objects, painless scripting must be used to update or
	 * delete them.
	 * 
	 * @param bulk     OpensearchBulk object recording the requests for updates by
	 *                 query
	 * @param id       Id of the entity.
	 * @param index    Index of the entity.
	 * @param document JsonObject containing the key value pairs of the
	 *                 document fields.
	 * @param relation The relation between the nested entity and its parent.
	 * @param update   Whether to update, or if false delete nested entity
	 *                 with the specified id.
	 * @throws URISyntaxException
	 */
	private void updateNestedEntityByQuery(OpensearchBulk bulk, String id, String index, JsonObject document,
			ParentRelation relation, boolean update) throws URISyntaxException {

		String path = "/" + relation.parentName + "/_update_by_query";

		// Determine the Id of the painless script to use
		String scriptId = update ? "update_" : "delete_";
		if (index.equals("sample") || index.equals("sampletype") && relation.parentName.equals("investigation")) {
			scriptId += "nested" + index;
		} else {
			scriptId += index;
		}

		// All updates/deletes require the entityId
		JsonObjectBuilder paramsBuilder = Json.createObjectBuilder().add("id", id);
		if (update) {
			if (relation.fields == null) {
				// Update affects all of the nested fields, so can add the entire document
				document = convertDocumentUnits(document);
				paramsBuilder.add("doc", Json.createArrayBuilder().add(document));
			} else {
				// Need to update individual nested fields
				convertScriptUnits(paramsBuilder, document, relation.fields);
			}
		}
		JsonObjectBuilder scriptBuilder = Json.createObjectBuilder().add("id", scriptId).add("params", paramsBuilder);
		String idField = relation.joinField.equals(relation.parentName) ? "id" : relation.joinField + ".id";
		// sample.id is a nested field on investigations, so need a nested query to
		// successfully add sampleparameter
		JsonObject queryObject = OpensearchQuery.buildTermQuery(idField, id);
		if (relation.relationType.equals(RelationType.NESTED_GRANDCHILD)
				|| index.equals("sampleparameter") && relation.parentName.equals("investigation")) {
			queryObject = OpensearchQuery.buildNestedQuery(relation.joinField, queryObject);
		}
		JsonObject bodyJson = Json.createObjectBuilder().add("query", queryObject).add("script", scriptBuilder).build();
		bulk.addUpdate(path, bodyJson.toString());
	}

	/**
	 * Gets "type.units" from the existing document, and adds "type.unitsSI" and the
	 * SI numeric value to the rebuilder if possible.
	 * 
	 * @param document     JsonObject of the original document.
	 * @param rebuilder    JsonObjectBuilder being used to create a new document
	 *                     with converted units.
	 * @param valueString  Field name of the numeric value.
	 * @param numericValue Value to possibly be converted.
	 */
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

	/**
	 * If appropriate, rebuilds document with conversion into SI units.
	 * 
	 * @param document JsonObject containing the document field/values.
	 * @return Either the original JsonDocument, or a copy with SI units and values
	 *         set.
	 */
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
		Double rangeBottom = document.containsKey("rangeBottom")
				? document.getJsonNumber("rangeBottom").doubleValue()
				: null;
		Double rangeTop = document.containsKey("rangeTop")
				? document.getJsonNumber("rangeTop").doubleValue()
				: null;
		convertUnits(document, rebuilder, "numericValueSI", numericValue);
		convertUnits(document, rebuilder, "rangeBottomSI", rangeBottom);
		convertUnits(document, rebuilder, "rangeTopSI", rangeTop);
		document = rebuilder.build();
		return document;
	}

	/**
	 * Builds the parameters for a painless script, converting into SI units if
	 * appropriate.
	 * 
	 * @param paramsBuilder JsonObjectBuilder for the painless script parameters.
	 * @param document      JsonObject containing the field/values.
	 * @param fields        List of fields to be included in the parameters.
	 */
	private void convertScriptUnits(JsonObjectBuilder paramsBuilder, JsonObject document,
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
	}

	/**
	 * Adds modification command to bulk. If relevant, also adds to the list of
	 * investigationIds which may contain relevant information (e.g. nested
	 * InvestigationUsers).
	 * 
	 * @param httpclient       The client being used to send HTTP
	 * @param bulk             OpensearchBulk object recording the requests for
	 *                         updates and aggregations
	 * @param id               Id of the entity.
	 * @param index            Index of the entity.
	 * @param document         JsonObject containing the key value pairs of
	 *                         the
	 *                         document fields.
	 * @param modificationType The type of operation to be performed.
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private void modifyEntity(CloseableHttpClient httpclient, OpensearchBulk bulk, String id, String index,
			JsonObject document, ModificationType modificationType)
			throws ClientProtocolException, IOException, URISyntaxException {

		JsonObject targetObject = Json.createObjectBuilder().add("_id", new Long(id)).add("_index", index).build();
		JsonObject update = Json.createObjectBuilder().add("update", targetObject).build();
		JsonObject docAsUpsert;
		switch (modificationType) {
			case CREATE:
				docAsUpsert = Json.createObjectBuilder().add("doc", document).add("doc_as_upsert", true).build();
				bulk.bulkBuilder.append(update.toString()).append("\n").append(docAsUpsert.toString()).append("\n");
				if (document.containsKey("investigation.id")) {
					// In principle a Dataset/Datafile could be created after InvestigationUser
					// entities are attached to an Investigation, so need to check for those
					bulk.investigationIds.add(document.getString("investigation.id"));
				}
				break;
			case UPDATE:
				docAsUpsert = Json.createObjectBuilder().add("doc", document).add("doc_as_upsert", true).build();
				bulk.bulkBuilder.append(update.toString()).append("\n").append(docAsUpsert.toString()).append("\n");
				break;
			case DELETE:
				bulk.deletionBuilder.append(Json.createObjectBuilder().add("delete", targetObject).build().toString())
						.append("\n");
				break;
		}
		if (aggregateFiles && index.equals("datafile") && document.containsKey("fileSize")) {
			aggregateFiles(modificationType, bulk, index, document, httpclient, id);
		}
	}

	/**
	 * Aggregates any change to file size to relevant paret entities.
	 * 
	 * @param modificationType The type of operation to be performed
	 * @param bulk             OpensearchBulk object recording the requests for
	 *                         updates and aggregations
	 * @param index            Index of the entity
	 * @param document         Document containing the parent entity ids
	 * @param httpclient       CloseableHttpClient to use
	 * @param id               Datafile id
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void aggregateFiles(ModificationType modificationType, OpensearchBulk bulk, String index,
			JsonObject document, CloseableHttpClient httpclient, String id)
			throws ClientProtocolException, IOException, URISyntaxException {
		long deltaFileSize = 0;
		long deltaFileCount = 0;
		switch (modificationType) {
			case CREATE:
				deltaFileSize = document.getJsonNumber("fileSize").longValueExact();
				deltaFileCount = 1;
				break;
			case UPDATE:
				deltaFileSize = document.getJsonNumber("fileSize").longValueExact() - extractFileSize(httpclient, id);
				break;
			case DELETE:
				deltaFileSize = -extractFileSize(httpclient, id);
				deltaFileCount = -1;
				break;
		}
		incrementEntity(bulk.investigationAggregations, document, deltaFileSize, deltaFileCount, "investigation.id");
		incrementEntity(bulk.datasetAggregations, document, deltaFileSize, deltaFileCount, "dataset.id");
	}

	/**
	 * Increments the changes to a parent entity by the values of deltaFileSize and
	 * deltaFileCount.
	 * 
	 * @param aggregations   Map of aggregated fileSize changes with the parent ids
	 *                       as keys.
	 * @param document       Document containing the parent entity id
	 * @param deltaFileSize  Change in file size
	 * @param deltaFileCount Change in file count
	 * @param idField        The field of the id of parent entity to be incremented
	 */
	private void incrementEntity(Map<String, long[]> aggregations, JsonObject document, long deltaFileSize,
			long deltaFileCount, String idField) {
		if (document.containsKey(idField)) {
			String id = document.getString(idField);
			long[] runningFileSize = aggregations.getOrDefault(id, new long[] { 0, 0 });
			long[] newValue = new long[] { runningFileSize[0] + deltaFileSize, runningFileSize[1] + deltaFileCount };
			aggregations.put(id, newValue);
		}
	}

	/**
	 * @param httpclient CloseableHttpClient to use
	 * @param id         Datafile id
	 * @return Size of the Datafile in bytes
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 */
	private long extractFileSize(CloseableHttpClient httpclient, String id)
			throws IOException, URISyntaxException, ClientProtocolException {
		JsonObject source = extractSource(httpclient, id);
		if (source != null && source.containsKey("fileSize")) {
			return source.getJsonNumber("fileSize").longValueExact();
		}
		return 0;
	}
}
