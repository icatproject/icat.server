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
    
    public static final String INVESTIGATIONS_LIST_BY_SURNAME_JPQL = "SELECT i FROM Investigation i WHERE i.investigatorCollection.investigatorPK.facilityUserId = :userId " +
            "AND i.investigatorCollection.facilityUser.lastName LIKE :surname";
    //faster
    public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL ="SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            // public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL ="SELECT ID " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.LAST_NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.facility_user_id = ?1 AND t0.id = t1.investigation_id UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, NULL AS LAST_NAME  FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE id NOT IN (SELECT investigation_id from investigator)) WHERE LAST_NAME LIKE ?2";
    
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
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_SQL = "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.facility_user_id = ?1 AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE NAME LIKE ?2";
    
    /*
     * Same but returning investigation Ids
     */
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID = "Investigation.findByKewordRtnIdNative";
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID_SQL = "SELECT ID " +
            "FROM (SELECT DISTINCT t0.ID, t3.NAME " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.facility_user_id = ?1 AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE NAME LIKE ?2";
    
    
    
    ////by keywrds
     public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS = "Investigation.findByKewordsNative";
   
     public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORDS_SQL = "SELECT DISTINCT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2, KEYWORD t3 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.facility_user_id = ?userId AND t0.id = t1.investigation_id AND t3.INVESTIGATION_ID = t0.ID UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t3.NAME  FROM INVESTIGATION t0, KEYWORD t3 " +
            "WHERE t3.INVESTIGATION_ID = t0.ID AND id NOT IN (SELECT investigation_id from investigator)) WHERE ";
    
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
    
    public static final String INVESTIGATION_LIST_BY_USERID_JPQL = "SELECT i FROM Investigation i WHERE i.investigatorCollection.investigatorPK.facilityUserId = :userId " +
            "AND i.investigatorCollection.investigatorPK.facilityUserId LIKE :userIdSearched";
    
    public static final String INVESTIGATION_NATIVE_LIST_BY_USERID_SQL = "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
            // public static final String INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL ="SELECT ID " +
            "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.FEDERAL_ID  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.facility_user_id = ?1 AND t0.id = t1.investigation_id UNION " +
            "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, NULL AS FEDERAL_ID  FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE id NOT IN (SELECT investigation_id from investigator)) WHERE FEDERAL_ID LIKE ?2";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * This is the search for the advanced search
     */
    public static final String ADVANCED_SEARCH = "Investigation.findByAdvancedSearch";
    public static final String ADVANCED_SEARCH_JPQL = "SELECT i FROM Investigation i WHERE (i.investigatorCollection.investigatorPK.facilityUserId = :userId OR i.investigatorCollection IS EMPTY) AND " +
            "(i.title LIKE :investigationName OR :investigationName IS NULL) AND " +
            "(i.sampleCollection.name LIKE :sampleName OR :sampleName IS NULL) AND" +
            " (i.investigatorCollection.facilityUser.lastName LIKE :investigatorName OR :investigatorName IS NULL) AND" +
            " (i.releaseDate < :endDate OR :endDate IS NULL) AND (i.releaseDate > :startDate OR :startDate IS NULL) AND" +
            " (i.instrument.name LIKE :instrument OR :instrument IS NULL)";
    
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
    public static final String INVESTIGATIONS_BY_USER_JPQL = "SELECT i FROM Investigation i WHERE" +
            " (i.investigatorCollection.investigatorPK.facilityUserId = :userId OR i.investigatorCollection IS EMPTY)";
    
    //much faster, second version for JPQL, but JPQL does not have UNION, OR does not work!!!!
    //String INVESTIGATIONS_BY_USER_JPQL = "SELECT count(i) FROM Investigation i  WHERE i.investigatorCollection.investigatorPK.facilityUserId = 'JAMES' UNION i.id NOT IN (SELECT j.investigatorPK.investigationId  FROM Investigator j)";
    
    //Much faster
    public static final String INVESTIGATIONS_BY_USER_SQL = "SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE  " +
            "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE t2.facility_user_id = t1.facility_user_id " +
            "AND t2.federal_id = ?1 AND t0.id = t1.investigation_id UNION " +
            "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE  FROM INVESTIGATION t0 WHERE id NOT IN (SELECT investigation_id from investigator)";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     *
     * All the investigations of the user.
     *
     */
    public static final String INVESTIGATIONS_FOR_USER = "Investigation.findOfUser";
    public static final String INVESTIGATIONS_FOR_USER_JPQL = "SELECT i FROM Investigation i WHERE" +
            " i.investigatorCollection.investigatorPK.facilityUserId = :userId";
    
    public static final String INVESTIGATIONS_FOR_USER_RTN_ID = "Investigation.findOfUser";
    public static final String INVESTIGATIONS_FOR_USER_RTN_ID_JPQL = "SELECT i,id FROM Investigation i WHERE" +
            " i.investigatorCollection.investigatorPK.facilityUserId = :userId";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumberNative";
    public static final String DATAFILE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumber";
    public static final String INVESTIGATIONS_BY_KEYWORD = "Investigation.findByKeyword";
    public static final String INVESTIGATION_LIST_BY_KEYWORD = "Investigation.findByKeword";
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * Find all keywords
     *
     */
    public static final String ALLKEYWORDS = "Investigation.getAllKeywords";
    public static final String ALLKEYWORDS_NATIVE = "Investigation.getAllKeywordsNative";
    //TODO cannot lower the result in JPQL
    public static final String ALLKEYWORDS_JPQL = "SELECT DISTINCT k.keywordPK.name FROM Keyword k ORDER BY k.keywordPK.name ASC";
    public static final String ALLKEYWORDS_SQL = "select distinct(lower(name)) as name from keyword order by name asc";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
     * Find all keywords for user, 
     * /?TODO wrogn query
     */
    public static final String KEYWORDS_FOR_USER = "Keywords.getAllKeywordsForUser";
    public static final String KEYWORDS_FOR_USER_JPQL = "SELECT DISTINCT k.keywordPK.name FROM Keyword k WHERE (k.investigation.investigatorCollection.investigatorPK.facilityUserId = :userId OR k.investigation.investigatorCollection IS EMPTY) AND (k.keywordPK.name LIKE :startKeyword OR :startKeyword IS NULL) ORDER BY k.keywordPK.name";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    public static int MAX_QUERY_RESULTSET = 600;
}
