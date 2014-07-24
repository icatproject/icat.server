package org.icatproject.integration.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.icatproject.integration.client.IcatException.IcatExceptionType;
import org.icatproject.integration.client.Session.Attributes;
import org.icatproject.integration.client.Session.DuplicateAction;

public class ICAT {

	private URI uri;

	private static final String basePath = "/icat";

	public ICAT(String urlString) throws URISyntaxException {
		this.uri = new URI(urlString);
	}

	private URI getUri(URIBuilder uriBuilder) throws IcatException {
		try {
			return uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private URIBuilder getUriBuilder(String path) {
		return new URIBuilder(uri).setPath(basePath + "/" + path);
	}

	private void expectNothing(CloseableHttpResponse response) throws IcatException, IOException {
		checkStatus(response);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String error = EntityUtils.toString(entity);
			if (!error.isEmpty()) {
				try (JsonParser parser = Json.createParser(new ByteArrayInputStream(error
						.getBytes()))) {
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
					if (code != null && message != null) {
						throw new IcatException(IcatExceptionType.valueOf(code), message);
					}
					throw new IcatException(IcatExceptionType.INTERNAL,
							"No http entity expected in response " + error);
				}
			}
		}
	}

	private String getString(CloseableHttpResponse response) throws IcatException, IOException {
		checkStatus(response);
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new IcatException(IcatExceptionType.INTERNAL,
					"No http entity returned in response");
		}
		return EntityUtils.toString(entity);
	}

	private void checkStatus(HttpResponse response) throws IcatException, IOException {
		StatusLine status = response.getStatusLine();
		if (status == null) {
			throw new IcatException(IcatExceptionType.INTERNAL, "Status line returned is empty");
		}
		int rc = status.getStatusCode();
		if (rc / 100 != 2) {
			HttpEntity entity = response.getEntity();
			String error;
			if (entity == null) {
				throw new IcatException(IcatExceptionType.INTERNAL, "No explanation provided");
			} else {
				error = EntityUtils.toString(entity);
			}

			System.out.println(error);
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
						}
						if (key.equals("message")) {
							message = parser.getString();
						}
					}
				}

				if (code == null || message == null) {
					throw new IcatException(IcatExceptionType.INTERNAL, error);
				}
				throw new IcatException(IcatExceptionType.valueOf(code), message);
			} catch (JsonParsingException e) {
				throw new IcatException(IcatExceptionType.INTERNAL, error);
			}
		}
	}

	public Session login(String plugin, Map<String, String> credentials) throws IcatException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("plugin", plugin).writeStartArray("credentials");

		for (Entry<String, String> entry : credentials.entrySet()) {
			gen.writeStartObject().write(entry.getKey(), entry.getValue()).writeEnd();
		}
		gen.writeEnd().writeEnd().close();

		URI uri = getUri(getUriBuilder("session"));
		List<NameValuePair> formparams = new ArrayList<>();
		formparams.add(new BasicNameValuePair("json", baos.toString()));
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new UrlEncodedFormEntity(formparams));
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				String responseString = getString(response);
				String sessionId = getFromJson(responseString, "sessionId");
				return new Session(this, sessionId);
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	private String getFromJson(String input, String sought) throws IcatException {
		try (JsonParser parser = Json.createParser(new ByteArrayInputStream(input.getBytes()))) {
			String key = "";
			String code = null;
			String message = null;
			while (parser.hasNext()) {
				JsonParser.Event event = parser.next();
				if (event == Event.KEY_NAME) {
					key = parser.getString();
				} else if (event == Event.VALUE_STRING || event == Event.VALUE_NUMBER) {
					if (key.equals(sought)) {
						return parser.getString();
					} else if (key.equals("code")) {
						code = parser.getString();
					} else if (key.equals("message")) {
						message = parser.getString();
					}
				}
			}
			if (code != null && message != null) {
				throw new IcatException(IcatExceptionType.valueOf(code), message);
			}
			throw new IcatException(IcatExceptionType.INTERNAL, "No " + sought + " in " + input);
		}
	}

	String getUserName(String sessionId) throws IcatException {
		URI uri = getUri(getUriBuilder("session/" + sessionId));
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
				String responseString = getString(response);
				return getFromJson(responseString, "userName");
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public double getRemainingMinutes(String sessionId) throws IcatException {
		URI uri = getUri(getUriBuilder("session/" + sessionId));
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
				String responseString = getString(response);
				return Double.parseDouble(getFromJson(responseString, "remainingMinutes"));
			}
		} catch (IOException | NumberFormatException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public void logout(String sessionId) throws IcatException {
		URI uri = getUri(getUriBuilder("session/" + sessionId));
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpDelete httpDelete = new HttpDelete(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
				expectNothing(response);
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	public void refresh(String sessionId) throws IcatException {
		URI uri = getUri(getUriBuilder("session/" + sessionId));
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPut httpPut = new HttpPut(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpPut)) {
				expectNothing(response);
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	public void importMetaData(String sessionId, Path path, DuplicateAction duplicate,
			Attributes attributes) throws IcatException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("sessionId", sessionId)
				.write("duplicate", duplicate.name().toLowerCase())
				.write("attributes", attributes.name().toLowerCase()).writeEnd().close();

		URI uri = getUri(getUriBuilder("port"));

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			InputStream stream = new BufferedInputStream(Files.newInputStream(path));

			HttpEntity httpEntity = MultipartEntityBuilder
					.create()
					.addPart("json", new StringBody(baos.toString(), ContentType.TEXT_PLAIN))
					.addPart("file",
							new InputStreamBody(stream, ContentType.APPLICATION_OCTET_STREAM, ""))
					.build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(httpEntity);
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				expectNothing(response);
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	public InputStream exportMetaData(String sessionId, String query, Attributes attributes)
			throws IcatException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("sessionId", sessionId);
		if (query == null) {
			gen.write("type", "dump");
		} else {
			gen.write("type", "query");
			gen.write("query", query);
		}
		gen.write("attributes", attributes.name().toLowerCase()).writeEnd().close();

		URIBuilder uriBuilder = getUriBuilder("port");
		uriBuilder.setParameter("json", baos.toString());
		URI uri = getUri(uriBuilder);

		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		HttpGet httpGet = new HttpGet(uri);

		boolean closeNeeded = true;
		try {
			httpclient = HttpClients.createDefault();
			response = httpclient.execute(httpGet);
			checkStatus(response);
			closeNeeded = false;
			return new HttpInputStream(httpclient, response);
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		} finally {
			if (closeNeeded && httpclient != null) {
				try {
					if (response != null) {
						try {
							response.close();
						} catch (Exception e) {
							// Ignore it
						}
					}
					httpclient.close();
				} catch (IOException e) {
					// Ignore it
				}
			}
		}

	}

	public String search(String sessionId, String query) throws IcatException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos);
		gen.writeStartObject().write("sessionId", sessionId).write("query", query).writeEnd()
				.close();

		URIBuilder uriBuilder = getUriBuilder("entity");
		uriBuilder.setParameter("json", baos.toString());
		URI uri = getUri(uriBuilder);

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet(uri);
			try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
				return getString(response);
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}

	}

	public String create(String sessionId, String entity) throws IcatException {
		URI uri = getUri(getUriBuilder("entity"));
		List<NameValuePair> formparams = new ArrayList<>();
		formparams.add(new BasicNameValuePair("json", "{\"sessionId\":\"" + sessionId
				+ "\",\"entity\":" + entity + "}"));
		System.out.println(formparams.get(0));
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(uri);
			httpPost.setEntity(new UrlEncodedFormEntity(formparams));
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				return getString(response);
			}
		} catch (IOException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

}
