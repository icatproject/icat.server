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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.DatafileParameter;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationParameter;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.InvestigationUser;
import org.icatproject.core.entity.Parameter;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleType;
import org.icatproject.core.entity.User;
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

	@Test
	public void modifyDatafile() throws IcatException {
		Investigation investigation = new Investigation();
		investigation.setId(0L);
		Dataset dataset = new Dataset();
		dataset.setId(0L);
		dataset.setInvestigation(investigation);

		Datafile elephantDatafile = new Datafile();
		elephantDatafile.setName("Elephants and Aardvarks");
		elephantDatafile.setDatafileModTime(new Date(0L));
		elephantDatafile.setId(42L);
		elephantDatafile.setDataset(dataset);

		DatafileFormat pdfFormat = new DatafileFormat();
		pdfFormat.setId(0L);
		pdfFormat.setName("pdf");
		Datafile rhinoDatafile = new Datafile();
		rhinoDatafile.setName("Rhinos and Aardvarks");
		rhinoDatafile.setDatafileModTime(new Date(3L));
		rhinoDatafile.setId(42L);
		rhinoDatafile.setDataset(dataset);
		rhinoDatafile.setDatafileFormat(pdfFormat);

		DatafileFormat pngFormat = new DatafileFormat();
		pngFormat.setId(0L);
		pngFormat.setName("png");

		JsonObject elephantQuery = SearchApi.buildQuery("Datafile", null, "elephant", null, null, null, null, null);
		JsonObject rhinoQuery = SearchApi.buildQuery("Datafile", null, "rhino", null, null, null, null, null);
		JsonObject pdfQuery = SearchApi.buildQuery("Datafile", null, "datafileFormat.name:pdf", null, null, null, null,
				null);
		JsonObject pngQuery = SearchApi.buildQuery("Datafile", null, "datafileFormat.name:png", null, null, null, null,
				null);
		JsonObject queryObject = Json.createObjectBuilder().add("id", Json.createArrayBuilder().add("42")).build();
		JsonObjectBuilder lowRangeBuilder = Json.createObjectBuilder().add("lower", 0L).add("upper", 1L);
		JsonObjectBuilder highRangeBuilder = Json.createObjectBuilder().add("lower", 2L).add("upper", 3L);
		JsonArrayBuilder rangesBuilder = Json.createArrayBuilder().add(lowRangeBuilder).add(highRangeBuilder);
		JsonObjectBuilder dimensionBuilder = Json.createObjectBuilder().add("dimension", "date").add("ranges",
				rangesBuilder);
		JsonArrayBuilder dimensionsBuilder = Json.createArrayBuilder().add(dimensionBuilder);
		JsonObject stringFacetQuery = Json.createObjectBuilder().add("query", queryObject).build();
		JsonObject rangeFacetQuery = Json.createObjectBuilder().add("query", queryObject)
				.add("dimensions", dimensionsBuilder).build();

		// Original
		Queue<String> queue = new ConcurrentLinkedQueue<>();
		queue.add(SearchApi.encodeOperation("create", elephantDatafile));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5), 42L);
		checkLsr(luceneApi.getResults(rhinoQuery, 5));
		checkLsr(luceneApi.getResults(pdfQuery, 5));
		checkLsr(luceneApi.getResults(pngQuery, 5));
		List<FacetDimension> facetDimensions = luceneApi.facetSearch("Datafile", stringFacetQuery, 5, 5);
		assertEquals(0, facetDimensions.size());
		facetDimensions = luceneApi.facetSearch("Datafile", rangeFacetQuery, 5, 5);
		assertEquals(1, facetDimensions.size());
		FacetDimension facetDimension = facetDimensions.get(0);
		assertEquals("date", facetDimension.getDimension());
		assertEquals(2, facetDimension.getFacets().size());
		FacetLabel facetLabel = facetDimension.getFacets().get(0);
		assertEquals("0_1", facetLabel.getLabel());
		assertEquals(new Long(1), facetLabel.getValue());
		facetLabel = facetDimension.getFacets().get(1);
		assertEquals("2_3", facetLabel.getLabel());
		assertEquals(new Long(0), facetLabel.getValue());

		// Change name and add a format
		queue = new ConcurrentLinkedQueue<>();
		queue.add(SearchApi.encodeOperation("update", rhinoDatafile));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5), 42L);
		checkLsr(luceneApi.getResults(pdfQuery, 5), 42L);
		checkLsr(luceneApi.getResults(pngQuery, 5));
		facetDimensions = luceneApi.facetSearch("Datafile", stringFacetQuery, 5, 5);
		assertEquals(1, facetDimensions.size());
		facetDimensions = luceneApi.facetSearch("Datafile", rangeFacetQuery, 5, 5);
		assertEquals(1, facetDimensions.size());
		facetDimension = facetDimensions.get(0);
		assertEquals("date", facetDimension.getDimension());
		assertEquals(2, facetDimension.getFacets().size());
		facetLabel = facetDimension.getFacets().get(0);
		assertEquals("0_1", facetLabel.getLabel());
		assertEquals(new Long(0), facetLabel.getValue());
		facetLabel = facetDimension.getFacets().get(1);
		assertEquals("2_3", facetLabel.getLabel());
		assertEquals(new Long(1), facetLabel.getValue());

		// Change just the format
		queue = new ConcurrentLinkedQueue<>();
		queue.add(SearchApi.encodeOperation("update", pngFormat));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5), 42L);
		checkLsr(luceneApi.getResults(pdfQuery, 5));
		checkLsr(luceneApi.getResults(pngQuery, 5), 42L);
		facetDimensions = luceneApi.facetSearch("Datafile", stringFacetQuery, 5, 5);
		assertEquals(1, facetDimensions.size());
		facetDimension = facetDimensions.get(0);
		assertEquals("datafileFormat.name", facetDimension.getDimension());
		assertEquals(1, facetDimension.getFacets().size());
		facetLabel = facetDimension.getFacets().get(0);
		assertEquals("png", facetLabel.getLabel());
		assertEquals(new Long(1), facetLabel.getValue());
		facetDimensions = luceneApi.facetSearch("Datafile", rangeFacetQuery, 5, 5);
		assertEquals(1, facetDimensions.size());
		facetDimension = facetDimensions.get(0);
		assertEquals("date", facetDimension.getDimension());
		assertEquals(2, facetDimension.getFacets().size());
		facetLabel = facetDimension.getFacets().get(0);
		assertEquals("0_1", facetLabel.getLabel());
		assertEquals(new Long(0), facetLabel.getValue());
		facetLabel = facetDimension.getFacets().get(1);
		assertEquals("2_3", facetLabel.getLabel());
		assertEquals(new Long(1), facetLabel.getValue());

		// Remove the format
		queue = new ConcurrentLinkedQueue<>();
		queue.add(SearchApi.encodeOperation("delete", pngFormat));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5), 42L);
		checkLsr(luceneApi.getResults(pdfQuery, 5));
		checkLsr(luceneApi.getResults(pngQuery, 5));
		facetDimensions = luceneApi.facetSearch("Datafile", stringFacetQuery, 5, 5);
		assertEquals(0, facetDimensions.size());
		facetDimensions = luceneApi.facetSearch("Datafile", rangeFacetQuery, 5, 5);
		assertEquals(1, facetDimensions.size());
		facetDimension = facetDimensions.get(0);
		assertEquals("date", facetDimension.getDimension());
		assertEquals(2, facetDimension.getFacets().size());
		facetLabel = facetDimension.getFacets().get(0);
		assertEquals("0_1", facetLabel.getLabel());
		assertEquals(new Long(0), facetLabel.getValue());
		facetLabel = facetDimension.getFacets().get(1);
		assertEquals("2_3", facetLabel.getLabel());
		assertEquals(new Long(1), facetLabel.getValue());

		// Remove the file
		queue = new ConcurrentLinkedQueue<>();
		queue.add(SearchApi.encodeDeletion(elephantDatafile));
		queue.add(SearchApi.encodeDeletion(rhinoDatafile));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5));
		checkLsr(luceneApi.getResults(pdfQuery, 5));
		checkLsr(luceneApi.getResults(pngQuery, 5));

		// Multiple commands at once
		queue = new ConcurrentLinkedQueue<>();
		queue.add(SearchApi.encodeOperation("create", elephantDatafile));
		queue.add(SearchApi.encodeOperation("update", rhinoDatafile));
		queue.add(SearchApi.encodeDeletion(elephantDatafile));
		queue.add(SearchApi.encodeDeletion(rhinoDatafile));
		modifyQueue(queue);
		checkLsr(luceneApi.getResults(elephantQuery, 5));
		checkLsr(luceneApi.getResults(rhinoQuery, 5));
		checkLsr(luceneApi.getResults(pdfQuery, 5));
		checkLsr(luceneApi.getResults(pngQuery, 5));
	}

	private void modifyQueue(Queue<String> queue) throws IcatException {
		Iterator<String> qiter = queue.iterator();
		if (qiter.hasNext()) {
			StringBuilder sb = new StringBuilder("[");

			while (qiter.hasNext()) {
				String item = qiter.next();
				if (sb.length() != 1) {
					sb.append(',');
				}
				sb.append(item);
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
		Set<String> expectedKeys = new HashSet<>(
				Arrays.asList("id", "investigation.id", "name", "date"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("0", source.getString("investigation.id"));
		assertEquals("DFaaa", source.getString("name"));
		assertNotNull(source.getJsonNumber("date"));
	}

	private void checkDataset(ScoredEntityBaseBean dataset) {
		JsonObject source = dataset.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(
				Arrays.asList("id", "investigation.id", "name", "startDate", "endDate"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("0", source.getString("investigation.id"));
		assertEquals("DSaaa", source.getString("name"));
		assertNotNull(source.getJsonNumber("startDate"));
		assertNotNull(source.getJsonNumber("endDate"));
	}

	private void checkInvestigation(ScoredEntityBaseBean investigation) {
		JsonObject source = investigation.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(Arrays.asList("id", "name", "startDate", "endDate"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("a h r", source.getString("name"));
		assertNotNull(source.getJsonNumber("startDate"));
		assertNotNull(source.getJsonNumber("endDate"));
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
		List<String> fields = Arrays.asList("date", "name", "investigation.id", "id");
		SearchResult lsr = luceneApi.getResults(query, null, 5, null, fields);
		String searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		checkDatafile(lsr.getResults().get(0));
		lsr = luceneApi.getResults(query, searchAfter.toString(), 200, null, fields);
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
		lsr = luceneApi.getResults(query, null, 5, sort, fields);
		checkLsrOrder(lsr, 0L, 1L, 2L, 3L, 4L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter.toString(), 5, sort, fields);
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
		lsr = luceneApi.getResults(query, null, 5, sort, fields);
		checkLsrOrder(lsr, 99L, 98L, 97L, 96L, 95L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter.toString(), 5, sort, fields);
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
		lsr = luceneApi.getResults(query, null, 5, sort, fields);
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
		lsr = luceneApi.getResults(query, null, 5, sort, fields);
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
		lsr = luceneApi.getResults(query, null, 5, sort, fields);
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
		lsr = luceneApi.getResults(query, null, 5, sort, fields);
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
		List<String> fields = Arrays.asList("startDate", "endDate", "name", "investigation.id", "id");
		SearchResult lsr = luceneApi.getResults(query, null, 5, null, fields);
		String searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		checkDataset(lsr.getResults().get(0));
		lsr = luceneApi.getResults(query, searchAfter.toString(), 100, null, fields);
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
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, fields);
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
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, fields);
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
		lsr = luceneApi.getResults(query, null, 5, sort, fields);
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

		ParameterType dateParameterType = new ParameterType();
		dateParameterType.setId(0L);
		dateParameterType.setName("D" + name);
		dateParameterType.setUnits(units);
		ParameterType numericParameterType = new ParameterType();
		numericParameterType.setId(0L);
		numericParameterType.setName("N" + name);
		numericParameterType.setUnits(units);
		ParameterType stringParameterType = new ParameterType();
		stringParameterType.setId(0L);
		stringParameterType.setName("S" + name);
		stringParameterType.setUnits(units);

		Parameter parameter;
		if (rel.equals("datafile")) {
			parameter = new DatafileParameter();
			Datafile datafile = new Datafile();
			datafile.setId(new Long(i));
			((DatafileParameter) parameter).setDatafile(datafile);
		} else if (rel.equals("dataset")) {
			parameter = new DatasetParameter();
			Dataset dataset = new Dataset();
			dataset.setId(new Long(i));
			((DatasetParameter) parameter).setDataset(dataset);
		} else if (rel.equals("investigation")) {
			parameter = new InvestigationParameter();
			Investigation investigation = new Investigation();
			investigation.setId(new Long(i));
			((InvestigationParameter) parameter).setInvestigation(investigation);
		} else {
			fail(rel + " is not valid");
			return;
		}
		parameter.setId(0L);

		parameter.setType(dateParameterType);
		parameter.setDateTimeValue(new Date(now + 60000 * k * k));
		gen.writeStartObject();
		parameter.getDoc(gen);
		gen.writeEnd();
		System.out.println(
				rel + " " + i + " '" + "D" + name + "' '" + units + "' '" + new Date(now + 60000 * k * k) + "'");

		parameter.setType(numericParameterType);
		parameter.setNumericValue(new Double(j * j));
		gen.writeStartObject();
		parameter.getDoc(gen);
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "N" + name + "' '" + units + "' " + new Double(j * j));

		parameter.setType(stringParameterType);
		parameter.setStringValue("v" + i * i);
		gen.writeStartObject();
		parameter.getDoc(gen);
		gen.writeEnd();
		System.out.println(rel + " " + i + " '" + "S" + name + "' '" + units + "' 'v" + i * i + "'");
	}

	@Test
	public void investigations() throws Exception {
		populate();

		/* Blocked results */
		JsonObject query = SearchApi.buildQuery("Investigation", null, null, null, null, null, null, null);
		List<String> fields = Arrays.asList("startDate", "endDate", "name", "id");
		SearchResult lsr = luceneApi.getResults(query, null, 5, null, fields);
		checkLsr(lsr, 0L, 1L, 2L, 3L, 4L);
		checkInvestigation(lsr.getResults().get(0));
		String searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);
		lsr = luceneApi.getResults(query, searchAfter, 6, null, fields);
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
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, fields);
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
		lsr = luceneApi.getResults(query, searchAfter, 5, sort, fields);
		checkLsrOrder(lsr, 4L, 3L, 2L, 1L, 0L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(searchAfter);

		query = SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "b");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);

		query = SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "FN");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 1L, 3L, 4L, 5L, 6L, 7L, 9L);

		query = SearchApi.buildQuery("Investigation", null, null, null, null, null, null, "FN AND \"b b\"");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);

		query = SearchApi.buildQuery("Investigation", "b1", null, null, null, null, null, "b");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 1L, 3L, 5L, 7L, 9L);

		query = SearchApi.buildQuery("Investigation", "c1", null, null, null, null, null, "b");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr);

		query = SearchApi.buildQuery("Investigation", null, "l v", null, null, null, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 4L);

		query = SearchApi.buildQuery("Investigation", "b1", "d", null, null, null, null, "b");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L);

		query = SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				null, null, "b");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L);

		query = SearchApi.buildQuery("Investigation", null, null, new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				null, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L, 4L, 5L);

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		query = SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, 7, 10));
		pojos.add(new ParameterPOJO(null, null, new Date(now + 60000 * 63), new Date(now + 60000 * 65)));
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		query = SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				pojos, null, "b");
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, "v81"));
		query = SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		query = SearchApi.buildQuery("Investigation", null, null, null, null, pojos, null, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L);

		List<String> samples = Arrays.asList("ddd", "nnn");
		query = SearchApi.buildQuery("Investigation", null, null, null, null, null, samples, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr, 3L);

		samples = Arrays.asList("ddd", "mmm");
		query = SearchApi.buildQuery("Investigation", null, null, null, null, null, samples, null);
		lsr = luceneApi.getResults(query, 100, null);
		checkLsr(lsr);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		samples = Arrays.asList("ddd", "nnn");
		query = SearchApi.buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				pojos, samples, "b");
		lsr = luceneApi.getResults(query, 100, null);
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
			Long investigationUserId = 0L;
			gen.writeStartArray();
			for (int i = 0; i < NUMINV; i++) {
				for (int j = 0; j < NUMUSERS; j++) {
					if (i % (j + 1) == 1) {
						String fn = "FN " + letters.substring(j, j + 1) + " " + letters.substring(j, j + 1);
						String name = letters.substring(j, j + 1) + j;
						User user = new User();
						user.setId(new Long(j));
						user.setName(name);
						user.setFullName(fn);
						Investigation investigation = new Investigation();
						investigation.setId(new Long(i));
						InvestigationUser investigationUser = new InvestigationUser();
						investigationUser.setId(investigationUserId);
						investigationUser.setUser(user);
						investigationUser.setInvestigation(investigation);

						gen.writeStartObject();
						investigationUser.getDoc(gen);
						gen.writeEnd();
						investigationUserId++;
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
				Facility facility = new Facility();
				facility.setName("");
				facility.setId(0L);
				investigation.setFacility(facility);
				InvestigationType type = new InvestigationType();
				type.setName("test");
				type.setId(0L);
				investigation.setType(type);
				investigation.setName(word);
				investigation.setTitle("");
				investigation.setVisitId("");
				investigation.setStartDate(new Date(now + i * 60000));
				investigation.setEndDate(new Date(now + (i + 1) * 60000));
				investigation.setId(new Long(i));
				gen.writeStartObject();
				investigation.getDoc(gen);
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
				DatasetType type = new DatasetType();
				type.setName("test");
				type.setId(0L);
				dataset.setType(type);
				dataset.setName(word);
				dataset.setStartDate(new Date(now + i * 60000));
				dataset.setEndDate(new Date(now + (i + 1) * 60000));
				dataset.setId(new Long(i));
				dataset.setInvestigation(investigation);

				gen.writeStartObject();
				dataset.getDoc(gen);
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

				gen.writeStartObject();
				datafile.getDoc(gen);
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

				Investigation investigation = new Investigation();
				investigation.setId(new Long(i % NUMINV));
				SampleType sampleType = new SampleType();
				sampleType.setId(0L);
				sampleType.setName("test");
				Sample sample = new Sample();
				sample.setId(new Long(i));
				sample.setInvestigation(investigation);
				sample.setType(sampleType);
				sample.setName(word);

				gen.writeStartObject();
				sample.getDoc(gen);
				gen.writeEnd();
				System.out.println("SAMPLE '" + word + "' " + i % NUMINV);
			}
			gen.writeEnd();

		}
		addDocuments("Sample", baos.toString());

		luceneApi.commit();

	}

}
