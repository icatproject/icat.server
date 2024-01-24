package org.icatproject.core.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.ws.rs.core.MediaType;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneApi {
	private enum ParserState {
		None, Results
	}

	static String basePath = "/icat.lucene";
	final static Logger logger = LoggerFactory.getLogger(LuceneApi.class);

	public static void encodeSortedDocValuesField(JsonGenerator gen, String name, Long value) {
		gen.writeStartObject().write("type", "SortedDocValuesField").write("name", name).write("value", value)
				.writeEnd();
	}

	public static void encodeStoredId(JsonGenerator gen, Long id) {
		gen.writeStartObject().write("type", "StringField").write("name", "id").write("value", Long.toString(id))
				.write("store", true).writeEnd();
	}

	private static SimpleDateFormat df;

	static {
		df = new SimpleDateFormat("yyyyMMddHHmm");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
	}

	public static void encodeStringField(JsonGenerator gen, String name, Date value) {
		String timeString;
		synchronized (df) {
			timeString = df.format(value);
		}
		gen.writeStartObject().write("type", "StringField").write("name", name).write("value", timeString).writeEnd();
	}

	public static void encodeDoubleField(JsonGenerator gen, String name, Double value) {
		gen.writeStartObject().write("type", "DoubleField").write("name", name).write("value", value)
				.write("store", true).writeEnd();
	}

	public static void encodeStringField(JsonGenerator gen, String name, Long value) {
		gen.writeStartObject().write("type", "StringField").write("name", name).write("value", Long.toString(value))
				.writeEnd();
	}

	public static void encodeStringField(JsonGenerator gen, String name, String value) {
		gen.writeStartObject().write("type", "StringField").write("name", name).write("value", value).writeEnd();

	}

	public static void encodeTextfield(JsonGenerator gen, String name, String value) {
		if (value != null) {
			gen.writeStartObject().write("type", "TextField").write("name", name).write("value", value).writeEnd();
		}
	}

	URI server;

	public LuceneApi(URI server) {
		this.server = server;
	}

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

	public LuceneSearchResult datafiles(long uid, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/datafiles/" + uid)
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			return getLsr(uri, httpclient);

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public LuceneSearchResult datafiles(String user, String text, Date lower, Date upper, List<ParameterPOJO> parms,
			int maxResults) throws IcatException {

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/datafiles")
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			logger.trace("Making call {}", uri);

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
					gen.write("lower", enc(lower));
				}
				if (upper != null) {
					gen.write("upper", enc(upper));
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
							gen.write("lowerDateValue", enc(parm.lowerDateValue));
						}
						if (parm.upperDateValue != null) {
							gen.write("upperDateValue", enc(parm.upperDateValue));
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
			return getLsr(uri, httpclient, baos);
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	};

	private String enc(Date dateValue) {
		synchronized (df) {
			return df.format(dateValue);
		}
	}

	public LuceneSearchResult datasets(Long uid, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/datasets/" + uid)
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			return getLsr(uri, httpclient);

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public LuceneSearchResult datasets(String user, String text, Date lower, Date upper, List<ParameterPOJO> parms,
			int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/datasets")
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			logger.trace("Making call {}", uri);

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
					gen.write("lower", enc(lower));
				}
				if (upper != null) {
					gen.write("upper", enc(upper));
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
							gen.write("lowerDateValue", enc(parm.lowerDateValue));
						}
						if (parm.upperDateValue != null) {
							gen.write("upperDateValue", enc(parm.upperDateValue));
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
			return getLsr(uri, httpclient, baos);
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public void freeSearcher(Long uid) throws IcatException {
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

	private LuceneSearchResult getLsr(URI uri, CloseableHttpClient httpclient) throws IcatException {
		HttpGet httpGet = new HttpGet(uri);
		LuceneSearchResult lsr = new LuceneSearchResult();
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

	private LuceneSearchResult getLsr(URI uri, CloseableHttpClient httpclient, ByteArrayOutputStream baos)
			throws IcatException {
		logger.debug(baos.toString());
		try {
			StringEntity input = new StringEntity(baos.toString());
			input.setContentType(MediaType.APPLICATION_JSON);
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(input);

			LuceneSearchResult lsr = new LuceneSearchResult();
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
								lsr.setUid(p.getLong());
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

	public LuceneSearchResult investigations(Long uid, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/investigations/" + uid)
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			return getLsr(uri, httpclient);

		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public LuceneSearchResult investigations(String user, String text, Date lower, Date upper,
			List<ParameterPOJO> parms, List<String> samples, String userFullName, int maxResults) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(server).setPath(basePath + "/investigations")
					.setParameter("maxResults", Integer.toString(maxResults)).build();
			logger.trace("Making call {}", uri);

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
					gen.write("lower", enc(lower));
				}
				if (upper != null) {
					gen.write("upper", enc(upper));
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
							gen.write("lowerDateValue", enc(parm.lowerDateValue));
						}
						if (parm.upperDateValue != null) {
							gen.write("upperDateValue", enc(parm.upperDateValue));
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
				if (samples != null && !samples.isEmpty()) {
					gen.writeStartArray("samples");
					for (String sample : samples) {
						gen.write(sample);
					}
					gen.writeEnd(); // array
				}
				if (userFullName != null) {
					gen.write("userFullName", userFullName);
				}
				gen.writeEnd(); // object
			}
			return getLsr(uri, httpclient, baos);
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

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
