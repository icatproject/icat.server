package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.net.URL;
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
import javax.json.stream.JsonGenerator;

import org.icatproject.core.IcatException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestElasticsearchApi {

	static ElasticsearchApi searchApi;
	final static Logger logger = LoggerFactory.getLogger(TestElasticsearchApi.class);

	@BeforeClass
	public static void beforeClass() throws Exception {
		String urlString = System.getProperty("elasticsearchUrl");
		logger.info("Using Elasticsearch service at {}", urlString);
		searchApi = new ElasticsearchApi(Arrays.asList(new URL(urlString)));
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
			searchApi.encodeTextField(gen, "text", "Elephants and Aardvarks");
			searchApi.encodeStringField(gen, "startDate", new Date());
			searchApi.encodeStringField(gen, "endDate", new Date());
			searchApi.encodeStoredId(gen, 42L);
			searchApi.encodeStringField(gen, "dataset", 2001L);
			gen.writeEnd();
		}

		String json = baos.toString();
		// Create
		queue.add(new QueueItem("Datafile", null, json));
		// Update
		queue.add(new QueueItem("Datafile", 42L, json));
		// Delete
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
			searchApi.modify(sb.toString());
		}
	}

	@Before
	public void before() throws Exception {
		searchApi.clear();
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

	@Test
	public void datafiles() throws Exception {
		populate();

		SearchResult lsr = searchApi
				.getResults(SearchApi.buildQuery("Datafile", null, null, null, null, null, null, null), 5);
		String uid = lsr.getUid();

		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		System.out.println(uid);
		lsr = searchApi.getResults(uid, SearchApi.buildQuery("Datafile", null, null, null, null, null, null, null),
				200);
		// assertTrue(lsr.getUid() == null);
		assertEquals(95, lsr.getResults().size());
		searchApi.freeSearcher(uid);

		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", "e4", null, null, null, null, null, null), 100);
		checkLsr(lsr, 1L, 6L, 11L, 16L, 21L, 26L, 31L, 36L, 41L, 46L, 51L, 56L, 61L, 66L, 71L, 76L, 81L, 86L, 91L, 96L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", "e4", "dfbbb", null, null, null, null, null), 100);
		checkLsr(lsr, 1L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", null, "dfbbb", null, null, null, null, null), 100);
		checkLsr(lsr, 1L, 27L, 53L, 79L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100);
		checkLsr(lsr, 3L, 4L, 5L, 6L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100);
		checkLsr(lsr);
		searchApi.freeSearcher(lsr.getUid());

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, null), 100);
		checkLsr(lsr, 5L);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", null, null, null, null, pojos, null, null), 100);
		checkLsr(lsr, 5L);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, "u sss", null));
		lsr = searchApi.getResults(SearchApi.buildQuery("Datafile", null, null, null, null, pojos, null, null), 100);
		checkLsr(lsr, 13L, 65L);
		searchApi.freeSearcher(lsr.getUid());
	}

	@Test
	public void datasets() throws Exception {
		populate();
		SearchResult lsr = searchApi
				.getResults(SearchApi.buildQuery("Dataset", null, null, null, null, null, null, null), 5);

		String uid = lsr.getUid();
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		System.out.println(uid);
		lsr = searchApi.getResults(uid, SearchApi.buildQuery("Dataset", null, null, null, null, null, null, null), 100);
		// assertTrue(lsr.getUid() == null);
		checkLsr(lsr, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L,
				25L, 26L, 27L, 28L, 29L);
		searchApi.freeSearcher(uid);

		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", "e4", null, null, null, null, null, null), 100);
		checkLsr(lsr, 1L, 6L, 11L, 16L, 21L, 26L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", "e4", "dsbbb", null, null, null, null, null), 100);
		checkLsr(lsr, 1L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", null, "dsbbb", null, null, null, null, null), 100);
		checkLsr(lsr, 1L, 27L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100);
		checkLsr(lsr, 3L, 4L, 5L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", null, null, null, null, pojos, null, null), 100);
		checkLsr(lsr, 4L);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, null), 100);
		checkLsr(lsr, 4L);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Dataset", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, null), 100);
		checkLsr(lsr);
		searchApi.freeSearcher(lsr.getUid());

	}

	private void fillParameters(JsonGenerator gen, int i, String rel) {
		int j = i % 26;
		int k = (i + 5) % 26;
		String name = "nm " + letters.substring(j, j + 1) + letters.substring(j, j + 1) + letters.substring(j, j + 1);
		String units = "u " + letters.substring(k, k + 1) + letters.substring(k, k + 1) + letters.substring(k, k + 1);

		gen.writeStartArray();
		gen.write(rel + "Parameter");
		gen.writeNull();
		gen.writeStartArray();
		searchApi.encodeStringField(gen, "parameterName", "S" + name);
		searchApi.encodeStringField(gen, "parameterUnits", units);
		searchApi.encodeStringField(gen, "parameterStringValue", "v" + i * i);
		searchApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "S" + name + "' '" + units + "' 'v" + i * i + "'");

		gen.writeStartArray();
		gen.write(rel + "Parameter");
		gen.writeNull();
		gen.writeStartArray();
		searchApi.encodeStringField(gen, "parameterName", "N" + name);
		searchApi.encodeStringField(gen, "parameterUnits", units);
		searchApi.encodeDoublePoint(gen, "parameterNumericValue", new Double(j * j));
		searchApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "N" + name + "' '" + units + "' " + new Double(j * j));

		gen.writeStartArray();
		gen.write(rel + "Parameter");
		gen.writeNull();
		gen.writeStartArray();
		searchApi.encodeStringField(gen, "parameterName", "D" + name);
		searchApi.encodeStringField(gen, "parameterUnits", units);
		searchApi.encodeStringField(gen, "parameterDateValue", new Date(now + 60000 * k * k));
		searchApi.encodeSortedDocValuesField(gen, rel, new Long(i));
		gen.writeEnd();
		gen.writeEnd();
		System.out.println(
				rel + " " + i + " '" + "D" + name + "' '" + units + "' '" + new Date(now + 60000 * k * k) + "'");

	}

	@Test
	public void investigations() throws Exception {
		populate();

		/* Blocked results */
		SearchResult lsr = searchApi.getResults(
				SearchApi.buildQuery("Investigation", null, null, null, null, null, null, null),
				5);
		String uid = lsr.getUid();
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		System.out.println(uid);
		lsr = searchApi.getResults(uid, SearchApi.buildQuery("Investigation", null, null, null, null, null, null, null),
				6);
		// assertTrue(lsr.getUid() == null);
		checkLsr(lsr, 5L, 6L, 7L, 8L, 9L);
		searchApi.freeSearcher(uid);

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "b"), 100);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "FN"),
				100);
		checkLsr(lsr, 1L, 3L, 4L, 5L, 6L, 7L, 9L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(
				SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "FN AND \"b b\""),
				100);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", "b1", null, null, null, null, null, "b"), 100);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", "c1", null, null, null, null, null, "b"), 100);
		checkLsr(lsr);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, "l v", null, null, null, null, null),
				100);
		checkLsr(lsr, 4L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", null, null, null, null, "b"), 100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, "b"), 100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null, null), 100);
		checkLsr(lsr, 3L, 4L, 5L);
		searchApi.freeSearcher(lsr.getUid());

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, 7, 10));
		pojos.add(new ParameterPOJO(null, null, new Date(now + 60000 * 63), new Date(now + 60000 * 65)));
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null, "b"), 100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, "v81"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100);
		checkLsr(lsr);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null),
				100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		List<String> samples = Arrays.asList("ddd", "nnn");
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, samples, null),
				100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());

		samples = Arrays.asList("ddd", "mmm");
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, samples, null),
				100);
		checkLsr(lsr);
		searchApi.freeSearcher(lsr.getUid());

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		samples = Arrays.asList("ddd", "nnn");
		lsr = searchApi.getResults(SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, samples, "b"), 100);
		checkLsr(lsr, 3L);
		searchApi.freeSearcher(lsr.getUid());
	}

	/**
	 * Populate Investigation, Dataset, Datafile
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
						gen.write("InvestigationUser");
						gen.writeNull();
						gen.writeStartArray();

						searchApi.encodeTextField(gen, "userFullName", fn);

						searchApi.encodeStringField(gen, "userName", name);
						searchApi.encodeSortedDocValuesField(gen, "investigation", new Long(i));

						gen.writeEnd();
						gen.writeEnd();
						System.out.println("'" + fn + "' " + name + " " + i);
					}
				}
			}
			gen.writeEnd();
		}
		searchApi.modify(baos.toString());
		logger.debug("IUs added:");
		searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, null, null), 100); // TODO
																													// RM

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
				gen.write("Investigation");
				gen.writeNull();
				gen.writeStartArray();
				searchApi.encodeTextField(gen, "text", word);
				searchApi.encodeStringField(gen, "startDate", new Date(now + i * 60000));
				searchApi.encodeStringField(gen, "endDate", new Date(now + (i + 1) * 60000));
				searchApi.encodeStoredId(gen, new Long(i));
				searchApi.encodeSortedDocValuesField(gen, "id", new Long(i));
				gen.writeEnd();
				gen.writeEnd();
				System.out.println("INVESTIGATION '" + word + "' " + new Date(now + i * 60000) + " " + i);
			}
			gen.writeEnd();
		}
		searchApi.modify(baos.toString());
		logger.debug("Is added:");
		searchApi.getResults(SearchApi.buildQuery("Investigation", null, null, null, null, null, null, null), 100); // TODO
																													// RM

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMINV; i++) {
				if (i % 2 == 1) {
					fillParameters(gen, i, "investigation");
				}
			}
			gen.writeEnd();
		}
		searchApi.modify(baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDS; i++) {
				int j = i % 26;
				String word = "DS" + letters.substring(j, j + 1) + letters.substring(j, j + 1)
						+ letters.substring(j, j + 1);
				gen.writeStartArray();
				gen.write("Dataset");
				gen.writeNull();
				gen.writeStartArray();
				searchApi.encodeTextField(gen, "text", word);
				searchApi.encodeStringField(gen, "startDate", new Date(now + i * 60000));
				searchApi.encodeStringField(gen, "endDate", new Date(now + (i + 1) * 60000));
				searchApi.encodeStoredId(gen, new Long(i));
				searchApi.encodeSortedDocValuesField(gen, "id", new Long(i));
				searchApi.encodeStringField(gen, "investigation", new Long(i % NUMINV));
				gen.writeEnd();
				gen.writeEnd();
				System.out.println("DATASET '" + word + "' " + new Date(now + i * 60000) + " " + i + " " + i % NUMINV);
			}
			gen.writeEnd();
		}
		searchApi.modify(baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDS; i++) {
				if (i % 3 == 1) {
					fillParameters(gen, i, "dataset");
				}
			}
			gen.writeEnd();
		}
		searchApi.modify(baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDF; i++) {
				int j = i % 26;
				String word = "DF" + letters.substring(j, j + 1) + letters.substring(j, j + 1)
						+ letters.substring(j, j + 1);
				gen.writeStartArray();
				gen.write("Datafile");
				gen.writeNull();
				gen.writeStartArray();
				searchApi.encodeTextField(gen, "text", word);
				searchApi.encodeStringField(gen, "date", new Date(now + i * 60000));
				searchApi.encodeStoredId(gen, new Long(i));
				searchApi.encodeStringField(gen, "dataset", new Long(i % NUMDS));
				searchApi.encodeStringField(gen, "investigation", new Long((i % NUMDS) % NUMINV));
				gen.writeEnd();
				gen.writeEnd();
				System.out.println("DATAFILE '" + word + "' " + new Date(now + i * 60000) + " " + i + " " + i % NUMDS);

			}
			gen.writeEnd();
		}
		searchApi.modify(baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMDF; i++) {
				if (i % 4 == 1) {
					fillParameters(gen, i, "datafile");
				}
			}
			gen.writeEnd();
		}
		searchApi.modify(baos.toString());

		baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartArray();
			for (int i = 0; i < NUMSAMP; i++) {
				int j = i % 26;
				String word = "SType " + letters.substring(j, j + 1) + letters.substring(j, j + 1)
						+ letters.substring(j, j + 1);
				gen.writeStartArray();
				gen.write("Sample");
				gen.writeNull();
				gen.writeStartArray();
				searchApi.encodeTextField(gen, "sampleText", word);
				searchApi.encodeSortedDocValuesField(gen, "investigation", new Long(i % NUMINV));
				gen.writeEnd();
				gen.writeEnd();
				System.out.println("SAMPLE '" + word + "' " + i % NUMINV);
			}
			gen.writeEnd();

		}
		searchApi.modify(baos.toString());

		searchApi.commit();

	}
}
