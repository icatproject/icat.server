package org.icatproject.core.manager;

import java.io.ByteArrayInputStream;
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
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;

public class LuceneApi extends SearchApi {
	private enum ParserState {
		None, Results, Dimensions, Labels
	}

	static String basePath = "/icat.lucene";

	/*
	 * Serves as a record of target entities where search is supported, and the
	 * relevant path to the search endpoint
	 */
	private static Map<String, String> targetPaths = new HashMap<>();
	static {
		targetPaths.put("Investigation", "investigations");
		targetPaths.put("Dataset", "datasets");
		targetPaths.put("Datafile", "datafiles");
	}

	private String getTargetPath(JsonObject query) throws IcatException {
		if (!query.containsKey("target")) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"'target' must be present in query for LuceneApi, but it was " + query.toString());
		}
		String path = targetPaths.get(query.getString("target"));
		if (path == null) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER,
					"'target' must be one of " + targetPaths.keySet() + ", but it was " + query.toString());
		}
		return path;
	}

	public LuceneApi(URI server) {
		super(server);
	}

	public void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor)
			throws IcatException, IOException, URISyntaxException {
		URI uri = new URIBuilder(server).setPath(basePath + "/addNow/" + entityName)
				.build();

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
					logger.error("About to throw internal exception because of", e);
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
	public JsonObject buildSearchAfter(ScoredEntityBaseBean lastBean, String sort) throws IcatException {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("doc", lastBean.getEngineDocId());
		builder.add("shardIndex", -1);
		if (!Float.isNaN(lastBean.getScore())) {
			builder.add("score", lastBean.getScore());
		}
		if (sort != null && !sort.equals("")) {
			try (JsonReader reader = Json.createReader(new ByteArrayInputStream(sort.getBytes()))) {
				JsonObject object = reader.readObject();
				JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
				for (String key : object.keySet()) {
					if (!lastBean.getSource().keySet().contains(key)) {
						throw new IcatException(IcatExceptionType.INTERNAL,
								"Cannot build searchAfter document from source as sorted field " + key + " missing.");
					}
					JsonValue value = lastBean.getSource().get(key);
					arrayBuilder.add(value);
				}
				builder.add("fields", arrayBuilder);
			}
		}
		return builder.build();
	}

	@Override
	public void clear() throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/clear").build();
			HttpPost httpPost = new HttpPost(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	@Override
	public void commit() throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/commit").build();
			logger.trace("Making call {}", uri);
			HttpPost httpPost = new HttpPost(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public List<FacetDimension> facetSearch(String target, JsonObject facetQuery, Integer maxResults, int maxLabels) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/" + target + "/facet")
					.setParameter("maxResults", Integer.toString(maxResults))
					.setParameter("maxLabels", Integer.toString(maxLabels)).build();
			logger.trace("Making call {}", uri);
			return getFacets(uri, httpclient, facetQuery.toString(), target);
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private List<FacetDimension> getFacets(URI uri, CloseableHttpClient httpclient, String facetQueryString, String target)
			throws IcatException {
		logger.debug(facetQueryString);
		try {
			StringEntity input = new StringEntity(facetQueryString);
			input.setContentType(MediaType.APPLICATION_JSON);
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(input);

			List<FacetDimension> facetDimensions = new ArrayList<>();
			ParserState state = ParserState.None;
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				try (JsonParser p = Json.createParser(response.getEntity().getContent())) {
					String key = null;
					while (p.hasNext()) {
						// Get next event in the stream
						Event e = p.next();
						if (e.equals(Event.KEY_NAME)) {
							// The key name will indicate the content to expect next, and is expected to be
							// one of
							// "dimensions", a specific dimension, or a label within that dimension.
							key = p.getString();
						} else if (e == Event.START_OBJECT) {
							if (state == ParserState.None && key != null && key.equals("dimensions")) {
								state = ParserState.Dimensions;
							} else if (state == ParserState.Dimensions) {
								facetDimensions.add(new FacetDimension(target, key));
								state = ParserState.Labels;
							}
						} else if (e == (Event.END_OBJECT)) {
							if (state == ParserState.Labels) {
								// We may have multiple dimensions, so change state so we can read the next one
								state = ParserState.Dimensions;
							} else if (state == ParserState.Dimensions) {
								state = ParserState.None;
							}
						} else if (state == ParserState.Labels) {
							FacetDimension currentFacets = facetDimensions.get(facetDimensions.size() - 1);
							currentFacets.getFacets().add(new FacetLabel(key, p.getLong()));
						}
					}
				}
			}
			return facetDimensions;
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public SearchResult getResults(JsonObject query, JsonValue searchAfter, Integer blockSize, String sort,
			List<String> fields) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String indexPath = getTargetPath(query);
			URIBuilder uriBuilder = new URIBuilder(server).setPath(basePath + "/" + indexPath)
					.setParameter("maxResults", Integer.toString(blockSize));
			if (searchAfter != null) {
				uriBuilder.setParameter("search_after", searchAfter.toString());
			}
			if (sort != null) {
				uriBuilder.setParameter("sort", sort);
			}
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("query", query);
			if (fields != null && fields.size() > 0) {
				JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
				fields.forEach((field) -> arrayBuilder.add(field));
				objectBuilder.add("fields", arrayBuilder.build());
			}
			String queryString = objectBuilder.build().toString();
			URI uri = uriBuilder.build();
			logger.trace("Making call {} with queryString {}", uri, queryString);
			return getResults(uri, httpclient, queryString);

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private SearchResult getResults(URI uri, CloseableHttpClient httpclient, String queryString)
			throws IcatException {
		logger.debug(queryString);
		try {
			StringEntity input = new StringEntity(queryString);
			input.setContentType(MediaType.APPLICATION_JSON);
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(input);

			SearchResult lsr = new SearchResult();
			List<ScoredEntityBaseBean> results = lsr.getResults();
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				try (JsonReader reader = Json.createReader(response.getEntity().getContent())) {
					JsonObject responseObject = reader.readObject();
					List<JsonObject> resultsArray = responseObject.getJsonArray("results")
							.getValuesAs(JsonObject.class);
					for (JsonObject resultObject : resultsArray) {
						int luceneDocId = resultObject.getInt("_id");
						Float score = Float.NaN;
						if (resultObject.keySet().contains("_score")) {
							score = resultObject.getJsonNumber("_score").bigDecimalValue().floatValue();
						}
						JsonObject source = resultObject.getJsonObject("_source");
						results.add(new ScoredEntityBaseBean(luceneDocId, score, source));
					}
					if (responseObject.containsKey("search_after")) {
						lsr.setSearchAfter(responseObject.getJsonObject("search_after"));
					}
				}
			}
			return lsr;
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void lock(String entityName) throws IcatException {
		try {
			URI uri = new URIBuilder(server).setPath(basePath + "/lock/" + entityName).build();
			logger.trace("Making call {}", uri);
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void unlock(String entityName) throws IcatException {
		try {
			URI uri = new URIBuilder(server).setPath(basePath + "/unlock/" + entityName).build();
			logger.trace("Making call {}", uri);
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void modify(String json) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/modify").build();
			HttpPost httpPost = new HttpPost(uri);
			StringEntity input = new StringEntity(json);
			input.setContentType(MediaType.APPLICATION_JSON);
			httpPost.setEntity(input);

			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

}
