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
		luceneApi.clear();
	}

	@Test
	public void locking() throws IcatException {

		try {
			luceneApi.unlock("Dataset");
			fail();
		} catch (IcatException e) {
			assertEquals("Lucene is not currently locked for Dataset", e.getMessage());
		}
		luceneApi.lock("Dataset");
		try {
			luceneApi.lock("Dataset");
			fail();
		} catch (IcatException e) {
			assertEquals("Lucene already locked for Dataset", e.getMessage());
		}
		luceneApi.unlock("Dataset");
		try {
			luceneApi.unlock("Dataset");
			fail();
		} catch (IcatException e) {
			assertEquals("Lucene is not currently locked for Dataset", e.getMessage());
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

	long now = new Date().getTime();

	@Test
	public void datafiles() throws Exception {
		populate();

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
		lsr = luceneApi.datafiles(null, null, null, null, null, 999);
	}

	/**
	 * 
	 * Populate UserGroup, Investigation, InvestigationParameter,
	 * InvestigationUser, Dataset,DatasetParameter,Datafile, DatafileParameter,
	 * Sample and SampleParameter
	 */
	private void populate() throws IcatException {
		int NUMINV = 10;
		int NUMUSERS = 5;
		int NUMDS = 30;
		int NUMDF = 100;
		int NUMSAMP = 15;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMINV; i++) {
				for (int j = 0; j < NUMUSERS; j++) {
					if (i % (j + 1) == 1) {
						String fn = "FN " + letters.substring(j, j + 1) + " " + letters.substring(j, j + 1);
						String name = letters.substring(j, j + 1) + j;
						gen.writeStartArray();

						LuceneApi.encodeTextfield(gen, "text", fn);

						LuceneApi.encodeStringField(gen, "name", name);
						LuceneApi.encodeSortedDocValuesField(gen, "investigation", new Long(i));

						gen.writeEnd();
					}
				}
			}
			gen.writeEnd();
		}
		addDocuments("InvestigationUserUser", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMINV; i++) {
				int j = i % 26;
				String word = letters.substring(j, j + 1) + letters.substring(j, j + 1) + letters.substring(j, j + 1);
				gen.writeStartArray();
				LuceneApi.encodeTextfield(gen, "text", word);
				LuceneApi.encodeStringField(gen, "date", new Date().getTime());
				LuceneApi.encodeStoredId(gen, new Long(i));
				LuceneApi.encodeSortedDocValuesField(gen, "id", new Long(i));
				gen.writeEnd();
			}
			gen.writeEnd();
		}
		addDocuments("Investigation", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMINV; i++) {
				if (i % 2 == 1) {
					fillParms(gen, i, "investigation");
				}
			}
			gen.writeEnd();
		}
		addDocuments("InvestigationParameter", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDS; i++) {
				int j = i % 26;
				String word = "DS" + letters.substring(j, j + 1) + letters.substring(j, j + 1)
						+ letters.substring(j, j + 1);
				gen.writeStartArray();
				LuceneApi.encodeTextfield(gen, "text", word);
				LuceneApi.encodeStringField(gen, "date", new Date().getTime());
				LuceneApi.encodeStoredId(gen, new Long(i));
				LuceneApi.encodeSortedDocValuesField(gen, "id", new Long(i));
				LuceneApi.encodeStringField(gen, "investigation", new Long(i % NUMINV));
				gen.writeEnd();
			}
			gen.writeEnd();
		}
		addDocuments("Dataset", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDS; i++) {
				if (i % 3 == 1) {
					fillParms(gen, i, "dataset");
				}
			}
			gen.writeEnd();
		}
		addDocuments("DatasetParameter", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDF; i++) {
				int j = i % 26;
				String word = "DF" + letters.substring(j, j + 1) + letters.substring(j, j + 1)
						+ letters.substring(j, j + 1);
				gen.writeStartArray();
				LuceneApi.encodeTextfield(gen, "text", word);
				LuceneApi.encodeStringField(gen, "date", new Date(now + 60000 * i).getTime());
				LuceneApi.encodeStoredId(gen, new Long(i));
				LuceneApi.encodeStringField(gen, "dataset", new Long(i % NUMDS));
				gen.writeEnd();
			}
			gen.writeEnd();
		}
		addDocuments("Datafile", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDF; i++) {
				if (i % 4 == 1) {
					fillParms(gen, i, "datafile");
				}
			}
			gen.writeEnd();
		}
		addDocuments("DatafileParameter", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMSAMP; i++) {
				int j = i % 26;
				String word = "SType " + letters.substring(j, j + 1) + letters.substring(j, j + 1)
						+ letters.substring(j, j + 1);
				gen.writeStartArray();
				LuceneApi.encodeTextfield(gen, "text", word);
				LuceneApi.encodeSortedDocValuesField(gen, "investigation", new Long(i % NUMINV));
				gen.writeEnd();
			}
			gen.writeEnd();
		}
		addDocuments("Sample", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMSAMP; i++) {
				if (i % 5 == 1) {
					fillParms(gen, i, "sample");
				}
			}
			gen.writeEnd();
		}
		addDocuments("SampleParameter", baos.toString());

	}

	private void fillParms(JsonGenerator gen, int i, String rel) {
		int j = i % 26;
		int k = (i + 5) % 26;
		String name = "nm " + letters.substring(j, j + 1) + letters.substring(j, j + 1) + letters.substring(j, j + 1);
		String units = "u " + letters.substring(k, k + 1) + letters.substring(k, k + 1) + letters.substring(j, j + 1);

		gen.writeStartArray();
		LuceneApi.encodeStringField(gen, "name", "S" + name);
		LuceneApi.encodeStringField(gen, "units", units);
		LuceneApi.encodeStringField(gen, "stringValue", " " + i * i);
		LuceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();

		gen.writeStartArray();
		LuceneApi.encodeStringField(gen, "name", "N" + name);
		LuceneApi.encodeStringField(gen, "units", units);
		LuceneApi.encodeStringField(gen, "numericValue", new Double(j * j));
		LuceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();

		gen.writeStartArray();
		LuceneApi.encodeStringField(gen, "name", "D" + name);
		LuceneApi.encodeStringField(gen, "units", units);
		LuceneApi.encodeStringField(gen, "dateTimeValue", new Date(now + k * k));
		LuceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();

	}

	@Test
	public void investigations() throws Exception {
		populate();
		LuceneSearchResult lsr = luceneApi.investigations(null, null, null, null, null, null, letters, 5);
		Long uid = lsr.getUid();
		logger.debug("uid {}", uid);
		for (ScoredEntityBaseBean q : lsr.getResults()) {
			logger.debug("+> {} {}", q.getEntityBaseBeanId(), q.getScore());
		}
		lsr = luceneApi.investigations(uid, 6);
		assertTrue(lsr.getUid() == null);
		logger.debug("uid {}", uid);
		for (ScoredEntityBaseBean q : lsr.getResults()) {
			logger.debug("+> {} {}", q.getEntityBaseBeanId(), q.getScore());
		}
		luceneApi.freeSearcher(uid);
	}

	@Test
	public void datasets() throws Exception {
		populate();
		LuceneSearchResult lsr = luceneApi.datasets(null, null, null, null, null, 5);
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
