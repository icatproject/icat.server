package org.icatproject.core.manager.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds information for the various types of request that need to be made as
 * part of a bulk modification.
 */
public class OpensearchBulk {

    public Map<String, Set<String>> updatesMap = new HashMap<>();
    public Set<String> investigationIds = new HashSet<>();
    public Map<String, long[]> investigationAggregations = new HashMap<>();
    public Map<String, long[]> datasetAggregations = new HashMap<>();
    public StringBuilder bulkBuilder = new StringBuilder();
    public StringBuilder deletionBuilder = new StringBuilder();
    public StringBuilder fileAggregationBuilder = new StringBuilder();

    /**
     * Adds a path and body for a single update to updatesMap, if not already
     * present.
     * 
     * @param path Path of request
     * @param body Body of request
     */
    public void addUpdate(String path, String body) {
        Set<String> bodies = updatesMap.getOrDefault(path, new HashSet<>());
        bodies.add(body);
        updatesMap.put(path, bodies);
    }

    /**
     * @return String of updates that should be performed as a bulk request
     */
    public String bulkBody() {
        return bulkBuilder.toString();
    }

    /**
     * @return String of deletes that should be performed as a bulk request
     */
    public String deletedBody() {
        return deletionBuilder.toString();
    }

    /**
     * @return String of file aggregations that should be performed as a bulk
     *         request
     */
    public String fileAggregationBody() {
        return fileAggregationBuilder.toString();
    }

}