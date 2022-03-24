package org.icatproject.core.manager;

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
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
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

	// TODO this method of encoding an entity as an array of 3 key objects that represent single field each
	// is something that should be streamlined, but would require changes to icat.lucene

	@Override
	public void encodeSortedDocValuesField(JsonGenerator gen, String name, Long value) {
		gen.writeStartObject().write("type", "SortedDocValuesField").write("name", name).write("value", value)
				.writeEnd();
	}

	@Override
	public void encodeDoublePoint(JsonGenerator gen, String name, Double value) {
		gen.writeStartObject().write("type", "DoublePoint").write("name", name).write("value", value)
				.write("store", true).writeEnd();
	}

	// public void encodeSortedSetDocValuesFacetField(JsonGenerator gen, String name, String value) {
	// 	gen.writeStartObject().write("type", "SortedSetDocValuesFacetField").write("name", name).write("value", value)
	// 			.writeEnd();

	// }

	@Override
	public void encodeStringField(JsonGenerator gen, String name, String value) {
		gen.writeStartObject().write("type", "StringField").write("name", name).write("value", value).writeEnd();
	}

	@Override
	public void encodeStringField(JsonGenerator gen, String name, Long value, Boolean store) {
		gen.writeStartObject().write("type", "StringField").write("name", name).write("value", Long.toString(value)).write("store", store).writeEnd();
	}

	@Override
	public void encodeTextField(JsonGenerator gen, String name, String value) {
		if (value != null) {
			gen.writeStartObject().write("type", "TextField").write("name", name).write("value", value).writeEnd();
		}
	}

	URI server;

	public LuceneApi(URI server) {
		this.server = server;
	}

	public void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor) throws IcatException, IOException, URISyntaxException {
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
							gen.writeStartArray();
							bean.getDoc(gen, this);
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
	public List<FacetDimension> facetSearch(JsonObject facetQuery, int maxResults, int maxLabels) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String indexPath = getTargetPath(facetQuery);
			URI uri = new URIBuilder(server).setPath(basePath + "/" + indexPath + "/facet")
					.setParameter("maxResults", Integer.toString(maxResults))
					.setParameter("maxLabels", Integer.toString(maxLabels)).build();
			logger.trace("Making call {}", uri);
			return getFacets(uri, httpclient, facetQuery.toString());

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public void freeSearcher(String uid) throws IcatException {
		try {
			URI uri = new URIBuilder(server).setPath(basePath + "/freeSearcher/" + uid).build();
			logger.trace("Making call {}", uri);
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpDelete httpDelete = new HttpDelete(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
					Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private List<FacetDimension> getFacets(URI uri, CloseableHttpClient httpclient, String facetQueryString)
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
								facetDimensions.add(new FacetDimension(key));
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
	public SearchResult getResults(JsonObject query, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String indexPath = getTargetPath(query);
			URI uri = new URIBuilder(server).setPath(basePath + "/" + indexPath)
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			logger.trace("Making call {}", uri);
			return getResults(uri, httpclient, query.toString());

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Override
	public SearchResult getResults(String uid, JsonObject query, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String indexPath = getTargetPath(query);
			URI uri = new URIBuilder(server).setPath(basePath + "/" + indexPath + "/" + uid)
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			return getResults(uri, httpclient);

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private SearchResult getResults(URI uri, CloseableHttpClient httpclient) throws IcatException {
		HttpGet httpGet = new HttpGet(uri);
		SearchResult lsr = new SearchResult();
		List<ScoredEntityBaseBean> results = lsr.getResults();
		ParserState state = ParserState.None;
		try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
			Rest.checkStatus(response, IcatExceptionType.INTERNAL);
			try (JsonParser p = Json.createParser(response.getEntity().getContent())) {
				String key = null;
				while (p.hasNext()) {
					Event e = p.next();
					if (e.equals(Event.KEY_NAME)) {
						key = p.getString();
					} else if (state == ParserState.Results) {
						if (e == (Event.START_ARRAY)) {
							p.next();
							Long id = p.getLong();
							p.next();
							results.add(new ScoredEntityBaseBean(id, p.getBigDecimal().floatValue()));
							p.next(); // skip the }
						}
					} else { // Not in results yet
						if (e == Event.START_ARRAY && key.equals("results")) {
							state = ParserState.Results;
						}
					}
				}
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
		return lsr;
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
			ParserState state = ParserState.None;
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				Rest.checkStatus(response, IcatExceptionType.INTERNAL);
				try (JsonParser p = Json.createParser(response.getEntity().getContent())) {
					String key = null;
					while (p.hasNext()) {
						Event e = p.next();
						if (e.equals(Event.KEY_NAME)) {
							key = p.getString();
						} else if (state == ParserState.Results) {
							if (e == (Event.START_ARRAY)) {
								p.next();
								Long id = p.getLong();
								p.next();
								results.add(new ScoredEntityBaseBean(id, p.getBigDecimal().floatValue()));
								p.next(); // skip the }
							}
						} else { // Not in results yet
							if (e == (Event.VALUE_NUMBER) && key.equals("uid")) {
								lsr.setUid(String.valueOf(p.getLong()));
							} else if (e == Event.START_ARRAY && key.equals("results")) {
								state = ParserState.Results;
							}

						}

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
