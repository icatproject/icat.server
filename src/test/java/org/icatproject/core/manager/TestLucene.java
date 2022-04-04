package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
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

import javax.json.Json;
import javax.json.JsonObject;
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
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationType;
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			luceneApi.encodeTextField(gen, "text", "Elephants and Aardvarks");
			luceneApi.encodeStringField(gen, "startDate", new Date());
			luceneApi.encodeStringField(gen, "endDate", new Date());
			luceneApi.encodeStringField(gen, "id", 42L, true);
			luceneApi.encodeStringField(gen, "dataset", 2001L);
			gen.writeEnd();
		}
		String elephantJson = baos.toString();

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			luceneApi.encodeTextField(gen, "text", "Rhinos and Aardvarks");
			luceneApi.encodeStringField(gen, "startDate", new Date());
			luceneApi.encodeStringField(gen, "endDate", new Date());
			luceneApi.encodeStoredId(gen, 42L);
			luceneApi.encodeStringField(gen, "dataset", 2001L);
			gen.writeEnd();
		}
		String rhinoJson = baos.toString();

		JsonObject elephantQuery = SearchApi.buildQuery("Datafile", null, "elephant", null, null, null, null, null);
		JsonObject rhinoQuery = SearchApi.buildQuery("Datafile", null, "rhino", null, null, null, null, null);

		Queue<QueueItem> queue = new ConcurrentLinkedQueue<>();
		queue.add(new QueueItem("Datafile", null, elephantJson));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5), 42L);
		checkLsr(luceneApi.getResults(rhinoQuery, 5));

		queue = new ConcurrentLinkedQueue<>();
		queue.add(new QueueItem("Datafile", 42L, rhinoJson));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5), 42L);

		queue = new ConcurrentLinkedQueue<>();
		queue.add(new QueueItem("Datafile", 42L, null));
		queue.add(new QueueItem("Datafile", 42L, null));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5));

		queue = new ConcurrentLinkedQueue<>();
		queue.add(new QueueItem("Datafile", null, elephantJson));
		queue.add(new QueueItem("Datafile", 42L, rhinoJson));
		queue.add(new QueueItem("Datafile", 42L, null));
		queue.add(new QueueItem("Datafile", 42L, null));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5));
	}

	private void modifyQueue(Queue<QueueItem> queue) throws IcatException {
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
			luceneApi.commit();
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

	private void checkDatafile(ScoredEntityBaseBean datafile) {
		JsonObject source = datafile.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(Arrays.asList("id", "dataset", "investigation", "name", "text", "date"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("0", source.getString("dataset"));
		assertEquals("0", source.getString("investigation"));
		assertEquals("DFaaa", source.getString("name"));
		assertEquals("DFaaa", source.getString("text"));
		assertNotNull(source.getString("date"));
	}

	private void checkDataset(ScoredEntityBaseBean dataset) {
		JsonObject source = dataset.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(Arrays.asList("id", "investigation", "name", "text", "startDate", "endDate"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("0", source.getString("investigation"));
		assertEquals("DSaaa", source.getString("name"));
		assertEquals("DSaaa null null", source.getString("text"));
		assertNotNull(source.getString("startDate"));
		assertNotNull(source.getString("endDate"));
	}

	private void checkInvestigation(ScoredEntityBaseBean investigation) {
		JsonObject source = investigation.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(Arrays.asList("id", "name", "text", "startDate", "endDate"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("a h r", source.getString("name"));
		assertEquals("null a h r null null", source.getString("text"));
		assertNotNull(source.getString("startDate"));
		assertNotNull(source.getString("endDate"));
	}

	private void checkLsr(SearchResult lsr, Long... n) {
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

	private void checkLsrOrder(SearchResult lsr, Long... n) {
		List<ScoredEntityBaseBean> results = lsr.getResults();
		if (n.length != results.size()) {
			checkLsr(lsr, n);
		}
		for (int i = 0; i < n.length; i++) {
			Long resultId = results.get(i).getEntityBaseBeanId();
			Long expectedId = (Long) Array.get(n, i);
			if (resultId != expectedId) {
				fail("Expected id " + expectedId + " in position " + i + " but got " + resultId);
			}
		}
	}

	@Test
	public void datafiles() throws Exception {
		populate();

		JsonObject query = SearchApi.buildQuery("Datafile", null, null, null, null, null, null, null);
		List<String> fields = Arrays.asList("date", "name", "investigation", "id", "text", "dataset");
		SearchResult lsr = luceneApi.getResults(query, null, 5, null, fields);
		String searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		checkDatafile(lsr.getResults().get(0));
		lsr = luceneApi.getResults(query, searchAfter.toString(), 200, null, null);
		assertNull(lsr.getSearchAfter());
		assertEquals(95, lsr.getResults().size());

		// Test searchAfter preserves the sorting of original search (asc)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("date", "asc");
			gen.writeEnd();
		}
		String sort = baos.toString();
		lsr = luceneApi.getResults(query, null, 5, sort, null);
		checkLsrOrder(lsr, 0L, 1L, 2L, 3L, 4L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter.toString(), 5, sort, null);
		checkLsrOrder(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		// Test searchAfter preserves the sorting of original search (desc)
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("date", "desc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, null, 5, sort, null);
		checkLsrOrder(lsr, 99L, 98L, 97L, 96L, 95L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter.toString(), 5, sort, null);
		checkLsrOrder(lsr, 94L, 93L, 92L, 91L, 90L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		// Test tie breaks on fields with identical values (asc)
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("name", "asc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, null, 5, sort, null);
		checkLsrOrder(lsr, 0L, 26L, 52L, 78L, 1L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("name", "asc");
			gen.write("date", "desc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, null, 5, sort, null);
		checkLsrOrder(lsr, 78L, 52L, 26L, 0L, 79L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		// Test tie breaks on fields with identical values (desc)
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("name", "desc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, null, 5, sort, null);
		checkLsrOrder(lsr, 25L, 51L, 77L, 24L, 50L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("name", "desc");
			gen.write("date", "desc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, null, 5, sort, null);
		checkLsrOrder(lsr, 77L, 51L, 25L, 76L, 50L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		query = SearchApi.buildQuery("Datafile", "e4", null, null, null, null, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 1L, 6L, 11L, 16L, 21L, 26L, 31L, 36L, 41L, 46L, 51L, 56L, 61L, 66L, 71L, 76L, 81L, 86L, 91L, 96L);

		query = SearchApi.buildQuery("Datafile", "e4", "dfbbb", null, null, null, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 1L);

		query = SearchApi.buildQuery("Datafile", null, "dfbbb", null, null, null, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 1L, 27L, 53L, 79L);

		query = SearchApi.buildQuery("Datafile", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L, 4L, 5L, 6L);

		query = SearchApi.buildQuery("Datafile", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr);

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		query = SearchApi.buildQuery("Datafile", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 5L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		query = SearchApi.buildQuery("Datafile", null, null, null, null, pojos, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 5L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, "u sss", null));
		query = SearchApi.buildQuery("Datafile", null, null, null, null, pojos, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 13L, 65L);
	}

	@Test
	public void datasets() throws Exception {
		populate();

		JsonObject query = SearchApi.buildQuery("Dataset", null, null, null, null, null, null, null);
		List<String> fields = Arrays.asList("startDate", "endDate", "name", "investigation", "id", "text");
		SearchResult lsr = luceneApi.getResults(query, null, 5, null, fields);
		String searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		checkDataset(lsr.getResults().get(0));
		lsr = luceneApi.getResults(query, searchAfter.toString(), 100, null, null);
		assertNull(lsr.getSearchAfter());
		checkLsr(lsr, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L,
				25L, 26L, 27L, 28L, 29L);


		// Test searchAfter preserves the sorting of original search (asc)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("startDate", "asc");
			gen.writeEnd();
		}
		String sort = baos.toString();
		lsr = luceneApi.getResults(query, 5, sort);
		checkLsrOrder(lsr, 0L, 1L, 2L, 3L, 4L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, null);
		checkLsrOrder(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		// Test searchAfter preserves the sorting of original search (desc)
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("endDate", "desc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, 5, sort);
		checkLsrOrder(lsr, 29L, 28L, 27L, 26L, 25L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, null);
		checkLsrOrder(lsr, 24L, 23L, 22L, 21L, 20L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		// Test tie breaks on fields with identical values (asc)
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("name", "asc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, null);
		checkLsrOrder(lsr, 0L, 26L, 1L, 27L, 2L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("name", "asc");
			gen.write("endDate", "desc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, 5, sort);
		checkLsrOrder(lsr, 26L, 0L, 27L, 1L, 28L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", "e4", null, null, null, null, null, null), 100,
				null);
		checkLsr(lsr, 1L, 6L, 11L, 16L, 21L, 26L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", "e4", "dsbbb", null, null, null, null, null), 100,
				null);
		checkLsr(lsr, 1L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", null, "dsbbb", null, null, null, null, null), 100,
				null);
		checkLsr(lsr, 1L, 27L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100, null);
		checkLsr(lsr, 3L, 4L, 5L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100, null);
		checkLsr(lsr, 3L);

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", null, null, null, null, pojos, null, null), 100,
				null);
		checkLsr(lsr, 4L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, null), 100, null);
		checkLsr(lsr, 4L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Dataset", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, null), 100, null);
		checkLsr(lsr);
	}

	private void fillParms(JsonGenerator gen, int i, String rel) {
		int j = i % 26;
		int k = (i + 5) % 26;
		String name = "nm " + letters.substring(j, j + 1) + letters.substring(j, j + 1) + letters.substring(j, j + 1);
		String units = "u " + letters.substring(k, k + 1) + letters.substring(k, k + 1) + letters.substring(k, k + 1);

		gen.writeStartArray();
		luceneApi.encodeStringField(gen, "name", "S" + name);
		luceneApi.encodeStringField(gen, "units", units);
		luceneApi.encodeStringField(gen, "stringValue", "v" + i * i);
		luceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "S" + name + "' '" + units + "' 'v" + i * i + "'");

		gen.writeStartArray();
		luceneApi.encodeStringField(gen, "name", "N" + name);
		luceneApi.encodeStringField(gen, "units", units);
		luceneApi.encodeDoublePoint(gen, "numericValue", new Double(j * j));
		luceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "N" + name + "' '" + units + "' " + new Double(j * j));

		gen.writeStartArray();
		luceneApi.encodeStringField(gen, "name", "D" + name);
		luceneApi.encodeStringField(gen, "units", units);
		luceneApi.encodeStringField(gen, "dateTimeValue", new Date(now + 60000 * k * k));
		luceneApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		System.out.println(
				rel + " " + i + " '" + "D" + name + "' '" + units + "' '" + new Date(now + 60000 * k * k) + "'");

	}

	@Test
	public void investigations() throws Exception {
		populate();

		/* Blocked results */
		JsonObject query = SearchApi.buildQuery("Investigation", null, null, null, null, null, null, null);
		List<String> fields = Arrays.asList("startDate", "endDate", "name", "id", "text");
		SearchResult lsr = luceneApi.getResults(query, null, 5, null, fields);
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		checkInvestigation(lsr.getResults().get(0));
		String searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter, 6, null, null);
		checkLsr(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNull(searchAfter);

		// Test searchAfter preserves the sorting of original search (asc)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("startDate", "asc");
			gen.writeEnd();
		}
		String sort = baos.toString();
		lsr = luceneApi.getResults(query, 5, sort);
		checkLsrOrder(lsr, 0L, 1L, 2L, 3L, 4L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, null);
		checkLsrOrder(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		// Test searchAfter preserves the sorting of original search (desc)
		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject();
			gen.write("endDate", "desc");
			gen.writeEnd();
		}
		sort = baos.toString();
		lsr = luceneApi.getResults(query, 5, sort);
		checkLsrOrder(lsr, 9L, 8L, 7L, 6L, 5L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, null);
		checkLsrOrder(lsr, 4L, 3L, 2L, 1L, 0L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "b"), 100,
				null);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "FN"), 100,
				null);
		checkLsr(lsr, 1L, 3L, 4L, 5L, 6L, 7L, 9L);

		lsr = luceneApi.getResults(
				SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "FN AND \"b b\""),
				100, null);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", "b1", null, null, null, null, null, "b"), 100,
				null);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", "c1", null, null, null, null, null, "b"), 100,
				null);
		checkLsr(lsr);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, "l v", null, null, null, null, null),
				100, null);
		checkLsr(lsr, 4L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", null, null, null, null, "b"), 100,
				null);
		checkLsr(lsr, 3L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, "b"), 100, null);
		checkLsr(lsr, 3L);

		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100, null);
		checkLsr(lsr, 3L, 4L, 5L);

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100, null);
		checkLsr(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, 7, 10));
		pojos.add(new ParameterPOJO(null, null, new Date(now + 60000 * 63), new Date(now + 60000 * 65)));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100, null);
		checkLsr(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, "b"), 100, null);
		checkLsr(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, "v81"));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100, null);
		checkLsr(lsr);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100, null);
		checkLsr(lsr, 3L);

		List<String> samples = Arrays.asList("ddd", "nnn");
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, samples, null),
				100, null);
		checkLsr(lsr, 3L);

		samples = Arrays.asList("ddd", "mmm");
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, samples, null),
				100, null);
		checkLsr(lsr);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		samples = Arrays.asList("ddd", "nnn");
		lsr = luceneApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, samples, "b"), 100, null);
		checkLsr(lsr, 3L);
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

						luceneApi.encodeTextField(gen, "text", fn);

						luceneApi.encodeStringField(gen, "name", name);
						luceneApi.encodeSortedDocValuesField(gen, "investigation", new Long(i));

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
				Investigation investigation = new Investigation();
				investigation.setFacility(new Facility());
				investigation.setType(new InvestigationType());
				investigation.setName(word);
				investigation.setStartDate(new Date(now + i * 60000));
				investigation.setEndDate(new Date(now + (i + 1) * 60000));
				investigation.setId(new Long(i));
				gen.writeStartArray();
				investigation.getDoc(gen, luceneApi);
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

				Investigation investigation = new Investigation();
				investigation.setId(new Long(i % NUMINV));
				Dataset dataset = new Dataset();
				dataset.setType(new DatasetType());
				dataset.setName(word);
				dataset.setStartDate(new Date(now + i * 60000));
				dataset.setEndDate(new Date(now + (i + 1) * 60000));
				dataset.setId(new Long(i));
				dataset.setInvestigation(investigation);

				gen.writeStartArray();
				dataset.getDoc(gen, luceneApi);
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

				Investigation investigation = new Investigation();
				investigation.setId(new Long((i % NUMDS) % NUMINV));
				Dataset dataset = new Dataset();
				dataset.setId(new Long(i % NUMDS));
				dataset.setInvestigation(investigation);
				Datafile datafile = new Datafile();
				datafile.setName(word);
				datafile.setDatafileModTime(new Date(now + i * 60000));
				datafile.setId(new Long(i));
				datafile.setDataset(dataset);

				gen.writeStartArray();
				datafile.getDoc(gen, luceneApi);
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
				luceneApi.encodeTextField(gen, "text", word);
				luceneApi.encodeSortedDocValuesField(gen, "investigation", new Long(i % NUMINV));
				gen.writeEnd();
				System.out.println("SAMPLE '" + word + "' " + i % NUMINV);
			}
			gen.writeEnd();

		}
		addDocuments("Sample", baos.toString());

		luceneApi.commit();

	}

}
