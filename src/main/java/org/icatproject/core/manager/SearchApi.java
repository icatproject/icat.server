package org.icatproject.core.manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.persistence.EntityManager;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO see what functionality can live here, and possibly convert from abstract to a fully generic API
public abstract class SearchApi {

	abstract void addNow(String entityName, List<Long> ids, EntityManager manager,
			Class<? extends EntityBaseBean> klass, ExecutorService getBeanDocExecutor) throws Exception;

	abstract void clear() throws IcatException;

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected static SimpleDateFormat df;

	static {
		df = new SimpleDateFormat("yyyyMMddHHmm");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
	}

	protected static Date dec(String value) throws java.text.ParseException {
		if (value == null) {
			return null;
		} else {
			synchronized (df) {
				return df.parse(value);
			}
		}
	}

	protected static Long decodeTime(String value) throws java.text.ParseException {
		if (value == null) {
			return null;
		} else {
			synchronized (df) {
				return df.parse(value).getTime();
			}
		}
	}

	protected static String enc(Date dateValue) {
		synchronized (df) {
			return df.format(dateValue);
		}
	}

	public abstract void encodeSortedDocValuesField(JsonGenerator gen, String name, Long value);

	public abstract void encodeSortedSetDocValuesFacetField(JsonGenerator gen, String name, String value);

	public abstract void encodeStoredId(JsonGenerator gen, Long id);

	public abstract void encodeStringField(JsonGenerator gen, String name, Date value);

	public abstract void encodeDoublePoint(JsonGenerator gen, String name, Double value);

	public abstract void encodeStringField(JsonGenerator gen, String name, Long value);

	public abstract void encodeStringField(JsonGenerator gen, String name, String value);

	public abstract void encodeTextField(JsonGenerator gen, String name, String value);

	public abstract void freeSearcher(String uid) throws IcatException;

	public abstract void commit() throws IcatException;

	public abstract List<FacetDimension> facetSearch(JsonObject facetQuery, int maxResults, int maxLabels)
			throws IcatException;

	public abstract SearchResult getResults(JsonObject query, int maxResults) throws IcatException;

	public abstract SearchResult getResults(String uid, JsonObject query, int maxResults) throws IcatException;

	public void lock(String entityName) throws IcatException {
		logger.info("Manually locking index not supported, no request sent");
	}

	public void unlock(String entityName) throws IcatException {
		logger.info("Manually unlocking index not supported, no request sent");
	}

	public abstract void modify(String json) throws IcatException;

	/**
	 * Legacy function for building a Query from individual arguments
	 * @param target
	 * @param user
	 * @param text
	 * @param lower
	 * @param upper
	 * @param parameters
	 * @param samples
	 * @param userFullName
	 * @return
	 */
	public static JsonObject buildQuery(String target, String user, String text, Date lower, Date upper,
			List<ParameterPOJO> parameters, List<String> samples, String userFullName) {
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
			builder.add("lower", LuceneApi.enc(lower));
		}
		if (upper != null) {
			builder.add("upper", LuceneApi.enc(upper));
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
					parameterBuilder.add("lowerDateValue", LuceneApi.enc(parameter.lowerDateValue));
				}
				if (parameter.upperDateValue != null) {
					parameterBuilder.add("upperDateValue", LuceneApi.enc(parameter.upperDateValue));
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
		if (samples != null && !samples.isEmpty()) {
			JsonArrayBuilder samplesBuilder = Json.createArrayBuilder();
			for (String sample : samples) {
				samplesBuilder.add(sample);
			}
			builder.add("samples", samplesBuilder);
		}
		if (userFullName != null) {
			builder.add("userFullName", userFullName);
		}
		return builder.build();
	}
}
