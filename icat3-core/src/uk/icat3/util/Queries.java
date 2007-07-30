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
    
    /**
     * Lists all the investigations without security
     */
    public static final String LIST_ALL_INVESTIGATIONS_JPQL = "SELECT DISTINCT i from Investigation i WHERE ";
    
    /**
     * Lists all the users investigations
     * 
     * icat_authorisation row is not marked deleted, the user id is their userId or ANY and the role has read permission 
     */
    public static final String LIST_ALL_USERS_INVESTIGATIONS_JPQL = "SELECT DISTINCT i from Investigation i, IcatAuthorisation ia WHERE" +
            " i.id = ia.investigationId AND i.markedDeleted = 'N' " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND ia.role.actionSelect = 'Y'";
    /**
     * Lists all the users investigations in SQL
     */
    public static final String LIST_ALL_USERS_INVESTIGATIONS_SQL = "SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_AQUIRED, t0.DELETED, t0.INSTRUMENT, t0.FACILITY_CYCLE, t0.INV_TYPE " +
            "FROM INVESTIGATION t0, ICAT_AUTHORISATION t1, ICAT_ROLE t2 WHERE " +
            "((((((t0.ID = t1.INVESTIGATION_ID) AND (t0.DELETED = 'N')) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND (t2.ROLE = t1.ROLE))";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * This searches the investigations that the user can view by a suranme
     *
     */
    public static final String INVESTIGATION_LIST_BY_SURNAME = "Investigation.findByUserSurname";
    public static final String INVESTIGATION_NATIVE_LIST_BY_SURNAME = "Investigation.findBySurnameNative";
    
    //public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL = "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
    //      "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.LAST_NAME " +
    //    "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE ((t1.FACILITY_USER_ID = ?1) AND (t1.INVESTIGATION_ID = t0.ID) OR (((SELECT COUNT(*) FROM INVESTIGATOR t1 WHERE (t1.INVESTIGATION_ID = t0.ID)) = 0))) " +
    //  "AND t2.LAST_NAME LIKE ?2)";
    
    public static final String INVESTIGATIONS_LIST_BY_SURNAME_JPQL = "SELECT i FROM Investigation i WHERE i.investigatorCollection.facilityUser.federalId  = :userId " +
            "AND i.investigatorCollection.facilityUser.lastName LIKE :surname";
    //faster
    public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL ="SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            // public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL ="SELECT ID " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.LAST_NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?1 AND t0.id = t1.investigation_id UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, NULL AS LAST_NAME  FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE id NOT IN (SELECT investigation_id from investigator)) WHERE LAST_NAME LIKE ?2";
    
    //new for permissions
    public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL + " AND i.investigatorCollection.facilityUser.lastName = :lastName AND i.investigatorCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * This searches the investigations that the user can view by one keyword
     * TODO needs to be multipe keywords
     */
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD = "Investigation.findByKewordNative";
    /*  public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_SQL = "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.NAME " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, c t2 " +
            "WHERE ((t1.FACILITY_USER_ID = ?1) AND (t1.INVESTIGATION_ID = t0.ID) OR (((SELECT COUNT(*) FROM INVESTIGATOR t1 WHERE (t1.INVESTIGATION_ID = t0.ID)) = 0))) " +
            "AND t2.NAME LIKE ?2)";*/
    
    //faster
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_SQL = "SELECT DISTINCT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?1 AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE NAME LIKE ?2";
    
    //new for permissions
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL + " AND i.keywordCollection.keywordPK.name LIKE :keyword AND i.keywordCollection.markedDeleted = 'N'";
    
    /*
     * Same but returning investigation Ids
     */
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID = "Investigation.findByKewordRtnIdNative";
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID_SQL = "SELECT DISTINCT ID " +
            "FROM (SELECT DISTINCT t0.ID, t3.NAME " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?1 AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE NAME LIKE ?2";
    
    //new for permissions
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID_JPQL = "SELECT DISTINCT i.id from Investigation i, IcatAuthorisation ia WHERE" +
            " i.id = ia.investigationId AND i.markedDeleted = 'N' " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND i.keywordCollection.keywordPK.name LIKE :keyword AND i.keywordCollection.markedDeleted = 'N'";
    
    ////by keywrds
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS = "Investigation.findByKewordsNative";
    
    /*public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL = "SELECT DISTINCT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?userId AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE ";
     */
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL = "SELECT DISTINCT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID,  FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?userId AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE ";
    
    //changed to LIKE for fed id search
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL_NOSECURITY = "SELECT DISTINCT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID,  FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id LIKE ?userId AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE ";
    
    //new for permissions
    // TEST public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_JPQL = LIST_ALL_INVESTIGATIONS_JPQL +"AND (i.keywordCollection.keywordPK.name LIKE '%or%' AND i.keywordCollection.keywordPK.name LIKE '%orbita%')";;
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +"AND ";
    //TEST public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_JPQL_NOSECURITY = LIST_ALL_INVESTIGATIONS_JPQL +" (i.keywordCollection.keywordPK.name LIKE '%or%' AND i.keywordCollection.keywordPK.name LIKE '%orbita%')";
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_JPQL_NOSECURITY = LIST_ALL_INVESTIGATIONS_JPQL +" ";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * This searches the investigations that the user can view by userid
     *
     */
    public static final String INVESTIGATION_NATIVE_LIST_BY_USERID = "Investigation.findByUserIDNative";
    public static final String INVESTIGATION_LIST_BY_USERID = "Investigation.findByUserID";
    
    
    /* public static final String INVESTIGATION_NATIVE_LIST_BY_USERID_SQL = "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.FEDERAL_ID " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE ((t1.FACILITY_USER_ID = ?1) AND (t1.INVESTIGATION_ID = t0.ID) OR (((SELECT COUNT(*) FROM INVESTIGATOR t1 WHERE (t1.INVESTIGATION_ID = t0.ID)) = 0)))" +
            " AND t2.FEDERAL_ID LIKE ?2)";*/
    
    // public static final String INVESTIGATION_LIST_BY_USERID_JPQL = "SELECT i FROM Investigation i WHERE i.investigatorCollection.facilityUser.federalId = :userId " +
    //          "AND i.investigatorCollection.investigatorPK.facilityUserId LIKE :userIdSearched";
    
    public static final String INVESTIGATION_NATIVE_LIST_BY_USERID_SQL = "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            // public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL ="SELECT ID " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.FEDERAL_ID  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?1 AND t0.id = t1.investigation_id UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, NULL AS FEDERAL_ID  FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE id NOT IN (SELECT investigation_id from investigator)) WHERE FEDERAL_ID LIKE ?2";
    
    //new for permissions
    public static final String INVESTIGATION_LIST_BY_USERID_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL +" AND i.investigatorCollection.facilityUser.federalId = :federalId AND i.keywordCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * This is the search for the advanced search
     */
    public static final String ADVANCED_SEARCH = "Investigation.findByAdvancedSearch";
    
    public static final String ADVANCED_SEARCH_SQL_1 = "SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, "+
            "t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, "+
            "t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE "+
            "FROM INVESTIGATION t0, ICAT_AUTHORISATION t1, ICAT_ROLE t2 "+
            "WHERE ((((((t0.ID = t1.INVESTIGATION_ID) AND (t0.DELETED = 'N')) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND (t2.ROLE = t1.ROLE)) " +
            "AND (inv_number = ?inv_number OR ?inv_number IS NULL) "+
            "AND (bcat_inv_str = ?bcat_inv_str OR ?bcat_inv_str IS NULL) "+
            "AND (Lower(title) LIKE '%'||Lower(?inv_title)||'%' OR ?inv_title IS NULL) ";
    
    public static final String ADVANCED_SEARCH_SQL_BASE = "";
            /*SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID,
            t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME,
            t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE
            FROM INVESTIGATION t0
            WHERE  (((t0.ID = t1.INVESTIGATION_ID) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N'))" +
            AND (inv_number = ? OR ? IS NULL)
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
    /*
     *
     * All the investigations that a user can view.  Ie their ones and the global ones (at the moment
     * these are ones with no ionvestigators
     *
     */
    public static final String INVESTIGATIONS_BY_USER = "Investigation.findByUser";
    
    //Slow!  and incorrect, OR needs to be a UNION
    // public static final String INVESTIGATIONS_BY_USER_JPQL = "SELECT i FROM Investigation i WHERE" +
    //       " (i.investigatorCollection.facilityUser.federalId = :userId OR i.investigatorCollection IS EMPTY)";
    
    //much faster, second version for JPQL, but JPQL does not have UNION, OR does not work!!!!
    //String INVESTIGATIONS_BY_USER_JPQL = "SELECT count(i) FROM Investigation i  WHERE i.investigatorCollection.investigatorPK.facilityUserId = 'JAMES' UNION i.id NOT IN (SELECT j.investigatorPK.investigationId  FROM Investigator j)";
    
    //Much faster
    public static final String INVESTIGATIONS_BY_USER_SQL = "SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?1 AND t0.id = t1.investigation_id UNION " +
            "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE  FROM INVESTIGATION t0 WHERE id NOT IN (SELECT investigation_id from investigator)";
    
    //new for permissions
    public static final String INVESTIGATIONS_BY_USER_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL;
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     *
     * All the investigations of the user.
     *
     */
    public static final String INVESTIGATIONS_FOR_USER = "Investigation.findOfUser";
    //new for permissions
    public static final String INVESTIGATIONS_FOR_USER_JPQL = LIST_ALL_USERS_INVESTIGATIONS_JPQL+ " AND i.investigatorCollection.facilityUser.federalId = :userId";
    
    public static final String INVESTIGATIONS_FOR_USER_RTN_ID = "Investigation.findOfUser";
    //   public static final String INVESTIGATIONS_FOR_USER_RTN_ID_JPQL = "SELECT i.id FROM Investigation i WHERE" +
    //         " i.investigatorCollection.facilityUser.federalId = :userId";
    
    //new for permissions
    public static final String INVESTIGATIONS_FOR_USER_RTN_ID_JPQL = "SELECT DISTINCT i.id from Investigation i, IcatAuthorisation ia WHERE" +
            " i.id = ia.investigationId AND i.markedDeleted = 'N'" +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND i.investigatorCollection.facilityUser.federalId = :userId AND i.investigatorCollection.markedDeleted = 'N'";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
         /*
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
    
    //new for permissions
    // TEST public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_JPQL_1 = LIST_ALL_USERS_INVESTIGATIONS_JPQL+" " +
    //  "AND d.datasetId.investigationId.instrument.name IN('alf','lad') " +
    // "AND d.datafileParameterCollection.datafileParameterPK.name = 'run_number' " +
    //"AND d.datafileParameterCollection.datafileParameterPK.name BETWEEN 2620 AND 2631";
    
    //new for permissions
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_JPQL_1 = LIST_ALL_USERS_INVESTIGATIONS_JPQL+" " +
            "AND d.datasetId.investigationId.instrument.name IN(";  //dynamically adding this here: IN(  'alf','lad'   )
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER_JPQL_2=") AND d.datafileParameterCollection.datafileParameterPK.name = 'run_number' " +
            "AND d.datafileParameterCollection.datafileParameterPK.name BETWEEN :lower AND :upper";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
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
    /*
     * Find all keywords for user,
     * //TODO wrong query
     */
    public static final String KEYWORDS_FOR_USER = "Keywords.getAllKeywordsForUser";
    // public static final String KEYWORDS_FOR_USER_JPQL = "SELECT DISTINCT k.keywordPK.name FROM Keyword k WHERE (k.investigation.investigatorCollection.investigatorPK.facilityUserId = :userId OR k.investigation.investigatorCollection IS EMPTY) AND (k.keywordPK.name LIKE :startKeyword OR :startKeyword IS NULL) ORDER BY k.keywordPK.name";
    //new for permissions
    public static final String KEYWORDS_FOR_USER_JPQL = "SELECT DISTINCT k.keywordPK.name from Keyword k, IcatAuthorisation ia WHERE" +
            " k.investigation.id = ia.investigationId AND i.markedDeleted = 'N'" +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND (k.keywordPK.name LIKE :startKeyword OR :startKeyword IS NULL) AND k.markedDeleted = 'N' ORDER BY k.keywordPK.name";
    public static final String KEYWORDS_NATIVE_FOR_USER = "Keywords.getAllKeywordsForUserNative";
    public static final String KEYWORDS_FOR_USER_SQL = "SELECT DISTINCT NAME FROM (SELECT DISTINCT t3.NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?userId AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE NAME LIKE ?startKeyword";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * Find all instruments list,
     *
     */
    public static final String ALL_INSTRUMENTS = "Instrument.listAll";
    public static final String ALL_INSTRUMENTS_JPQL = "SELECT DISTINCT i FROM Instrument i";
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    public static int MAX_QUERY_RESULTSET = 500;
}
