package org.icatproject.core.manager.search;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Utility for building queries in Json understood by Opensearch.
 */
public class OpensearchQueryBuilder {

    private static JsonObject matchAllQuery = Json.createObjectBuilder().add("match_all", Json.createObjectBuilder())
            .build();

    /**
     * @param query JsonObject representing an Opensearch query.
     * @return JsonObjectBuilder with JsonObject <code>{"query": {...query}}</code>
     */
    public static JsonObjectBuilder addQuery(JsonObject query) {
        return Json.createObjectBuilder().add("query", query);
    }

    /**
     * @return <code>{"match_all": {}}</code>
     */
    public static JsonObject buildMatchAllQuery() {
        return matchAllQuery;
    }

    /**
     * @param field Field containing the match.
     * @param value Value to match.
     * @return <code>{"match": {"`field`.keyword": {"query": `value`, "operator": "and"}}}</code>
     */
    public static JsonObject buildMatchQuery(String field, String value) {
        JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("query", value).add("operator", "and");
        JsonObjectBuilder matchBuilder = Json.createObjectBuilder().add(field + ".keyword", fieldBuilder);
        return Json.createObjectBuilder().add("match", matchBuilder).build();
    }

    /**
     * @param path Path to nested Object.
     * @param queryObjects Any number of pre-built queries.
     * @return <code>{"nested": {"path": `path`, "query": {"bool": {"filter": [...queryObjects]}}}}</code>
     */
    public static JsonObject buildNestedQuery(String path, JsonObject... queryObjects) {
        JsonObject builtQueries = null;
        if (queryObjects.length == 0) {
            builtQueries = matchAllQuery;
        } else if (queryObjects.length == 1) {
            builtQueries = queryObjects[0];
        } else {
            JsonArrayBuilder filterBuilder = Json.createArrayBuilder();
            for (JsonObject queryObject : queryObjects) {
                filterBuilder.add(queryObject);
            }
            JsonObjectBuilder boolBuilder = Json.createObjectBuilder().add("filter", filterBuilder);
            builtQueries = Json.createObjectBuilder().add("bool", boolBuilder).build();
        }
        JsonObjectBuilder nestedBuilder = Json.createObjectBuilder().add("path", path).add("query", builtQueries);
        return Json.createObjectBuilder().add("nested", nestedBuilder).build();
    }

    /**
     * @param value String value to query for.
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
        return Json.createObjectBuilder().add("query_string", queryStringBuilder).build();
    }

    /**
     * @param field Field containing the term.
     * @param value Term to match.
     * @return <code>{"term": {`field`: `value`}}</code>
     */
    public static JsonObject buildTermQuery(String field, String value) {
        return Json.createObjectBuilder().add("term", Json.createObjectBuilder().add(field, value)).build();
    }

    /**
     * @param field Field containing on of the terms.
     * @param values JsonArrat of possible terms.
     * @return <code>{"terms": {`field`: `values`}}</code>
     */
    public static JsonObject buildTermsQuery(String field, JsonArray values) {
        return Json.createObjectBuilder().add("terms", Json.createObjectBuilder().add(field, values)).build();
    }

    /**
     * @param field Field to apply the range to.
     * @param lowerValue Lowest allowed value in the range.
     * @param upperValue Highest allowed value in the range.
     * @return <code>{"range": {`field`: {"gte": `upperValue`, "lte": `lowerValue`}}}</code>
     */
    public static JsonObject buildLongRangeQuery(String field, Long lowerValue, Long upperValue) {
        JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("gte", lowerValue).add("lte", upperValue);
        JsonObjectBuilder rangeBuilder = Json.createObjectBuilder().add(field, fieldBuilder);
        return Json.createObjectBuilder().add("range", rangeBuilder).build();
    }

    /**
     * @param field Field to apply the range to.
     * @param lowerValue Lowest allowed value in the range.
     * @param upperValue Highest allowed value in the range.
     * @return <code>{"range": {`field`: {"gte": `upperValue`, "lte": `lowerValue`}}}</code>
     */
    public static JsonObject buildRangeQuery(String field, JsonNumber lowerValue, JsonNumber upperValue) {
        JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("gte", lowerValue).add("lte", upperValue);
        JsonObjectBuilder rangeBuilder = Json.createObjectBuilder().add(field, fieldBuilder);
        return Json.createObjectBuilder().add("range", rangeBuilder).build();
    }

    /**
     * @param field Field to facet.
     * @param ranges JsonArray of ranges to allocate documents to.
     * @return <code>{"range": {"field": `field`, "keyed": true, "ranges": `ranges`}}</code>
     */
    public static JsonObject buildRangeFacet(String field, JsonArray ranges) {
        JsonObjectBuilder rangeBuilder = Json.createObjectBuilder();
        rangeBuilder.add("field", field).add("keyed", true).add("ranges", ranges);
        return Json.createObjectBuilder().add("range", rangeBuilder).build();
    }

    /**
     * @param field Field to facet.
     * @param maxLabels Maximum number of labels per dimension.
     * @return <code>{"terms": {"field": `field`, "size": `maxLabels`}}</code>
     */
    public static JsonObject buildStringFacet(String field, int maxLabels) {
        JsonObjectBuilder termsBuilder = Json.createObjectBuilder();
        termsBuilder.add("field", field).add("size", maxLabels);
        return Json.createObjectBuilder().add("terms", termsBuilder).build();
    }

}
