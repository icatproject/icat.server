package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.core.MediaType;

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
	private static URI uribase;
	final static Logger logger = LoggerFactory.getLogger(TestLucene.class);

	@BeforeClass
	public static void beforeClass() throws Exception {
		String urlString = System.getProperty("luceneUrl");
		logger.info("Using lucene service at {}", urlString);
		uribase = new URI(urlString);
		luceneApi = new LuceneApi(uribase);
	}

	String letters = "abcdefghijklmnopqrstuvwxyz";

	long now = new Date().getTime();

	int NUMINV = 10;

	int NUMUSERS = 5;

	int NUMDS = 30;

	int NUMDF = 100;

	int NUMSAMP = 15;

	private class QueueItem {

		private String entityName;
		private Long id;
		private String json;

		public QueueItem(String entityName, Long id, String json) {
			this.entityName = entityName;
			this.id = id;
			this.json = json;
		}

	}

	@Test
	public void modify() throws IcatException {
		Queue<QueueItem> queue = new ConcurrentLinkedQueue<>();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			LuceneApi.encodeTextfield(gen, "text", "Elephants and Aardvarks");
			LuceneApi.encodeStringField(gen, "startDate", new Date());
			LuceneApi.encodeStringField(gen, "endDate", new Date());
			LuceneApi.encodeStoredId(gen, 42L);
			LuceneApi.encodeStringField(gen, "dataset", 2001L);
			gen.writeEnd();
		}

		String json = baos.toString();

		queue.add(new QueueItem("Datafile", null, json));

		queue.add(new QueueItem("Datafile", 42L, json));

		queue.add(new QueueItem("Datafile", 42L, null));
		queue.add(new QueueItem("Datafile", 42L, null));

		Iterator<QueueItem> qiter = queue.iterator();
		if (qiter.hasNext()) {
			StringBuilder sb = new StringBuilder("[");

			while (qiter.hasNext()) {
				QueueItem item = qiter.next();
				if (sb.length() != 1) {
					sb.append(',');
				}
				sb.append("[\"").append(item.entityName).append('"');
				if (item.id != null) {
					sb.append(',').append(item.id);
				} else {
					sb.append(",null");
				}
				if (item.json != null) {
					sb.append(',').append(item.json);
				} else {
					sb.append(",null");
				}
				sb.append(']');
				qiter.remove();
			}
			sb.append(']');
			logger.debug("XXX " + sb.toString());

			luceneApi.modify(sb.toString());
		}

	}

	private void addDocuments(String entityName, String json) throws IcatException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URI uri = new URIBuilder(uribase).setPath(LuceneApi.basePath + "/addNow/" + entityName).build();
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

	@Before
	public void before() throws Exception {
		luceneApi.clear();
	}

	private void checkLsr(LuceneSearchResult lsr, Long... n) {
		Set<Long> wanted = new HashSet<>(Arrays.asList(n));
		Set<Long> got = new HashSet<>();

		for (ScoredEntityBaseBean q : lsr.getResults()) {
			got.add(q.getEntityBaseBeanId());
		}

		Set<Long> missing = new HashSet<>(wanted);
		missing.removeAll(got);
		if (!missing.isEmpty()) {
			for (Long l : missing) {
				logger.error("Entry missing: {}", l);
			}
			fail("Missing entries");
		}

		missing = new HashSet<>(got);
		missing.removeAll(wanted);
		if (!missing.isEmpty()) {
			for (Long l : missing) {
				logger.error("Extra entry: {}", l);
			}
			fail("Extra entries");
		}

	}

	@Test
	public void datafiles() throws Exception {
		populate();

		LuceneSearchResult lsr = luceneApi.datafiles(null, null, null, null, null, 5);
		Long uid = lsr.getUid();

		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		System.out.println(uid);
		lsr = luceneApi.datafiles(uid, 200);
		assertTrue(lsr.getUid() == null);
		assertEquals(95, lsr.getResults().size());
		luceneApi.freeSearcher(uid);

		lsr = luceneApi.datafiles("e4", null, null, null, null, 100);
		checkLsr(lsr, 1L, 6L, 11L, 16L, 21L, 26L, 31L, 36L, 41L, 46L, 51L, 56L, 61L, 66L, 71L, 76L, 81L, 86L, 91L, 96L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datafiles("e4", "dfbbb", null, null, null, 100);
		checkLsr(lsr, 1L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datafiles(null, "dfbbb", null, null, null, 100);
		checkLsr(lsr, 1L, 27L, 53L, 79L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datafiles(null, null, new Date(now + 60000 * 3), new Date(now + 60000 * 6), null, 100);
		checkLsr(lsr, 3L, 4L, 5L, 6L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datafiles("b1", "dsddd", new Date(now + 60000 * 3), new Date(now + 60000 * 6), null, 100);
		checkLsr(lsr);
		luceneApi.freeSearcher(lsr.getUid());

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		lsr = luceneApi.datafiles(null, null, new Date(now + 60000 * 3), new Date(now + 60000 * 6), pojos, 100);
		checkLsr(lsr, 5L);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		lsr = luceneApi.datafiles(null, null, null, null, pojos, 100);
		checkLsr(lsr, 5L);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, "u sss", null));
		lsr = luceneApi.datafiles(null, null, null, null, pojos, 100);
		checkLsr(lsr, 13L, 65L);
		luceneApi.freeSearcher(lsr.getUid());
	}

	@Test
	public void datasets() throws Exception {
		populate();
		LuceneSearchResult lsr = luceneApi.datasets(null, null, null, null, null, 5);

		Long uid = lsr.getUid();
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		System.out.println(uid);
		lsr = luceneApi.datasets(uid, 100);
		assertTrue(lsr.getUid() == null);
		checkLsr(lsr, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L,
				25L, 26L, 27L, 28L, 29L);
		luceneApi.freeSearcher(uid);

		lsr = luceneApi.datasets("e4", null, null, null, null, 100);
		checkLsr(lsr, 1L, 6L, 11L, 16L, 21L, 26L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datasets("e4", "dsbbb", null, null, null, 100);
		checkLsr(lsr, 1L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datasets(null, "dsbbb", null, null, null, 100);
		checkLsr(lsr, 1L, 27L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datasets(null, null, new Date(now + 60000 * 3), new Date(now + 60000 * 6), null, 100);
		checkLsr(lsr, 3L, 4L, 5L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.datasets("b1", "dsddd", new Date(now + 60000 * 3), new Date(now + 60000 * 6), null, 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = luceneApi.datasets(null, null, null, null, pojos, 100);
		checkLsr(lsr, 4L);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = luceneApi.datasets(null, null, new Date(now + 60000 * 3), new Date(now + 60000 * 6), pojos, 100);
		checkLsr(lsr, 4L);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = luceneApi.datasets("b1", "dsddd", new Date(now + 60000 * 3), new Date(now + 60000 * 6), pojos, 100);
		checkLsr(lsr);
		luceneApi.freeSearcher(lsr.getUid());

	}

	private void fillParms(JsonGenerator gen, int i, String rel) {
		int j = i % 26;
		int k = (i + 5) % 26;
		String name = "nm " + letters.substring(j, j + 1) + letters.substring(j, j + 1) + letters.substring(j, j + 1);
		String units = "u " + letters.substring(k, k + 1) + letters.substring(k, k + 1) + letters.substring(k, k + 1);

		gen.writeStartArray();
		LuceneApi.encodeStringField(gen, "name", "S" + name);
		LuceneApi.encodeStringField(gen, "units", units);
		LuceneApi.encodeStringField(gen, "stringValue", "v" + i * i);
		LuceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "S" + name + "' '" + units + "' 'v" + i * i + "'");

		gen.writeStartArray();
		LuceneApi.encodeStringField(gen, "name", "N" + name);
		LuceneApi.encodeStringField(gen, "units", units);
		LuceneApi.encodeDoubleField(gen, "numericValue", new Double(j * j));
		LuceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "N" + name + "' '" + units + "' " + new Double(j * j));

		gen.writeStartArray();
		LuceneApi.encodeStringField(gen, "name", "D" + name);
		LuceneApi.encodeStringField(gen, "units", units);
		LuceneApi.encodeStringField(gen, "dateTimeValue", new Date(now + 60000 * k * k));
		LuceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		System.out.println(
				rel + " " + i + " '" + "D" + name + "' '" + units + "' '" + new Date(now + 60000 * k * k) + "'");

	}

	@Test
	public void investigations() throws Exception {
		populate();

		/* Blocked results */
		LuceneSearchResult lsr = luceneApi.investigations(null, null, null, null, null, null, null, 5);
		Long uid = lsr.getUid();
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		System.out.println(uid);
		lsr = luceneApi.investigations(uid, 6);
		assertTrue(lsr.getUid() == null);
		checkLsr(lsr, 5L, 6L, 7L, 8L, 9L);
		luceneApi.freeSearcher(uid);

		lsr = luceneApi.investigations(null, null, null, null, null, null, "b", 100);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations(null, null, null, null, null, null, "FN", 100);
		checkLsr(lsr, 1L, 3L, 4L, 5L, 6L, 7L, 9L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations(null, null, null, null, null, null, "FN AND \"b b\"", 100);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations("b1", null, null, null, null, null, "b", 100);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations("c1", null, null, null, null, null, "b", 100);
		checkLsr(lsr);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations("b1", null, null, null, null, null, "b", 100);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations(null, "l v", null, null, null, null, null, 100);
		checkLsr(lsr, 4L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations("b1", "d", null, null, null, null, "b", 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations("b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6), null, null, "b",
				100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		lsr = luceneApi.investigations(null, null, new Date(now + 60000 * 3), new Date(now + 60000 * 6), null, null,
				null, 100);
		checkLsr(lsr, 3L, 4L, 5L);
		luceneApi.freeSearcher(lsr.getUid());

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		lsr = luceneApi.investigations(null, null, null, null, pojos, null, null, 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, 7, 10));
		pojos.add(new ParameterPOJO(null, null, new Date(now + 60000 * 63), new Date(now + 60000 * 65)));
		lsr = luceneApi.investigations(null, null, null, null, pojos, null, null, 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		lsr = luceneApi.investigations("b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6), pojos, null,
				"b", 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, "v81"));
		lsr = luceneApi.investigations(null, null, null, null, pojos, null, null, 100);
		checkLsr(lsr);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		lsr = luceneApi.investigations(null, null, null, null, pojos, null, null, 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		List<String> samples = Arrays.asList("ddd", "nnn");
		lsr = luceneApi.investigations(null, null, null, null, null, samples, null, 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());

		samples = Arrays.asList("ddd", "mmm");
		lsr = luceneApi.investigations(null, null, null, null, null, samples, null, 100);
		checkLsr(lsr);
		luceneApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		samples = Arrays.asList("ddd", "nnn");
		lsr = luceneApi.investigations("b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6), pojos, samples,
				"b", 100);
		checkLsr(lsr, 3L);
		luceneApi.freeSearcher(lsr.getUid());
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

	/**
	 * Populate UserGroup, Investigation, InvestigationParameter,
	 * InvestigationUser, Dataset,DatasetParameter,Datafile, DatafileParameter
	 * and Sample
	 */
	private void populate() throws IcatException {

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
						System.out.println("'" + fn + "' " + name + " " + i);
					}
				}
			}
			gen.writeEnd();
		}
		addDocuments("InvestigationUser", baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMINV; i++) {
				int j = i % 26;
				int k = (i + 7) % 26;
				int l = (i + 17) % 26;
				String word = letters.substring(j, j + 1) + " " + letters.substring(k, k + 1) + " "
						+ letters.substring(l, l + 1);
				gen.writeStartArray();
				LuceneApi.encodeTextfield(gen, "text", word);
				LuceneApi.encodeStringField(gen, "startDate", new Date(now + i * 60000));
				LuceneApi.encodeStringField(gen, "endDate", new Date(now + (i + 1) * 60000));
				LuceneApi.encodeStoredId(gen, new Long(i));
				LuceneApi.encodeSortedDocValuesField(gen, "id", new Long(i));
				gen.writeEnd();
				System.out.println("INVESTIGATION '" + word + "' " + new Date(now + i * 60000) + " " + i);
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
				LuceneApi.encodeStringField(gen, "startDate", new Date(now + i * 60000));
				LuceneApi.encodeStringField(gen, "endDate", new Date(now + (i + 1) * 60000));
				LuceneApi.encodeStoredId(gen, new Long(i));
				LuceneApi.encodeSortedDocValuesField(gen, "id", new Long(i));
				LuceneApi.encodeStringField(gen, "investigation", new Long(i % NUMINV));
				gen.writeEnd();
				System.out.println("DATASET '" + word + "' " + new Date(now + i * 60000) + " " + i + " " + i % NUMINV);
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
				LuceneApi.encodeStringField(gen, "date", new Date(now + i * 60000));
				LuceneApi.encodeStoredId(gen, new Long(i));
				LuceneApi.encodeStringField(gen, "dataset", new Long(i % NUMDS));
				gen.writeEnd();
				System.out.println("DATAFILE '" + word + "' " + new Date(now + i * 60000) + " " + i + " " + i % NUMDS);

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
				System.out.println("SAMPLE '" + word + "' " + i % NUMINV);
			}
			gen.writeEnd();

		}
		addDocuments("Sample", baos.toString());

		luceneApi.commit();

	}

}
