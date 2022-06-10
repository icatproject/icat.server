package org.icatproject.core.manager.search;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class QueryBuilder {

    private static JsonObject matchAllQuery = Json.createObjectBuilder().add("match_all", Json.createObjectBuilder())
            .build();

    public static JsonObjectBuilder addQuery(JsonObject query) {
        return Json.createObjectBuilder().add("query", query);
    }

    public static JsonObject buildMatchAllQuery() {
        return matchAllQuery;
    }

    public static JsonObject buildMatchQuery(String field, String value) {
        JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("query", value).add("operator", "and");
        JsonObjectBuilder matchBuilder = Json.createObjectBuilder().add(field + ".keyword", fieldBuilder);
        return Json.createObjectBuilder().add("match", matchBuilder).build();
    }

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

    public static JsonObject buildTermQuery(String field, String value) {
        return Json.createObjectBuilder().add("term", Json.createObjectBuilder().add(field, value)).build();
    }

    public static JsonObject buildTermsQuery(String field, JsonArray values) {
        return Json.createObjectBuilder().add("terms", Json.createObjectBuilder().add(field, values)).build();
    }

    public static JsonObject buildLongRangeQuery(String field, Long lowerValue, Long upperValue) {
        JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("gte", lowerValue).add("lte", upperValue);
        JsonObjectBuilder rangeBuilder = Json.createObjectBuilder().add(field, fieldBuilder);
        return Json.createObjectBuilder().add("range", rangeBuilder).build();
    }

    public static JsonObject buildRangeQuery(String field, JsonNumber lowerValue, JsonNumber upperValue) {
        JsonObjectBuilder fieldBuilder = Json.createObjectBuilder().add("gte", lowerValue).add("lte", upperValue);
        JsonObjectBuilder rangeBuilder = Json.createObjectBuilder().add(field, fieldBuilder);
        return Json.createObjectBuilder().add("range", rangeBuilder).build();
    }

    public static JsonObject buildRangeFacet(String field, JsonArray ranges) {
        JsonObjectBuilder rangeBuilder = Json.createObjectBuilder();
        rangeBuilder.add("field", field).add("keyed", true).add("ranges", ranges);
        return Json.createObjectBuilder().add("range", rangeBuilder).build();
    }

    public static JsonObject buildStringFacet(String field, int maxLabels) {
        JsonObjectBuilder termsBuilder = Json.createObjectBuilder();
        termsBuilder.add("field", field).add("size", maxLabels);
        return Json.createObjectBuilder().add("terms", termsBuilder).build();
    }

}
