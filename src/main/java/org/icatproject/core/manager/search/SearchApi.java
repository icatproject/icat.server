package org.icatproject.core.manager.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.Rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SearchApi {

	protected static final Logger logger = LoggerFactory.getLogger(SearchApi.class);
	protected static SimpleDateFormat df;
	protected static final Set<String> indices = new HashSet<>(Arrays.asList("datafile", "dataset", "investigation"));

	protected URI server;

	static {
		df = new SimpleDateFormat("yyyyMMddHHmm");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
	}

	public SearchApi(URI server) {
		this.server = server;
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
	 * Encodes the deletion of the provided entity as Json.
	 * 
	 * @param bean Entity to be deleted from the search engine index.
	 * @return String of Json in the format
	 *         <code>{"delete": {"_index": `entityName`, "_id": `id`}}</code>
	 */
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

	/**
	 * Writes a key value pair to the JsonGenerator being used to encode an entity.
	 * 
	 * @param gen   JsonGenerator being used to encode.
	 * @param name  Name of the field.
	 * @param value Double value to encode as a double.
	 */
	public static void encodeDouble(JsonGenerator gen, String name, Double value) {
		gen.write(name, value);
	}

	/**
	 * Writes a key value pair to the JsonGenerator being used to encode an entity.
	 * 
	 * @param gen   JsonGenerator being used to encode.
	 * @param name  Name of the field.
	 * @param value Date value to encode as a long.
	 */
	public static void encodeLong(JsonGenerator gen, String name, Date value) {
		gen.write(name, value.getTime());
	}

	/**
	 * Writes a key value pair to the JsonGenerator being used to encode an entity.
	 * 
	 * @param gen   JsonGenerator being used to encode.
	 * @param name  Name of the field.
	 * @param value Long value to encode as a long.
	 */
	public static void encodeLong(JsonGenerator gen, String name, Long value) {
		gen.write(name, value);
	}

	/**
	 * Encodes the creation or updating of the provided entity as Json.
	 * 
	 * @param operation The operation to encode. Should either be "create" or
	 *                  "update".
	 * @param bean      Entity to perform the operation on.
	 * @return String of Json in the format
	 *         <code>{`operation`: {"_index": `entityName`, "_id": `id`, "doc": {...}}}</code>
	 * @throws IcatException
	 */
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

	/**
	 * Writes a key value pair to the JsonGenerator being used to encode an entity.
	 * 
	 * @param gen   JsonGenerator being used to encode.
	 * @param name  Name of the field.
	 * @param value Long value to encode as a string.
	 */
	public static void encodeString(JsonGenerator gen, String name, Long value) {
		gen.write(name, Long.toString(value));
	}

	/**
	 * Writes a key value pair to the JsonGenerator being used to encode an entity.
	 * 
	 * @param gen   JsonGenerator being used to encode.
	 * @param name  Name of the field.
	 * @param value String value to encode as a string.
	 */
	public static void encodeString(JsonGenerator gen, String name, String value) {
		gen.write(name, value);
	}

	/**
	 * Writes a key value pair to the JsonGenerator being used to encode an entity,
	 * provided that value is not null.
	 * 
	 * @param gen   JsonGenerator being used to encode.
	 * @param name  Name of the field.
	 * @param value String value to encode as a string.
	 */
	public static void encodeText(JsonGenerator gen, String name, String value) {
		if (value != null) {
			gen.write(name, value);
		}
	}

	/**
	 * Builds a Json representation of the final search result based on the sort
	 * criteria used. This allows future searches to efficiently "search after" this
	 * result.
	 * 
	 * @param lastBean The last ScoredEntityBaseBean of the current search results.
	 * @param sort     String representing a JsonObject of sort criteria.
	 * @return JsonValue representing the lastBean to allow future searches to
	 *         search after it.
	 * @throws IcatException If the score of the lastBean is NaN, or one of the sort
	 *                       fields is not present in the source of the lastBean.
	 */
	public JsonValue buildSearchAfter(ScoredEntityBaseBean lastBean, String sort) throws IcatException {
		JsonArrayBuilder arrayBuilder;
		if (sort != null && !sort.equals("") || sort.equals("{}")) {
			arrayBuilder = searchAfterArrayBuilder(lastBean, sort);
		} else {
			arrayBuilder = Json.createArrayBuilder();
			if (Float.isNaN(lastBean.getScore())) {
				throw new IcatException(IcatExceptionType.INTERNAL,
						"Cannot build searchAfter document from source as score was NaN.");
			}
			arrayBuilder.add(lastBean.getScore());
		}
		arrayBuilder.add(lastBean.getEntityBaseBeanId());
		return arrayBuilder.build();
	}

	/**
	 * Builds a Json representation of the sorted fields of the final search result.
	 * This allows future searches to efficiently "search after" this result.
	 * 
	 * @param lastBean The last ScoredEntityBaseBean of the current search results.
	 * @param sort     String representing a JsonObject of sort criteria.
	 * @return JsonArray representing the sorted fields to allow future searches to
	 *         search after it.
	 * @throws IcatException If one of the sort fields is not present in the source
	 *                       of the lastBean.
	 */
	protected static JsonArrayBuilder searchAfterArrayBuilder(ScoredEntityBaseBean lastBean, String sort)
			throws IcatException {
		try (JsonReader reader = Json.createReader(new StringReader(sort))) {
			JsonObject object = reader.readObject();
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for (String key : object.keySet()) {
				if (!lastBean.getSource().containsKey(key)) {
					throw new IcatException(IcatExceptionType.INTERNAL,
							"Cannot build searchAfter document from source as sorted field " + key + " missing.");
				}
				JsonValue value = lastBean.getSource().get(key);
				arrayBuilder.add(value);
			}
			return arrayBuilder;
		}
	}

	/**
	 * Parses the JsonObject response from the search engine into a FacetDimension,
	 * and adds it to results.
	 * 
	 * @param results      List of FacetDimensions to add the results from this
	 *                     dimension to.
	 * @param target       The entity being targeted.
	 * @param dimension    The dimension (field) being faceted.
	 * @param aggregations JsonObject containing the response from the search
	 *                     engine.
	 * @throws IcatException
	 */
	protected static void parseFacetsResponse(List<FacetDimension> results, String target, String dimension,
			JsonObject aggregations) throws IcatException {
		if (dimension.equals("doc_count")) {
			// For nested aggregations, there is a doc_count entry at the same level as the
			// dimension objects, but we're not interested in this
			return;
		}
		FacetDimension facetDimension = new FacetDimension(target, dimension);
		List<FacetLabel> facets = facetDimension.getFacets();

		JsonObject aggregation = aggregations.getJsonObject(dimension);
		JsonValue bucketsValue = aggregation.get("buckets");
		ValueType valueType = bucketsValue.getValueType();
		switch (valueType) {
			case ARRAY:
				List<JsonObject> buckets = ((JsonArray) bucketsValue).getValuesAs(JsonObject.class);
				if (buckets.size() == 0) {
					return;
				}
				for (JsonObject bucket : buckets) {
					FacetLabel facetLabel = new FacetLabel(bucket);
					facets.add(facetLabel);
				}
				break;
			case OBJECT:
				JsonObject bucketsObject = (JsonObject) bucketsValue;
				Set<String> keySet = bucketsObject.keySet();
				if (keySet.size() == 0) {
					return;
				}
				for (String key : keySet) {
					JsonObject bucket = bucketsObject.getJsonObject(key);
					FacetLabel facetLabel = new FacetLabel(key, bucket);
					facets.add(facetLabel);
				}
				break;
			default:
				String msg = "Expected 'buckets' to have ARRAY or OBJECT type, but it was " + valueType;
				throw new IcatException(IcatExceptionType.INTERNAL, msg);
		}
		results.add(facetDimension);
	}

	/**
	 * Adds documents to the index identified by entityName immediately.
	 * Practically, this should be used for populating documents from existing
	 * database records as opposed to adding documents as they are created.
	 * 
	 * @param entityName         The entity to create documents for.
	 * @param ids                List of ids corresponding to the documents to add.
	 * @param manager            EntityManager for finding the beans from their id.
	 * @param klass              Class of the entity to create documents for.
	 * @param getBeanDocExecutor
	 * @throws IcatException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public abstract void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor)
			throws IcatException, IOException, URISyntaxException;

	/**
	 * This is only for testing purposes. Other calls to the service will not
	 * work properly while this operation is in progress.
	 * 
	 * Deletes all documents across all indices.
	 * 
	 * @throws IcatException
	 */
	public abstract void clear() throws IcatException;

	/**
	 * Commits any pending documents to their respective index.
	 * 
	 * @throws IcatException
	 */
	public abstract void commit() throws IcatException;

	/**
	 * Perform faceting on an entity/index. The query associated with the request
	 * should determine which Documents to consider, and optionally the dimensions
	 * to facet. If no dimensions are provided, "sparse" faceting is performed
	 * across relevant string fields (but no Range faceting occurs).
	 * 
	 * @param target     Name of the entity/index to facet on.
	 * @param facetQuery JsonObject containing the criteria to facet on.
	 * @param maxResults The maximum number of results to include in the returned
	 *                   Json.
	 * @param maxLabels  The maximum number of labels to return for each dimension
	 *                   of the facets.
	 * @return List of FacetDimensions that were collected for the query.
	 * @throws IcatException
	 */
	public abstract List<FacetDimension> facetSearch(String target, JsonObject facetQuery, Integer maxResults,
			Integer maxLabels) throws IcatException;

	/**
	 * Gets SearchResult for query without sort or searchAfter (pagination).
	 * 
	 * @param query      JsonObject containing the criteria to search on.
	 * @param maxResults Maximum number of results to retrieve from the engine.
	 * @return SearchResult for the query.
	 * @throws IcatException
	 */
	public SearchResult getResults(JsonObject query, int maxResults) throws IcatException {
		return getResults(query, null, maxResults, null, Arrays.asList("id"));
	}

	/**
	 * Gets SearchResult for query without searchAfter (pagination).
	 * 
	 * @param query      JsonObject containing the criteria to search on.
	 * @param maxResults Maximum number of results to retrieve from the engine.
	 * @param sort       String of Json representing the sort criteria.
	 * @return SearchResult for the query.
	 * @throws IcatException
	 */
	public SearchResult getResults(JsonObject query, int maxResults, String sort) throws IcatException {
		return getResults(query, null, maxResults, sort, Arrays.asList("id"));
	}

	/**
	 * Gets SearchResult for query.
	 * 
	 * @param query           JsonObject containing the criteria to search on.
	 * @param searchAfter     JsonValue representing the last result of a previous
	 *                        search in order to skip results that have already been
	 *                        returned.
	 * @param blockSize       Maximum number of results to retrieve from the engine.
	 * @param sort            String of Json representing the sort criteria.
	 * @param requestedFields List of fields to return in the document source.
	 * @return SearchResult for the query.
	 * @throws IcatException
	 */
	public abstract SearchResult getResults(JsonObject query, JsonValue searchAfter, Integer blockSize, String sort,
			List<String> requestedFields) throws IcatException;

	/**
	 * Not implemented.
	 * 
	 * @param entityName
	 * @param minId
	 * @param maxId
	 * @param delete
	 * @throws IcatException
	 */
	public void lock(String entityName, Long minId, Long maxId, Boolean delete) throws IcatException {
		logger.info("Manually locking index not supported, no request sent");
	}

	/**
	 * Not implemented.
	 * 
	 * @param entityName
	 * @throws IcatException
	 */
	public void unlock(String entityName) throws IcatException {
		logger.info("Manually unlocking index not supported, no request sent");
	}

	/**
	 * Perform one or more document modification operations.
	 * 
	 * @param json String of a JsonArray containing individual create/update/delete
	 *             operations as JsonObjects.
	 * @throws IcatException
	 */
	public abstract void modify(String json) throws IcatException;

	/**
	 * POST to path without a body or response handling.
	 * 
	 * @param path Path on the search engine to POST to.
	 * @throws IcatException
	 */
	protected void post(String path) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(path).build();
			HttpPost httpPost = new HttpPost(uri);
			logger.trace("Making call {}", uri);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	/**
	 * POST to path with a body but without response handling.
	 * 
	 * @param path Path on the search engine to POST to.
	 * @param body String of Json to send as the request body.
	 * @throws IcatException
	 */
	protected void post(String path, String body) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(path).build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			logger.trace("Making call {} with body {}", uri, body);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	/**
	 * POST to path with a body and response handling.
	 * 
	 * @param path         Path on the search engine to POST to.
	 * @param body         String of Json to send as the request body.
	 * @return JsonObject returned by the search engine.
	 * @throws IcatException
	 */
	protected JsonObject postResponse(String path, String body) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(path).build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			logger.trace("Making call {} with body {}", uri, body);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				JsonReader jsonReader = Json.createReader(response.getEntity().getContent());
				return jsonReader.readObject();
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}


	/**
	 * POST to path with a body and response handling.
	 * 
	 * @param path         Path on the search engine to POST to.
	 * @param body         String of Json to send as the request body.
	 * @param parameterMap Map of parameters to encode in the URI.
	 * @return JsonObject returned by the search engine.
	 * @throws IcatException
	 */
	protected JsonObject postResponse(String path, String body, Map<String, String> parameterMap) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URIBuilder builder = new URIBuilder(server).setPath(path);
			for (Entry<String, String> entry : parameterMap.entrySet()) {
				builder.addParameter(entry.getKey(), entry.getValue());
			}
			URI uri = builder.build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
			logger.trace("Making call {} with body {}", uri, body);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				int code = response.getStatusLine().getStatusCode();
				Rest.checkStatus(response, code == 400 ? IcatExceptionType.BAD_PARAMETER : IcatExceptionType.INTERNAL);
				JsonReader jsonReader = Json.createReader(response.getEntity().getContent());
				return jsonReader.readObject();
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

}
