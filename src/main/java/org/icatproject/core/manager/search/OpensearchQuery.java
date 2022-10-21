package org.icatproject.core.manager.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.utils.IcatUnits.SystemValue;

/**
 * Utilities for building queries in Json understood by Opensearch.
 */
public class OpensearchQuery {

	private static JsonObject matchAll = build("match_all", Json.createObjectBuilder());
	public static JsonObject matchAllQuery = build("query", matchAll);

	private JsonObjectBuilder builder = Json.createObjectBuilder();
	private OpensearchApi opensearchApi;

	public OpensearchQuery(OpensearchApi opensearchApi) {
		this.opensearchApi = opensearchApi;
	}

	/**
	 * @param filter Path to nested Object.
	 * @param should Any number of pre-built queries.
	 * @return <code>{"bool": {"filter": [...filter], "should": [...should]}}</code>
	 */
	public static JsonObject buildBoolQuery(List<JsonObject> filter, List<JsonObject> should) {
		JsonObjectBuilder boolBuilder = Json.createObjectBuilder();
		addToBoolArray("should", should, boolBuilder);
		addToBoolArray("filter", filter, boolBuilder);
		return build("bool", boolBuilder);
	}

	/**
	 * @param occur       String of an occurance keyword ("filter", "should", "must"
	 *                    etc.)
	 * @param queries     List of JsonObjects representing the queries to occur.
	 * @param boolBuilder Builder of the main boolean query.
	 */
	private static void addToBoolArray(String occur, List<JsonObject> queries, JsonObjectBuilder boolBuilder) {
		if (queries != null && queries.size() > 0) {
			JsonArrayBuilder filterBuilder = Json.createArrayBuilder();
			for (JsonObject queryObject : queries) {
				filterBuilder.add(queryObject);
			}
			boolBuilder.add(occur, filterBuilder);
		}
	}

	/**
	 * @param field Field containing the match.
	 * @param value Value to match.
	 * @return <code>{"match": {"`field`.keyword": {"query": `value`, "operator": "and"}}}</code>
	 */
	public static JsonObject buildMatchQuery(String field, String value) {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("query", value).add("operator", "and");
		JsonObject matchBuilder = build(field + ".keyword", fieldBuilder);
		return build("match", matchBuilder);
	}

	/**
	 * @param path         Path to nested Object.
	 * @param queryObjects Any number of pre-built queries.
	 * @return <code>{"nested": {"path": `path`, "query": {"bool": {"filter": [...queryObjects]}}}}</code>
	 */
	public static JsonObject buildNestedQuery(String path, JsonObject... queryObjects) {
		JsonObject builtQueries;
		if (queryObjects.length == 0) {
			builtQueries = matchAllQuery;
		} else if (queryObjects.length == 1) {
			builtQueries = queryObjects[0];
		} else {
			JsonArrayBuilder filterBuilder = Json.createArrayBuilder();
			for (JsonObject queryObject : queryObjects) {
				filterBuilder.add(queryObject);
			}
			JsonObject boolObject = build("filter", filterBuilder.build());
			builtQueries = build("bool", boolObject);
		}
		JsonObjectBuilder nestedBuilder = Json.createObjectBuilder().add("path", path).add("query", builtQueries);
		return build("nested", nestedBuilder);
	}

	/**
	 * @param value  String value to query for.
	 * @param fields List of fields to check for value.
	 * @return <code>{"query_string": {"query": `value`, "fields": [...fields]}}</code>
	 */
	public static JsonObject buildStringQuery(String value, String... fields) {
		JsonObjectBuilder queryStringBuilder = Json.createObjectBuilder().add("query", value);
		if (fields.length > 0) {
			JsonArrayBuilder fieldsBuilder = Json.createArrayBuilder();
			for (String field : fields) {
				fieldsBuilder.add(field);
			}
			queryStringBuilder.add("fields", fieldsBuilder);
		}
		return build("query_string", queryStringBuilder);
	}

	/**
	 * @param field Field containing the term.
	 * @param value Term to match.
	 * @return <code>{"term": {`field`: `value`}}</code>
	 */
	public static JsonObject buildTermQuery(String field, String value) {
		return build("term", Json.createObjectBuilder().add(field, value));
	}

	/**
	 * @param field Field containing the number.
	 * @param value Number to match.
	 * @return <code>{"term": {`field`: `value`}}</code>
	 */
	public static JsonObject buildTermQuery(String field, JsonNumber value) {
		return build("term", build(field, value));
	}

	/**
	 * @param field Field containing the double value.
	 * @param value Double to match.
	 * @return <code>{"term": {`field`: `value`}}</code>
	 */
	public static JsonObject buildTermQuery(String field, double value) {
		return build("term", Json.createObjectBuilder().add(field, value));
	}

	/**
	 * @param field  Field containing on of the terms.
	 * @param values JsonArray of possible terms.
	 * @return <code>{"terms": {`field`: `values`}}</code>
	 */
	public static JsonObject buildTermsQuery(String field, JsonArray values) {
		return build("terms", build(field, values));
	}

	/**
	 * @param field      Field to apply the range to.
	 * @param lowerValue Lowest allowed value in the range.
	 * @param upperValue Highest allowed value in the range.
	 * @return <code>{"range": {`field`: {"gte": `upperValue`, "lte": `lowerValue`}}}</code>
	 */
	public static JsonObject buildDoubleRangeQuery(String field, Double lowerValue, Double upperValue) {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder();
		if (lowerValue != null)
			fieldBuilder.add("gte", lowerValue);
		if (upperValue != null)
			fieldBuilder.add("lte", upperValue);
		return buildRange(field, fieldBuilder);
	}

	/**
	 * @param field      Field to apply the range to.
	 * @param lowerValue Lowest allowed value in the range.
	 * @param upperValue Highest allowed value in the range.
	 * @return <code>{"range": {`field`: {"gte": `upperValue`, "lte": `lowerValue`}}}</code>
	 */
	public static JsonObject buildLongRangeQuery(String field, Long lowerValue, Long upperValue) {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder();
		if (lowerValue != null)
			fieldBuilder.add("gte", lowerValue);
		if (upperValue != null)
			fieldBuilder.add("lte", upperValue);
		return buildRange(field, fieldBuilder);
	}

	/**
	 * @param field      Field to apply the range to.
	 * @param lowerValue Lowest allowed value in the range.
	 * @param upperValue Highest allowed value in the range.
	 * @return <code>{"range": {`field`: {"gte": `upperValue`, "lte": `lowerValue`}}}</code>
	 */
	public static JsonObject buildRangeQuery(String field, JsonNumber lowerValue, JsonNumber upperValue) {
		JsonObjectBuilder fieldBuilder = Json.createObjectBuilder();
		if (lowerValue != null)
			fieldBuilder.add("gte", lowerValue);
		if (upperValue != null)
			fieldBuilder.add("lte", upperValue);
		return buildRange(field, fieldBuilder);
	}

	/**
	 * @param field        Field to apply the range to
	 * @param fieldBuilder JsonObjectBuilder for the field
	 * @return <code>{"range": {`field`: `fieldBuilder`}}</code>
	 */
	private static JsonObject buildRange(String field, JsonObjectBuilder fieldBuilder) {
		JsonObject rangeObject = build(field, fieldBuilder);
		return build("range", rangeObject);
	}

	/**
	 * @param field  Field to facet.
	 * @param ranges JsonArray of ranges to allocate documents to.
	 * @return <code>{"range": {"field": `field`, "keyed": true, "ranges": `ranges`}}</code>
	 */
	public static JsonObject buildRangeFacet(String field, JsonArray ranges) {
		JsonObjectBuilder rangeBuilder = Json.createObjectBuilder();
		rangeBuilder.add("field", field).add("keyed", true).add("ranges", ranges);
		return build("range", rangeBuilder);
	}

	/**
	 * @param field     Field to facet.
	 * @param maxLabels Maximum number of labels per dimension.
	 * @return <code>{"terms": {"field": `field`, "size": `maxLabels`}}</code>
	 */
	public static JsonObject buildStringFacet(String field, int maxLabels) {
		JsonObjectBuilder termsBuilder = Json.createObjectBuilder();
		termsBuilder.add("field", field).add("size", maxLabels);
		return build("terms", termsBuilder);
	}

	/**
	 * @param key     Arbitrary key
	 * @param builder Arbitrary JsonObjectBuilder
	 * @return <code>{`key`: `builder`}}</code>
	 */
	private static JsonObject build(String key, JsonObjectBuilder builder) {
		return Json.createObjectBuilder().add(key, builder).build();
	}

	/**
	 * @param key   Arbitrary key
	 * @param value Arbitrary JsonValue
	 * @return <code>{`key`: `value`}}</code>
	 */
	private static JsonObject build(String key, JsonValue value) {
		return Json.createObjectBuilder().add(key, value).build();
	}

	/**
	 * Extracts and parses a date value from jsonObject. If the value is a NUMBER
	 * (ms since epoch), then it is taken as is. If it is a STRING, then it is
	 * expected in the yyyyMMddHHmm format.
	 * 
	 * @param jsonObject   JsonObject to extract the date from.
	 * @param key          Key of the date field to extract.
	 * @param offset       In the event of the date being a string, we do not have
	 *                     second or ms precision. To ensure ranges are successful,
	 *                     it may be necessary to add 59999 ms to the parsed value
	 *                     as an offset.
	 * @param defaultValue The value to return if key is not present in jsonObject.
	 * @return Time since epoch in ms.
	 * @throws IcatException
	 */
	private static long parseDate(JsonObject jsonObject, String key, int offset, long defaultValue)
			throws IcatException {
		if (jsonObject.containsKey(key)) {
			ValueType valueType = jsonObject.get(key).getValueType();
			switch (valueType) {
				case STRING:
					String dateString = jsonObject.getString(key);
					try {
						return SearchApi.decodeTime(dateString) + offset;
					} catch (Exception e) {
						throw new IcatException(IcatExceptionType.BAD_PARAMETER,
								"Could not parse date " + dateString + " using expected format yyyyMMddHHmm");
					}
				case NUMBER:
					return jsonObject.getJsonNumber(key).longValueExact();
				default:
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"Dates should be represented by a NUMBER or STRING JsonValue, but got " + valueType);
			}
		}
		return defaultValue;
	}

	/**
	 * Parses incoming Json encoding the requested facets and uses bodyBuilder to
	 * construct Json that can be understood by Opensearch.
	 * 
	 * @param dimensions      JsonArray of JsonObjects representing dimensions to be
	 *                        faceted.
	 * @param maxLabels       The maximum number of labels to collect for each
	 *                        dimension.
	 * @param dimensionPrefix Optional prefix to apply to the dimension names. This
	 *                        is needed to distinguish between potentially ambiguous
	 *                        dimensions, such as "(investigation.)type.name" and
	 *                        "(investigationparameter.)type.name".
	 */
	public void parseFacets(JsonArray dimensions, int maxLabels, String dimensionPrefix) {
		JsonObjectBuilder aggsBuilder = Json.createObjectBuilder();
		for (JsonObject dimensionObject : dimensions.getValuesAs(JsonObject.class)) {
			String dimensionString = dimensionObject.getString("dimension");
			String field = dimensionPrefix == null ? dimensionString : dimensionPrefix + "." + dimensionString;
			if (dimensionObject.containsKey("ranges")) {
				JsonArray ranges = dimensionObject.getJsonArray("ranges");
				aggsBuilder.add(dimensionString, buildRangeFacet(field, ranges));
			} else {
				aggsBuilder.add(dimensionString,
						buildStringFacet(field + ".keyword", maxLabels));
			}
		}
		buildFacetRequestJson(dimensionPrefix, aggsBuilder);
	}

	/**
	 * Uses bodyBuilder to construct Json for faceting string fields.
	 * 
	 * @param dimensions      List of dimensions to perform string based faceting
	 *                        on.
	 * @param maxLabels       The maximum number of labels to collect for each
	 *                        dimension.
	 * @param dimensionPrefix Optional prefix to apply to the dimension names. This
	 *                        is needed to distinguish between potentially ambiguous
	 *                        dimensions, such as "(investigation.)type.name" and
	 *                        "(investigationparameter.)type.name".
	 */
	public void parseFacets(List<String> dimensions, int maxLabels, String dimensionPrefix) {
		JsonObjectBuilder aggsBuilder = Json.createObjectBuilder();
		for (String dimensionString : dimensions) {
			String field = dimensionPrefix == null ? dimensionString : dimensionPrefix + "." + dimensionString;
			aggsBuilder.add(dimensionString, buildStringFacet(field + ".keyword", maxLabels));
		}
		buildFacetRequestJson(dimensionPrefix, aggsBuilder);
	}

	/**
	 * Finalises the construction of faceting Json by handling the possibility of
	 * faceting a nested object.
	 * 
	 * @param dimensionPrefix Optional prefix to apply to the dimension names. This
	 *                        is needed to distinguish between potentially ambiguous
	 *                        dimensions, such as "(investigation.)type.name" and
	 *                        "(investigationparameter.)type.name".
	 * @param aggsBuilder     JsonObjectBuilder that has the faceting details.
	 */
	private void buildFacetRequestJson(String dimensionPrefix, JsonObjectBuilder aggsBuilder) {
		if (dimensionPrefix == null) {
			builder.add("aggs", aggsBuilder);
		} else {
			builder.add("aggs", Json.createObjectBuilder()
					.add(dimensionPrefix, Json.createObjectBuilder()
							.add("nested", Json.createObjectBuilder().add("path", dimensionPrefix))
							.add("aggs", aggsBuilder)));
		}
	}

	/**
	 * Parses a filter object applied to a single field. Note that in the case that
	 * this field is actually a nested object, more complex logic will be applied to
	 * ensure that only object matching all nested filters are returned.
	 * 
	 * @param filterBuilder Builder for the array of queries to filter by.
	 * @param field         Field to apply the filter to. In the case of nested
	 *                      queries, this should only be the name of the top level
	 *                      field. For example "investigationparameter".
	 * @param value         JsonValue representing the filter query. This can be a
	 *                      STRING for simple terms, or an OBJECT containing nested
	 *                      "value", "exact" or "range" filters.
	 * @throws IcatException
	 */
	private void parseFilter(JsonArrayBuilder filterBuilder, String field, JsonValue value) throws IcatException {
		ValueType valueType = value.getValueType();
		switch (valueType) {
			case STRING:
				filterBuilder.add(buildTermQuery(field + ".keyword", ((JsonString) value).getString()));
				return;
			case OBJECT:
				JsonObject valueObject = (JsonObject) value;
				if (valueObject.containsKey("filter")) {
					List<JsonObject> queryObjectsList = new ArrayList<>();
					for (JsonObject nestedFilter : valueObject.getJsonArray("filter").getValuesAs(JsonObject.class)) {
						String nestedField = nestedFilter.getString("field");
						if (nestedFilter.containsKey("value")) {
							// String based term query
							String stringValue = nestedFilter.getString("value");
							queryObjectsList.add(buildTermQuery(field + "." + nestedField + ".keyword", stringValue));
						} else if (nestedFilter.containsKey("exact")) {
							parseExactFilter(field, queryObjectsList, nestedFilter, nestedField);
						} else {
							parseRangeFilter(field, queryObjectsList, nestedFilter, nestedField);
						}
					}
					JsonObject[] queryObjects = queryObjectsList.toArray(new JsonObject[0]);
					filterBuilder.add(buildNestedQuery(field, queryObjects));
				} else {
					throw new IcatException(IcatExceptionType.BAD_PARAMETER,
							"expected an ARRAY with the key 'filter', but received " + valueObject);
				}
				return;

			default:
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"filter values should be STRING, OBJECT or and ARRAY of the former, but were " + valueType);
		}

	}

	/**
	 * Parses a range based filter for a single field.
	 * 
	 * @param field            Field to apply the filter to. In the case of nested
	 *                         queries, this should only be the name of the top
	 *                         level
	 *                         field. For example "investigationparameter"
	 * @param queryObjectsList List of JsonObjects to add the filter to
	 * @param nestedFilter     The nested JsonObject which contains the details of
	 *                         the filter
	 * @param nestedField      The nested field on which to actually apply the
	 *                         filter
	 */
	private void parseRangeFilter(String field, List<JsonObject> queryObjectsList, JsonObject nestedFilter,
			String nestedField) {
		JsonNumber from = nestedFilter.getJsonNumber("from");
		JsonNumber to = nestedFilter.getJsonNumber("to");
		String units = nestedFilter.getString("units", null);
		if (units != null) {
			SystemValue fromValue = opensearchApi.icatUnits.new SystemValue(from.doubleValue(), units);
			SystemValue toValue = opensearchApi.icatUnits.new SystemValue(to.doubleValue(), units);
			if (fromValue.value != null && toValue.value != null) {
				// If we were able to parse the units, apply query to the SI value
				String fieldSI = field + "." + nestedField + "SI";
				queryObjectsList.add(buildDoubleRangeQuery(fieldSI, fromValue.value, toValue.value));
			} else {
				// If units could not be parsed, make them part of the query on the raw data
				queryObjectsList.add(buildRangeQuery(field + "." + nestedField, from, to));
				queryObjectsList.add(buildTermQuery(field + ".type.units.keyword", units));
			}
		} else {
			// If units were not provided, just apply to the raw data
			queryObjectsList.add(buildRangeQuery(field + "." + nestedField, from, to));
		}
	}

	/**
	 * Parses an exact filter for a single field.
	 * 
	 * @param field            Field to apply the filter to. In the case of nested
	 *                         queries, this should only be the name of the top
	 *                         level
	 *                         field. For example "investigationparameter"
	 * @param queryObjectsList List of JsonObjects to add the filter to
	 * @param nestedFilter     The nested JsonObject which contains the details of
	 *                         the filter
	 * @param nestedField      The nested field on which to actually apply the
	 *                         filter
	 */
	private void parseExactFilter(String field, List<JsonObject> queryObjectsList, JsonObject nestedFilter,
			String nestedField) {
		JsonNumber exact = nestedFilter.getJsonNumber("exact");
		String units = nestedFilter.getString("units", null);
		if (units != null) {
			SystemValue exactValue = opensearchApi.icatUnits.new SystemValue(exact.doubleValue(), units);
			if (exactValue.value != null) {
				// If we were able to parse the units, apply query to the SI value
				JsonObject bottomQuery = buildDoubleRangeQuery(field + ".rangeBottomSI", null, exactValue.value);
				JsonObject topQuery = buildDoubleRangeQuery(field + ".rangeTopSI", exactValue.value, null);
				JsonObject inRangeQuery = buildBoolQuery(Arrays.asList(bottomQuery, topQuery), null);
				JsonObject exactQuery = buildTermQuery(field + "." + nestedField + "SI", exactValue.value);
				queryObjectsList.add(buildBoolQuery(null, Arrays.asList(inRangeQuery, exactQuery)));
			} else {
				// If units could not be parsed, make them part of the query on the raw data
				JsonObject bottomQuery = buildRangeQuery(field + ".rangeBottom", null, exact);
				JsonObject topQuery = buildRangeQuery(field + ".rangeTop", exact, null);
				JsonObject inRangeQuery = buildBoolQuery(Arrays.asList(bottomQuery, topQuery), null);
				JsonObject exactQuery = buildTermQuery(field + "." + nestedField, exact);
				queryObjectsList.add(buildBoolQuery(null, Arrays.asList(inRangeQuery, exactQuery)));
				queryObjectsList.add(buildTermQuery(field + ".type.units.keyword", units));
			}
		} else {
			// If units were not provided, just apply to the raw data
			JsonObject bottomQuery = buildRangeQuery(field + ".rangeBottom", null, exact);
			JsonObject topQuery = buildRangeQuery(field + ".rangeTop", exact, null);
			JsonObject inRangeQuery = buildBoolQuery(Arrays.asList(bottomQuery, topQuery), null);
			JsonObject exactQuery = buildTermQuery(field + "." + nestedField, exact);
			queryObjectsList.add(buildBoolQuery(null, Arrays.asList(inRangeQuery, exactQuery)));
		}
	}

	/**
	 * Parses the search query from the incoming queryRequest into Json that the
	 * search cluster can understand.
	 * 
	 * @param queryRequest    The Json object containing the information on the
	 *                        requested query, NOT formatted for the search cluster.
	 * @param index           The index to search.
	 * @param dimensionPrefix Used to build nested queries for arbitrary fields.
	 * @param defaultFields   Default fields to apply parsed string queries to.
	 * @throws IcatException If the query cannot be parsed.
	 */
	public void parseQuery(JsonObject queryRequest, String index, String dimensionPrefix, List<String> defaultFields)
			throws IcatException {
		// In general, we use a boolean query to compound queries on individual fields
		JsonObjectBuilder queryBuilder = Json.createObjectBuilder();
		JsonObjectBuilder boolBuilder = Json.createObjectBuilder();

		// Non-scored elements are added to the "filter"
		JsonArrayBuilder filterBuilder = Json.createArrayBuilder();

		long lowerTime = Long.MIN_VALUE;
		long upperTime = Long.MAX_VALUE;
		for (String queryKey : queryRequest.keySet()) {
			switch (queryKey) {
				case "target":
				case "facets":
					break; // Avoid using the target index, or facet request as a term in the search
				case "lower":
					lowerTime = parseDate(queryRequest, "lower", 0, Long.MIN_VALUE);
					break;
				case "upper":
					upperTime = parseDate(queryRequest, "upper", 59999, Long.MAX_VALUE);
					break;
				case "filter":
					parseQueryFilter(queryRequest, index, filterBuilder);
					break;
				case "text":
					parseQueryText(queryRequest, index, defaultFields, boolBuilder);
					break;
				case "user":
					parseQueryUser(queryRequest, filterBuilder);
					break;
				case "userFullName":
					parseQueryUserFullName(queryRequest, filterBuilder);
					break;
				case "samples":
					parseQuerySamples(queryRequest, filterBuilder);
					break;
				case "parameters":
					parseQueryParameters(queryRequest, index, filterBuilder);
					break;
				default:
					parseQueryDefault(queryRequest, dimensionPrefix, filterBuilder, queryKey);
			}
		}

		if (lowerTime != Long.MIN_VALUE || upperTime != Long.MAX_VALUE) {
			if (index.equals("datafile")) {
				// datafile has only one date field
				filterBuilder.add(buildLongRangeQuery("date", lowerTime, upperTime));
			} else {
				filterBuilder.add(buildLongRangeQuery("startDate", lowerTime, upperTime));
				filterBuilder.add(buildLongRangeQuery("endDate", lowerTime, upperTime));
			}
		}

		JsonArray filterArray = filterBuilder.build();
		if (filterArray.size() > 0) {
			boolBuilder.add("filter", filterArray);
		}
		builder.add("query", queryBuilder.add("bool", boolBuilder));
	}

	/**
	 * Parses a generic field name from the queryRequest, and adds them to
	 * filterBuilder.
	 * 
	 * @param queryRequest    JsonObject with the requested query
	 * @param dimensionPrefix Used to build nested queries for arbitrary fields
	 * @param filterBuilder   JsonArrayBuilder for adding criteria to filter on
	 * @param queryKey        The key from the queryRequest to be treated as a
	 *                        Document field
	 * @throws IcatException
	 */
	private void parseQueryDefault(JsonObject queryRequest, String dimensionPrefix, JsonArrayBuilder filterBuilder,
			String queryKey) throws IcatException {
		// If the term doesn't require special logic, handle according to type
		JsonObject defaultTermQuery;
		String field = queryKey;
		if (dimensionPrefix != null) {
			field = dimensionPrefix + "." + field;
		}
		ValueType valueType = queryRequest.get(queryKey).getValueType();
		switch (valueType) {
			case STRING:
				defaultTermQuery = buildTermQuery(field + ".keyword", queryRequest.getString(queryKey));
				break;
			case NUMBER:
				defaultTermQuery = buildTermQuery(field, queryRequest.getJsonNumber(queryKey));
				break;
			case ARRAY:
				// Only support array of String as list of ICAT ids is currently only use case
				defaultTermQuery = buildTermsQuery(field, queryRequest.getJsonArray(queryKey));
				break;
			default:
				throw new IcatException(IcatExceptionType.BAD_PARAMETER,
						"Query values should be ARRAY, STRING or NUMBER, but had value of type " + valueType);
		}
		if (dimensionPrefix != null) {
			// e.g. "sample.id" should use a nested query as sample is nested on other
			// entities
			filterBuilder.add(buildNestedQuery(dimensionPrefix, defaultTermQuery));
		} else {
			// Otherwise, we can associate the query directly with the searched entity
			filterBuilder.add(defaultTermQuery);
		}
	}

	/**
	 * Parses parameters from the queryRequest, and adds them to filterBuilder.
	 * 
	 * @param queryRequest  JsonObject with the requested query
	 * @param index         The index to search
	 * @param filterBuilder JsonArrayBuilder for adding criteria to filter on
	 * @throws IcatException
	 */
	private void parseQueryParameters(JsonObject queryRequest, String index, JsonArrayBuilder filterBuilder)
			throws IcatException {
		for (JsonObject parameterObject : queryRequest.getJsonArray("parameters").getValuesAs(JsonObject.class)) {
			String path = index + "parameter";
			List<JsonObject> parameterQueries = new ArrayList<>();
			if (parameterObject.containsKey("name")) {
				String name = parameterObject.getString("name");
				parameterQueries.add(buildMatchQuery(path + ".type.name", name));
			}
			if (parameterObject.containsKey("units")) {
				String units = parameterObject.getString("units");
				parameterQueries.add(buildMatchQuery(path + ".type.units", units));
			}
			if (parameterObject.containsKey("stringValue")) {
				String stringValue = parameterObject.getString("stringValue");
				parameterQueries.add(buildMatchQuery(path + ".stringValue", stringValue));
			} else if (parameterObject.containsKey("lowerDateValue") && parameterObject.containsKey("upperDateValue")) {
				long lower = parseDate(parameterObject, "lowerDateValue", 0, Long.MIN_VALUE);
				long upper = parseDate(parameterObject, "upperDateValue", 59999, Long.MAX_VALUE);
				parameterQueries.add(buildLongRangeQuery(path + ".dateTimeValue", lower, upper));
			} else if (parameterObject.containsKey("lowerNumericValue")
					&& parameterObject.containsKey("upperNumericValue")) {
				JsonNumber lower = parameterObject.getJsonNumber("lowerNumericValue");
				JsonNumber upper = parameterObject.getJsonNumber("upperNumericValue");
				parameterQueries.add(buildRangeQuery(path + ".numericValue", lower, upper));
			}
			filterBuilder.add(buildNestedQuery(path, parameterQueries.toArray(new JsonObject[0])));
		}
	}

	/**
	 * Parses samples from the queryRequest, and adds them to filterBuilder.
	 * 
	 * @param queryRequest  JsonObject with the requested query
	 * @param filterBuilder JsonArrayBuilder for adding criteria to filter on
	 */
	private void parseQuerySamples(JsonObject queryRequest, JsonArrayBuilder filterBuilder) {
		JsonArray samples = queryRequest.getJsonArray("samples");
		for (int i = 0; i < samples.size(); i++) {
			String sample = samples.getString(i);
			JsonObject stringQuery = buildStringQuery(sample, "sample.name",
					"sample.type.name");
			filterBuilder.add(buildNestedQuery("sample", stringQuery));
		}
	}

	/**
	 * Parses the userFullName from the queryRequest, and adds it to filterBuilder.
	 * This uses joins to InvestigationUser and performs a non-exact string match.
	 * 
	 * @param queryRequest  JsonObject with the requested query
	 * @param filterBuilder JsonArrayBuilder for adding criteria to filter on
	 * @throws IcatException
	 */
	private void parseQueryUserFullName(JsonObject queryRequest, JsonArrayBuilder filterBuilder) {
		String fullName = queryRequest.getString("userFullName");
		JsonObject fullNameQuery = buildStringQuery(fullName, "investigationuser.user.fullName");
		filterBuilder.add(buildNestedQuery("investigationuser", fullNameQuery));
	}

	/**
	 * Parses the user from the queryRequest, and adds it to filterBuilder. This
	 * uses joins to both InvestigationUser and InstrumentScientist entities to
	 * mimic common ICAT rules that only allow users to see their "own" data by
	 * using an exact term match.
	 * 
	 * @param queryRequest  JsonObject with the requested query
	 * @param filterBuilder JsonArrayBuilder for adding criteria to filter on
	 * @throws IcatException
	 */
	private void parseQueryUser(JsonObject queryRequest, JsonArrayBuilder filterBuilder) throws IcatException {
		String user = queryRequest.getString("user");
		// Because InstrumentScientist is on a separate index, we need to explicitly
		// perform a search here
		JsonObject termQuery = buildTermQuery("user.name.keyword", user);
		String body = Json.createObjectBuilder().add("query", termQuery).build().toString();
		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("_source", "instrument.id");
		JsonObject postResponse = opensearchApi.postResponse("/instrumentscientist/_search", body, parameterMap);
		JsonArray hits = postResponse.getJsonObject("hits").getJsonArray("hits");
		JsonArrayBuilder instrumentIdsBuilder = Json.createArrayBuilder();
		for (JsonObject hit : hits.getValuesAs(JsonObject.class)) {
			String instrumentId = hit.getJsonObject("_source").getString("instrument.id");
			instrumentIdsBuilder.add(instrumentId);
		}
		JsonObject instrumentQuery = buildTermsQuery("investigationinstrument.instrument.id",
				instrumentIdsBuilder.build());
		JsonObject nestedInstrumentQuery = buildNestedQuery("investigationinstrument", instrumentQuery);
		// InvestigationUser should be a nested field on the main Document
		JsonObject investigationUserQuery = buildMatchQuery("investigationuser.user.name", user);
		JsonObject nestedUserQuery = buildNestedQuery("investigationuser", investigationUserQuery);
		// At least one of being an InstrumentScientist or an InvestigationUser is
		// necessary
		JsonArrayBuilder array = Json.createArrayBuilder().add(nestedInstrumentQuery).add(nestedUserQuery);
		filterBuilder.add(Json.createObjectBuilder().add("bool", Json.createObjectBuilder().add("should", array)));
	}

	/**
	 * Parses text for a single field from the queryRequest, and adds it to
	 * boolBuilder.
	 * 
	 * @param queryRequest  JsonObject with the requested query
	 * @param index         Index (entity) to apply the query to
	 * @param defaultFields If text does not contain specific field targetting, then
	 *                      matches will be attempting against the defaultFields
	 * @param boolBuilder   JsonObjectBuilder for adding criteria to
	 */
	private void parseQueryText(JsonObject queryRequest, String index, List<String> defaultFields,
			JsonObjectBuilder boolBuilder) {
		// The free text is the only element we perform scoring on, so "must" occur
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		String text = queryRequest.getString("text");
		arrayBuilder.add(buildStringQuery(text, defaultFields.toArray(new String[0])));
		if (index.equals("investigation")) {
			JsonObject stringQuery = buildStringQuery(text, "sample.name", "sample.type.name");
			arrayBuilder.add(buildNestedQuery("sample", stringQuery));
			JsonObjectBuilder textBoolBuilder = Json.createObjectBuilder().add("should", arrayBuilder);
			JsonObjectBuilder textMustBuilder = Json.createObjectBuilder().add("bool", textBoolBuilder);
			boolBuilder.add("must", Json.createArrayBuilder().add(textMustBuilder));
		} else {
			boolBuilder.add("must", arrayBuilder);
		}
	}

	/**
	 * Parses a filter for a single field from the queryRequest, and adds it to
	 * filterBuilder.
	 * 
	 * @param queryRequest  JsonObject with the requested query
	 * @param index         Index (entity) to apply the query to
	 * @param filterBuilder JsonArrayBuilder for adding criteria to filter on
	 * @throws IcatException
	 */
	private void parseQueryFilter(JsonObject queryRequest, String index, JsonArrayBuilder filterBuilder)
			throws IcatException {
		JsonObject filterObject = queryRequest.getJsonObject("filter");
		for (String fld : filterObject.keySet()) {
			JsonValue value = filterObject.get(fld);
			String field = fld.replace(index + ".", "");
			if (value.getValueType().equals(ValueType.ARRAY)) {
				JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
				for (JsonValue arrayValue : ((JsonArray) value).getValuesAs(JsonString.class)) {
					parseFilter(arrayBuilder, field, arrayValue);
				}
				// If the key was just a nested entity (no ".") then we should FILTER all of our
				// queries on that entity.
				String occur = fld.contains(".") ? "should" : "filter";
				filterBuilder.add(Json.createObjectBuilder().add("bool",
						Json.createObjectBuilder().add(occur, arrayBuilder)));
			} else {
				parseFilter(filterBuilder, field, value);
			}
		}
	}

	/**
	 * Parse sort criteria and add it to the request body.
	 * 
	 * @param sort String of JsonObject containing the sort criteria.
	 */
	public void parseSort(String sort) {
		if (sort == null || sort.equals("")) {
			builder.add("sort", Json.createArrayBuilder()
					.add(Json.createObjectBuilder().add("_score", "desc"))
					.add(Json.createObjectBuilder().add("id", "asc")).build());
		} else {
			JsonObject sortObject = Json.createReader(new StringReader(sort)).readObject();
			JsonArrayBuilder sortArrayBuilder = Json.createArrayBuilder();
			for (String key : sortObject.keySet()) {
				if (key.toLowerCase().contains("date") || key.startsWith("file")) {
					// Dates and fileSize/fileCount are numeric, so can be used as is
					sortArrayBuilder.add(Json.createObjectBuilder().add(key, sortObject.getString(key)));
				} else {
					// Text fields should use the .keyword field for sorting
					sortArrayBuilder.add(Json.createObjectBuilder().add(key + ".keyword", sortObject.getString(key)));
				}
			}
			builder.add("sort", sortArrayBuilder.add(Json.createObjectBuilder().add("id", "asc")).build());
		}
	}

	/**
	 * Add searchAfter to the request body.
	 * 
	 * @param searchAfter Possibly null JsonValue representing the last document of
	 *                    a previous search.
	 */
	public void parseSearchAfter(JsonValue searchAfter) {
		if (searchAfter != null) {
			builder.add("search_after", searchAfter);
		}
	}

	/**
	 * @return The parsed query, as a String with Json formatting
	 */
	public String body() {
		return builder.build().toString();
	}

}
