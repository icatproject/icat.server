/*
 * Queries.java
 *
 * Created on 16 November 2006, 11:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 *
 * @author gjd37
 */
public class Queries {
    
    
    /////////////////////////////////////    These are to be added together to form queries  ///////////////////////
    //Returns all of investigation
    public static final String RETURN_ALL_INVESTIGATIONS_JPQL = "SELECT DISTINCT i from Investigation i ";
    //Returns investigation id
    public static final String RETURN_ALL_INVESTIGATION_IDS_JPQL = "SELECT DISTINCT i.id from Investigation i ";
    
    public static final String QUERY_USERS_INVESTIGATIONS_JPQL = ", IcatAuthorisation ia WHERE" +
            " i.id = ia.elementId AND ia.elementType = :investigationType AND i.markedDeleted = 'N' " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND ia.role.actionSelect = 'Y'";
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
    public static final String LIST_ALL_USERS_INVESTIGATIONS_SQL = "SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_AQUIRED, t0.DELETED, t0.INSTRUMENT, t0.FACILITY_CYCLE, t0.INV_TYPE " +
            "FROM INVESTIGATION t0, ICAT_AUTHORISATION t1, ICAT_ROLE t2 WHERE " +
            "(((((((t0.ID = t1.ELEMENT_ID) AND (t1.ELEMENT_TYPE = 'INVESTIGATION')) AND (t0.DELETED = 'N')) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND (t2.ROLE = t1.ROLE))";
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This searches the investigations that the user can view by a suranme
     *
     */
    public static final String INVESTIGATION_LIST_BY_SURNAME = "Investigation.findByUserSurname";
    public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +
            " AND i.investigatorCollection.facilityUser.lastName LIKE :surname AND " +
            "i.investigatorCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This searches the investigations that the user can view by one keyword
     * TODO needs to be multipe keywords
     */
    public static final String INVESTIGATION_LIST_BY_KEYWORD = "Investigation.findByKewordNative";
    public static final String INVESTIGATION_LIST_BY_KEYWORD_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL + " AND i.keywordCollection.keywordPK.name LIKE :keyword AND i.keywordCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Same but returning investigation Ids
     */
    public static final String INVESTIGATION_LIST_BY_KEYWORD_RTN_ID = "Investigation.findByKewordRtnIdNative";
    public static final String INVESTIGATION_LIST_BY_KEYWORD_RTN_ID_JPQL = RETURN_ALL_INVESTIGATION_IDS_JPQL +
            QUERY_USERS_INVESTIGATIONS_JPQL +" AND i.keywordCollection.keywordPK.name LIKE :keyword AND i.keywordCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Search my keywords (AND and OR and fuzzy)
     */
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS = "Investigation.findByKewordsNative";
    
    // Query =  LIST_ALL_USERS_INVESTIGATIONS_JPQL + " AND (i.keywordCollection.keywordPK.name LIKE '%or%' AND i.keywordCollection.keywordPK.name LIKE '%orbita%') AND i.keywordCollection.markedDeleted = 'N'";;
    // SQL to generate from Query above: INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL = SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_ACQUIRED, t0.DELETED, t0.FACILITY_CYCLE, t0.INSTRUMENT, t0.INV_TYPE, t0.FACILITY FROM KEYWORD t5, KEYWORD t3, ICAT_ROLE t2, ICAT_AUTHORISATION t1, INVESTIGATION t0 WHERE
    //           (((((((((t0.ID = t1.ELEMENT_ID) AND (t1.ELEMENT_TYPE = ?)) AND (t0.DELETED = ?)) AND ((t1.USER_ID = ?) OR (t1.USER_ID = ?))) AND (t1.DELETED = ?)) AND (t2.ACTION_SELECT = ?)) AND ((t3.NAME LIKE ?) AND (t4.NAME LIKE ?))) AND (t5.DELETED = ?)) AND ((((t2.ROLE = t1.ROLE) AND (t3.INVESTIGATION_ID = t0.ID)) AND (t4.INVESTIGATION_ID = t0.ID))))
    
    public static final String INVESTIGATION_LIST_BY_KEYWORDS_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +" AND ";
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL_START = "SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_ACQUIRED, t0.DELETED, t0.FACILITY_CYCLE, t0.INSTRUMENT, t0.INV_TYPE, t0.FACILITY FROM  KEYWORD t3, ICAT_ROLE t2, ICAT_AUTHORISATION t1, INVESTIGATION t0 WHERE "+
            " (((((((((t0.ID = t1.ELEMENT_ID) AND (t1.ELEMENT_TYPE = 'INVESTIGATION')) AND (t0.DELETED = 'N')) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND ("; //insert this programatically  (t3.NAME LIKE ?keyword1) OR (t3.NAME LIKE ?keyword2) OR (t3.NAME LIKE ?keyword3)
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL_END = ")) AND (t3.DELETED = 'N')) AND ((((t2.ROLE = t1.ROLE) AND (t3.INVESTIGATION_ID = t0.ID)) AND (t3.INVESTIGATION_ID = t0.ID))))";
    
    // QUERY = INVESTIGATION_LIST_BY_KEYWORDS_JPQL_NOSECURITY = "SELECT i from Investigation i WHERE i.markedDeleted = 'N' AND (i.keywordCollection.keywordPK.name LIKE '%shull%' AND i.keywordCollection.keywordPK.name LIKE '%ccw%') AND i.keywordCollection.markedDeleted = 'N'";
    // SQL to generate from Query above: INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL_NOSECURITY = SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_ACQUIRED, t0.DELETED, t0.FACILITY_CYCLE, t0.INSTRUMENT, t0.INV_TYPE, t0.FACILITY FROM KEYWORD t3, KEYWORD t2, KEYWORD t1, INVESTIGATION t0 WHERE
    //          ((((t0.DELETED = ?) AND ( (t1.NAME LIKE ?) AND (t2.NAME LIKE ?) )) AND (t3.DELETED = ?)) AND (((t1.INVESTIGATION_ID = t0.ID) AND (t2.INVESTIGATION_ID = t0.ID)) AND (t3.INVESTIGATION_ID = t0.ID)))
    
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL_NOSECURITY_START = "SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_ACQUIRED, t0.DELETED, t0.FACILITY_CYCLE, t0.INSTRUMENT, t0.INV_TYPE, t0.FACILITY FROM KEYWORD t3, INVESTIGATION t0 WHERE "+
            "((((t0.DELETED = 'N') AND ( ";  //insert this programatically  (t3.NAME LIKE ?keyword1) OR (t3.NAME LIKE ?keyword2) OR (t3.NAME LIKE ?keyword3)
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL_NOSECURITY_END =  ")) AND (t3.DELETED = 'N')) AND (((t3.INVESTIGATION_ID = t0.ID) AND (t3.INVESTIGATION_ID = t0.ID)) AND (t3.INVESTIGATION_ID = t0.ID)))";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This searches the investigations that the user can view by userid
     *
     */
    public static final String INVESTIGATION_LIST_BY_USERID = "Investigation.findByUserID";
    public static final String INVESTIGATION_LIST_BY_USERID_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +" AND i.investigatorCollection.facilityUser.federalId LIKE :federalId AND i.investigatorCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This is the search for the advanced search
     */
    public static final String ADVANCED_SEARCH = "Investigation.findByAdvancedSearch";
    
    public static final String ADVANCED_SEARCH_SQL_1 = LIST_ALL_USERS_INVESTIGATIONS_SQL +
            "AND (inv_number = ?inv_number OR ?inv_number IS NULL) "+
            "AND (bcat_inv_str = ?bcat_inv_str OR ?bcat_inv_str IS NULL) "+
            "AND (Lower(title) LIKE '%'||Lower(?inv_title)||'%' OR ?inv_title IS NULL) ";
    
    public static final String ADVANCED_SEARCH_SQL_BASE = "";
    
            /*"SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_AQUIRED, t0.DELETED, t0.INSTRUMENT, t0.FACILITY_CYCLE, t0.INV_TYPE
            FROM INVESTIGATION t0, ICAT_AUTHORISATION t1, ICAT_ROLE t2 WHERE
            (((((((t0.ID = t1.ELEMENT_ID) AND (t1.ELEMENT_TYPE = 'INVESTIGATION')) AND (t0.DELETED = 'N')) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND (t2.ROLE = t1.ROLE))
            WHERE  (((t0.ID = t1.INVESTIGATION_ID) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N'))
            AND (inv_number = ? OR ? IS NULL)
            AND (visit_id = ? OR ? IS NULL)
            AND (inv_abstract LIKE ? OR ? IS NULL)
            AND (inv_type = ? OR ? IS NULL)
            AND (grant_id = ? OR ? IS NULL)
            AND (bcat_inv_str = ? OR ? IS NULL)
            AND Lower(title) LIKE '%'||Lower(?)||'%' OR ? IS NULL
             *
            -- if instruments (ie if the instruments parameter is passed in, non-null)
            AND instrument IN(?,?,?,etc)
             
            -- if "more params" (ie if there are other parameters passed in
            --                   which haven't been catered for yet)
            AND id IN(
             
            -- if investigators (surnames)
              SELECT i.investigation_id
              FROM investigator i, facility_user fu
              WHERE i.facility_user_id = fu.facility_user_id
              AND (InStr(Lower(fu.last_name),Lower(?)) > 0)
             
            -- if "more params"
              INTERSECT
             
            -- if keywords ("AND" search)
              SELECT investigation_id
              FROM keyword
              WHERE Lower(name) LIKE '%'||Lower(?)||'%'
              AND Lower(name) LIKE '%'||Lower(?)||'%'
              AND Lower(name) LIKE '%'||Lower(?)||'%'
              etc
             
            -- if "more params"
              INTERSECT
             
            -- if sample
              SELECT investigation_id
              FROM sample
              WHERE (InStr(Lower(name),Lower(?)) > 0)
             
            -- if "more params"
              INTERSECT
             
            -- if datafile and NOT datafile_parameter
              SELECT ds.investigation_id
              FROM dataset ds, DATAFILE df
              WHERE df.dataset_id = ds.id
              AND (InStr(Lower(df.name),Lower(?)) > 0 OR ? IS NULL)
              AND (( (df.datafile_create_time >= ?date1 AND df.datafile_create_time < (?date2-1)))
                   OR df.datafile_create_time IS NULL)
            /* if the run number is to be stored in the dataset or datafile table then...
              AND (run_number between ?run1 and ?run2 or ?run1 is null)
             */
    
            /*-- if datafile parameter
              SELECT ds.investigation_id
              FROM dataset ds, DATAFILE df, datafile_parameter dfp
              WHERE df.dataset_id = ds.id
              AND (InStr(Lower(df.name),Lower(?)) > 0 OR ? IS NULL)
              AND (( (df.datafile_create_time >= ?date1 AND df.datafile_create_time < (?date2-1)))
                   OR df.datafile_create_time IS NULL)
              AND dfp.datafile_id = df.id
              AND dfp.NAME = 'run_number'
              AND dfp.numeric_value BETWEEN ?1 AND ?2
             
             
            -- if investigators OR keywords OR sample OR datafile or datafile_parameter
            )"";*/
    
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
     * All the investigations of the user. (ie an investigator of)
     *
     */
    public static final String INVESTIGATIONS_FOR_USER = "Investigation.findOfUser";
    public static final String INVESTIGATIONS_FOR_USER_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL+ " AND i.investigatorCollection.facilityUser.federalId = :userId AND i.investigatorCollection.markedDeleted = 'N'";
    
    public static final String INVESTIGATIONS_FOR_USER_RTN_ID = "Investigation.findOfUser";
    public static final String INVESTIGATIONS_FOR_USER_RTN_ID_JPQL = LIST_ALL_USERS_INVESTIGATION_IDS_JPQL +
            " AND ia.markedDeleted = 'N' AND i.investigatorCollection.facilityUser.federalId = :userId AND i.investigatorCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * Data files by instrument and run number
     *
     */
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumberNative";
    public static final String DATAFILE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumber";
    
    //this is the SQL that is going to be dynamically generated
    private static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_SQL_BASE = "SELECT d.ID, d.COMMAND, d.CHECKSUM, d.DESCRIPTION, d.SIGNATURE, d.DATAFILE_VERSION_COMMENT,"+
            "d.MOD_TIME, d.DATAFILE_CREATE_TIME, d.MOD_ID, d.FILE_SIZE, d.LOCATION, d.DATAFILE_MODIFY_TIME, "+
            "d.DATAFILE_VERSION, d.NAME, d.DATASET_ID, d.DATAFILE_FORMAT, d.DATAFILE_FORMAT_VERSION "+
            "FROM DATAFILE d, dataset ds, datafile_parameter dp, "+
            "(select i.id, i.instrument" +
            "  from investigator g, facility_user f, investigation i "+
            "where f.facility_user_id = g.facility_user_id "+
            "and f.federal_id like 'JAMES-JAMES' "+
            "and i.id = g.investigation_id "+
            " UNION ALL "+
            "select id, instrument "+
            "from investigation "+
            "where id not in (select investigation_id from investigator) "+
            ") fed_inv "+
            "WHERE d.dataset_id = ds.id "+
            "AND ds.investigation_id = fed_inv.id "+
            "AND fed_inv.instrument IN('alf','lad') "+
            "AND dp.datafile_id = d.id "+
            "AND dp.NAME = 'run_number' "+
            "AND dp.numeric_value BETWEEN 2620 AND 2631";
    
    //Uses the new partiton DataFile_paramter table, JWH_DEF_PARAM_PARTITIONED
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_SQL_1 = "SELECT d.ID, d.COMMAND, d.CHECKSUM, d.DESCRIPTION, d.SIGNATURE, d.DATAFILE_VERSION_COMMENT,"+
            "d.MOD_TIME, d.DATAFILE_CREATE_TIME, d.MOD_ID, d.FILE_SIZE, d.LOCATION, d.DATAFILE_MODIFY_TIME, "+
            "d.DATAFILE_VERSION, d.NAME, d.DATASET_ID, d.DATAFILE_FORMAT, d.DATAFILE_FORMAT_VERSION "+
            "FROM DATAFILE d, dataset ds, DATAFILE_PARAMETER dp, "+ //using JWH_DEF_PARAM_PARTITIONED instead of DataFile_paramter
            "(select i.id, i.instrument" +
            "  from investigator g, facility_user f, investigation i "+
            "where f.facility_user_id = g.facility_user_id "+
            "and f.federal_id = ?userId "+
            "and i.id = g.investigation_id "+
            " UNION ALL "+
            "select id, instrument "+
            "from investigation "+
            "where id not in (select investigation_id from investigator) "+
            ") fed_inv "+
            "WHERE d.dataset_id = ds.id "+
            "AND ds.investigation_id = fed_inv.id "+
            "AND fed_inv.instrument IN(";  //dynamically adding this here: IN(  'alf','lad'   )
    
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_SQL_2  = ") AND dp.datafile_id = d.id "+
            "AND dp.NAME = 'run_number' "+
            "AND dp.numeric_value BETWEEN ?lower AND ?upper";
    
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
            " k.investigation.id = ia.elementId AND ia.elementType = :investigationType AND ia.markedDeleted = 'N'" +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND (k.keywordPK.name LIKE :startKeyword OR :startKeyword IS NULL) AND k.markedDeleted = 'N' ORDER BY k.keywordPK.name";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Find all instruments list,
     *
     */
    public static final String ALL_INSTRUMENTS = "Instrument.listAll";
    public static final String ALL_INSTRUMENTS_JPQL = "SELECT DISTINCT i FROM Instrument i WHERE i.markedDeleted = 'N'";
    
    /**
     * Find all investigation types,
     *
     */
    public static final String ALL_INVESTIGATION_TYPES = "InvestigationType.listAll";
    public static final String ALL_INVESTIGATION_TYPES_JPQL = "SELECT DISTINCT i FROM InvestigationType i WHERE i.markedDeleted = 'N'";
    
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
    public static final String ALL_DATASET_STATUS_JPQL = "SELECT DISTINCT ds FROM DatasetStatus ds WHERE ds.markedDeleted = 'N'";
    
    /**
     * Find all dataset type list,
     *
     */
    public static final String ALL_DATASET_TYPE = "DatasetType.listAll";
    public static final String ALL_DATASET_TYPE_JPQL = "SELECT DISTINCT ds FROM DatasetType ds WHERE ds.markedDeleted = 'N'";
    
    /**
     * Find all dataset type list,
     *
     */
    public static final String DATASET_FINDBY_UNIQUE = "Dataset.findbyUnique";
    public static final String DATASET_FINDBY_UNIQUE_JPQL = "SELECT d FROM Dataset d WHERE " +
            "(d.sampleId = :sampleId OR d.sampleId IS NULL) AND " +
            "(d.name = :name OR d.name IS NULL) AND (d.investigation = :investigation OR d.investigation IS NULL) AND " +
            "(d.datasetType = :datasetType OR d.datasetType IS NULL)";
    
    /**
     * Find ICAT AUTHORISATION by UNIQUE KEY (user, element id and type, parent type and id
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY = "IcatAuthorisation.findByUniqueKey";
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId AND " +
            "i.userId = :userId AND i.parentElementType = :parentElementType AND " +
            "i.parentElementId = :parentElementId";
    
    /**
     * Find ICAT AUTHORISATION by user and element id
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE = "IcatAuthorisation.findByUnique";
    public static final String ICAT_AUTHORISATION_FINDBY_UNIQUE_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId = :elementId AND " +
            "(i.userId = :userId OR :userId IS NULL) AND i.markedDeleted = 'N'";
    
    /**
     * Find ICAT AUTHORISATION by user and element id null, ie creating a inv
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_NULL_INVESTIGATION = "IcatAuthorisation.findByIdNullInvestigationId";
    public static final String ICAT_AUTHORISATION_FINDBY_NULL_INVESTIGATION_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId IS NULL AND " +
            "i.userId = :userId AND i.markedDeleted = 'N'";
    /**
     * Find ICAT AUTHORISATION by user and element id null, ie creating a  df, ds
     *
     */
    public static final String ICAT_AUTHORISATION_FINDBY_NULL_DATASET_FILE = "IcatAuthorisation.findByIdNullDataset_fileId";
    public static final String ICAT_AUTHORISATION_FINDBY_NULL_DATASET_FILE_JPQL = "SELECT i FROM IcatAuthorisation i WHERE " +
            "i.elementType = :elementType AND i.elementId IS NULL AND " +
            "i.parentElementType = :parentElementType AND " +
            "i.parentElementId = :parentElementId AND " +
            "i.userId = :userId AND i.markedDeleted = 'N'";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    //////////////////////// Deleted stuff  ///////////////////////////////////////////
    /**
     * Find all deleted datasets
     *
     */
    public static final String LIST_MY_DELETED_DATASETS = "Dataset.ListAllDeleted";
    public static final String LIST_MY_DELETED_DATASETS_JPQL = "SELECT DISTINCT ds from Dataset ds, IcatAuthorisation ia WHERE" +
            " ds.id = ia.elementId AND ia.elementType = 'DATASET' AND ds.markedDeleted = 'Y' " +
            " AND ia.userId = :userId " +
            " AND ia.markedDeleted = 'N' AND ia.role.actionSelect = 'Y'";
    
    /**
     * Max number of returned items in a collection
     */
    public static int MAX_QUERY_RESULTSET = 500;
}
