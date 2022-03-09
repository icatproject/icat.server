package org.icatproject.core.manager;

import java.io.ByteArrayOutputStream;
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
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class ElasticsearchApi extends SearchApi {

	private static ElasticsearchClient client;
	private Map<String, Integer> pitMap = new HashMap<>();

	public ElasticsearchApi(List<URL> servers) {
		List<HttpHost> hosts = new ArrayList<HttpHost>();
		for (URL server : servers) {
			hosts.add(new HttpHost(server.getHost(), server.getPort(), server.getProtocol()));
		}
		RestClient restClient = RestClient.builder(hosts.toArray(new HttpHost[1])).build();
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
		new JacksonJsonpMapper();
		client = new ElasticsearchClient(transport);
		try {
			initMappings();
		} catch (Exception e) {
			logger.warn("ElasticsearchApi init failed when setting explicit mappings");
		}
	}

	private void initMappings() throws IcatException {
		CreateIndexResponse response;
		try {
			response = client.indices().create(c -> c.index("datafile").mappings(m -> m
					.dynamic(DynamicMapping.False)
					.properties("id", p -> p.long_(l -> l))
					.properties("text", p -> p.text(t -> t))
					.properties("date", p -> p.date(d -> d))
					.properties("userName", p -> p.text(t -> t))
					.properties("userFullName", p -> p.text(t -> t))
					.properties("parameterName", p -> p.text(t -> t))
					.properties("parameterUnits", p -> p.text(t -> t))
					.properties("parameterStringValue", p -> p.text(t -> t))
					.properties("parameterDateValue", p -> p.date(d -> d))
					.properties("parameterNumericValue", p -> p.double_(d -> d))));
			response.acknowledged();
			response = client.indices().create(c -> c.index("dataset").mappings(m -> m
					.dynamic(DynamicMapping.False)
					.properties("id", p -> p.long_(l -> l))
					.properties("text", p -> p.text(t -> t))
					.properties("startDate", p -> p.date(d -> d))
					.properties("endDate", p -> p.date(d -> d))
					.properties("userName", p -> p.text(t -> t))
					.properties("userFullName", p -> p.text(t -> t))
					.properties("parameterName", p -> p.text(t -> t))
					.properties("parameterUnits", p -> p.text(t -> t))
					.properties("parameterStringValue", p -> p.text(t -> t))
					.properties("parameterDateValue", p -> p.date(d -> d))
					.properties("parameterNumericValue", p -> p.double_(d -> d))));
			response.acknowledged();
			response = client.indices().create(c -> c.index("investigation").mappings(m -> m
					.dynamic(DynamicMapping.False)
					.properties("id", p -> p.long_(l -> l))
					.properties("text", p -> p.text(t -> t))
					.properties("startDate", p -> p.date(d -> d))
					.properties("endDate", p -> p.date(d -> d))
					.properties("userName", p -> p.text(t -> t))
					.properties("userFullName", p -> p.text(t -> t))
					.properties("sampleName", p -> p.text(t -> t))
					.properties("sampleText", p -> p.text(t -> t))
					.properties("parameterName", p -> p.text(t -> t))
					.properties("parameterUnits", p -> p.text(t -> t))
					.properties("parameterStringValue", p -> p.text(t -> t))
					.properties("parameterDateValue", p -> p.date(d -> d))
					.properties("parameterNumericValue", p -> p.double_(d -> d))));
			response.acknowledged();
			// TODO consider both dynamic field names and nested fields
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor) throws IcatException {
		// getBeanDocExecutor is not used for the Elasticsearch implementation
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartArray();
		for (Long id : ids) {
			EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);
			if (bean != null) {
				gen.writeStartArray();
				gen.write(entityName); // Index
				gen.writeNull(); // Search engine ID is null as we are adding
				bean.getDoc(gen, this); // Fields
				gen.writeEnd();
			}
		}
		gen.writeEnd();
		modify(baos.toString());
	}

	@Override
	public void clear() throws IcatException {
		try {
			client.indices().delete(c -> c.index("_all"));
			initMappings();
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	// TODO Ideally want to write these as k:v pairs in an object, not objects in a
	// list, but this is to be consistent with Lucene

	public void encodeSortedDocValuesField(JsonGenerator gen, String name, Long value) {
		gen.writeStartObject().write(name, value).writeEnd();
	}

	public void encodeStoredId(JsonGenerator gen, Long id) {
		gen.writeStartObject().write("id", Long.toString(id)).writeEnd();
	}

	public void encodeStringField(JsonGenerator gen, String name, Date value) {
		String timeString;
		synchronized (df) {
			timeString = df.format(value);
		}
		gen.writeStartObject().write(name, timeString).writeEnd();
	}

	public void encodeDoublePoint(JsonGenerator gen, String name, Double value) {
		gen.writeStartObject().write(name, value).writeEnd();
	}

	public void encodeSortedSetDocValuesFacetField(JsonGenerator gen, String name, String value) {
		gen.writeStartObject().write(name, value).writeEnd();

	}

	public void encodeStringField(JsonGenerator gen, String name, Long value) {
		gen.writeStartObject().write(name, value).writeEnd();
	}

	public void encodeStringField(JsonGenerator gen, String name, String value) {
		gen.writeStartObject().write(name, value).writeEnd();
	}

	public void encodeTextField(JsonGenerator gen, String name, String value) {
		if (value != null) {
			gen.writeStartObject().write(name, value).writeEnd();
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
			client.indices().refresh();
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public List<FacetDimension> facetSearch(JsonObject facetQuery, int maxResults, int maxLabels) throws IcatException {
		// TODO this should be generalised
		try {
			String index = "_all";
			Set<String> fields = facetQuery.keySet();
			BoolQuery.Builder builder = new BoolQuery.Builder();
			for (String field : fields) {
				// Only expecting a target and text field as part of the current facet
				// implementation
				if (field.equals("target")) {
					index = facetQuery.getString("target").toLowerCase();
				} else if (field.equals("text")) {
					String text = facetQuery.getString("text");
					builder.must(m -> m.queryString(q -> q.defaultField("text").query(text)));
				}
			}
			String indexFinal = index;
			SearchResponse<Object> response = client.search(s -> s
					.index(indexFinal)
					.size(maxResults)
					.query(q -> q.bool(builder.build()))
					.aggregations("samples", a -> a.terms(t -> t.field("sampleName").size(maxLabels)))
					.aggregations("parameters", a -> a.terms(t -> t.field("parameterName").size(maxLabels))),
					Object.class);

			List<StringTermsBucket> sampleBuckets = response.aggregations().get("samples").sterms().buckets().array();
			List<StringTermsBucket> parameterBuckets = response.aggregations().get("parameters").sterms().buckets()
					.array();
			List<FacetDimension> facetDimensions = new ArrayList<>();
			FacetDimension sampleDimension = new FacetDimension("sampleName");
			FacetDimension parameterDimension = new FacetDimension("parameterName");
			for (StringTermsBucket sampleBucket : sampleBuckets) {
				sampleDimension.getFacets().add(new FacetLabel(sampleBucket.key(), sampleBucket.docCount()));
			}
			for (StringTermsBucket parameterBucket : parameterBuckets) {
				parameterDimension.getFacets().add(new FacetLabel(parameterBucket.key(), parameterBucket.docCount()));
			}
			facetDimensions.add(sampleDimension);
			facetDimensions.add(parameterDimension);
			return facetDimensions;
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public SearchResult getResults(JsonObject query, int maxResults)
			throws IcatException {
		try {
			String index;
			if (query.keySet().contains("target")) {
				index = query.getString("target").toLowerCase();
			} else {
				index = query.getString("_all");
			}
			OpenPointInTimeResponse pitResponse = client.openPointInTime(p -> p
					.index(index)
					.keepAlive(t -> t.time("1m")));
			String pit = pitResponse.id();
			pitMap.put(pit, 0);
			return getResults(pit, query, maxResults);
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public SearchResult getResults(String uid, JsonObject query, int maxResults)
			throws IcatException {
		try {
			Set<String> fields = query.keySet();
			BoolQuery.Builder builder = new BoolQuery.Builder();
			for (String field : fields) {
				if (field.equals("text")) {
					String text = query.getString("text");
					builder.must(m -> m.queryString(q -> q.defaultField("text").query(text)));
				} else if (field.equals("lower")) {
					Long time = decodeTime(query.getString("lower"));
					builder.must(m -> m
							.bool(b -> b
									.should(s -> s
											.range(r -> r
													.field("date")
													.gte(JsonData.of(time))))
									.should(s -> s
											.range(r -> r
													.field("startDate")
													.gte(JsonData.of(time))))));
				} else if (field.equals("upper")) {
					Long time = decodeTime(query.getString("upper"));
					builder.must(m -> m
							.bool(b -> b
									.should(s -> s
											.range(r -> r
													.field("date")
													.lte(JsonData.of(time))))
									.should(s -> s
											.range(r -> r
													.field("endDate")
													.lte(JsonData.of(time))))));
				} else if (field.equals("user")) {
					String user = query.getString("user");
					builder.filter(f -> f.term(t -> t.field("userName").value(v -> v.stringValue(user))));
				} else if (field.equals("userFullName")) {
					String userFullName = query.getString("userFullName");
					builder.filter(f -> f.queryString(q -> q.defaultField("userFullName").query(userFullName)));
				} else if (field.equals("samples")) {
					for (JsonValue sampleValue : query.getJsonArray("samples")) {
						builder.filter(
								f -> f.queryString(q -> q.defaultField("sampleText").query(sampleValue.toString())));
					}
				} else if (field.equals("parameters")) {
					for (JsonValue parameterValue : query.getJsonArray("parameters")) {
						// TODO there are more things to support and consider here... e.g. parameters
						// with a numeric range not a numeric value
						BoolQuery.Builder parameterBuilder = new BoolQuery.Builder();
						JsonObject parameterObject = (JsonObject) parameterValue;
						String name = parameterObject.getString("name", null);
						String units = parameterObject.getString("units", null);
						String stringValue = parameterObject.getString("stringValue", null);
						Long lowerDate = decodeTime(parameterObject.getString("lowerDateValue", null));
						Long upperDate = decodeTime(parameterObject.getString("upperDateValue", null));
						JsonNumber lowerNumeric = parameterObject.getJsonNumber("lowerNumericValue");
						JsonNumber upperNumeric = parameterObject.getJsonNumber("upperNumericValue");
						if (name != null) {
							parameterBuilder.must(m -> m.match(a -> a.field("parameterName").operator(Operator.And)
									.query(q -> q.stringValue(name))));
						}
						if (units != null) {
							parameterBuilder.must(m -> m.match(a -> a.field("parameterUnits").operator(Operator.And)
									.query(q -> q.stringValue(units))));
						}
						if (stringValue != null) {
							parameterBuilder.must(m -> m.match(a -> a.field("parameterStringValue")
									.operator(Operator.And).query(q -> q.stringValue(stringValue))));
						} else if (lowerDate != null && upperDate != null) {
							parameterBuilder.must(m -> m.range(r -> r.field("parameterDateValue")
									.gte(JsonData.of(lowerDate)).lte(JsonData.of(upperDate))));
						} else if (lowerNumeric != null && upperNumeric != null) {
							parameterBuilder.must(m -> m.range(
									r -> r.field("parameterNumericValue").gte(JsonData.of(lowerNumeric.doubleValue()))
											.lte(JsonData.of(upperNumeric.doubleValue()))));
						}
						builder.filter(f -> f.bool(b -> parameterBuilder));
					}
					// TODO consider support for other fields (would require dynamic fields)
				}
			}
			Integer from = pitMap.get(uid);
			SearchResponse<ElasticsearchDocument> response = client.search(s -> s
					.size(maxResults)
					.pit(p -> p.id(uid).keepAlive(t -> t.time("1m")))
					.query(q -> q.bool(builder.build()))
					.docvalueFields(d -> d.field("id"))
					.from(from)
					.sort(o -> o.score(c -> c.order(SortOrder.Desc))), ElasticsearchDocument.class);
			SearchResult result = new SearchResult();
			result.setUid(uid);
			pitMap.put(uid, from + maxResults);
			List<ScoredEntityBaseBean> entities = result.getResults();
			for (Hit<ElasticsearchDocument> hit : response.hits().hits()) {
				entities.add(new ScoredEntityBaseBean(hit.source().getId(), hit.score().floatValue()));
			}
			return result;
		} catch (ElasticsearchException | IOException | ParseException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	// TODO does this need to be separated for different entity types?
	private ElasticsearchDocument buildDocument(JsonArray jsonArray) throws IcatException {
		ElasticsearchDocument document = new ElasticsearchDocument();
		try {
			for (JsonValue fieldValue : jsonArray) {
				JsonObject fieldObject = (JsonObject) fieldValue;
				for (Entry<String, JsonValue> fieldEntry : fieldObject.entrySet()) {
					if (fieldEntry.getKey().equals("id")) {
						if (fieldEntry.getValue().getValueType().equals(ValueType.STRING)) {
							document.setId(Long.parseLong(fieldObject.getString("id")));
						} else if (fieldEntry.getValue().getValueType().equals(ValueType.NUMBER)) {
							document.setId((long) fieldObject.getInt("id"));
						}
					} else if (fieldEntry.getKey().equals("text")) {
						document.setText(fieldObject.getString("text"));
					} else if (fieldEntry.getKey().equals("date")) {
						document.setDate(dec(fieldObject.getString("date")));
					} else if (fieldEntry.getKey().equals("startDate")) {
						document.setStartDate(dec(fieldObject.getString("startDate")));
					} else if (fieldEntry.getKey().equals("endDate")) {
						document.setEndDate(dec(fieldObject.getString("endDate")));
					} else if (fieldEntry.getKey().equals("user.name")) {
						document.getUserName().add(fieldObject.getString("user.name"));
					} else if (fieldEntry.getKey().equals("user.fullName")) {
						document.getUserFullName().add(fieldObject.getString("user.fullName"));
					} else if (fieldEntry.getKey().equals("sample.name")) {
						document.getSampleName().add(fieldObject.getString("sample.name"));
					} else if (fieldEntry.getKey().equals("sample.text")) {
						document.getSampleText().add(fieldObject.getString("sample.text"));
					} else if (fieldEntry.getKey().equals("parameter.name")) {
						document.getParameterName().add(fieldObject.getString("parameter.name"));
					} else if (fieldEntry.getKey().equals("parameter.units")) {
						document.getParameterUnits().add(fieldObject.getString("parameter.units"));
					} else if (fieldEntry.getKey().equals("parameter.stringValue")) {
						document.getParameterStringValue().add(fieldObject.getString("parameter.stringValue"));
					} else if (fieldEntry.getKey().equals("parameter.dateValue")) {
						document.getParameterDateValue().add(dec(fieldObject.getString("parameter.dateValue")));
					} else if (fieldEntry.getKey().equals("parameter.numericValue")) {
						document.getParameterNumericValue()
								.add(fieldObject.getJsonNumber("parameter.numericValue").doubleValue());
					}
				}
			}
			return document;
		} catch (ParseException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void modify(String json) throws IcatException {
		// Format should be [[<index>, <id>, <object>], ...]
		JsonReader jsonReader = Json.createReader(new StringReader(json));
		JsonArray outerArray = jsonReader.readArray();
		List<BulkOperation> operations = new ArrayList<>();
		for (JsonArray innerArray : outerArray.getValuesAs(JsonArray.class)) {
			// Index should always be present
			if (innerArray.isNull(0)) {
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"Cannot modify a document without the target index");
			}
			String index = innerArray.getString(0).toLowerCase();

			if (innerArray.isNull(2)) {
				// If the representation is null, delete the document with provided id
				if (innerArray.isNull(1)) {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"Cannot modify document when both the id and object representing its fields are null");
				}
				String id = String.valueOf(innerArray.getInt(1));
				operations.add(new BulkOperation.Builder().delete(c -> c.index(index).id(id)).build());
			} else {
				// Both creating and updates are handled by the index operation
				ElasticsearchDocument document = buildDocument(innerArray.getJsonArray(2));
				String id;
				if (innerArray.isNull(1)) {
					// If we weren't given an id, try and get one from the document
					// Avoid using a generated id, as this prevents us updating the document later
					Long documentId = document.getId();
					if (documentId == null) {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER,
								"Cannot index a document without an id");
					}
					id = String.valueOf(documentId);
				} else {
					id = String.valueOf(innerArray.getInt(1));
				}
				operations
						.add(new BulkOperation.Builder().index(c -> c.index(index).id(id).document(document)).build());
			}
		}
		try {
			BulkResponse response = client.bulk(c -> c.operations(operations));
			if (response.errors()) {
				// Throw an Exception for the first error we had in the list of operations
				for (BulkResponseItem responseItem : response.items()) {
					if (responseItem.error().reason() != "") {
						throw new IcatException(IcatExceptionType.INTERNAL, responseItem.error().reason());
					}
				}
			}
			;
		} catch (ElasticsearchException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

}
