package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.DatafileParameter;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.DatasetTechnique;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.FacilityCycle;
import org.icatproject.core.entity.Instrument;
import org.icatproject.core.entity.InstrumentScientist;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationFacilityCycle;
import org.icatproject.core.entity.InvestigationInstrument;
import org.icatproject.core.entity.InvestigationParameter;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.InvestigationUser;
import org.icatproject.core.entity.Parameter;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleParameter;
import org.icatproject.core.entity.SampleType;
import org.icatproject.core.entity.Technique;
import org.icatproject.core.entity.User;
import org.icatproject.core.manager.search.FacetDimension;
import org.icatproject.core.manager.search.FacetLabel;
import org.icatproject.core.manager.search.LuceneApi;
import org.icatproject.core.manager.search.OpensearchApi;
import org.icatproject.core.manager.search.ParameterPOJO;
import org.icatproject.core.manager.search.ScoredEntityBaseBean;
import org.icatproject.core.manager.search.SearchApi;
import org.icatproject.core.manager.search.SearchResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class TestSearchApi {

	private class Filter {
		private String fld;
		private String value;
		private JsonArray array;

		public Filter(String fld, String... values) {
			this.fld = fld;
			if (values.length == 1) {
				this.value = values[0];
			}
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for (String value : values) {
				arrayBuilder.add(value);
			}
			array = arrayBuilder.build();
		}

		public Filter(String fld, JsonObject... values) {
			this.fld = fld;
			JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for (JsonObject value : values) {
				arrayBuilder.add(value);
			}
			array = arrayBuilder.build();
		}
	}

	private static final String SEARCH_AFTER_NOT_NULL = "Expected searchAfter to be set, but it was null";
	private static final List<String> datafileFields = Arrays.asList("id", "name", "location", "date", "dataset.id",
			"dataset.name", "investigation.id", "investigation.name", "InvestigationInstrument instrument.id",
			"InvestigationFacilityCycle facilityCycle.id");
	private static final List<String> datasetFields = Arrays.asList("id", "name", "startDate", "endDate",
			"investigation.id", "investigation.name", "investigation.title", "investigation.startDate",
			"InvestigationInstrument instrument.id", "InvestigationFacilityCycle facilityCycle.id");
	private static final List<String> investigationFields = Arrays.asList("id", "name", "title", "startDate", "endDate",
			"InvestigationInstrument instrument.id", "InvestigationInstrument instrument.name",
			"InvestigationInstrument instrument.fullName", "InvestigationFacilityCycle facilityCycle.id");

	private static Facility facility = new Facility();
	private static FacilityCycle facilityCycle = new FacilityCycle();
	private static InvestigationType investigationType = new InvestigationType();
	static {
		facility.setName("facility");
		facility.setId(0L);
		facilityCycle.setFacility(facility);
		facilityCycle.setId(0L);
		investigationType.setName("type");
		investigationType.setId(0L);
	}

	final static Logger logger = LoggerFactory.getLogger(TestSearchApi.class);

	@Parameterized.Parameters
	public static Iterable<SearchApi> data() throws URISyntaxException, IcatException {
		String searchEngine = System.getProperty("searchEngine");
		String searchUrls = System.getProperty("searchUrls");
		URI searchUri = new URI(searchUrls);
		logger.info("Using {} service at {}", searchEngine, searchUrls);
		switch (searchEngine) {
			case "LUCENE":
				return Arrays.asList(new LuceneApi(searchUri));
			case "OPENSEARCH":
			case "ELASTICSEARCH":
				return Arrays.asList(new OpensearchApi(searchUri, "\u2103: celsius", false));
			default:
				String msg = "Search engine must be one of LUCENE, OPENSEARCH or ELASTICSEARCH but was " + searchEngine;
				throw new IcatException(IcatExceptionType.BAD_PARAMETER, msg);
		}
	}

	@Parameterized.Parameter
	public SearchApi searchApi;

	String letters = "abcdefghijklmnopqrstuvwxyz";
	Date date = new Date();
	long now = date.getTime();
	int NUMINV = 10;
	int NUMUSERS = 5;
	int NUMDS = 30;
	int NUMDF = 100;
	int NUMSAMP = 15;

	/**
	 * Utility function for building a Query from individual arguments
	 */
	public static JsonObject buildQuery(String target, String user, String text, Date lower, Date upper,
			List<ParameterPOJO> parameters, String userFullName, Filter... filters) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		if (target != null) {
			builder.add("target", target);
		}
		if (user != null) {
			builder.add("user", user);
		}
		if (text != null) {
			builder.add("text", text);
		}
		if (lower != null) {
			builder.add("lower", lower.getTime());
		}
		if (upper != null) {
			builder.add("upper", upper.getTime());
		}
		if (parameters != null && !parameters.isEmpty()) {
			JsonArrayBuilder parametersBuilder = Json.createArrayBuilder();
			for (ParameterPOJO parameter : parameters) {
				JsonObjectBuilder parameterBuilder = Json.createObjectBuilder();
				if (parameter.name != null) {
					parameterBuilder.add("name", parameter.name);
				}
				if (parameter.units != null) {
					parameterBuilder.add("units", parameter.units);
				}
				if (parameter.stringValue != null) {
					parameterBuilder.add("stringValue", parameter.stringValue);
				}
				if (parameter.lowerDateValue != null) {
					parameterBuilder.add("lowerDateValue", parameter.lowerDateValue.getTime());
				}
				if (parameter.upperDateValue != null) {
					parameterBuilder.add("upperDateValue", parameter.upperDateValue.getTime());
				}
				if (parameter.lowerNumericValue != null) {
					parameterBuilder.add("lowerNumericValue", parameter.lowerNumericValue);
				}
				if (parameter.upperNumericValue != null) {
					parameterBuilder.add("upperNumericValue", parameter.upperNumericValue);
				}
				parametersBuilder.add(parameterBuilder);
			}
			builder.add("parameters", parametersBuilder);
		}
		if (userFullName != null) {
			builder.add("userFullName", userFullName);
		}
		if (filters.length > 0) {
			JsonObjectBuilder filterBuilder = Json.createObjectBuilder();
			for (Filter filter : filters) {
				if (filter.value != null) {
					filterBuilder.add(filter.fld, filter.value);
				} else {
					filterBuilder.add(filter.fld, filter.array);
				}
			}
			builder.add("filter", filterBuilder);
		}
		return builder.build();
	}

	private static JsonObject buildFacetIdQuery(String idField, String idValue) {
		return Json.createObjectBuilder().add(idField, Json.createArrayBuilder().add(idValue)).build();
	}

	private static JsonObject buildFacetRangeObject(String key, double from, double to) {
		return Json.createObjectBuilder().add("from", from).add("to", to).add("key", key).build();
	}

	private static JsonObject buildFacetRangeObject(String key, long from, long to) {
		return Json.createObjectBuilder().add("from", from).add("to", to).add("key", key).build();
	}

	private static JsonObject buildFacetRangeRequest(JsonObject queryObject, String dimension,
			JsonObject... rangeObjects) {
		JsonArrayBuilder rangesBuilder = Json.createArrayBuilder();
		for (JsonObject rangeObject : rangeObjects) {
			rangesBuilder.add(rangeObject);
		}
		JsonObjectBuilder rangedDimensionBuilder = Json.createObjectBuilder().add("dimension", dimension).add("ranges",
				rangesBuilder);
		JsonArrayBuilder rangedDimensionsBuilder = Json.createArrayBuilder().add(rangedDimensionBuilder);
		return Json.createObjectBuilder().add("query", queryObject).add("dimensions", rangedDimensionsBuilder).build();
	}

	private static JsonObject buildFacetStringRequest(String idField, String idValue, String dimension) {
		JsonObject idQuery = buildFacetIdQuery(idField, idValue);
		JsonObjectBuilder stringDimensionBuilder = Json.createObjectBuilder().add("dimension", dimension);
		JsonArrayBuilder stringDimensionsBuilder = Json.createArrayBuilder().add(stringDimensionBuilder);
		return Json.createObjectBuilder().add("query", idQuery).add("dimensions", stringDimensionsBuilder).build();
	}

	private void checkDatafile(ScoredEntityBaseBean datafile) {
		JsonObject source = datafile.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(Arrays.asList("id", "name", "location", "date", "dataset.id",
				"dataset.name", "investigation.id", "investigation.name", "investigationinstrument",
				"investigationfacilitycycle"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("DFaaa", source.getString("name"));
		assertEquals("/dir/DFaaa", source.getString("location"));
		assertNotNull(source.getJsonNumber("date"));
		assertEquals("0", source.getString("dataset.id"));
		assertEquals("DSaaa", source.getString("dataset.name"));
		assertEquals("0", source.getString("investigation.id"));
		assertEquals("a h r", source.getString("investigation.name"));
		JsonArray instruments = source.getJsonArray("investigationinstrument");
		assertEquals(1, instruments.size());
		assertEquals("0", instruments.getJsonObject(0).getString("instrument.id"));
		JsonArray facilityCycles = source.getJsonArray("investigationfacilitycycle");
		assertEquals(1, facilityCycles.size());
		assertEquals("0", facilityCycles.getJsonObject(0).getString("facilityCycle.id"));
	}

	private void checkDataset(ScoredEntityBaseBean dataset) {
		JsonObject source = dataset.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(Arrays.asList("id", "name", "startDate", "endDate", "investigation.id",
				"investigation.name", "investigation.title", "investigation.startDate", "investigationinstrument",
				"investigationfacilitycycle"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("DSaaa", source.getString("name"));
		assertNotNull(source.getJsonNumber("startDate"));
		assertNotNull(source.getJsonNumber("endDate"));
		assertEquals("0", source.getString("investigation.id"));
		assertEquals("a h r", source.getString("investigation.name"));
		assertEquals("title", source.getString("investigation.title"));
		assertNotNull(source.getJsonNumber("investigation.startDate"));
		JsonArray instruments = source.getJsonArray("investigationinstrument");
		assertEquals(1, instruments.size());
		assertEquals("0", instruments.getJsonObject(0).getString("instrument.id"));
		JsonArray facilityCycles = source.getJsonArray("investigationfacilitycycle");
		assertEquals(1, facilityCycles.size());
		assertEquals("0", facilityCycles.getJsonObject(0).getString("facilityCycle.id"));
	}

	private void checkFacets(List<FacetDimension> facetDimensions, FacetDimension... dimensions) {
		assertEquals(dimensions.length, facetDimensions.size());
		for (int i = 0; i < dimensions.length; i++) {
			FacetDimension expectedFacet = dimensions[i];
			FacetDimension actualFacet = facetDimensions.get(i);
			assertEquals(expectedFacet.getDimension(), actualFacet.getDimension());
			List<FacetLabel> expectedLabels = expectedFacet.getFacets();
			List<FacetLabel> actualLabels = actualFacet.getFacets();
			String message = "Expected " + expectedLabels.toString() + " but got " + actualLabels.toString();
			assertEquals(message, expectedLabels.size(), actualLabels.size());
			for (int j = 0; j < expectedLabels.size(); j++) {
				FacetLabel expectedLabel = expectedLabels.get(j);
				FacetLabel actualLabel = actualLabels.get(j);
				String label = expectedLabel.getLabel();
				long expectedValue = expectedLabel.getValue();
				long actualValue = actualLabel.getValue();
				assertEquals(label, actualLabel.getLabel());
				message = "Label <" + label + ">: ";
				assertEquals(message, expectedValue, actualValue);
			}
		}
	}

	private void checkInvestigation(ScoredEntityBaseBean investigation) {
		JsonObject source = investigation.getSource();
		assertNotNull(source);
		Set<String> expectedKeys = new HashSet<>(Arrays.asList(
				"id", "name", "title", "startDate", "endDate", "investigationinstrument",
				"investigationfacilitycycle"));
		assertEquals(expectedKeys, source.keySet());
		assertEquals("0", source.getString("id"));
		assertEquals("a h r", source.getString("name"));
		assertNotNull(source.getJsonNumber("startDate"));
		assertNotNull(source.getJsonNumber("endDate"));
		JsonArray instruments = source.getJsonArray("investigationinstrument");
		assertEquals(1, instruments.size());
		assertEquals("0", instruments.getJsonObject(0).getString("instrument.id"));
		assertEquals("bl0", instruments.getJsonObject(0).getString("instrument.name"));
		assertEquals("Beamline 0", instruments.getJsonObject(0).getString("instrument.fullName"));
		JsonArray facilityCycles = source.getJsonArray("investigationfacilitycycle");
		assertEquals(1, facilityCycles.size());
		assertEquals("0", facilityCycles.getJsonObject(0).getString("facilityCycle.id"));
	}

	private void checkResults(SearchResult lsr, Long... n) {
		Set<Long> wanted = new HashSet<>(Arrays.asList(n));
		Set<Long> got = new HashSet<>();

		for (ScoredEntityBaseBean q : lsr.getResults()) {
			got.add(q.getId());
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

	private void checkOrder(SearchResult lsr, Long... n) {
		List<ScoredEntityBaseBean> results = lsr.getResults();
		if (n.length != results.size()) {
			checkResults(lsr, n);
		}
		for (int i = 0; i < n.length; i++) {
			long resultId = results.get(i).getId();
			long expectedId = (long) Array.get(n, i);
			if (resultId != expectedId) {
				fail("Expected id " + expectedId + " in position " + i + " but got " + resultId);
			}
		}
	}

	private Datafile datafile(long id, String name, String location, Date date, Dataset dataset) {
		Datafile datafile = new Datafile();
		datafile.setId(id);
		datafile.setName(name);
		datafile.setLocation(location);
		datafile.setDatafileModTime(date);
		datafile.setDataset(dataset);
		return datafile;
	}

	private DatafileFormat datafileFormat(long id, String name) {
		DatafileFormat datafileFormat = new DatafileFormat();
		datafileFormat.setId(id);
		datafileFormat.setName(name);
		return datafileFormat;
	}

	private Dataset dataset(long id, String name, Date startDate, Date endDate, Investigation investigation) {
		DatasetType type = new DatasetType();
		type.setName("type");
		type.setId(0L);
		Dataset dataset = new Dataset();
		dataset.setId(id);
		dataset.setName(name);
		dataset.setCreateTime(startDate);
		dataset.setModTime(endDate);
		dataset.setType(type);
		dataset.setInvestigation(investigation);
		return dataset;
	}

	private Investigation investigation(long id, String name, Date startDate, Date endDate) {
		Investigation investigation = new Investigation();
		investigation.setId(id);
		investigation.setName(name);
		investigation.setVisitId("visitId");
		investigation.setTitle("title");
		investigation.setCreateTime(startDate);
		investigation.setModTime(endDate);
		investigation.setFacility(facility);
		investigation.setType(investigationType);
		return investigation;
	}

	private InvestigationUser investigationUser(long id, long userId, String name, String fullName,
			Investigation investigation) {
		User user = new User();
		user.setName(name);
		user.setFullName(fullName);
		user.setId(userId);
		InvestigationUser investigationUser = new InvestigationUser();
		investigationUser.setId(id);
		investigationUser.setInvestigation(investigation);
		investigationUser.setUser(user);
		return investigationUser;
	}

	private Parameter parameter(long id, Date value, ParameterType parameterType, EntityBaseBean parent) {
		Parameter parameter = parameter(id, parameterType, parent);
		parameter.setDateTimeValue(value);
		return parameter;
	}

	private Parameter parameter(long id, String value, ParameterType parameterType, EntityBaseBean parent) {
		Parameter parameter = parameter(id, parameterType, parent);
		parameter.setStringValue(value);
		return parameter;
	}

	private Parameter parameter(long id, double value, ParameterType parameterType, EntityBaseBean parent) {
		Parameter parameter = parameter(id, parameterType, parent);
		parameter.setNumericValue(value);
		return parameter;
	}

	private Parameter parameter(long id, String value, double rangeBottom, double rangeTop, ParameterType parameterType,
			EntityBaseBean parent) {
		Parameter parameter = parameter(id, parameterType, parent);
		parameter.setStringValue(value);
		parameter.setRangeBottom(rangeBottom);
		parameter.setRangeTop(rangeTop);
		return parameter;
	}

	private Parameter parameter(long id, ParameterType parameterType, EntityBaseBean parent) {
		Parameter parameter;
		if (parent instanceof Datafile) {
			parameter = new DatafileParameter();
			((DatafileParameter) parameter).setDatafile((Datafile) parent);
		} else if (parent instanceof Dataset) {
			parameter = new DatasetParameter();
			((DatasetParameter) parameter).setDataset((Dataset) parent);
		} else if (parent instanceof Investigation) {
			parameter = new InvestigationParameter();
			((InvestigationParameter) parameter).setInvestigation((Investigation) parent);
		} else if (parent instanceof Sample) {
			parameter = new SampleParameter();
			((SampleParameter) parameter).setSample((Sample) parent);
		} else {
			fail(parent.getClass().getSimpleName() + " is not valid");
			return null;
		}
		parameter.setType(parameterType);
		parameter.setId(id);
		return parameter;
	}

	private ParameterType parameterType(long id, String name, String units) {
		ParameterType parameterType = new ParameterType();
		parameterType.setId(id);
		parameterType.setName(name);
		parameterType.setUnits(units);
		return parameterType;
	}

	private Sample sample(long id, String name, Investigation investigation) {
		SampleType sampleType = new SampleType();
		sampleType.setId(0L);
		sampleType.setName("test");
		Sample sample = new Sample();
		sample.setId(id);
		sample.setName(name);
		sample.setInvestigation(investigation);
		return sample;
	}

	private void modify(String... operations) throws IcatException {
		StringBuilder sb = new StringBuilder("[");
		for (String operation : operations) {
			if (sb.length() != 1) {
				sb.append(',');
			}
			sb.append(operation);
		}
		sb.append(']');
		searchApi.modify(sb.toString());
		searchApi.commit();
	}

	private void populateParameters(List<String> queue, int i, EntityBaseBean parent) throws IcatException {
		int j = i % 26;
		int k = (i + 5) % 26;
		String name = "nm " + letters.substring(j, j + 1) + letters.substring(j, j + 1) + letters.substring(j, j + 1);
		String units = "u " + letters.substring(k, k + 1) + letters.substring(k, k + 1) + letters.substring(k, k + 1);
		ParameterType dateParameterType = parameterType(0, "D" + name, units);
		ParameterType numericParameterType = parameterType(0, "N" + name, units);
		ParameterType stringParameterType = parameterType(0, "S" + name, units);
		Parameter dateParameter = parameter(3 * i, new Date(now + 60000 * k * k), dateParameterType, parent);
		Parameter numericParameter = parameter(3 * i + 1, new Double(j * j), numericParameterType, parent);
		Parameter stringParameter = parameter(3 * i + 2, "v" + i * i, stringParameterType, parent);
		queue.add(SearchApi.encodeOperation("create", dateParameter));
		queue.add(SearchApi.encodeOperation("create", numericParameter));
		queue.add(SearchApi.encodeOperation("create", stringParameter));
	}

	/**
	 * Populate UserGroup, Investigation, InvestigationParameter,
	 * InvestigationUser, Dataset,DatasetParameter,Datafile, DatafileParameter
	 * and Sample
	 */
	private void populate() throws IcatException {
		List<String> queue = new ArrayList<>();
		long investigationUserId = 0;

		Instrument instrumentZero = populateInstrument(queue, 0);
		Instrument instrumentOne = populateInstrument(queue, 1);
		Technique techniqueZero = populateTechnique(queue, 0);
		Technique techniqueOne = populateTechnique(queue, 1);

		for (int investigationId = 0; investigationId < NUMINV; investigationId++) {
			String word = word(investigationId % 26, (investigationId + 7) % 26, (investigationId + 17) % 26);
			Date startDate = new Date(now + investigationId * 60000);
			Date endDate = new Date(now + (investigationId + 1) * 60000);
			Investigation investigation = investigation(investigationId, word, startDate, endDate);
			queue.add(SearchApi.encodeOperation("create", investigation));

			InvestigationFacilityCycle investigationFacilityCycle = new InvestigationFacilityCycle();
			investigationFacilityCycle.setId(new Long(investigationId));
			investigationFacilityCycle.setFacilityCycle(facilityCycle);
			investigationFacilityCycle.setInvestigation(investigation);
			queue.add(SearchApi.encodeOperation("create", investigationFacilityCycle));

			InvestigationInstrument investigationInstrument = new InvestigationInstrument();
			investigationInstrument.setId(new Long(investigationId));
			if (investigationId % 2 == 0) {
				investigationInstrument.setInstrument(instrumentZero);
			} else {
				investigationInstrument.setInstrument(instrumentOne);
			}
			investigationInstrument.setInvestigation(investigation);
			queue.add(SearchApi.encodeOperation("create", investigationInstrument));

			for (int userId = 0; userId < NUMUSERS; userId++) {
				if (investigationId % (userId + 1) == 1) {
					String fullName = "FN " + letters.substring(userId, userId + 1) + " "
							+ letters.substring(userId, userId + 1);
					String name = letters.substring(userId, userId + 1) + userId;
					InvestigationUser investigationUser = investigationUser(investigationUserId, userId, name, fullName,
							investigation);
					queue.add(SearchApi.encodeOperation("create", investigationUser));
					investigationUserId++;
				}
			}

			if (investigationId % 2 == 1) {
				populateParameters(queue, investigationId, investigation);
			}

			for (int sampleBatch = 0; sampleBatch * NUMINV < NUMSAMP; sampleBatch++) {
				int sampleId = sampleBatch * NUMINV + investigationId;
				if (sampleId >= NUMSAMP) {
					break;
				}
			}

			for (int datasetBatch = 0; datasetBatch * NUMINV < NUMDS; datasetBatch++) {
				int datasetId = datasetBatch * NUMINV + investigationId;
				if (datasetId >= NUMDS) {
					break;
				}
				startDate = new Date(now + datasetId * 60000);
				endDate = new Date(now + (datasetId + 1) * 60000);
				word = word("DS", datasetId % 26);
				Dataset dataset = dataset(datasetId, word, startDate, endDate, investigation);

				if (datasetId % 2 == 0) {
					populateDatasetTechnique(queue, techniqueZero, dataset);
				} else {
					populateDatasetTechnique(queue, techniqueOne, dataset);
				}

				if (datasetId < NUMSAMP) {
					word = word("SType ", datasetId);
					Sample sample = sample(datasetId, word, investigation);
					queue.add(SearchApi.encodeOperation("create", sample));
					dataset.setSample(sample);
				}

				queue.add(SearchApi.encodeOperation("create", dataset));

				if (datasetId % 3 == 1) {
					populateParameters(queue, datasetId, dataset);
				}

				for (int datafileBatch = 0; datafileBatch * NUMDS < NUMDF; datafileBatch++) {
					int datafileId = datafileBatch * NUMDS + datasetId;
					if (datafileId >= NUMDF) {
						break;
					}
					word = word("DF", datafileId % 26);
					Datafile datafile = datafile(datafileId, word, "/dir/" + word, new Date(now + datafileId * 60000),
							dataset);
					queue.add(SearchApi.encodeOperation("create", datafile));

					if (datafileId % 4 == 1) {
						populateParameters(queue, datafileId, datafile);
					}
				}
			}
		}

		modify(queue.toArray(new String[0]));
	}

	/**
	 * Queues creation of an Instrument and a corresponding instrument scientist.
	 * 
	 * @param queue        Queue to add create operations to.
	 * @param instrumentId ICAT entity Id to use for the instrument/instrument
	 *                     scientist.
	 * @return The Instrument entity created.
	 * @throws IcatException
	 */
	private Instrument populateInstrument(List<String> queue, long instrumentId) throws IcatException {
		Instrument instrument = new Instrument();
		instrument.setId(instrumentId);
		instrument.setName("bl" + instrumentId);
		instrument.setFullName("Beamline " + instrumentId);
		queue.add(SearchApi.encodeOperation("create", instrument));
		User user = new User();
		user.setId(new Long(NUMUSERS) + instrumentId);
		user.setName("scientist_" + instrumentId);
		InstrumentScientist instrumentScientist = new InstrumentScientist();
		instrumentScientist.setId(instrumentId);
		instrumentScientist.setInstrument(instrument);
		instrumentScientist.setUser(user);
		queue.add(SearchApi.encodeOperation("create", instrumentScientist));
		return instrument;
	}

	/**
	 * Queues creation of an Technique.
	 * 
	 * @param queue       Queue to add create operations to.
	 * @param techniqueId ICAT entity Id to use for the Technique.
	 * @return The Technique entity created.
	 * @throws IcatException
	 */
	private Technique populateTechnique(List<String> queue, long techniqueId) throws IcatException {
		Technique technique = new Technique();
		technique.setId(techniqueId);
		technique.setName("technique" + techniqueId);
		technique.setDescription("Technique number " + techniqueId);
		technique.setPid(Long.toString(techniqueId));
		queue.add(SearchApi.encodeOperation("create", technique));
		return technique;
	}

	/**
	 * Queues creation of an DatasetTechnique.
	 * 
	 * @param queue Queue to add create operations to.
	 * @return The DatasetTechnique entity created.
	 * @throws IcatException
	 */
	private void populateDatasetTechnique(List<String> queue, Technique technique, Dataset dataset)
			throws IcatException {
		DatasetTechnique datasetTechnique = new DatasetTechnique();
		datasetTechnique.setId(technique.getId() * 100 + dataset.getId());
		datasetTechnique.setTechnique(technique);
		datasetTechnique.setDataset(dataset);
		queue.add(SearchApi.encodeOperation("create", datasetTechnique));
	}

	private String word(int j, int k, int l) {
		String jString = letters.substring(j, j + 1);
		String kString = letters.substring(k, k + 1);
		String lString = letters.substring(l, l + 1);
		return jString + " " + kString + " " + lString;
	}

	private String word(String prefix, int j) {
		String jString = letters.substring(j, j + 1);
		return prefix + jString + jString + jString;
	}

	@Before
	public void before() throws Exception {
		searchApi.clear();
	}

	@Test
	public void datafiles() throws Exception {
		populate();
		JsonObjectBuilder sortBuilder = Json.createObjectBuilder();
		String sort;

		// Test size and searchAfter
		JsonObject query = buildQuery("Datafile", null, null, null, null, null, null);
		SearchResult lsr = searchApi.getResults(query, null, 5, null, datafileFields);
		JsonValue searchAfter = lsr.getSearchAfter();
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		checkDatafile(lsr.getResults().get(0));
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		lsr = searchApi.getResults(query, searchAfter, 200, null, datafileFields);
		assertNull(lsr.getSearchAfter());
		assertEquals(95, lsr.getResults().size());

		// Test searchAfter preserves the sorting of original search (asc)
		sort = sortBuilder.add("date", "asc").build().toString();
		lsr = searchApi.getResults(query, null, 5, sort, datafileFields);
		checkOrder(lsr, 0L, 1L, 2L, 3L, 4L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 5, sort, datafileFields);
		checkOrder(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		// Test searchAfter preserves the sorting of original search (desc)
		sort = sortBuilder.add("date", "desc").build().toString();
		lsr = searchApi.getResults(query, null, 5, sort, datafileFields);
		checkOrder(lsr, 99L, 98L, 97L, 96L, 95L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 5, sort, datafileFields);
		checkOrder(lsr, 94L, 93L, 92L, 91L, 90L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		// Test tie breaks on fields with identical values (asc)
		sort = sortBuilder.add("name", "asc").build().toString();
		lsr = searchApi.getResults(query, null, 5, sort, datafileFields);
		checkOrder(lsr, 0L, 26L, 52L, 78L, 1L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		sort = sortBuilder.add("name", "asc").add("date", "desc").build().toString();
		lsr = searchApi.getResults(query, null, 5, sort, datafileFields);
		checkOrder(lsr, 78L, 52L, 26L, 0L, 79L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		// Test tie breaks on fields with identical values (desc)
		sort = sortBuilder.add("name", "desc").build().toString();
		lsr = searchApi.getResults(query, null, 5, sort, datafileFields);
		checkOrder(lsr, 25L, 51L, 77L, 24L, 50L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		sort = sortBuilder.add("name", "desc").add("date", "desc").build().toString();
		lsr = searchApi.getResults(query, null, 5, sort, datafileFields);
		checkOrder(lsr, 77L, 51L, 25L, 76L, 50L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		query = buildQuery("Datafile", "e4", null, null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 1L, 6L, 11L, 16L, 21L, 26L, 31L, 36L, 41L, 46L, 51L, 56L, 61L, 66L, 71L, 76L, 81L, 86L, 91L,
				96L);

		// Test instrumentScientists only see their data
		query = buildQuery("Datafile", "scientist_0", null, null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 2L, 4L, 6L, 8L);

		query = buildQuery("Datafile", "e4", "dfbbb", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 1L);

		query = buildQuery("Datafile", null, "dfbbb", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 1L, 27L, 53L, 79L);

		query = buildQuery("Datafile", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L, 4L, 5L, 6L);

		query = buildQuery("Datafile", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr);

		// Target visitId
		query = buildQuery("Datafile", null, "visitId:visitId", null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		query = buildQuery("Datafile", null, "visitId:qwerty", null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr);

		// Target sample.name
		query = buildQuery("Datafile", null, "sample.name:ddd", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L, 33L, 63L, 93L);

		// Multiple samples associated with investigation 3
		query = buildQuery("Datafile", null, "ddd nnn", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L, 13L, 33L, 43L, 63L, 73L, 93L);

		// By default, sample ddd OR sample mmm gives two
		query = buildQuery("Datafile", null, "ddd mmm", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L, 12L, 33L, 42L, 63L, 72L, 93L);

		// AND logic should not return any results
		query = buildQuery("Datafile", null, "+ddd +mmm", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr);

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		query = buildQuery("Datafile", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 5L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v25"));
		query = buildQuery("Datafile", null, null, null, null, pojos, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 5L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, "u sss", null));
		query = buildQuery("Datafile", null, null, null, null, pojos, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 13L, 65L);
	}

	@Test
	public void datasets() throws Exception {
		populate();
		JsonObjectBuilder sortBuilder = Json.createObjectBuilder();
		String sort;

		JsonObject query = buildQuery("Dataset", null, null, null, null, null, null);
		SearchResult lsr = searchApi.getResults(query, null, 5, null, datasetFields);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		checkDataset(lsr.getResults().get(0));
		JsonValue searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 100, null, datasetFields);
		assertNull(lsr.getSearchAfter());
		checkResults(lsr, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L,
				25L, 26L, 27L, 28L, 29L);

		// Test searchAfter preserves the sorting of original search (asc)
		sort = sortBuilder.add("date", "asc").build().toString();
		lsr = searchApi.getResults(query, 5, sort);
		checkOrder(lsr, 0L, 1L, 2L, 3L, 4L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 5, sort, datasetFields);
		checkOrder(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		// Test searchAfter preserves the sorting of original search (desc)
		sort = sortBuilder.add("date", "desc").build().toString();
		lsr = searchApi.getResults(query, 5, sort);
		checkOrder(lsr, 29L, 28L, 27L, 26L, 25L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 5, sort, datasetFields);
		checkOrder(lsr, 24L, 23L, 22L, 21L, 20L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		// Test tie breaks on fields with identical values (asc)
		sort = sortBuilder.add("name", "asc").build().toString();
		lsr = searchApi.getResults(query, null, 5, sort, datasetFields);
		checkOrder(lsr, 0L, 26L, 1L, 27L, 2L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		sort = sortBuilder.add("name", "asc").add("date", "desc").build().toString();
		lsr = searchApi.getResults(query, 5, sort);
		checkOrder(lsr, 26L, 0L, 27L, 1L, 28L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		lsr = searchApi.getResults(buildQuery("Dataset", "e4", null, null, null, null, null), 100,
				null);
		checkResults(lsr, 1L, 6L, 11L, 16L, 21L, 26L);

		// Test instrumentScientists only see their data
		query = buildQuery("Dataset", "scientist_0", null, null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 2L, 4L, 6L, 8L);

		// Test filter
		query = buildQuery("Dataset", null, null, null, null, null, null, new Filter("dataset.type.name", "type"));
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		query = buildQuery("Dataset", null, null, null, null, null, null,
				new Filter("dataset.type.name", "type", "typo"));
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		query = buildQuery("Dataset", null, null, null, null, null, null, new Filter("dataset.type.name", "typo"));
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr);

		lsr = searchApi.getResults(buildQuery("Dataset", "e4", "dsbbb", null, null, null, null), 100,
				null);
		checkResults(lsr, 1L);

		lsr = searchApi.getResults(buildQuery("Dataset", null, "dsbbb", null, null, null, null), 100,
				null);
		checkResults(lsr, 1L, 27L);

		lsr = searchApi.getResults(buildQuery("Dataset", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null), 100, null);
		checkResults(lsr, 3L, 4L, 5L);

		lsr = searchApi.getResults(buildQuery("Dataset", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), null, null), 100, null);
		checkResults(lsr, 3L);

		// Target visitId
		query = buildQuery("Dataset", null, "visitId:visitId", null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		query = buildQuery("Dataset", null, "visitId:qwerty", null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr);

		// Target sample.name
		query = buildQuery("Dataset", null, "sample.name:ddd", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		// Multiple samples associated with investigation 3
		query = buildQuery("Dataset", null, "ddd nnn", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L, 13L);

		// By default, sample ddd OR sample mmm gives two
		query = buildQuery("Dataset", null, "ddd mmm", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L, 12L);

		// AND logic should not return any results
		query = buildQuery("Dataset", null, "+ddd +mmm", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr);

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = searchApi.getResults(buildQuery("Dataset", null, null, null, null, pojos, null), 100,
				null);
		checkResults(lsr, 4L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = searchApi.getResults(buildQuery("Dataset", null, null, new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null), 100, null);
		checkResults(lsr, 4L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v16"));
		lsr = searchApi.getResults(buildQuery("Dataset", "b1", "dsddd", new Date(now + 60000 * 3),
				new Date(now + 60000 * 6), pojos, null), 100, null);
		checkResults(lsr);

		// Test DatasetTechnique Facets
		JsonObject stringFacetRequestZero = buildFacetStringRequest("dataset.id", "0", "technique.name");
		JsonObject stringFacetRequestOne = buildFacetStringRequest("dataset.id", "1", "technique.name");
		FacetDimension facetZero = new FacetDimension("", "technique.name", new FacetLabel("technique0", 1L));
		FacetDimension facetOne = new FacetDimension("", "technique.name", new FacetLabel("technique1", 1L));
		checkFacets(searchApi.facetSearch("DatasetTechnique", stringFacetRequestZero, 5, 5), facetZero);
		checkFacets(searchApi.facetSearch("DatasetTechnique", stringFacetRequestOne, 5, 5), facetOne);
	}

	@Test
	public void investigations() throws Exception {
		populate();
		JsonObjectBuilder sortBuilder = Json.createObjectBuilder();
		String sort;

		/* Blocked results */
		JsonObject query = buildQuery("Investigation", null, null, null, null, null, null);
		SearchResult lsr = searchApi.getResults(query, null, 5, null, investigationFields);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		checkInvestigation(lsr.getResults().get(0));
		JsonValue searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 6, null, investigationFields);
		checkResults(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNull(searchAfter);

		// Test searchAfter preserves the sorting of original search (asc)
		sort = sortBuilder.add("date", "asc").build().toString();
		lsr = searchApi.getResults(query, 5, sort);
		checkOrder(lsr, 0L, 1L, 2L, 3L, 4L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 5, sort, investigationFields);
		checkOrder(lsr, 5L, 6L, 7L, 8L, 9L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		// Test searchAfter preserves the sorting of original search (desc)
		sort = sortBuilder.add("date", "desc").build().toString();
		lsr = searchApi.getResults(query, 5, sort);
		checkOrder(lsr, 9L, 8L, 7L, 6L, 5L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);
		lsr = searchApi.getResults(query, searchAfter, 5, sort, investigationFields);
		checkOrder(lsr, 4L, 3L, 2L, 1L, 0L);
		searchAfter = lsr.getSearchAfter();
		assertNotNull(SEARCH_AFTER_NOT_NULL, searchAfter);

		// Test instrumentScientists only see their data
		query = buildQuery("Investigation", "scientist_0", null, null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 2L, 4L, 6L, 8L);

		// Test filter
		query = buildQuery("Investigation", null, null, null, null, null, null,
				new Filter("investigation.type.name", "type"));
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		query = buildQuery("Investigation", null, null, null, null, null, null,
				new Filter("investigation.type.name", "type", "typo"));
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		query = buildQuery("Investigation", null, null, null, null, null, null,
				new Filter("investigation.type.name", "typo"));
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr);

		query = buildQuery("Investigation", null, null, null, null, null, "b");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 1L, 3L, 5L, 7L, 9L);

		query = buildQuery("Investigation", null, null, null, null, null, "FN");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 1L, 3L, 4L, 5L, 6L, 7L, 9L);

		query = buildQuery("Investigation", null, null, null, null, null, "FN AND \"b b\"");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 1L, 3L, 5L, 7L, 9L);

		query = buildQuery("Investigation", "b1", null, null, null, null, "b");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 1L, 3L, 5L, 7L, 9L);

		query = buildQuery("Investigation", "c1", null, null, null, null, "b");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr);

		query = buildQuery("Investigation", null, "l v", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 4L);

		query = buildQuery("Investigation", "b1", "d", null, null, null, "b");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		query = buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				null, "b");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		query = buildQuery("Investigation", null, null, new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L, 4L, 5L);

		List<ParameterPOJO> pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		query = buildQuery("Investigation", null, null, null, null, pojos, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, 7, 10));
		pojos.add(new ParameterPOJO(null, null, new Date(now + 60000 * 63), new Date(now + 60000 * 65)));
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		query = buildQuery("Investigation", "b1", "d", new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				pojos, "b");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO(null, null, "v9"));
		pojos.add(new ParameterPOJO(null, null, "v81"));
		query = buildQuery("Investigation", null, null, null, null, pojos, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		query = buildQuery("Investigation", null, null, null, null, pojos, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		// Target visitId
		query = buildQuery("Investigation", null, "visitId:visitId", null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L);
		query = buildQuery("Investigation", null, "visitId:qwerty", null, null, null, null);
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr);

		// Target sample.name
		query = buildQuery("Investigation", null, "sample.name:ddd", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		// Multiple samples associated with investigation 3
		query = buildQuery("Investigation", null, "ddd nnn", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);

		// By default, sample ddd OR sample mmm gives two investigations
		query = buildQuery("Investigation", null, "ddd mmm", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 2L, 3L);

		// AND logic should not return any results
		query = buildQuery("Investigation", null, "+ddd +mmm", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr);

		// Fields on Investigation and Sample
		query = buildQuery("Investigation", null, "visitId ddd", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
		// ID 3 should be most relevant since it matches both terms
		lsr = searchApi.getResults(query, 1, null);
		checkResults(lsr, 3L);
		// Specifying fields should not alter behaviour
		query = buildQuery("Investigation", null, "visitId:visitId sample.name:ddd", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
		// Individual MUST should work when applied to either an Investigation or Sample
		query = buildQuery("Investigation", null, "+visitId:visitId", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
		query = buildQuery("Investigation", null, "+sample.name:ddd", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);
		// This query is expected to fail, as we apply both terms to Investigation and
		// Sample (since we have no fields) and neither possesses both terms.
		query = buildQuery("Investigation", null, "+visitId +ddd", null, null, null, null);
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr);

		pojos = new ArrayList<>();
		pojos.add(new ParameterPOJO("Snm ddd", "u iii", "v9"));
		query = buildQuery("Investigation", "b1", "d ddd nnnn", new Date(now + 60000 * 3), new Date(now + 60000 * 6),
				pojos, "b");
		lsr = searchApi.getResults(query, 100, null);
		checkResults(lsr, 3L);
	}

	@Test
	public void locking() throws IcatException {
		// Only LuceneApi needs manually locking
		if (searchApi instanceof LuceneApi) {
			logger.info("Performing locking tests for {}", searchApi.getClass().getSimpleName());
			try {
				searchApi.unlock("Dataset");
				fail();
			} catch (IcatException e) {
				assertEquals("Lucene is not currently locked for Dataset", e.getMessage());
			}
			searchApi.lock("Dataset", 0L, 1L, true);
			try {
				searchApi.lock("Dataset", 0L, 1L, true);
				fail();
			} catch (IcatException e) {
				assertEquals("Lucene already locked for Dataset", e.getMessage());
			}
			searchApi.unlock("Dataset");
			try {
				searchApi.unlock("Dataset");
				fail();
			} catch (IcatException e) {
				assertEquals("Lucene is not currently locked for Dataset", e.getMessage());
			}
		} else {
			logger.info("Locking tests not relevant for {}", searchApi.getClass().getSimpleName());
		}
	}

	@Ignore // Aggregating in real time is really slow, so don't test
	@Test
	public void fileSizeAggregation() throws IcatException {
		// Build entities
		Investigation investigation = investigation(0, "name", date, date);
		Dataset dataset = dataset(0, "name", date, date, investigation);
		Datafile datafile = datafile(0, "name", "/dir", new Date(0), dataset);
		datafile.setFileSize(123L);

		// Build queries
		JsonObject datafileQuery = buildQuery("Datafile", null, "*", null, null, null, null);
		JsonObject datasetQuery = buildQuery("Dataset", null, "*", null, null, null, null);
		JsonObject investigationQuery = buildQuery("Investigation", null, "*", null, null, null, null);
		List<String> fields = Arrays.asList("id", "fileSize", "fileCount");

		// Create
		String createInvestigation = SearchApi.encodeOperation("create", investigation);
		String createDataset = SearchApi.encodeOperation("create", dataset);
		String createDatafile = SearchApi.encodeOperation("create", datafile);
		modify(createInvestigation, createDataset, createDatafile);
		checkFileSize(datafileQuery, fields, 123, 1);
		checkFileSize(datasetQuery, fields, 123, 1);
		checkFileSize(investigationQuery, fields, 123, 1);

		// Update
		datafile.setFileSize(456L);
		modify(SearchApi.encodeOperation("update", datafile));
		checkFileSize(datafileQuery, fields, 456, 1);
		checkFileSize(datasetQuery, fields, 456, 1);
		checkFileSize(investigationQuery, fields, 456, 1);

		// Delete
		modify(SearchApi.encodeOperation("delete", datafile));
		checkFileSize(datasetQuery, fields, 0, 0);
		checkFileSize(investigationQuery, fields, 0, 0);
	}

	private void checkFileSize(JsonObject query, List<String> fields, long expectedFileSize, long expectedFileCount)
			throws IcatException {
		SearchResult results = searchApi.getResults(query, null, 5, null, fields);
		checkResults(results, 0L);
		JsonObject source = results.getResults().get(0).getSource();
		long fileSize = source.getJsonNumber("fileSize").longValueExact();
		long fileCount = source.getJsonNumber("fileCount").longValueExact();
		assertEquals(expectedFileSize, fileSize);
		assertEquals(expectedFileCount, fileCount);
	}

	@Test
	public void modifyDatafile() throws IcatException {
		// Build entities
		DatafileFormat pdfFormat = datafileFormat(0, "pdf");
		DatafileFormat pngFormat = datafileFormat(0, "png");
		Investigation investigation = investigation(0, "name", date, date);
		Dataset dataset = dataset(0, "name", date, date, investigation);
		Datafile elephantDatafile = datafile(42, "Elephants and Aardvarks", "/dir", new Date(0), dataset);
		Datafile rhinoDatafile = datafile(42, "Rhinos and Aardvarks", "/dir", new Date(3), dataset);
		rhinoDatafile.setDatafileFormat(pdfFormat);

		// Build queries
		JsonObject elephantQuery = buildQuery("Datafile", null, "elephant", null, null, null, null);
		JsonObject rhinoQuery = buildQuery("Datafile", null, "rhino", null, null, null, null);
		JsonObject pdfQuery = buildQuery("Datafile", null, "datafileFormat.name:pdf", null, null, null, null);
		JsonObject pngQuery = buildQuery("Datafile", null, "datafileFormat.name:png", null, null, null, null);
		JsonObject lowRange = buildFacetRangeObject("low", 0L, 2L);
		JsonObject highRange = buildFacetRangeObject("high", 2L, 4L);
		JsonObject facetIdQuery = buildFacetIdQuery("id", "42");
		JsonObject rangeFacetRequest = buildFacetRangeRequest(facetIdQuery, "date", lowRange, highRange);
		JsonObject stringFacetRequest = buildFacetStringRequest("id", "42", "datafileFormat.name");
		JsonObject sparseFacetRequest = Json.createObjectBuilder().add("query", facetIdQuery).build();
		FacetDimension lowFacet = new FacetDimension("", "date", new FacetLabel("low", 1L), new FacetLabel("high", 0L));
		FacetDimension highFacet = new FacetDimension("", "date", new FacetLabel("low", 0L),
				new FacetLabel("high", 1L));
		FacetDimension pdfFacet = new FacetDimension("", "datafileFormat.name", new FacetLabel("pdf", 1L));
		FacetDimension pngFacet = new FacetDimension("", "datafileFormat.name", new FacetLabel("png", 1L));

		// Original
		modify(SearchApi.encodeOperation("create", elephantDatafile));
		checkResults(searchApi.getResults(elephantQuery, 5), 42L);
		checkResults(searchApi.getResults(rhinoQuery, 5));
		checkResults(searchApi.getResults(pdfQuery, 5));
		checkResults(searchApi.getResults(pngQuery, 5));
		checkFacets(searchApi.facetSearch("Datafile", stringFacetRequest, 5, 5));
		checkFacets(searchApi.facetSearch("Datafile", sparseFacetRequest, 5, 5));
		checkFacets(searchApi.facetSearch("Datafile", rangeFacetRequest, 5, 5), lowFacet);

		// Change name and add a format
		modify(SearchApi.encodeOperation("update", rhinoDatafile));
		checkResults(searchApi.getResults(elephantQuery, 5));
		checkResults(searchApi.getResults(rhinoQuery, 5), 42L);
		checkResults(searchApi.getResults(pdfQuery, 5), 42L);
		checkResults(searchApi.getResults(pngQuery, 5));
		checkFacets(searchApi.facetSearch("Datafile", stringFacetRequest, 5, 5), pdfFacet);
		checkFacets(searchApi.facetSearch("Datafile", sparseFacetRequest, 5, 5), pdfFacet);
		checkFacets(searchApi.facetSearch("Datafile", rangeFacetRequest, 5, 5), highFacet);

		// Change just the format
		modify(SearchApi.encodeOperation("update", pngFormat));
		checkResults(searchApi.getResults(elephantQuery, 5));
		checkResults(searchApi.getResults(rhinoQuery, 5), 42L);
		checkResults(searchApi.getResults(pdfQuery, 5));
		checkResults(searchApi.getResults(pngQuery, 5), 42L);
		checkFacets(searchApi.facetSearch("Datafile", stringFacetRequest, 5, 5), pngFacet);
		checkFacets(searchApi.facetSearch("Datafile", sparseFacetRequest, 5, 5), pngFacet);
		checkFacets(searchApi.facetSearch("Datafile", rangeFacetRequest, 5, 5), highFacet);

		// Remove the format
		modify(SearchApi.encodeOperation("delete", pngFormat));
		checkResults(searchApi.getResults(elephantQuery, 5));
		checkResults(searchApi.getResults(rhinoQuery, 5), 42L);
		checkResults(searchApi.getResults(pdfQuery, 5));
		checkResults(searchApi.getResults(pngQuery, 5));
		checkFacets(searchApi.facetSearch("Datafile", stringFacetRequest, 5, 5));
		checkFacets(searchApi.facetSearch("Datafile", sparseFacetRequest, 5, 5));
		checkFacets(searchApi.facetSearch("Datafile", rangeFacetRequest, 5, 5), highFacet);

		// Remove the file
		modify(SearchApi.encodeDeletion(elephantDatafile), SearchApi.encodeDeletion(rhinoDatafile));
		checkResults(searchApi.getResults(elephantQuery, 5));
		checkResults(searchApi.getResults(rhinoQuery, 5));
		checkResults(searchApi.getResults(pdfQuery, 5));
		checkResults(searchApi.getResults(pngQuery, 5));

		// Multiple commands at once
		modify(SearchApi.encodeOperation("create", elephantDatafile),
				SearchApi.encodeOperation("update", rhinoDatafile),
				SearchApi.encodeDeletion(elephantDatafile),
				SearchApi.encodeDeletion(rhinoDatafile));
		checkResults(searchApi.getResults(elephantQuery, 5));
		checkResults(searchApi.getResults(rhinoQuery, 5));
		checkResults(searchApi.getResults(pdfQuery, 5));
		checkResults(searchApi.getResults(pngQuery, 5));
	}

	@Test
	public void unitConversion() throws IcatException {
		// Build queries for raw and SI values
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		String lowKey = "272.5_273.5";
		String midKey = "272999.5_273000.5";
		String highKey = "273272.5_273273.5";
		JsonObject lowRange = buildFacetRangeObject(lowKey, 272.5, 273.5);
		JsonObject midRange = buildFacetRangeObject(midKey, 272999.5, 273000.5);
		JsonObject highRange = buildFacetRangeObject(highKey, 273272.5, 273273.5);
		JsonObject mKQuery = objectBuilder.add("type.units", "mK").build();
		JsonObject celsiusQuery = objectBuilder.add("type.units", "celsius").build();
		JsonObject wrongQuery = objectBuilder.add("type.units", "wrong").build();
		JsonObject kelvinQuery = objectBuilder.add("type.unitsSI", "Kelvin").build();
		JsonObject mKFacetQuery = buildFacetRangeRequest(mKQuery, "numericValue", lowRange, midRange, highRange);
		JsonObject celsiusFacetQuery = buildFacetRangeRequest(celsiusQuery, "numericValue", lowRange, midRange,
				highRange);
		JsonObject wrongFacetQuery = buildFacetRangeRequest(wrongQuery, "numericValue", lowRange, midRange, highRange);
		JsonObject systemFacetQuery = buildFacetRangeRequest(kelvinQuery, "numericValueSI", lowRange, midRange,
				highRange);

		// Build expected values
		FacetDimension rawExpectedFacet = new FacetDimension("", "numericValue",
				new FacetLabel(lowKey, 0L), new FacetLabel(midKey, 1L), new FacetLabel(highKey, 0L));
		FacetDimension lowExpectedFacet = new FacetDimension("", "numericValueSI",
				new FacetLabel(lowKey, 1L), new FacetLabel(midKey, 0L), new FacetLabel(highKey, 0L));
		FacetDimension highExpectedFacet = new FacetDimension("", "numericValueSI",
				new FacetLabel(lowKey, 0L), new FacetLabel(midKey, 0L), new FacetLabel(highKey, 1L));
		FacetDimension noneExpectedFacet = new FacetDimension("", "numericValueSI",
				new FacetLabel(lowKey, 0L), new FacetLabel(midKey, 0L), new FacetLabel(highKey, 0L));

		// Build entities
		Investigation investigation = investigation(0L, "name", date, date);
		ParameterType parameterType = parameterType(0, "parameter", "mK");
		Parameter parameter = parameter(0, 273000, parameterType, investigation);

		// Create with units of mK
		modify(SearchApi.encodeOperation("create", investigation), SearchApi.encodeOperation("create", parameter));
		// Assert the raw value is still 273000 (mK)
		checkFacets(searchApi.facetSearch("InvestigationParameter", mKFacetQuery, 5, 5), rawExpectedFacet);
		// Assert the SI value is 273 (K)
		checkFacets(searchApi.facetSearch("InvestigationParameter", systemFacetQuery, 5, 5), lowExpectedFacet);

		// Change units only to "celsius"
		parameterType.setUnits("celsius");
		modify(SearchApi.encodeOperation("update", parameter));
		// Assert the raw value is still 273000 (deg C)
		checkFacets(searchApi.facetSearch("InvestigationParameter", celsiusFacetQuery, 5, 5), rawExpectedFacet);
		// Assert the SI value is 273273.15 (K)
		checkFacets(searchApi.facetSearch("InvestigationParameter", systemFacetQuery, 5, 5), highExpectedFacet);

		// Change units to something wrong
		parameterType.setUnits("wrong");
		modify(SearchApi.encodeOperation("update", parameterType));
		// Assert the raw value is still 273000 (wrong)
		checkFacets(searchApi.facetSearch("InvestigationParameter", wrongFacetQuery, 5, 5), rawExpectedFacet);
		// Assert that the SI value has not been set due to conversion failing
		checkFacets(searchApi.facetSearch("InvestigationParameter", systemFacetQuery, 5, 5), noneExpectedFacet);
	}

	@Test
	public void exactFilter() throws IcatException {
		// Build entities
		Investigation numericInvestigation = investigation(0, "numeric", date, date);
		Investigation rangeInvestigation = investigation(1, "range", date, date);
		ParameterType numericParameterType = parameterType(0, "numericParameter", "K");
		ParameterType rangeParameterType = parameterType(1, "rangeParameter", "K");
		Parameter numericParameter = parameter(0, 273, numericParameterType, numericInvestigation);
		Parameter rangeParameter = parameter(1, "270 - 275", 270, 275, rangeParameterType, rangeInvestigation);

		JsonObjectBuilder filterBuilder = Json.createObjectBuilder();
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		JsonObject value = Json.createObjectBuilder().add("field", "numericValue").add("exact", 273).build();
		JsonObject numericName = Json.createObjectBuilder().add("field", "type.name").add("value", "numericParameter")
				.build();
		arrayBuilder.add(numericName).add(value);
		filterBuilder.add("key", "key").add("label", "label").add("filter", arrayBuilder);
		JsonObject numericFilter = filterBuilder.build();

		filterBuilder = Json.createObjectBuilder();
		arrayBuilder = Json.createArrayBuilder();
		JsonObject rangeName = Json.createObjectBuilder().add("field", "type.name").add("value", "rangeParameter")
				.build();
		arrayBuilder.add(rangeName).add(value);
		filterBuilder.add("key", "key").add("label", "label").add("filter", arrayBuilder);
		JsonObject rangeFilter = filterBuilder.build();

		// Create
		modify(SearchApi.encodeOperation("create", numericInvestigation),
				SearchApi.encodeOperation("create", rangeInvestigation),
				SearchApi.encodeOperation("create", numericParameter),
				SearchApi.encodeOperation("create", rangeParameter));

		JsonObject query = buildQuery("Investigation", null, null, null, null, null, null,
				new Filter("investigationparameter", numericFilter));
		SearchResult lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 0L);

		query = buildQuery("Investigation", null, null, null, null, null, null,
				new Filter("investigationparameter", rangeFilter));
		lsr = searchApi.getResults(query, 5, null);
		checkResults(lsr, 1L);
	}

	@Test
	public void sampleParameters() throws IcatException {
		// Build entities
		Investigation investigation = investigation(0, "investigation", date, date);
		Dataset dataset = dataset(1, "dataset", date, date, investigation);
		Datafile datafile = datafile(2, "datafile", "datafile.txt", date, dataset);
		Sample sample = sample(3, "sample", investigation);
		ParameterType parameterType = parameterType(4, "parameter", "K");
		SampleParameter parameter = (SampleParameter) parameter(5, "stringValue", parameterType, sample);
		dataset.setSample(sample);

		// Queries and expected responses
		JsonObjectBuilder query = Json.createObjectBuilder().add("sample.id", Json.createArrayBuilder().add("3"));
		JsonObjectBuilder dimension = Json.createObjectBuilder().add("dimension", "type.name");
		JsonArrayBuilder dimensions = Json.createArrayBuilder().add(dimension);
		JsonObject facet = Json.createObjectBuilder().add("query", query).add("dimensions", dimensions).build();

		JsonObjectBuilder filterBuilder = Json.createObjectBuilder();
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		JsonObject value = Json.createObjectBuilder().add("field", "stringValue").add("value", "stringValue").build();
		JsonObject numericName = Json.createObjectBuilder().add("field", "type.name").add("value", "parameter").build();
		arrayBuilder.add(numericName).add(value);
		filterBuilder.add("key", "key").add("label", "label").add("filter", arrayBuilder);
		JsonObject filter = filterBuilder.build();

		FacetDimension expectedFacet = new FacetDimension("", "type.name", new FacetLabel("parameter", 1L));
		JsonObject investigationQuery = buildQuery("Investigation", null, null, null, null, null, null,
				new Filter("sampleparameter", filter));
		JsonObject datasetQuery = buildQuery("Dataset", null, null, null, null, null, null,
				new Filter("sampleparameter", filter));
		JsonObject datafileQuery = buildQuery("Datafile", null, null, null, null, null, null,
				new Filter("sampleparameter", filter));

		// Create
		modify(SearchApi.encodeOperation("create", investigation),
				SearchApi.encodeOperation("create", dataset),
				SearchApi.encodeOperation("create", datafile),
				SearchApi.encodeOperation("create", sample),
				SearchApi.encodeOperation("create", parameterType),
				SearchApi.encodeOperation("create", parameter));

		// Test
		checkFacets(searchApi.facetSearch("SampleParameter", facet, 5, 5), expectedFacet);

		SearchResult lsr = searchApi.getResults(investigationQuery, null, 5, null, investigationFields);
		checkResults(lsr, 0L);
		lsr = searchApi.getResults(datasetQuery, null, 5, null, datasetFields);
		checkResults(lsr, 1L);
		lsr = searchApi.getResults(datafileQuery, null, 5, null, datafileFields);
		checkResults(lsr, 2L);
	}

}
