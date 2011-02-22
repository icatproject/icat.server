/*
 * Queries.java
 *
 * Created on 16 November 2006, 11:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import uk.icat3.restriction.RestrictionCondition;

/**
 *
 * @author gjd37
 */
public class Queries {
    
    public static final String SUPER_USER = "SUPER_USER";

    /////////////////////////////////////    These are to be added together to form queries  ///////////////////////
    //Returns all of investigation
    public static final String RETURN_ALL_INVESTIGATIONS_JPQL = "SELECT DISTINCT i from Investigation i ";

    // Returns all facility users
    public static String RETURN_ALL_PARAMETERS = "SELECT DISTINCT i from Parameter i ";

    // Returns all facility users
    public static String RETURN_ALL_FACILITY_USERS = "SELECT DISTINCT i from FacilityUser i ";
    
    //Returns all of datafiles
    public static final String RETURN_ALL_DATAFILES_JPQL = "SELECT DISTINCT i from Datafile i ";

    /** Returns all of Datafile ids*/
    public static final String RETURN_ALL_DATAFILES_ID_JPQL = "SELECT DISTINCT i.id from Datafile i ";

    /** Returns all of Investigation ids*/
    public static final String RETURN_ALL_INVESTIGATIONS_ID_JPQL = "SELECT DISTINCT i.id from Investigation i ";

    /** Returns all Sample ids */
    public static final String RETURN_ALL_SAMPLE_ID_JPQL = "SELECT DISTINCT i.id from Sample i ";

    /** Returns all of datasets */
    public static final String RETURN_ALL_DATASETS_JPQL = "SELECT DISTINCT i from Dataset i ";

    /** Returns all of datasets id */
    public static final String RETURN_ALL_DATASETS_ID_JPQL = "SELECT DISTINCT i.id from Dataset i ";

    /** Return number of results */
    public static final String RETURN_DATASET_COUNT_RESULT_JPQL = "SELECT count (DISTINCT i.id) from Dataset i ";
    
    //Returns all of samples
    public static final String RETURN_ALL_SAMPLES_JPQL = "SELECT DISTINCT i from Sample i ";
    
    //Returns investigation id
    public static final String RETURN_ALL_INVESTIGATION_IDS_JPQL = "SELECT DISTINCT i.id from Investigation i ";
    
    // Search all investigations WHERE (investigation.instrument = facilityScientist AND facilityScientist fedid) OR
    // OR (userId = SUPER) OR (userId is in icatAuthrosation table and role is select)   
    public static final String QUERY_USERS_INVESTIGATIONS_JPQL = ", IcatAuthorisation ia WHERE" +           
            " ((i.id = ia.elementId AND ia.elementType = :objectType " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY'))" + " OR (ia.elementId IS NULL AND ia.elementType = uk.icat3.util.ElementType.INVESTIGATION AND ia.userId = :userId)"+ 
            " AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N' ";

    /**
     * Error with this search, MERGE JOIN CARTESIAN for fis.facilityInstrumentScientistPK.instrumentName = i.dataset.investigation.instrument
     * i.dataset.investigation.instrument is not working properly
     */
    public static final String QUERY_USERS_DATAFILES_JPQL = ", IcatAuthorisation ia WHERE" +
            " ((i.dataset.id = ia.elementId AND ia.elementType = :objectType " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY'))" + " OR (ia.elementId IS NULL AND ia.elementType = uk.icat3.util.ElementType.INVESTIGATION AND ia.userId = :userId)" +
            " AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N' ";

     public static final String QUERY_USERS_DATASETS_JPQL = ", IcatAuthorisation ia WHERE" +
            "  ((i.id = ia.elementId AND ia.elementType = :objectType " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY'))" + " OR (ia.elementId IS NULL AND ia.elementType = uk.icat3.util.ElementType.INVESTIGATION AND ia.userId = :userId)" +
            " AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N' ";

      public static final String QUERY_USERS_SAMPLES_JPQL = ", IcatAuthorisation ia WHERE" +
            " ((i.investigationId.id = ia.elementId AND ia.elementType = :objectType " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY'))" +  " OR (ia.elementId IS NULL AND ia.elementType = uk.icat3.util.ElementType.INVESTIGATION AND ia.userId = :userId)" +
            " AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y') AND i.markedDeleted = 'N' ";

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Lists all the users investigations
     *
     * icat_authorisation row is not marked deleted, the user id is their userId or ANY and the role has read permission
     */
    public static final String LIST_ALL_USERS_INVESTIGATIONS_JPQL = RETURN_ALL_INVESTIGATIONS_JPQL + QUERY_USERS_INVESTIGATIONS_JPQL;
    public static final String LIST_ALL_USERS_INVESTIGATION_IDS_JPQL = RETURN_ALL_INVESTIGATION_IDS_JPQL + QUERY_USERS_INVESTIGATIONS_JPQL;
    
    /**
     * Lists all the users investigations in SQL (genreated from above JPQL)
     */
    //public static final String LIST_ALL_USERS_INVESTIGATIONS_SQL = "SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_AQUIRED, t0.DELETED, t0.INSTRUMENT, t0.FACILITY_CYCLE, t0.INV_TYPE " +
    //        "FROM INVESTIGATION t0, ICAT_AUTHORISATION t1, ICAT_ROLE t2 WHERE " +
    //       "(((((((t0.ID = t1.ELEMENT_ID) AND (t1.ELEMENT_TYPE = 'INVESTIGATION')) AND (t0.DELETED = 'N')) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND (t2.ROLE = t1.ROLE))";
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This searches the investigations that the user can view by a suranme
     *
     */
    public static final String INVESTIGATION_LIST_BY_SURNAME = "Investigation.findByUserSurname";
    public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +
            "AND EXISTS (SELECT inv FROM i.investigatorCollection inv WHERE LOWER(inv.facilityUser.lastName) LIKE :surname AND " +
            "inv.markedDeleted = 'N')";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This searches the investigations that the user can view by one keyword
     * TODO needs to be multipe keywords
     */
    public static final String INVESTIGATION_LIST_BY_KEYWORD = "Investigation.findByKewordNative";
    public static final String INVESTIGATION_LIST_BY_KEYWORD_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +
            "AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.keywordPK.name LIKE :keyword AND " +
            "kw.markedDeleted = 'N')";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Same but returning investigation Ids
     */
    public static final String INVESTIGATION_LIST_BY_KEYWORD_RTN_ID = "Investigation.findByKewordRtnIdNative";
    public static final String INVESTIGATION_LIST_BY_KEYWORD_RTN_ID_JPQL = RETURN_ALL_INVESTIGATION_IDS_JPQL +
            QUERY_USERS_INVESTIGATIONS_JPQL +
            "AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.keywordPK.name LIKE :keyword AND " +
            "kw.markedDeleted = 'N')";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Search my keywords (AND and OR and fuzzy)
     *
     * Did try with SQL but too difficult, so doing with dynamic JPQL, this is slightly slower as the SQL needs to be
     * generated everytime from the JPQL but its much simplier.
     *
     *  (t3.NAME LIKE ?keyword1) OR (t3.NAME LIKE ?keyword2) OR (t3.NAME LIKE ?keyword3)  this worked for dynamic SQL
     *  (t3.NAME LIKE ?keyword1) AND (t3.NAME LIKE ?keyword2) AND (t3.NAME LIKE ?keyword3) this failed cos its checking same
     *  value for the ANDs, so canot be t3.NAME ?keyword1 AND ?keyword2 so stayed with JPQL
     */
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS = "Investigation.findByKewordsNative";
    
    // Query =  LIST_ALL_USERS_INVESTIGATIONS_JPQL " + AND (EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.keywordPK.name LIKE :keyword1 AND kw.markedDeleted = 'N') AND EXISTS (SELECT kw2 FROM i.keywordCollection kw2 WHERE kw2.keywordPK.name LIKE :keyword1 AND kw2.markedDeleted = 'N'))";
    public static final String INVESTIGATION_LIST_BY_KEYWORDS_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL ;
    // " AND (EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.markedDeleted = 'N' AND "; //kw.keywordPK.name LIKE :keyword1 OR/AND kw.keywordPK.name LIKE :keyword2 ))
    
    // QUERY = INVESTIGATION_LIST_BY_KEYWORDS_JPQL_NOSECURITY = "SELECT i from Investigation i WHERE i.markedDeleted = 'N' AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.keywordPK.name LIKE :keyword1 OR/AND kw.keywordPK.name LIKE :keyword2 AND kw.markedDeleted = 'N')";
    public static final String INVESTIGATION_LIST_BY_KEYWORDS_JPQL_NOSECURITY = "SELECT i from Investigation i WHERE i.markedDeleted = 'N' ";// AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.markedDeleted = 'N' AND ";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This searches the investigations that the user can view by userid
     *
     */
    public static final String INVESTIGATION_LIST_BY_USERID = "Investigation.findByUserID";
    public static final String INVESTIGATION_LIST_BY_USERID_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +
            " AND EXISTS ( SELECT inv FROM i.investigatorCollection inv WHERE inv.facilityUser.federalId LIKE :federalId AND inv.markedDeleted = 'N')";
    
    public static final String INVESTIGATION_LIST_BY_USERID_RTID = "Investigation.findByUserIDRtId";
    public static final String INVESTIGATION_LIST_BY_USERID_RTID_JPQL = LIST_ALL_USERS_INVESTIGATION_IDS_JPQL +
            " AND EXISTS ( SELECT inv FROM i.investigatorCollection inv WHERE inv.facilityUser.federalId LIKE :federalId AND inv.markedDeleted = 'N')";
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This is the search for the advanced search.  This is skeleton of the query, it is constructed in java
     */
    public static final String ADVANCED_SEARCH = "Investigation.findByAdvancedSearch";
    // String QUERY = "SELECT i FROM Investigation i, IcatAuthorisation ia WHERE i.id = ia.elementId AND ia.elementType = :investigationType AND i.markedDeleted = 'N' " +
    //            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
    //            " AND ia.markedDeleted = 'N' AND i.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y' AND "+
    //
    //            " (i.visitId = :visitId OR  :visitId IS NULL) AND" +
    //            " (i.invType.name = :invType  OR :invType IS NULL) AND " +
    //            " (i.invAbstract LIKE :invAbstract OR :invAbstract IS NULL) AND" +
    //            " (i.grantId = :grantId OR :grantId IS NULL) AND" +
    //            " (i.title = :title OR :title IS NULL) AND" +
    //            " (i.bcatInvStr = :bcatInvStr OR :bcatInvStr IS NULL) AND " +
    //            " (i.invNumber = :invNumber  OR :invNumber IS NULL) " +
    //
    //            " AND i.instrument.name IN(:instrument)  AND i.instrument.markedDeleted = 'N' "+ //expand IN, remove this if instrument null
    //
    //            " AND EXISTS (SELECT sample FROM i.sampleCollection sample WHERE sample.name LIKE :sampleName AND " +
    //            "sample.markedDeleted = 'N') "+//iterate, remove if no sample is null
    //
    //            " AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.keywordPK.name LIKE :keyword AND " +
    //            " kw.markedDeleted = 'N')  "+ //iterate, remove if no keyword is null
    //
    //            " AND EXISTS ( SELECT inv FROM i.investigatorCollection inv WHERE " +
    //            "LOWER(inv.facilityUser.lastName) LIKE :surname AND inv.markedDeleted = 'N')  "+ //iterate, remove this if instrument null
    //
    //            " AND EXISTS (SELECT df FROM Datafile df, IcatAuthorisation iadf3 WHERE " +
    //            " df.id = iadf3.elementId AND iadf3.elementType = :dataFileType AND df.markedDeleted = 'N' " +
    //            " AND (iadf3.userId = :userId OR iadf3.userId = 'ANY')" +
    //            " AND iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND iadf3.role.actionCanSelect = 'Y' " +
    //            " AND df.dataset.investigation = i AND (df.datafileCreateTime > :lowerTime OR :lowerTime IS NULL AND df.datafileCreateTime < :upperTime OR :upperTime IS NULL) AND " +
    //            " df.markedDeleted = 'N' AND (df.name = :datafileName OR :datafileName IS NULL))  " + //remove if all are null
    //
    //            " AND EXISTS (SELECT dfp FROM DatafileParameter dfp, IcatAuthorisation ia2 " +
    //            " WHERE dfp.datafile.id = ia2.elementId AND ia2.elementType = :dataFileType AND dfp.markedDeleted = 'N' " +
    //            " AND (ia2.userId = :userId OR ia2.userId = 'ANY')" +
    //            " AND ia2.markedDeleted = 'N' AND dfp.datafile.markedDeleted = 'N' AND ia2.role.actionCanSelect = 'Y' AND dfp.datafile.dataset.investigation = i AND dfp.numericValue BETWEEN :lower AND :upper AND " +
    //            " dfp.datafileParameterPK.name = 'run_number' AND dfp.markedDeleted = 'N')"; //remove this if run number null
    
    public static final String ADVANCED_SEARCH_JPQL_START = LIST_ALL_USERS_INVESTIGATIONS_JPQL +
            " AND (i.visitId = :visitId OR  :visitId IS NULL) AND" +
            " (i.invType.name = :invType  OR :invType IS NULL) AND " +
            " (i.invAbstract LIKE :invAbstract OR :invAbstract IS NULL) AND" +
            " (i.grantId = :grantId OR :grantId IS NULL) AND" +
            " (i.title LIKE :invTitle OR :invTitle IS NULL) AND" +
            " (i.bcatInvStr LIKE :bcatInvStr OR :bcatInvStr IS NULL) AND " +
            " (i.invNumber = :invNumber  OR :invNumber IS NULL) AND "+
            " (((i.invStartDate BETWEEN :lowerTime AND :upperTime) OR (:lowerTime IS NULL)) OR " +
            " ((i.invEndDate BETWEEN :lowerTime AND :upperTime) OR (:upperTime IS NULL)))";
    
    //public static final String ADVANCED_SEARCH_JPQL_INSTRUMENT =  " AND i.instrument.name IN(:instrument)  AND i.instrument.markedDeleted = 'N' ";//expand IN, remove this if instrument null
    
    public static final String ADVANCED_SEARCH_JPQL_DATAFILE = " AND EXISTS (SELECT DISTINCT df FROM Datafile df, IcatAuthorisation iadf3 WHERE " +
            " df.dataset = i.datasetCollection AND " +
            "  df.name OPERATION :datafileName AND " + //remove if all are null
            " iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND df.dataset.markedDeleted = 'N' AND " +           
            " (df.dataset.id = iadf3.elementId AND iadf3.elementType = :dataSetType " +
            " AND (iadf3.userId = :userId OR iadf3.userId = 'ANY') " +
            " AND iadf3.role.actionCanSelect = 'Y'))";
    
     public static final String ADVANCED_SEARCH_JPQL_DATAFILE_CASE_INSENSITIVE = " AND EXISTS (SELECT DISTINCT df FROM Datafile df, IcatAuthorisation iadf3 WHERE " +            
            " df.dataset = i.datasetCollection AND " +
            "  LOWER(df.name) OPERATION :datafileName  AND  " + //remove if all are null
            " iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND df.dataset.markedDeleted = 'N' AND " +
            " (df.dataset.id = iadf3.elementId AND iadf3.elementType = :dataSetType " +
            " AND (iadf3.userId = :userId OR iadf3.userId = 'ANY') " +
            " AND iadf3.role.actionCanSelect = 'Y'))";
    
        public static final String ADVANCED_SEARCH_JPQL_DATAFILE_PARAMETER = " AND EXISTS (SELECT dfp.datafileParameterPK.datafileId FROM DatafileParameter dfp,  IcatAuthorisation iadf4 WHERE " +
            " dfp.datafile.dataset = i.datasetCollection AND dfp.numericValue BETWEEN :lower AND :upper AND " +
            " dfp.datafileParameterPK.name = 'run_number' AND dfp.markedDeleted = 'N' AND " + //remove this if run number null"
            " iadf4.markedDeleted = 'N' AND dfp.datafile.markedDeleted = 'N' AND dfp.datafile.dataset.markedDeleted = 'N' AND " +            
            " (dfp.datafile.dataset.id = iadf4.elementId AND iadf4.elementType = :dataSetType " +
            " AND (iadf4.userId = :userId OR iadf4.userId = 'ANY') " +
            " AND iadf4.role.actionCanSelect = 'Y'))";
   
   
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * All the investigations that a user can view.  Ie their ones and the global ones (at the moment
     * these are ones with no ionvestigators
     *
     */
    public static final String INVESTIGATIONS_BY_USER = "Investigation.findByUser";
    public static final String INVESTIGATIONS_BY_USER_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL;
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * Data files by instrument and run number
     *
     */
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumberNative";
    public static final String DATAFILE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumber";
    
//     String QUERY = "SELECT DISTINCT i from Datafile i , DatafileParameter dfp,  IcatAuthorisation ia, FacilityInstrumentScientist fis " +
//                "WHERE  " +
//                "((:userId = 'SUPER_USER') OR (:userId = fis.facilityInstrumentScientistPK.federalId AND  fis.facilityInstrumentScientistPK.instrumentName = i.dataset.investigation.instrument AND fis.markedDeleted = 'N') " +
//                "OR (i.id = ia.elementId AND ia.elementType = :objectType  " +
//                "AND (ia.userId = :userId OR ia.userId = 'ANY') AND ia.markedDeleted = 'N' " +
//                "AND ia.role.actionCanSelect = 'Y')) AND i = dfp.datafile " +
//                "AND i.markedDeleted = 'N'  " +
//                "AND i.dataset.investigation.instrument IN (:instrument1, :instrument2) " +
//                "AND ((dfp.numericValue BETWEEN :lower AND :upper)) " +
//                "AND (dfp.datafileParameterPK.name = 'run_number') " +
//                "AND dfp.markedDeleted = 'N' AND dfp.markedDeleted = 'N' " +
//                "AND i.dataset.markedDeleted = 'N'";
     
    public static final String DATAFILE_BY_INSTRUMENT_AND_RUN_NUMBER_JPQL_START = RETURN_ALL_DATAFILES_JPQL +", DatafileParameter dfp "+ QUERY_USERS_DATAFILES_JPQL;
    
    public static final String DATAFILE_BY_INSTRUMENT_AND_RUN_NUMBER_JPQL_END =
                " i = dfp.datafile AND i.markedDeleted = 'N'  " +                
                "AND ((dfp.numericValue BETWEEN :lower AND :upper)) " +
                "AND (dfp.datafileParameterPK.name = 'run_number') " +
                "AND dfp.markedDeleted = 'N' AND dfp.markedDeleted = 'N' " +
                "AND i.dataset.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Find all keywords
     *
     */
    public static final String ALLKEYWORDS = "Investigation.getAllKeywords";
    public static final String ALLKEYWORDS_NATIVE_ALPHA_NUMERIC = "Investigation.getAllKeywordsNativeAplhaNumeric";
    public static final String ALLKEYWORDS_NATIVE_ALPHA = "Investigation.getAllKeywordsNativeAlpha";
    public static final String ALLKEYWORDS_JPQL = "SELECT DISTINCT k.keywordPK.name FROM Keyword k WHERE k.markedDeleted = 'N'";
    
    //TODO these are ORACLE queries only
    //all alpha numeric
    public static final String ALLKEYWORDS_ALPHA_NUMERIC_SQL = "SELECT DISTINCT NAME FROM keyword WHERE regexp_like(NAME,'^[[:alnum:]]*$') AND DELETED = 'N'";
    //all alpha
    public static final String ALLKEYWORDS_ALPHA_SQL = "SELECT DISTINCT NAME FROM keyword WHERE regexp_like(NAME,'^[[:alpha:]]*$') AND DELETED = 'N'";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Find all keywords for user
     */
    public static final String KEYWORDS_FOR_USER = "Keywords.getAllKeywordsForUser";
    public static final String KEYWORDS_FOR_USER_JPQL = "SELECT DISTINCT k.keywordPK.name from Keyword k, IcatAuthorisation ia WHERE" +
            " k.investigation.id = ia.elementId AND ia.elementType = :objectType AND ia.markedDeleted = 'N'" +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND (k.keywordPK.name LIKE :startKeyword OR :startKeyword IS NULL) AND k.markedDeleted = 'N'";// ORDER BY k.keywordPK.name";
    
    public static final String KEYWORDS_FOR_USER_ALPHA = "Keywords.getAllKeywordsForUserAlpha";
    public static final String KEYWORDS_FOR_USER_ALPHA_SQL = "SELECT DISTINCT t0.NAME FROM KEYWORD t0, ICAT_AUTHORISATION t2, INVESTIGATION t1 " +
            "WHERE ((((((((t1.ID = t2.ELEMENT_ID) AND (t2.ELEMENT_TYPE = 'INVESTIGATION')) AND (t2.DELETED = 'N')) AND ((t2.USER_ID = ?userId) OR (t2.USER_ID = 'ANY'))) AND (t2.DELETED = 'N')) AND ( (regexp_like(t0.NAME,'^[[:alpha:]]*$')) AND ((t0.NAME LIKE ?startKeyword) OR (?startKeyword IS NULL)) ) ) AND (t0.DELETED = 'N')) AND (t1.ID = t0.INVESTIGATION_ID))";
    
    public static final String KEYWORDS_FOR_USER_ALPHA_NUMERIC = "Keywords.getAllKeywordsForUserAlphaNumeric";
    public static final String KEYWORDS_FOR_USER_ALPHA_NUMERIC_SQL = "SELECT DISTINCT t0.NAME FROM KEYWORD t0, ICAT_AUTHORISATION t2, INVESTIGATION t1 " +
            "WHERE ((((((((t1.ID = t2.ELEMENT_ID) AND (t2.ELEMENT_TYPE = 'INVESTIGATION')) AND (t2.DELETED = 'N')) AND ((t2.USER_ID = ?userId) OR (t2.USER_ID = 'ANY'))) AND (t2.DELETED = 'N')) AND ( (regexp_like(t0.NAME,'^[[:alnum:]]*$')) AND ((t0.NAME LIKE ?startKeyword) OR (?startKeyword IS NULL)) ) ) AND (t0.DELETED = 'N')) AND (t1.ID = t0.INVESTIGATION_ID))";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Find all sample list,
     *
     */
    public static final String SAMPLES_BY_NAME = "Sample.findBySampleName";
    public static final String SAMPLES_BY_NAME_JPQL = "SELECT s FROM Sample s WHERE s.name LIKE :name AND s.markedDeleted = 'N'";
    //public static final String SAMPLES_BY_NAME_JPQL = "SELECT DISTINCT s from Sample s, IcatAuthorisation ia WHERE" +
          //  " s.investigationId.id = ia.elementId AND ia.elementType = :objectType AND ia.markedDeleted = 'N'" +
          //  " AND (:userId = '"+SUPER_USER+"' OR  ia.userId = :userId OR ia.userId = 'ANY')" +
          //  " AND ia.markedDeleted = 'N' AND s.name LIKE :name AND s.markedDeleted = 'N'";
    
    /**
     * Find all datasets by sample id
     *
     */
    public static final String DATASETS_BY_SAMPLES = "Sample.findByDataset";
    public static final String DATASETS_BY_SAMPLES_JPQL = "SELECT ds FROM Dataset ds WHERE ds.sampleId = :sampleId";
    
    
    /**
     * Find all instruments list,
     *
     */
    public static final String ALL_INSTRUMENTS = "Instrument.listAll";
    public static final String ALL_INSTRUMENTS_JPQL = "SELECT DISTINCT i.name FROM Instrument i WHERE i.markedDeleted = 'N'";

    /**
     * Find all instruments list,
     */
    public static final String INSTRUMENTS = "Instrument.list";
    public static final String INSTRUMENTS_JPQL = "SELECT DISTINCT i FROM Instrument i WHERE i.markedDeleted = 'N'";

    /**
     * Find all FacilityCycles list,
     *
     */
    public static final String ALL_FACILITYCYCLES = "FacilityCycle.listAll";
    public static final String ALL_FACILITYCYCLES_JPQL = "SELECT DISTINCT f FROM FacilityCycle f WHERE f.markedDeleted = 'N'";

    /**
     * Find parameters by name and units
     */
    public static final String PARAMETER_SEARCH_BY_NAME_UNITS = "ParameterSearch.findByUnits";
    public static final String PARAMETER_SEARCH_BY_NAME_UNITS_JPQL = "SELECT p FROM Parameter p WHERE " +
            "lower(p.parameterPK.name) LIKE :name " +
            "AND lower(p.parameterPK.units) LIKE :units AND p.markedDeleted = 'N'";
    /**
     * Find parameters by name
     */
    public static final String PARAMETER_SEARCH_BY_NAME = "ParameterSearch.findByName";
    public static final String PARAMETER_SEARCH_BY_NAME_JPQL = "SELECT p FROM Parameter p WHERE " +
            "lower(p.parameterPK.name) LIKE :name " +
            "AND p.markedDeleted = 'N'";
    /**
     * Find parameters by units
     */
    public static final String PARAMETER_SEARCH_BY_UNITS = "ParameterSearch.findByEagerUnits";
    public static final String PARAMETER_SEARCH_BY_UNITS_JPQL = "SELECT p FROM Parameter p WHERE " +
            "lower(p.parameterPK.units) LIKE :units AND p.markedDeleted = 'N'";

    /**
     * Find parameters by name and units
     */
    public static final String PARAMETER_SEARCH_BY_NAME_UNITS_SENSITIVE = "ParameterSearch.findByUnitsSensitive";
    public static final String PARAMETER_SEARCH_BY_NAME_UNITS_JPQL_SENSITIVE = "SELECT p FROM Parameter p WHERE " +
            "p.parameterPK.name LIKE :name " +
            "AND p.parameterPK.units LIKE :units AND p.markedDeleted = 'N'";
    /**
     * Find parameters by name
     */
    public static final String PARAMETER_SEARCH_BY_NAME_SENSITIVE = "ParameterSearch.findByNameSensitive";
    public static final String PARAMETER_SEARCH_BY_NAME_JPQL_SENSITIVE = "SELECT p FROM Parameter p WHERE " +
            "p.parameterPK.name LIKE :name " +
            "AND p.markedDeleted = 'N'";
    /**
     * Find parameters by units
     */
    public static final String PARAMETER_SEARCH_BY_UNITS_SENSITIVE = "ParameterSearch.findByEagerUnitsSensitive";
    public static final String PARAMETER_SEARCH_BY_UNITS_JPQL_SENSITIVE = "SELECT p FROM Parameter p WHERE " +
            "p.parameterPK.units LIKE :units AND p.markedDeleted = 'N'";

    /**
     * Find all investigation types,
     *
     */
    public static final String ALL_INVESTIGATION_TYPES = "InvestigationType.listAll";
    public static final String ALL_INVESTIGATION_TYPES_JPQL = "SELECT DISTINCT i.name FROM InvestigationType i WHERE i.markedDeleted = 'N'";
    
    /**
     * Find all roles list,
     *
     */
    public static final String ALL_ROLES = "IcatRole.listAll";
    public static final String ALL_ROLES_JPQL = "SELECT DISTINCT i FROM IcatRole i WHERE i.markedDeleted = 'N'";
    
    /**
     * Find all parameters list,
     *
     */
    public static final String ALL_PARAMETERS = "Parameter.listAll";
    public static final String ALL_PARAMETERS_JPQL = "SELECT DISTINCT p FROM Parameter p WHERE p.markedDeleted = 'N'";
    
    
    /**
     * Find all dataset status list,
     *
     */
    public static final String ALL_DATASET_STATUS = "DatasetStatus.listAll";
    public static final String ALL_DATASET_STATUS_JPQL = "SELECT DISTINCT ds.name FROM DatasetStatus ds WHERE ds.markedDeleted = 'N'";
    
    /**
     * Find all dataset type list,
     *
     */
    public static final String ALL_DATASET_TYPE = "DatasetType.listAll";
    public static final String ALL_DATASET_TYPE_JPQL = "SELECT DISTINCT ds.name FROM DatasetType ds WHERE ds.markedDeleted = 'N'";
    
    /**
     * Find all datafile format list,
     *
     */
    public static final String ALL_DATAFILE_FORMAT = "DatafileFormat.listAll";
    public static final String ALL_DATAFILE_FORMAT_JPQL = "SELECT DISTINCT dff FROM DatafileFormat dff WHERE dff.markedDeleted = 'N'";
    
    /**
     * Find all dataset type list,
     *
     */
    public static final String DATASET_FINDBY_UNIQUE = "Dataset.findbyUnique";
    public static final String DATASET_FINDBY_UNIQUE_JPQL = "SELECT d FROM Dataset d WHERE " +
            "(d.sampleId = :sampleId OR d.sampleId IS NULL) AND " +
            "(d.name = :name OR d.name IS NULL) AND (d.investigation = :investigation OR d.investigation IS NULL) AND " +
            "(d.datasetType = :datasetType OR d.datasetType IS NULL)";
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Find ICAT AUTHORISATION by UNIQUE KEY (user, element id and type, parent type and id
     *
     * find if inseted is unique
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY = "IcatAuthorisation.findByUniqueKey";
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId AND " +
            "i.userId = :userId AND i.parentElementType = :parentElementType AND " +
            "i.parentElementId = :parentElementId";
    
    // Find unuqiue child record
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_CREATE = "IcatAuthorisation.findByUniqueKeyCreate";
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_CREATE_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId IS NULL AND " +
            "i.userId = :userId AND i.parentElementType = :parentElementType AND " +
            "i.parentElementId = :parentElementId";
    
    
    /**
     * Find ICAT AUTHORISATION by element id, used to remove all when inv, ds,df is removed
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_ELEMENTID = "IcatAuthorisation.findByElementIdOnly";
    public static final String ICAT_AUTHORISATION_FINDBY_ELEMENTID_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId";
    
    /**
     * Find ICAT AUTHORISATION by user and element id, used to get all icat auths for a element type
     *
     */
    public static final String ICAT_AUTHORISATION_FINDALL_FOR_ELEMENTTYPE = "IcatAuthorisation.findAllForElementType";
    public static final String ICAT_AUTHORISATION_FINDALL_FOR_ELEMENTTYPE_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId AND " +
            "(i.userId = :userId OR :userId IS NULL) AND i.markedDeleted = 'N'";

    /**
     * Find ICAT AUTHORISATION by user and element id and type
     */
    public static final String ICAT_AUTHORISATION_FINDBY_ELEMENTID_ELEMENTTYPE_USERID ="IcatAuthorisation.findByElementIdAndElementTypeAndUserId";
    public static final String ICAT_AUTHORISATION_FINDBY_ELEMENTID_ELEMENTTYPE_USERID_JPQL="SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId AND i.userId = :userId";
    
    /**
     * Find ICAT AUTHORISATION by UNIQUE KEY (user, element id and type, parent type and id and not deleted
     *
     * Finds a auth for creating a ds, or ds
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_CREATE_DATAFILE_DATASET = "IcatAuthorisation.findByCreateDFDS";
    public static final String ICAT_AUTHORISATION_FINDBY_CREATE_DATAFILE_DATASET_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId IS NULL AND " +
            "i.userId = :userId AND i.parentElementType = :parentElementType AND " +
            "i.parentElementId = :parentElementId AND i.markedDeleted = 'N'";
    
    /**
     * Find ICAT AUTHORISATION by UNIQUE KEY (user, element id and type, parent type and id and not deleted
     *
     * Finds a auth for creating a inv
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_CREATE_INVESTIGATION = "IcatAuthorisation.findByCreateINV";
    public static final String ICAT_AUTHORISATION_FINDBY_CREATE_INVESTIGATION_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId IS NULL AND " +
            "i.userId = :userId AND i.parentElementType IS NULL AND " +
            "i.parentElementId IS NULL AND i.markedDeleted = 'N'";
    
    /**
     * Find ICAT AUTHORISATION by UNIQUE KEY (user, element id and type, parent type and id and not deleted
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_INVESTIGATION = "IcatAuthorisation.findByInvestigation";
    public static final String ICAT_AUTHORISATION_FINDBY_INVESTIGATION_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId AND " +
            "i.userId = :userId AND i.parentElementType IS NULL AND " +
            "i.parentElementId IS NULL AND i.markedDeleted = 'N'";
    
    /**
     * Find ICAT AUTHORISATION by UNIQUE KEY (user, element id and type, parent type and id and not deleted
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_DATAFILE_DATASET = "IcatAuthorisation.findByDFDS";
    public static final String ICAT_AUTHORISATION_FINDBY_DATAFILE_DATASET_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId AND " +
            "i.userId = :userId AND i.parentElementType = :parentElementType AND " +
            "i.parentElementId = :parentElementId AND i.markedDeleted = 'N'";


    /**
     * Find dataset by name with authorisation
     */
    public static final String DATASET_FINDBY_NAME_NOTDELETED = "Dataset.findByNameNotDeleted";
    public static final String DATASET_FINDBY_NAME_NOTDELETED_JPQL = "SELECT i FROM Dataset i WHERE i.name = :name and i.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    //////////////////////// Deleted stuff  ///////////////////////////////////////////
    /**
     * Find all deleted datasets
     *
     */
    public static final String LIST_MY_DELETED_DATASETS = "Dataset.ListAllDeleted";
    public static final String LIST_MY_DELETED_DATASETS_JPQL = "SELECT DISTINCT ds from Dataset ds, IcatAuthorisation ia WHERE" +
            " ds.id = ia.elementId AND ia.elementType = :objectType AND ds.markedDeleted = 'Y' " +
            " AND ia.userId = :userId " +
            " AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y'";
    
    /**
     * Find all deleted datasets
     *
     */
    public static final String LIST_MY_DELETED_DATAFILES = "Datafile.ListAllDeleted";
    public static final String LIST_MY_DELETED_DATAFILE_JPQL = "SELECT DISTINCT df from Datafile df, IcatAuthorisation ia WHERE" +
            " df.id = ia.elementId AND ia.elementType = :objectType AND df.markedDeleted = 'Y' " +
            " AND ia.userId = :userId " +
            " AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y'";
    
    /**
     * Max number of returned items in a collection
     */
    public static int MAX_QUERY_RESULTSET = 400;

    /** Number of results is limited to MAX_QUERY_RESULTSET */
    public static final int NO_PAGINATION = -1;
    /** Number of results is unlimited */
    public static final int NO_LIMITED_RESULTS = -99;
    /** Common name for JPQL setence select object*/
    public static final String PARAM_NAME_JPQL = "i";
    /** Common name for JPQL setence Datafile object*/
    public static final String DATAFILE_NAME = "df";
    /** Common name for JPQL setence Dataset object*/
    public static final String DATASET_NAME = "ds";
    /** Common name for JPQL setence Sample object*/
    public static final String SAMPLE_NAME = "sample";
    /** Common name for JPQL setence Investigator object*/
    public static final String INVESTIGATOR_NAME = "invtor";
    /** Common name for JPQL setence Keyword object*/
    public static String KEYWORD_NAME = "k";
    /** No restriction conditions */
    public static final RestrictionCondition NO_RESTRICTION = null;
    /** DateTime string format */
    public static final String sqlDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    /** Date format tansform */
    public static final DateFormat dateFormat = new SimpleDateFormat(sqlDateTimeFormat);
    /** Indicates a empty condition */
    public static String EMPTY_CONDITION = "empty";
}
