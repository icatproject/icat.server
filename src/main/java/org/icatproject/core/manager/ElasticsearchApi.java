package org.icatproject.core.manager;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class ElasticsearchApi extends SearchApi {

	private static ElasticsearchClient client;
	private final static Map<String, Map<String, Property>> INDEX_PROPERTIES = new HashMap<>();
	private final static Map<String, String> TARGET_MAP = new HashMap<>();
	private final static Map<String, String> UPDATE_BY_QUERY_MAP = new HashMap<>();

	static {
		// Add mappings from related entities to the searchable entity they should be
		// flattened to
		TARGET_MAP.put("sample", "investigation");
		TARGET_MAP.put("investigationparameter", "investigation");
		TARGET_MAP.put("datasetparameter", "dataset");
		TARGET_MAP.put("datafileparameter", "datafile");
		TARGET_MAP.put("investigationuser", "investigation");

		// Child entities that should also update by query to other supported entities,
		// not just their direct parent
		UPDATE_BY_QUERY_MAP.put("investigationuser", "investigation");

		// Mapping properties that are common to all TARGETS
		Map<String, Property> commonProperties = new HashMap<>();
		// commonProperties.put("id", new Property.Builder().text(t -> t).build());
		commonProperties.put("id", new Property.Builder().long_(t -> t).build());
		commonProperties.put("text", new Property.Builder().text(t -> t).build());
		commonProperties.put("userName", new Property.Builder().text(t -> t).build());
		commonProperties.put("userFullName", new Property.Builder().text(t -> t).build());
		commonProperties.put("parameterName", new Property.Builder().text(t -> t).build());
		commonProperties.put("parameterUnits", new Property.Builder().text(t -> t).build());
		commonProperties.put("parameterStringValue", new Property.Builder().text(t -> t).build());
		commonProperties.put("parameterDateValue", new Property.Builder().date(d -> d).build());
		commonProperties.put("parameterNumericValue", new Property.Builder().double_(d -> d).build());

		// Datafile
		Map<String, Property> datafileProperties = new HashMap<>();
		datafileProperties.put("date", new Property.Builder().date(d -> d).build());
		datafileProperties.put("dataset", new Property.Builder().text(t -> t).build());
		datafileProperties.put("investigation", new Property.Builder().text(t -> t).build());
		INDEX_PROPERTIES.put("datafile", datafileProperties);

		// Dataset
		Map<String, Property> datasetProperties = new HashMap<>();
		datasetProperties.put("startDate", new Property.Builder().date(d -> d).build());
		datasetProperties.put("endDate", new Property.Builder().date(d -> d).build());
		datasetProperties.put("investigation", new Property.Builder().text(t -> t).build());
		INDEX_PROPERTIES.put("dataset", datasetProperties);

		// Investigation
		Map<String, Property> investigationProperties = new HashMap<>();
		investigationProperties.put("startDate", new Property.Builder().date(d -> d).build());
		investigationProperties.put("endDate", new Property.Builder().date(d -> d).build());
		investigationProperties.put("sampleName", new Property.Builder().text(t -> t).build());
		investigationProperties.put("sampleText", new Property.Builder().text(t -> t).build());
		INDEX_PROPERTIES.put("investigation", investigationProperties);
	}

	// Maps Elasticsearch Points In Time (PIT) to the number of results to skip
	// for successive searching
	private final Map<String, Integer> pitMap = new HashMap<>();

	public ElasticsearchApi(List<URL> servers) throws IcatException {
		super(null);
		List<HttpHost> hosts = new ArrayList<HttpHost>();
		for (URL server : servers) {
			hosts.add(new HttpHost(server.getHost(), server.getPort(), server.getProtocol()));
		}
		RestClient restClient = RestClient.builder(hosts.toArray(new HttpHost[1])).build();
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		new JacksonJsonpMapper();
		client = new ElasticsearchClient(transport);
		initMappings();
	}

	public void initMappings() throws IcatException {
		try {
			client.cluster().putSettings(s -> s.persistent("action.auto_create_index", JsonData.of(false)));
			client.putScript(p -> p.id("update_user").script(s -> s
					.lang("painless")
					.source("if (ctx._source.userName == null) {ctx._source.userName = params['userName']}"
							+ "else {ctx._source.userName.addAll(params['userName'])}"
							+ "if (ctx._source.userFullName == null) {ctx._source.userFullName = params['userFullName']}"
							+ "else {ctx._source.userFullName.addAll(params['userFullName'])}")));
			client.putScript(p -> p.id("update_sample").script(s -> s
					.lang("painless")
					.source("if (ctx._source.sampleName == null) {ctx._source.sampleName = params['sampleName']}"
							+ "else {ctx._source.sampleName.addAll(params['sampleName'])}"
							+ "if (ctx._source.sampleText == null) {ctx._source.sampleText = params['sampleText']}"
							+ "else {ctx._source.sampleText.addAll(params['sampleText'])}")));
			client.putScript(p -> p.id("update_parameter").script(s -> s
					.lang("painless")
					.source("if (ctx._source.parameterName == null) {ctx._source.parameterName = params['parameterName']}"
							+ "else {ctx._source.parameterName.addAll(params['parameterName'])}"
							+ "if (ctx._source.parameterUnits == null) {ctx._source.parameterUnits = params['parameterUnits']}"
							+ "else {ctx._source.parameterUnits.addAll(params['parameterUnits'])}"
							+ "if (ctx._source.parameterStringValue == null) {ctx._source.parameterStringValue = params['parameterStringValue']}"
							+ "else {ctx._source.parameterStringValue.addAll(params['parameterStringValue'])}"
							+ "if (ctx._source.parameterDateValue == null) {ctx._source.parameterDateValue = params['parameterDateValue']}"
							+ "else {ctx._source.parameterDateValue.addAll(params['parameterDateValue'])}"
							+ "if (ctx._source.parameterNumericValue == null) {ctx._source.parameterNumericValue = params['parameterNumericValue']}"
							+ "else {ctx._source.parameterNumericValue.addAll(params['parameterNumericValue'])}")));

			for (String index : INDEX_PROPERTIES.keySet()) {
				client.indices()
						.create(c -> c.index(index)
								.mappings(m -> m.dynamic(DynamicMapping.False).properties(INDEX_PROPERTIES.get(index))))
						.acknowledged();
			}
			// TODO consider both dynamic field names and nested fields
		} catch (ElasticsearchException | IOException e) {
			logger.warn("Unable to initialise mappings due to error {}, {}", e.getClass(), e.getMessage());
		}
	}

	@Override
	public void clear() throws IcatException {
		try {
			commit();
			client.deleteByQuery(d -> d.index("_all").query(q -> q.matchAll(m -> m)));
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void freeSearcher(String uid)
			throws IcatException {
		try {
			pitMap.remove(uid);
			client.closePointInTime(p -> p.id(uid));
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void commit() throws IcatException {
		try {
			logger.debug("Manual commit of Elastic search called, refreshing indices");
			client.indices().refresh();
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public List<FacetDimension> facetSearch(String target, JsonObject facetQuery, Integer maxResults, int maxLabels) throws IcatException {
		// TODO this should be generalised
		return null;
		// try {
		// 	String index = "_all";
		// 	Set<String> fields = facetQuery.keySet();
		// 	BoolQuery.Builder builder = new BoolQuery.Builder();
		// 	for (String field : fields) {
		// 		// Only expecting a target and text field as part of the current facet
		// 		// implementation
		// 		if (field.equals("target")) {
		// 			index = facetQuery.getString("target").toLowerCase();
		// 		} else if (field.equals("text")) {
		// 			String text = facetQuery.getString("text");
		// 			builder.must(m -> m.queryString(q -> q.defaultField("text").query(text)));
		// 		}
		// 	}
		// 	String indexFinal = index;
		// 	SearchResponse<Object> response = client.search(s -> s
		// 			.index(indexFinal)
		// 			.size(maxResults)
		// 			.query(q -> q.bool(builder.build()))
		// 			.aggregations("samples", a -> a.terms(t -> t.field("sampleName").size(maxLabels)))
		// 			.aggregations("parameters", a -> a.terms(t -> t.field("parameterName").size(maxLabels))),
		// 			Object.class);

		// 	List<StringTermsBucket> sampleBuckets = response.aggregations().get("samples").sterms().buckets().array();
		// 	List<StringTermsBucket> parameterBuckets = response.aggregations().get("parameters").sterms().buckets()
		// 			.array();
		// 	List<FacetDimension> facetDimensions = new ArrayList<>();
		// 	FacetDimension sampleDimension = new FacetDimension("sampleName");
		// 	FacetDimension parameterDimension = new FacetDimension("parameterName");
		// 	for (StringTermsBucket sampleBucket : sampleBuckets) {
		// 		sampleDimension.getFacets().add(new FacetLabel(sampleBucket.key(), sampleBucket.docCount()));
		// 	}
		// 	for (StringTermsBucket parameterBucket : parameterBuckets) {
		// 		parameterDimension.getFacets().add(new FacetLabel(parameterBucket.key(), parameterBucket.docCount()));
		// 	}
		// 	facetDimensions.add(sampleDimension);
		// 	facetDimensions.add(parameterDimension);
		// 	return facetDimensions;
		// } catch (ElasticsearchException | IOException e) {
		// 	throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		// }
	}

	// @Override
	// public SearchResult getResults(JsonObject query, int maxResults, String sort)
	// throws IcatException {
	// // TODO sort argument not supported
	// try {
	// String index;
	// if (query.keySet().contains("target")) {
	// index = query.getString("target").toLowerCase();
	// } else {
	// index = query.getString("_all");
	// }
	// OpenPointInTimeResponse pitResponse = client.openPointInTime(p -> p
	// .index(index)
	// .keepAlive(t -> t.time("1m")));
	// String pit = pitResponse.id();
	// pitMap.put(pit, 0);
	// return getResults(pit, query, maxResults);
	// } catch (ElasticsearchException | IOException e) {
	// throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " +
	// e.getMessage());
	// }
	// }

	// @Override
	// public SearchResult getResults(String uid, JsonObject query, int maxResults)
	// throws IcatException {
	// try {
	// logger.debug("getResults for query: {}", query.toString());
	// Set<String> fields = query.keySet();
	// BoolQuery.Builder builder = new BoolQuery.Builder();
	// for (String field : fields) {
	// if (field.equals("text")) {
	// String text = query.getString("text");
	// builder.must(m -> m.queryString(q -> q.defaultField("text").query(text)));
	// } else if (field.equals("lower")) {
	// Long time = decodeTime(query.getString("lower"));
	// builder.must(m -> m
	// .bool(b -> b
	// .should(s -> s
	// .range(r -> r
	// .field("date")
	// .gte(JsonData.of(time))))
	// .should(s -> s
	// .range(r -> r
	// .field("startDate")
	// .gte(JsonData.of(time))))));
	// } else if (field.equals("upper")) {
	// Long time = decodeTime(query.getString("upper"));
	// builder.must(m -> m
	// .bool(b -> b
	// .should(s -> s
	// .range(r -> r
	// .field("date")
	// .lte(JsonData.of(time))))
	// .should(s -> s
	// .range(r -> r
	// .field("endDate")
	// .lte(JsonData.of(time))))));
	// } else if (field.equals("user")) {
	// String user = query.getString("user");
	// builder.filter(f -> f.match(t -> t
	// .field("userName")
	// .operator(Operator.And)
	// .query(q -> q.stringValue(user))));
	// } else if (field.equals("userFullName")) {
	// String userFullName = query.getString("userFullName");
	// builder.filter(f -> f.queryString(q ->
	// q.defaultField("userFullName").query(userFullName)));
	// } else if (field.equals("samples")) {
	// JsonArray samples = query.getJsonArray("samples");
	// for (int i = 0; i < samples.size(); i++) {
	// String sample = samples.getString(i);
	// builder.filter(
	// f -> f.queryString(q -> q.defaultField("sampleText").query(sample)));
	// }
	// } else if (field.equals("parameters")) {
	// for (JsonValue parameterValue : query.getJsonArray("parameters")) {
	// // TODO there are more things to support and consider here... e.g. parameters
	// // with a numeric range not a numeric value
	// BoolQuery.Builder parameterBuilder = new BoolQuery.Builder();
	// JsonObject parameterObject = (JsonObject) parameterValue;
	// String name = parameterObject.getString("name", null);
	// String units = parameterObject.getString("units", null);
	// String stringValue = parameterObject.getString("stringValue", null);
	// Long lowerDate = decodeTime(parameterObject.getString("lowerDateValue",
	// null));
	// Long upperDate = decodeTime(parameterObject.getString("upperDateValue",
	// null));
	// JsonNumber lowerNumeric = parameterObject.getJsonNumber("lowerNumericValue");
	// JsonNumber upperNumeric = parameterObject.getJsonNumber("upperNumericValue");
	// if (name != null) {
	// parameterBuilder.must(m -> m.match(a ->
	// a.field("parameterName").operator(Operator.And)
	// .query(q -> q.stringValue(name))));
	// }
	// if (units != null) {
	// parameterBuilder.must(m -> m.match(a ->
	// a.field("parameterUnits").operator(Operator.And)
	// .query(q -> q.stringValue(units))));
	// }
	// if (stringValue != null) {
	// parameterBuilder.must(m -> m.match(a -> a.field("parameterStringValue")
	// .operator(Operator.And).query(q -> q.stringValue(stringValue))));
	// } else if (lowerDate != null && upperDate != null) {
	// parameterBuilder.must(m -> m.range(r -> r.field("parameterDateValue")
	// .gte(JsonData.of(lowerDate)).lte(JsonData.of(upperDate))));
	// } else if (lowerNumeric != null && upperNumeric != null) {
	// parameterBuilder.must(m -> m.range(
	// r ->
	// r.field("parameterNumericValue").gte(JsonData.of(lowerNumeric.doubleValue()))
	// .lte(JsonData.of(upperNumeric.doubleValue()))));
	// }
	// builder.filter(f -> f.bool(b -> parameterBuilder));
	// }
	// // TODO consider support for other fields (would require dynamic fields)
	// }
	// }
	// Integer from = pitMap.get(uid);
	// SearchResponse<ElasticsearchDocument> response = client.search(s -> s
	// .size(maxResults)
	// .pit(p -> p.id(uid).keepAlive(t -> t.time("1m")))
	// .query(q -> q.bool(builder.build()))
	// // TODO check the ordering?
	// .from(from)
	// .sort(o -> o.score(c -> c.order(SortOrder.Desc)))
	// .sort(o -> o.field(f -> f.field("id").order(SortOrder.Asc))),
	// ElasticsearchDocument.class);
	// SearchResult result = new SearchResult();
	// // result.setUid(uid);
	// pitMap.put(uid, from + maxResults);
	// List<ScoredEntityBaseBean> entities = result.getResults();
	// for (Hit<ElasticsearchDocument> hit : response.hits().hits()) {
	// entities.add(new ScoredEntityBaseBean(Long.parseLong(hit.id()),
	// hit.score().floatValue(), ""));
	// }
	// return result;
	// } catch (ElasticsearchException | IOException | ParseException e) {
	// throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " +
	// e.getMessage());
	// }
	// }

	@Override
	public void modify(String json) throws IcatException {
		// Format should be [[<index>, <id>, <object>], ...]
		logger.debug("modify: {}", json);
		JsonReader jsonReader = Json.createReader(new StringReader(json));
		JsonArray outerArray = jsonReader.readArray();
		List<BulkOperation> operations = new ArrayList<>();
		List<UpdateByQueryRequest.Builder> updateByQueryRequests = new ArrayList<>();
		Map<String, ElasticsearchDocument> investigationsMap = new HashMap<>();
		try {
			for (JsonArray innerArray : outerArray.getValuesAs(JsonArray.class)) {
				// Index should always be present, and be recognised
				if (innerArray.isNull(0)) {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"Cannot modify a document without the target index");
				}
				String index = innerArray.getString(0).toLowerCase();
				if (!INDEX_PROPERTIES.keySet().contains(index)) {
					if (UPDATE_BY_QUERY_MAP.containsKey(index)) {
						String parentIndex = TARGET_MAP.get(index);
						if (!innerArray.isNull(2)) {
							// Both creating and updates are handled by the index operation
							// ElasticsearchDocument document = buildDocument(innerArray.getJsonArray(2),
							// index, parentIndex);
							logger.trace("{}, {}, {}", innerArray.getJsonArray(2).toString(), index, parentIndex);
							ElasticsearchDocument document = new ElasticsearchDocument(innerArray.getJsonArray(2),
									index, parentIndex);
							logger.trace(document.toString());
							// String documentId = document.getId();
							Long documentId = document.getId();
							if (documentId == null) {
								throw new IcatException(IcatExceptionType.BAD_PARAMETER,
										"Cannot index a document without an id");
							}
							// TODO generalise, currently this assumes we have a user
							ArrayList<String> indices = new ArrayList<>(INDEX_PROPERTIES.keySet());
							indices.remove(parentIndex);
							logger.debug("Adding update by query with: {}, {}, {}, {}", parentIndex, documentId,
									document.getUserName(), document.getUserFullName());
							updateByQueryRequests.add(new UpdateByQueryRequest.Builder()
									.index(indices)
									.query(q -> q.term(
											t -> t.field(parentIndex).value(v -> v.stringValue(documentId.toString()))))
									.script(s -> s.stored(i -> i
											.id("update_user")
											.params("userName", JsonData.of(document.getUserName()))
											.params("userFullName", JsonData.of(document.getUserFullName())))));
						}
					}
					if (TARGET_MAP.containsKey(index)) {
						String parentIndex = TARGET_MAP.get(index);
						if (innerArray.isNull(2)) {
							// TODO we need to delete all the fields on the parent entitity that start with
							// the sample name, this should be possible I think...?
							// But we have can't because we provide the child ID, but never index this
							// Either here, or for Lucene
							logger.warn(
									"Cannot delete document for related entity {}, instead update parent document {}",
									index, parentIndex);
						} else {
							// Both creating and updates are handled by the index operation
							ElasticsearchDocument document = new ElasticsearchDocument(innerArray.getJsonArray(2),
									index, parentIndex);
							Long documentId = document.getId();
							if (documentId == null) {
								throw new IcatException(IcatExceptionType.BAD_PARAMETER,
										"Cannot index a document without an id");
							}
							String scriptId = "update_";
							if (index.equals("investigationuser")) {
								scriptId += "user";
							} else if (index.equals("investigationparameter")) {
								scriptId += "parameter";
							} else if (index.equals("datasetparameter")) {
								scriptId += "parameter";
							} else if (index.equals("datafileparameter")) {
								scriptId += "parameter";
							} else if (index.equals("sample")) {
								scriptId += "sample";
							} else {
								throw new IcatException(IcatExceptionType.BAD_PARAMETER,
										"Cannot map target {} to a parent index");
							}
							String scriptIdFinal = scriptId;
							operations.add(new BulkOperation.Builder().update(c -> c
									.index(parentIndex)
									.id(documentId.toString())
									.action(a -> a.upsert(document).script(s -> s.stored(t -> t
											.id(scriptIdFinal)
											.params("userName", JsonData.of(document.getUserName()))
											.params("userFullName", JsonData.of(document.getUserFullName()))
											.params("sampleName", JsonData.of(document.getSampleName()))
											.params("sampleText", JsonData.of(document.getSampleText()))
											.params("parameterName", JsonData.of(document.getParameterName()))
											.params("parameterUnits", JsonData.of(document.getParameterUnits()))
											.params("parameterStringValue",
													JsonData.of(document.getParameterStringValue()))
											.params("parameterDateValue", JsonData.of(document.getParameterDateValue()))
											.params("parameterNumericValue",
													JsonData.of(document.getParameterNumericValue()))))))
									.build());
						}
					} else {
						logger.warn("Cannot index document for unsupported index {}", index);
						continue;
					}
				} else {
					if (innerArray.isNull(2)) {
						// If the representation is null, delete the document with provided id
						if (innerArray.isNull(1)) {
							throw new IcatException(IcatExceptionType.BAD_PARAMETER,
									"Cannot modify document when both the id and object representing its fields are null");
						}
						String id = String.valueOf(innerArray.getInt(1));
						operations.add(new BulkOperation.Builder().delete(c -> c.index(index).id(id)).build());
					} else {
						ElasticsearchDocument document = new ElasticsearchDocument(innerArray.getJsonArray(2));
						// Get information on user, which might be on the parent investigation
						String investigationId = document.getInvestigation();
						logger.debug("Looking for investigations with id: {} for index: {}", investigationId, index);
						if (investigationId != null) {
							ElasticsearchDocument investigation = investigationsMap.get(investigationId);
							if (investigation == null) {
								GetResponse<ElasticsearchDocument> getResponse = client.get(
										g -> g.index("investigation").id(investigationId), ElasticsearchDocument.class);
								if (getResponse.found()) {
									investigation = getResponse.source();
								} else {
									investigation = new ElasticsearchDocument();
								}
								investigationsMap.put(investigationId, investigation);
							}
							document.getUserName().addAll(investigation.getUserName());
							document.getUserFullName().addAll(investigation.getUserFullName());
						}

						// TODO REVERT?
						// Both creating and updates are handled by the index operation
						String id;
						if (innerArray.isNull(1)) {
							// If we weren't given an id, try and get one from the document
							// Avoid using a generated id, as this prevents us updating the document later
							Long documentId = document.getId();
							if (documentId == null) {
								throw new IcatException(IcatExceptionType.BAD_PARAMETER,
										"Cannot index a document without an id");
							}
							id = documentId.toString();
						} else {
							id = String.valueOf(innerArray.getInt(1));
						}
						operations.add(new BulkOperation.Builder().update(c -> c
								.index(index)
								.id(id)
								.action(a -> a.doc(document).docAsUpsert(true))).build());
					}
				}
			}
			BulkResponse bulkResponse = client.bulk(c -> c.operations(operations));
			if (bulkResponse.errors()) {
				// Throw an Exception for the first error we had in the list of operations
				for (BulkResponseItem responseItem : bulkResponse.items()) {
					if (responseItem.error() != null) {
						throw new IcatException(IcatExceptionType.INTERNAL, responseItem.error().reason());
					}
				}
			}
			// TODO this isn't bulked - a single failure will not invalidate the rest...
			for (UpdateByQueryRequest.Builder request : updateByQueryRequests) {
				commit();
				client.updateByQuery(request.build());
			}
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

}
