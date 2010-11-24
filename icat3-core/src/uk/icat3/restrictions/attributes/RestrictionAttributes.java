/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 19 nov. 2010
 */

package uk.icat3.restrictions.attributes;

/**
 * This class defines all the attributes which is able to search for.
 * This attributes are sectioned depending on the database table where they
 * come from.
 * 
 * @author cruzcruz
 */
public enum RestrictionAttributes {
    DATASET_ID("id"),
    DATASET_NAME("name"),
    DATASET_DESCRIPTION("description"),
    DATASET_LOCATION("location"),
    DATASET_STATUS("datasetStatus"),
    DATASET_TYPE("datasetType"),

    DATAFILE_ID("id"),
    DATAFILE_NAME("name"),
    DATAFILE_DESCRIPTION("description"),
    DATAFILE_LOCATION("location"),
    DATAFILE_VERSION("datafileVersion"),
    DATAFILE_VERSION_COMMENT("datafileVersionComment"),
    DATAFILE_CREATE_TIME("datafileCreateTime"),
    DATAFILE_MODIFY_TIME("datafileModifyTime"),
    DATAFILE_FILE_SIZE("fileSize"),
    DATAFILE_COMMAND("command"),
    DATAFILE_CHECKSUM("checksum"),
    DATAFILE_SIGNATURE("signature"),
    DATAFILE_FORMAT_TYPE("datafileFormat.formatType"),

    SAMPLE_ID("id"),
    SAMPLE_INSTANCE("instance"),
    SAMPLE_NAME("name"),
    SAMPLE_CHEMICAL_FORMULA("chemicalFormula"),
    SAMPLE_SAFETY_INFORMATION("safetyInformation"),

    INVESTIGATION_ID("id"),
    INVESTIGATION_INV_NUMBER("invNumber"),
    INVESTIGATION_VISIT_ID("visitId"),
    INVESTIGATION_TITLE("title"),
    INVESTIGATION_ABSTRACT("invAbstract"),
    INVESTIGATION_PREV_INV_NUMBER("prevInvNumber"),
    INVESTIGATION_BCAT_INV_STR("bcatInvStr"),
    INVESTIGATION_PARAM_NAME("invParamName"),
    INVESTIGATION_PARAM_VALUE("invParamValue"),
    INVESTIGATION_FACILITY("facility"),
    INVESTIGATION_INSTRUMENT("instrument"),
    INVESTIGATION_TYPE("invType"),
    INVESTIGATION_RELEASE_DATE("releaseDate"),
    INVESTIGATION_START_DATE("invStartDate"),
    INVESTIGATION_END_DATE("invEndDate");

    private final String value;

    RestrictionAttributes(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isNumeric() {
        if (this == RestrictionAttributes.INVESTIGATION_ID ||
            this == RestrictionAttributes.SAMPLE_ID ||
            this == RestrictionAttributes.DATASET_ID ||
            this == RestrictionAttributes.DATAFILE_ID)
            return true;
        return false;
    }

    public boolean isDateTime() {
        if (this == RestrictionAttributes.INVESTIGATION_START_DATE ||
            this == RestrictionAttributes.INVESTIGATION_END_DATE ||
            this == RestrictionAttributes.INVESTIGATION_RELEASE_DATE ||
            this == RestrictionAttributes.DATAFILE_CREATE_TIME)
            return true;

        return false;
    }

    public boolean isString() {
        if (!isDateTime() && ! isNumeric())
            return true;
        return false;
    }

    public boolean isDataset () {
        // Dataset attributes
        if (this.ordinal() <= RestrictionAttributes.DATASET_TYPE.ordinal()) {
            return true;
        }
        return false;
    }

    public boolean isDatafile() {
        // Datafile attributes
        if (this.ordinal() >= RestrictionAttributes.DATAFILE_ID.ordinal()  &&
                 this.ordinal() <= RestrictionAttributes.DATAFILE_FORMAT_TYPE.ordinal()) {
           return true;
        }
        return false;
    }

    public boolean isSample() {
        // Sample attributes
        if (this.ordinal() >= RestrictionAttributes.SAMPLE_ID.ordinal()  &&
                 this.ordinal() <= RestrictionAttributes.SAMPLE_SAFETY_INFORMATION.ordinal()) {
            return true;
        }
        return false;
    }

    public boolean isInvestigation() {
        // Investigation attributes
       if (this.ordinal() >= RestrictionAttributes.INVESTIGATION_ID.ordinal()) {
            return true;
        }
        return false;
    }
}
