package org.icatproject.core.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneApi {
	URI server;

	static String basePath = "/icat.lucene";
	final static Logger logger = LoggerFactory.getLogger(LuceneApi.class);

	public LuceneApi(URI server) {
		this.server = server;
	}

	void deleteAll(String entityName) throws IcatException {
		try {
			URI uri = new URIBuilder(server).setPath(basePath + "/deleteAll/" + entityName).build();
			logger.trace("Making call {}", uri);
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpDelete httpDelete = new HttpDelete(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
					checkStatus(response);
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	void commit() throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/commit").build();
			logger.trace("Making call {}", uri);
			HttpPost httpPost = new HttpPost(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				checkStatus(response);
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	static void checkStatus(HttpResponse response) throws IcatException {
		StatusLine status = response.getStatusLine();
		if (status == null) {
			throw new IcatException(IcatExceptionType.INTERNAL, "Status line in response is empty");
		}
		int rc = status.getStatusCode();
		if (rc / 100 != 2) {
			HttpEntity entity = response.getEntity();
			String error;
			if (entity == null) {
				throw new IcatException(IcatExceptionType.INTERNAL, "No http entity returned in response");
			} else {
				try {
					error = EntityUtils.toString(entity);
				} catch (ParseException | IOException e) {
					throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
				}
			}
			try (JsonParser parser = Json.createParser(new ByteArrayInputStream(error.getBytes()))) {
				String code = null;
				String message = null;
				String key = "";
				while (parser.hasNext()) {
					JsonParser.Event event = parser.next();
					if (event == Event.KEY_NAME) {
						key = parser.getString();
					} else if (event == Event.VALUE_STRING) {
						if (key.equals("code")) {
							code = parser.getString();
						} else if (key.equals("message")) {
							message = parser.getString();
						}
					}
				}

				if (code == null || message == null) {
					throw new IcatException(IcatExceptionType.INTERNAL, error);
				}
				throw new IcatException(IcatExceptionType.INTERNAL, message);

			} catch (JsonParsingException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, error);
			}
		}

	}

	public void addDocument(String entityName, String json) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/add/" + entityName).build();
			HttpPost httpPost = new HttpPost(uri);
			StringEntity input = new StringEntity(json);
			input.setContentType(MediaType.APPLICATION_JSON);
			httpPost.setEntity(input);

			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				checkStatus(response);
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	public void delete(String entityName, Long id) {
		// TODO IndexWriter iwriter = indexWriters.get(entityName);
		// try {
		// iwriter.deleteDocuments(new Term("id", id.toString()));
		// logger.trace("Deleted {} from {} lucene index", id, entityName);
		// } catch (IOException e) {
		// throw new IcatException(IcatExceptionType.INTERNAL, e.getMessage());
		// }

	}

	public void lock(String entityName) throws IcatException {
		try {
			URI uri = new URIBuilder(server).setPath(basePath + "/lock/" + entityName).build();
			logger.trace("Making call {}", uri);
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					checkStatus(response);
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	public void unlock(String entityName) throws IcatException {
		try {
			URI uri = new URIBuilder(server).setPath(basePath + "/unlock/" + entityName).build();
			logger.trace("Making call {}", uri);
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					checkStatus(response);
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	public LuceneSearchResult datafiles(String user, String text, String lower, String upper, List<ParameterPOJO> parms,
			int maxResults) throws IcatException {

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/datafiles")
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			logger.trace("Making call {}", uri);
			HttpPost httpPost = new HttpPost(uri);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JsonGenerator gen = Json.createGenerator(baos)) {
				gen.writeStartObject();
				if (user != null) {
					gen.write("user", user);
				}
				if (text != null) {
					gen.write("text", text);
				}
				if (lower != null) {
					gen.write("lower", lower);
				}
				if (upper != null) {
					gen.write("upper", upper);
				}
				if (parms != null && !parms.isEmpty()) {
					gen.writeStartArray("params");
					for (ParameterPOJO parm : parms) {
						gen.writeStartObject();
						if (parm.name != null) {
							gen.write("name", parm.name);
						}
						if (parm.units != null) {
							gen.write("units", parm.units);
						}
						if (parm.stringValue != null) {
							gen.write("stringValue", parm.stringValue);
						}
						if (parm.lowerDateValue != null) {
							gen.write("lowerDateValue", parm.lowerDateValue);
						}
						if (parm.upperDateValue != null) {
							gen.write("upperDateValue", parm.upperDateValue);
						}
						if (parm.lowerNumericValue != null) {
							gen.write("lowerNumericValue", parm.lowerNumericValue);
						}
						if (parm.upperNumericValue != null) {
							gen.write("upperNumericValue", parm.upperNumericValue);
						}
						gen.writeEnd(); // object
					}
					gen.writeEnd(); // array
				}
				gen.writeEnd(); // object
			}
			logger.debug(baos.toString());
			StringEntity input = new StringEntity(baos.toString());
			input.setContentType(MediaType.APPLICATION_JSON);
			httpPost.setEntity(input);

			LuceneSearchResult lsr = new LuceneSearchResult();
			List<ScoredEntityBaseBean> results = lsr.getResults();
			ParserState state = ParserState.None;
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				checkStatus(response);
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
								lsr.setUid(p.getLong());
							} else if (e == Event.START_ARRAY && key.equals("results")) {
								state = ParserState.Results;
							}

						}

					}
				}
			}
			return lsr;

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private enum ParserState {
		None, Results
	};

	public void freeSearcher(Long uid) throws IcatException {
		try {
			URI uri = new URIBuilder(server).setPath(basePath + "/freeSearcher/" + uid).build();
			logger.trace("Making call {}", uri);
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpDelete httpDelete = new HttpDelete(uri);
				try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
					checkStatus(response);
				}
			}
		} catch (URISyntaxException | IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public LuceneSearchResult datafiles(long uid, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/datafiles/" + uid)
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			HttpGet httpGet = new HttpGet(uri);
			LuceneSearchResult lsr = new LuceneSearchResult();
			List<ScoredEntityBaseBean> results = lsr.getResults();
			ParserState state = ParserState.None;
			try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
				checkStatus(response);
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
			}
			return lsr;

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

}
