package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLucene {

	static LuceneApi luceneApi;
	private static URI url;
	final static Logger logger = LoggerFactory.getLogger(TestLucene.class);

	@BeforeClass
	public static void beforeClass() throws Exception {
		url = new URI(System.getProperty("serverUrl"));
		logger.debug("Using lucene service at {}", url);
		luceneApi = new LuceneApi(url);
	}

	@Before
	public void before() throws Exception {
		for (String name : EntityInfoHandler.getEntityNamesList()) {
			Class<EntityBaseBean> klass = EntityInfoHandler.getClass(name);
			try {
				klass.getDeclaredMethod("getDoc", JsonGenerator.class);
				luceneApi.deleteAll(name);
			} catch (NoSuchMethodException e) {
				// There is no getDoc method so not interested
			}
		}
		luceneApi.commit();
	}

	@Test
	public void locking() throws IcatException {

		try {
			luceneApi.unlock("fred");
			fail();
		} catch (IcatException e) {
			assertEquals("Lucene is not currently locked", e.getMessage());
		}
		luceneApi.lock("fred");
		try {
			luceneApi.lock("fred");
			fail();
		} catch (IcatException e) {
			assertEquals("Lucene already locked by fred", e.getMessage());
		}
		try {
			luceneApi.lock("bill");
			fail();
		} catch (IcatException e) {
			assertEquals("Lucene already locked by fred", e.getMessage());
		}
		luceneApi.unlock("fred");
		try {
			luceneApi.unlock("fred");
			fail();
		} catch (IcatException e) {
			assertEquals("Lucene is not currently locked", e.getMessage());
		}
	}

	@Test
	public void addDocument() throws IcatException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			gen.writeStartObject().write("type", "TextField").write("name", "text")
					.write("value", "Elephants and Aardvarks").writeEnd();

			gen.writeStartObject().write("type", "StringField").write("name", "date")
					.write("date", new Date().getTime()).writeEnd();

			gen.writeStartObject().write("type", "StringField").write("name", "id").write("value", "42")
					.write("store", true).writeEnd();

			gen.writeStartObject().write("type", "StringField").write("name", "dataset").write("value", "2001")
					.writeEnd();
			gen.writeEnd();
		}
		luceneApi.addDocument("Datafile", baos.toString());
	}

	String letters = "abcdefghijklmnopqrstuvwxyz";

	private void addDocuments(String entityName, String json) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(url).setPath(LuceneApi.basePath + "/addNow/" + entityName).build();
			HttpPost httpPost = new HttpPost(uri);
			StringEntity input = new StringEntity(json);
			input.setContentType(MediaType.APPLICATION_JSON);
			httpPost.setEntity(input);

			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				LuceneApi.checkStatus(response);
			}
		} catch (IOException | URISyntaxException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}

	@Test
	public void datafiles() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < 40; i++) {
				int j = i % 26;
				String word = letters.substring(j, j + 1) + letters.substring(j, j + 1) + letters.substring(j, j + 1);
				gen.writeStartArray();
				gen.writeStartObject().write("type", "TextField").write("name", "text").write("value", word).writeEnd();

				gen.writeStartObject().write("type", "StringField").write("name", "date")
						.write("date", new Date().getTime()).writeEnd();

				gen.writeStartObject().write("type", "StringField").write("name", "id").write("value", Long.toString(i))
						.write("store", true).writeEnd();

				gen.writeStartObject().write("type", "StringField").write("name", "dataset").write("value", "2001")
						.writeEnd();
				gen.writeEnd();
			}
			gen.writeEnd();
		}
		addDocuments("Datafile", baos.toString());
		LuceneSearchResult lsr = luceneApi.datafiles(null, null, null, null, null, 5);
		Long uid = lsr.getUid();
		logger.debug("uid {}", uid);
		for (ScoredEntityBaseBean q : lsr.getResults()) {
			logger.debug("+> {} {}", q.getEntityBaseBeanId(), q.getScore());
		}
		lsr = luceneApi.datafiles(uid, 6);
		assertTrue(lsr.getUid() == null);
		logger.debug("uid {}", uid);
		for (ScoredEntityBaseBean q : lsr.getResults()) {
			logger.debug("+> {} {}", q.getEntityBaseBeanId(), q.getScore());
		}
		luceneApi.freeSearcher(uid);
	}
}
