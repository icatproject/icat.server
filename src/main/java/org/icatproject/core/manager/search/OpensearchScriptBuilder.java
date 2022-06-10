package org.icatproject.core.manager.search;

import java.util.Set;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

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
     * @param childName The name of the nested child entity.
     * @return Painless code for determining the id of a given child within a nested
     *         array.
     */
    private static String findNestedChild(String childName) {
        return "int childIndex = -1; int i = 0; if (ctx._source." + childName + " != null) "
                + "{while (childIndex == -1 && i < ctx._source." + childName + ".size()) "
                + "{if (ctx._source." + childName + ".get(i).id == params.id) {childIndex = i;} i++;}}";
    }

    /**
     * @param childName The name of the nested child entity.
     * @return Painless code for removing a given child within a nested array based
     *         on its id.
     */
    private static String removeNestedChild(String childName) {
        return findNestedChild(childName) + " if (childIndex != -1) {ctx._source." + childName
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
     * are set
     * by a single grandchild.
     * 
     * @param childName The name of the nested child entity.
     * @param docFields The fields belonging to the grandchild entity to be
     *                  modified.
     * @param update    If true the script will replace a nested entity, else the
     *                  nested entity will be removed from the array.
     * @return The painless script as a String.
     */
    public static String buildGrandchildScript(String childName, Set<String> docFields, boolean update) {
        String source = findNestedChild(childName);
        String ctxSource = "ctx._source." + childName + ".get(childIndex)";
        for (String field : docFields) {
            source += updateField(field, ctxSource, update);
        }
        return buildScript(source);
    }

}
