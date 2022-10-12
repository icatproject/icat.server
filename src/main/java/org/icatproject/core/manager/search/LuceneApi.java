package org.icatproject.core.manager.search;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.Rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneApi extends SearchApi {

	public String basePath = "/icat.lucene";
	private static final Logger logger = LoggerFactory.getLogger(LuceneApi.class);

	/**
	 * Gets the target index from query and checks its validity.
	 * 
	 * @param query JsonObject containing the criteria to search on.
	 * @return The lowercase target index.
	 * @throws IcatException If "target" was not a key in query, or if the value was
	 *                       not a supported index.
	 */
	private static String getTargetPath(JsonObject query) throws IcatException {
		if (!query.containsKey("target")) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"'target' must be present in query for LuceneApi, but it was " + query);
		}
		String path = query.getString("target").toLowerCase();
		if (!indices.contains(path)) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"'target' must be one of " + indices + ", but it was " + path);
		}
		return path;
	}

	@Override
	public JsonObject buildSearchAfter(ScoredEntityBaseBean lastBean, String sort) throws IcatException {
		// As icat.lucene always requires the Lucene id, shardIndex and score
		// irrespective of the sort, override the default implementation
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("doc", lastBean.getEngineDocId());
		builder.add("shardIndex", lastBean.getShardIndex());
		float score = lastBean.getScore();
		if (!Float.isNaN(score)) {
			builder.add("score", score);
		}
		JsonArrayBuilder arrayBuilder;
		if (sort == null || sort.equals("") || sort.equals("{}")) {
			arrayBuilder = Json.createArrayBuilder().add(score);
		} else {
			arrayBuilder = searchAfterArrayBuilder(lastBean, sort);
		}
		builder.add("fields", arrayBuilder.add(lastBean.getEntityBaseBeanId()));
		return builder.build();
	}

	public LuceneApi(URI server) {
		super(server);
	}

	@Override
	public void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor)
			throws IcatException, IOException, URISyntaxException {
		URI uri = new URIBuilder(server).setPath(basePath + "/addNow/" + entityName).build();

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(uri);
			PipedOutputStream beanDocs = new PipedOutputStream();
			httpPost.setEntity(new InputStreamEntity(new PipedInputStream(beanDocs)));
			getBeanDocExecutor.submit(() -> {
				try (JsonGenerator gen = Json.createGenerator(beanDocs)) {
					gen.writeStartArray();
					for (Long id : ids) {
						EntityBaseBean bean = (EntityBaseBean) manager.find(klass, id);
						if (bean != null) {
							gen.writeStartObject();
							bean.getDoc(gen);
							gen.writeEnd();
						}
					}
					gen.writeEnd();
					return null;
				} catch (Exception e) {
					logger.error("About to throw internal exception for ids {} because of", ids, e);
					throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
				} finally {
					manager.close();
				}
			});

			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		}
	}

	@Override
	public void clear() throws IcatException {
		post(basePath + "/clear");
	}

	@Override
	public void commit() throws IcatException {
		post(basePath + "/commit");
	}

	@Override
	public List<FacetDimension> facetSearch(String target, JsonObject facetQuery, Integer maxResults, Integer maxLabels)
			throws IcatException {
		String path = basePath + "/" + target + "/facet";

		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("maxResults", maxResults.toString());
		parameterMap.put("maxLabels", maxLabels.toString());

		JsonObject postResponse = postResponse(path, facetQuery.toString(), parameterMap);

		List<FacetDimension> results = new ArrayList<>();
		JsonObject aggregations = postResponse.getJsonObject("aggregations");
		for (String dimension : aggregations.keySet()) {
			parseFacetsResponse(results, target, dimension, aggregations);
		}
		return results;
	}

	@Override
	public SearchResult getResults(JsonObject query, JsonValue searchAfter, Integer blockSize, String sort,
			List<String> fields) throws IcatException {
		String indexPath = getTargetPath(query);

		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("maxResults", blockSize.toString());
		if (searchAfter != null) {
			parameterMap.put("search_after", searchAfter.toString());
		}
		if (sort != null) {
			parameterMap.put("sort", sort);
		}

		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		objectBuilder.add("query", query);
		if (fields != null && fields.size() > 0) {
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			fields.forEach((field) -> arrayBuilder.add(field));
			objectBuilder.add("fields", arrayBuilder.build());
		}
		String queryString = objectBuilder.build().toString();

		JsonObject postResponse = postResponse(basePath + "/" + indexPath, queryString, parameterMap);
		SearchResult lsr = new SearchResult();
		List<ScoredEntityBaseBean> results = lsr.getResults();
		List<JsonObject> resultsArray = postResponse.getJsonArray("results").getValuesAs(JsonObject.class);
		for (JsonObject resultObject : resultsArray) {
			int luceneDocId = resultObject.getInt("_id");
			int shardIndex = resultObject.getInt("_shardIndex");
			float score = Float.NaN;
			if (resultObject.containsKey("_score")) {
				score = resultObject.getJsonNumber("_score").bigDecimalValue().floatValue();
			}
			JsonObject source = resultObject.getJsonObject("_source");
			ScoredEntityBaseBean result = new ScoredEntityBaseBean(luceneDocId, shardIndex, score, source);
			results.add(result);
			logger.trace("Result id {} with score {}", result.getEntityBaseBeanId(), score);
		}
		if (postResponse.containsKey("search_after")) {
			lsr.setSearchAfter(postResponse.getJsonObject("search_after"));
		}

		return lsr;
	}

	/**
	 * Locks the index for entityName, optionally removing all existing documents. While
	 * locked, document modifications will fail (excluding addNow as a result of a
	 * populate thread).
	 * 
	 * A check is also performed against the minId and maxId used for population.
	 * This ensures that no data is duplicated in the index.
	 * 
	 * @param entityName Index to lock.
	 * @param minId      The exclusive minimum ICAT id being populated for. If
	 *                   Documents already exist with an id greater than this, the
	 *                   lock will fail. If null, treated as if it were
	 *                   Long.MIN_VALUE
	 * @param maxId      The inclusive maximum ICAT id being populated for. If
	 *                   Documents already exist with an id less than or equal to
	 *                   this, the lock will fail. If null, treated as if it were
	 *                   Long.MAX_VALUE
	 * @param delete     If true, all existing documents of entityName are deleted.
	 * @throws IcatException
	 */
	@Override
	public void lock(String entityName, Long minId, Long maxId, Boolean delete) throws IcatException {
		String path = basePath + "/lock/" + entityName;
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URIBuilder builder = new URIBuilder(server).setPath(path);
			if (minId != null) {
				builder.addParameter("minId", minId.toString());
			}
			if (maxId != null) {
				builder.addParameter("maxId", maxId.toString());
			}
			if (delete != null) {
				builder.addParameter("delete", delete.toString());
			}
			URI uri = builder.build();
			logger.debug("Making call {}", uri);
			HttpPost httpPost = new HttpPost(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				int code = response.getStatusLine().getStatusCode();
				Rest.checkStatus(response, code == 400 ? IcatExceptionType.BAD_PARAMETER : IcatExceptionType.INTERNAL);
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	/**
	 * Unlocks the index for entityName, committing all pending documents. While
	 * locked, document modifications will fail (excluding addNow as a result of a
	 * populate thread).
	 * 
	 * @param entityName Index to lock.
	 * @throws IcatException
	 */
	@Override
	public void unlock(String entityName) throws IcatException {
		post(basePath + "/unlock/" + entityName);
	}

	@Override
	public void modify(String json) throws IcatException {
		post(basePath + "/modify", json);
	}

}
