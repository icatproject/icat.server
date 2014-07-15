package org.icatproject.integration.client;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.icatproject.integration.client.IcatException.IcatExceptionType;

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
			if (!EntityUtils.toString(entity).isEmpty()) {
				throw new IcatException(IcatExceptionType.INTERNAL,
						"No http entity expected in response");
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

	private void checkStatus(HttpResponse response) throws IcatException, ParseException,
			IOException {
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
					throw new IcatException(IcatExceptionType.INTERNAL, "TestingClient " + error);
				}
				throw new IcatException(IcatExceptionType.valueOf(code), message);

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

}
