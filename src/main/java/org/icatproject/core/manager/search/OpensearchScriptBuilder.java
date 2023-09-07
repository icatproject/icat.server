package org.icatproject.core.manager.search;

import java.util.Set;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

public class OpensearchScriptBuilder {

    /**
     * Builds Json for creating a new script with the provided painless source code.
     * 
     * @param source Painless source code as a String.
     * @return Json for creating a new script.
     */
    private static String buildScript(String source) {
        JsonObjectBuilder builder = Json.createObjectBuilder().add("lang", "painless").add("source", source);
        return Json.createObjectBuilder().add("script", builder).build().toString();
    }

    /**
     * In order to access a specific nested child entity, access `childIndex` in
     * later parts of the painless script.
     * 
     * @param childName      The name of the nested child entity.
     * @param declareChildId Should be true for only the first time a child is found
     *                       during a script so that the variable can be reused.
     * @return Painless code for determining the id of a given child within a nested
     *         array.
     */
    private static String findNestedChild(String childName, boolean declareChildId) {
        String source;
        if (declareChildId) {
            source = "int childIndex = -1; int i = 0;";
        } else {
            source = "childIndex = -1; i = 0;";
        }
        return source + " if (ctx._source." + childName + " != null) "
                + "{while (childIndex == -1 && i < ctx._source." + childName + ".size()) "
                + "{if (ctx._source." + childName + ".get(i).id == params.id) {childIndex = i;} i++;}}";
    }

    /**
     * @param childName The name of the nested child entity.
     * @return Painless code for removing a given child within a nested array based
     *         on its id.
     */
    private static String removeNestedChild(String childName) {
        return findNestedChild(childName, true) + " if (childIndex != -1) {ctx._source." + childName
                + ".remove(childIndex);}";
    }

    /**
     * @param field     The field belonging to the child entity to be modified.
     * @param ctxSource The context source where the field can be found.
     * @param update    If true the script will replace the field, else the
     *                  value will be deleted.
     * @return Painless code for updating one field within ctxSource.
     */
    private static String updateField(String field, String ctxSource, boolean update) {
        if (update) {
            if (field.equals("numericValueSI")) {
                return "if (" + ctxSource + ".numericValue != null && params.containsKey('conversionFactor')) {"
                        + ctxSource + ".numericValueSI = params.conversionFactor * " + ctxSource
                        + ".numericValue;} else {" + ctxSource + ".remove('numericValueSI');}";
            } else {
                return ctxSource + "['" + field + "']" + " = params['" + field + "']; ";
            }
        } else {
            return ctxSource + ".remove('" + field + "'); ";
        }
    }

    /**
     * Builds a script which updates specific fields on a parent entity that are set
     * by (at most) a single non-nested child.
     * 
     * @param docFields The fields belonging to the child entity to be modified.
     * @param update    If true the script will replace the docFields, else the
     *                  value will be deleted.
     * @return The painless script as a String.
     */
    public static String buildChildScript(Set<String> docFields, boolean update) {
        String source = "";
        for (String field : docFields) {
            source += updateField(field, "ctx._source", update);
        }
        return buildScript(source);
    }

    /**
     * Builds a script which sets the array of nested child entities to a new array.
     * Note that this will overwrite any existing nested Objects. It should not be
     * used to add a new entry to an existing array, but is more efficient in cases
     * where we know the array will not yet be set.
     * 
     * @param childName The name of the nested child entity.
     * @return The painless script as a String.
     */
    public static String buildCreateNestedChildScript(String childName) {
        String source = "ctx._source." + childName + " = params.doc";
        return buildScript(source);
    }

    /**
     * Builds a script which updates or removes a single specific nested entity
     * based on ICAT entity Id.
     * 
     * @param childName The name of the nested child entity.
     * @param update    If true the script will replace a nested entity, else the
     *                  nested entity will be removed from the array.
     * @return The painless script as a String.
     */
    public static String buildNestedChildScript(String childName, boolean update) {
        String source = removeNestedChild(childName);
        if (update) {
            source += " if (ctx._source." + childName + " != null) {ctx._source." + childName
                    + ".addAll(params.doc);} else {ctx._source." + childName + " = params.doc;}";
        }
        return buildScript(source);
    }

    /**
     * Builds a script which updates specific fields on a nested child entity that
     * are set by a single grandchild.
     * 
     * @param childName The name of the nested child entity.
     * @param docFields The fields belonging to the grandchild entity to be
     *                  modified.
     * @param update    If true the script will replace a nested entity, else the
     *                  nested entity will be removed from the array.
     * @return The painless script as a String.
     */
    public static String buildGrandchildScript(String childName, Set<String> docFields, boolean update) {
        String source = findNestedChild(childName, true);
        String ctxSource = "ctx._source." + childName + ".get(childIndex)";
        if (docFields != null) {
            source += "if (childIndex != -1) { ";
            for (String field : docFields) {
                source += updateField(field, ctxSource, update);
            }
            source += " } ";
        }
        return buildScript(source);
    }

    /**
     * Builds a script which increments fileSize by deltaFileSize. If
     * fileSize is null then deltaFileSize is taken as its new value.
     * 
     * @return The painless script as a String.
     */
    public static String buildFileSizeScript() {
        String source = "if (ctx._source.fileSize != null) ";
        source += "{ctx._source.fileSize += params.deltaFileSize;} else {ctx._source.fileSize = params.deltaFileSize;}";
        source += "if (ctx._source.fileCount != null) ";
        source += "{ctx._source.fileCount += params.deltaFileCount;} else {ctx._source.fileCount = params.deltaFileCount;}";
        return buildScript(source);
    }

    /**
     * Modifies ParameterTypes with logic to ensure the update is applied to all
     * possible Parameters (Investigation, Dataset, Datafile, Sample).
     * 
     * @param fields The fields belonging to the ParameterType to be
     *               modified.
     * @param update If true the script will replace a nested entity, else the
     *               nested entity will be removed from the array.
     * @return
     */
    public static String buildParameterTypesScript(Set<String> docFields, boolean update) {
        String source = buildParameterTypeScript(docFields, update, "investigationparameter", true);
        source += buildParameterTypeScript(docFields, update, "datasetparameter", false);
        source += buildParameterTypeScript(docFields, update, "datafileparameter", false);
        source += buildParameterTypeScript(docFields, update, "sampleparameter", false);
        return buildScript(source);
    }

    /**
     * Modifies a single type of Parameter (Investigation, Dataset, Datafile,
     * Sample) with changes to a ParameterType.
     * 
     * @param update          If true the script will replace a nested entity, else
     *                        the nested entity will be removed from the array
     * @param nestedChildName Name of the Parameter entity to modify
     * @param declareChildId  Whether the childId needs to be declared. This should
     *                        only be true for the first parameter in the script.
     * @param fields          The fields belonging to the ParameterType to be
     *                        modified
     * 
     * @return The script to modify the Parameter as a String
     */
    private static String buildParameterTypeScript(Set<String> docFields, boolean update, String nestedChildName,
            boolean declareChildId) {
        String ctxSource = "ctx._source." + nestedChildName + ".get(childIndex)";
        String source = findNestedChild(nestedChildName, declareChildId);
        if (docFields != null) {
            source += "if (childIndex != -1) { ";
            for (String field : docFields) {
                source += updateField(field, ctxSource, update);
            }
            source += " } ";
        }
        return source;
    }
}
