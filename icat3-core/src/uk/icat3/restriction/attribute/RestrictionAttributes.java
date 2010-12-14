/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 19 nov. 2010
 */

package uk.icat3.restriction.attribute;

import uk.icat3.restriction.RestrictionType;

/**
 * This class defines all the attributes which is able to search for.
 * This attributes are sectioned depending on the database table where they
 * come from.
 *
 * The string value is the name of the table field inside database.
 * 
 * @author cruzcruz
 */
public enum RestrictionAttributes {
    // Dataset attributes
    DATASET_ID("id"),
    DATASET(""),
    DATASET_NAME("name"),
    DATASET_DESCRIPTION("description"),
    DATASET_LOCATION("location"),
    DATASET_STATUS("datasetStatus"),
    DATASET_TYPE("datasetType"),
    // Datafile attributes
    DATAFILE_ID("id"),
    DATAFILE(""),
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
    // Sample attributes
    SAMPLE_ID("id"),
    SAMPLE(""),
    SAMPLE_INSTANCE("instance"),
    SAMPLE_NAME("name"),
    SAMPLE_CHEMICAL_FORMULA("chemicalFormula"),
    SAMPLE_SAFETY_INFORMATION("safetyInformation"),
    // Investigation attributes
    INVESTIGATION_ID("id"),
    INVESTIGATION(""),
    INVESTIGATION_INV_NUMBER("invNumber"),
    INVESTIGATION_VISIT_ID("visitId"),
    INVESTIGATION_TITLE("title"),
    INVESTIGATION_ABSTRACT("invAbstract"),
    INVESTIGATION_PREV_INV_NUMBER("prevInvNumber"),
    INVESTIGATION_BCAT_INV_STR("bcatInvStr"),
    INVESTIGATION_PARAM_NAME("invParamName"),
    INVESTIGATION_PARAM_VALUE("invParamValue"),
    INVESTIGATION_FACILITY("facility"),
    INVESTIGATION_FACILITY_CYCLE_NAME("facilityCycle.name"),
    INVESTIGATION_INSTRUMENT("instrument"),
    INVESTIGATION_TYPE("invType"),
    INVESTIGATION_RELEASE_DATE("releaseDate"),
    INVESTIGATION_START_DATE("invStartDate"),
    INVESTIGATION_END_DATE("invEndDate"),
    // Investigator attributes
    INVESTIGATOR_ID("id"),
    INVESTIGATOR_USER_FIRST_NAME("facilityUser.firstName"),
    INVESTIGATOR_USER_MIDDLE_NAME("facilityUser.middleName"),
    INVESTIGATOR_USER_LAST_NAME("facilityUser.lastName"),
    INVESTIGATOR_USER_ID("facilityUser.facilityUserId"),
    // Facility User attributes
    FACILITY_USER_ID("facilityUserId"),
    FACILITY_USER_FIRST_NAME("firstName"),
    FACILITY_USER_MIDDLE_NAME("middleName"),
    FACILITY_USER_INITIALS("initials"),
    FACILITY_USER_TITLE("title"),
    FACILITY_USER_LAST_NAME("lastName"),
    // Keyword
    KEYWORD("keywordPK.name"),
    // Parameter
    PARAMETER_NAME("parameterPK.name"),
    PARAMETER_SEARCHABLE("searchable"),
    PARAMETER_VALUE_TYPE("valueType"),
    PARAMETER_IS_SAMPLE_PARAMETER("isSampleParameter"),
    PARAMETER_IS_DATASET_PARAMETER("isDatasetParameter"),
    PARAMETER_IS_DATAFILE_PARAMETER("isDatafileParameter"),
    PARAMETER_UNITS("parameterPK.units");


    /** Value of the selected enum */
    private final String value;
    /** Constructor */
    RestrictionAttributes(String value) {
        this.value = value;
    }
    /**
     * Return value of the attribute
     *
     * @return
     */
    public String getValue() {
        return this.value;
    }
    /**
     * Check if attribute is numeric value
     * @return True if attribute is numeric, otherwise false.
     */
    public boolean isNumeric() {
        if (this == RestrictionAttributes.INVESTIGATION_ID ||
            this == RestrictionAttributes.SAMPLE_ID ||
            this == RestrictionAttributes.DATASET_ID ||
            this == RestrictionAttributes.DATAFILE_ID ||
            this == RestrictionAttributes.INVESTIGATOR_ID)
            return true;
        return false;
    }
    /**
     * Check if attribute is Datetime value
     * @return True if attribute is Datetime, otherwise false.
     */
    public boolean isDateTime() {
        if (this == RestrictionAttributes.INVESTIGATION_START_DATE ||
            this == RestrictionAttributes.INVESTIGATION_END_DATE ||
            this == RestrictionAttributes.INVESTIGATION_RELEASE_DATE ||
            this == RestrictionAttributes.DATAFILE_CREATE_TIME)
            return true;

        return false;
    }
    /**
     * Check if attribute is String value
     * @return True if attribute is String, otherwise false.
     */
    public boolean isString() {
        if (!isDateTime() && !isNumeric() && !isObject())
            return true;
        return false;
    }
    /**
     * Check if attribute belongs to Dataset
     * @return True if attribute belongs to Dataset, otherwise false.
     */
    public boolean isDataset () {
        // Dataset attributes
        if (this.ordinal() <= RestrictionAttributes.DATASET_TYPE.ordinal())
            return true;
        return false;
    }
     /**
     * Check if attribute belongs to Datafile
     * @return True if attribute belongs to Datafile, otherwise false.
     */
    public boolean isDatafile() {
        // Datafile attributes
        if (this.ordinal() >= RestrictionAttributes.DATAFILE_ID.ordinal()  &&
                 this.ordinal() <= RestrictionAttributes.DATAFILE_FORMAT_TYPE.ordinal())
           return true;
        return false;
    }
     /**
     * Check if attribute belongs to Sample
     * @return True if attribute belongs to Sample, otherwise false.
     */
    public boolean isSample() {
        // Sample attributes
        if (this.ordinal() >= RestrictionAttributes.SAMPLE_ID.ordinal()  &&
                 this.ordinal() <= RestrictionAttributes.SAMPLE_SAFETY_INFORMATION.ordinal())
            return true;
        return false;
    }
     /**
     * Check if attribute belongs to Investigation
     * @return True if attribute belongs to Investigation, otherwise false.
     */
    public boolean isInvestigation() {
       // Investigation attributes
       if (this.ordinal() >= RestrictionAttributes.INVESTIGATION_ID.ordinal() &&
               this.ordinal() <= RestrictionAttributes.INVESTIGATION_END_DATE.ordinal())
            return true;
        return false;
    }
     /**
     * Check if attribute belongs to Investigator
     * @return True if attribute belongs to Investigator, otherwise false.
     */
    public boolean isInvestigator () {
        if (this.ordinal() >= RestrictionAttributes.INVESTIGATOR_ID.ordinal() &&
                this.ordinal() <= RestrictionAttributes.INVESTIGATOR_USER_ID.ordinal())
            return true;
        return false;
    }
    /**
     * Check if attribute belongs to Facility User
     * @return True if attribute belongs to FacilityUser, otherwise false.
     */
    public boolean isFacilityUser () {
        if (this.ordinal() >= RestrictionAttributes.FACILITY_USER_ID.ordinal() &&
                this.ordinal() <= RestrictionAttributes.FACILITY_USER_LAST_NAME.ordinal())
            return true;
        return false;
    }
    /**
     * Check if attribute belongs to Keyword
     * @return True if attribute belongs to FacilityUser, otherwise false.
     */
    public boolean isKeyword () {
        if (this == RestrictionAttributes.KEYWORD )
            return true;
        return false;
    }
    /**
     * Check if attribute belongs to Parameter
     * @return True if attribute belongs to FacilityUser, otherwise false.
     */
    public boolean isParameter () {
        if (this.ordinal() >= RestrictionAttributes.PARAMETER_NAME.ordinal() &&
                this.ordinal() <= RestrictionAttributes.PARAMETER_UNITS.ordinal())
            return true;
        return false;
    }
    /**
     * Check if attribute is an Object (Dataset, Investigation, Datafile, Sample
     * @return True if attribute belongs to FacilityUser, otherwise false.
     */
    public boolean isObject () {
        if (this == RestrictionAttributes.DATASET ||
                this == RestrictionAttributes.INVESTIGATION ||
                this == RestrictionAttributes.DATAFILE ||
                this == RestrictionAttributes.SAMPLE)
            return true;
        return false;
    }
    /**
     * Retrun restriction search type of the attribute
     * 
     * @return
     */
    public RestrictionType getAttributeType() {
        if (this.isDatafile())
            return RestrictionType.DATAFILE;
        if (this.isDataset())
            return RestrictionType.DATASET;
        if (this.isSample())
            return RestrictionType.SAMPLE;
        if (this.isInvestigation())
            return RestrictionType.INVESTIGATION;
        return null;
    }
}
