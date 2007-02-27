/*
 * TestSearch.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Investigation;
import uk.icat3.search.AdvancedSearchDTO;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.KeywordSearch;
import uk.icat3.util.EntityManagerResource;
import uk.icat3.util.InvestigationIncludes;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */
public class TestSearch {
    
    private  static Logger log = Logger.getLogger(TestSearch.class);
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    /** Creates a new instance of TestSearch */
    public TestSearch() {
    }
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-core-testing-PU");
        em = emf.createEntityManager();
        EntityManagerResource.getInstance().set(em);
        
        // Begin transaction
        em.getTransaction().begin();
        
        
    }
    
    protected static void tearDown(){
        // Commit the transaction
        em.getTransaction().commit();
        
        em.close();
    }
    
    
    public  void seachByKeyword(String userId, String keyword ) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(userId,keyword, em);
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        log.info("Results: "+investigations.size());
        tearDown();
        
    }
    
    public  void seachByKeywords(String userId, Collection<String> keywords,LogicalOperator operator, boolean fuzzy, InvestigationIncludes includes) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(userId,keywords, operator, includes, fuzzy,  false, -1, -1, em);
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId()+" "+investigation.getTitle());
        }
        log.info("Results: "+investigations.size());
        tearDown();
        
    }
    
    public  void seachBySurname(String userId, String surname) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(userId,surname,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    
    public void seachByUserID(String userId, String searchUserID) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByUserID(userId,searchUserID,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    
    public void seachByRunNumber(String userId, Collection<String> instruments, Long startRun, Long endRun) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Datafile> datafiles = DatafileSearch.searchByRunNumber(userId,instruments,startRun,endRun, 0,300,em);
        log.info("Results: "+datafiles.size());
        for(Datafile datafile : datafiles){
            log.info(datafile.getId());
        }
        
        tearDown();
        
    }
    
    public void seachByAdvanced(String userId, AdvancedSearchDTO dto) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations =  InvestigationSearch.searchByAdvanced(userId,dto, 0,300,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId()+" "+investigation.getTitle());
        }
        
        tearDown();
        
    }
    
    public void getAllKeywords(String userId) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<String> keywords =  KeywordSearch.getAllKeywords(userId,em);
        log.info("Results: "+keywords.size());
        for(String keyword : keywords){
            log.info(keyword);
        }
        
        tearDown();
        
    }
    
    public void getUserKeywords(String userId, String startkeyword) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<String> keywords =  KeywordSearch.getKeywordsForUser(userId,startkeyword,em);
        log.info("Results: "+keywords.size());
        for(String keyword : keywords){
            log.info(keyword);
        }
        
        tearDown();
        
    }
    
    public void getUserInvestigations(String userId) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations =  InvestigationSearch.getUsersInvestigations(userId,em);
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId()+" "+investigation.getTitle());
        }
        log.info("Results: "+investigations.size());
        tearDown();
        
    }
    
    
    public void test() throws Exception {
        
        setUp();
        
        String INVESTIGATIONS_BY_USER_SQL = "SELECT i.id FROM Investigation i WHERE" +
                " (i.investigatorCollection.investigatorPK.facilityUserId = :userId AND   i.investigatorCollection IS EMPTY";
        
        //  String INVESTIGATIONS_BY_USER_SQL = "SELECT ID, PREV_INV_NUMBER, BCAT_INV_STR, VISIT_ID, GRANT_ID, INV_ABSTRACT, RELEASE_DATE, TITLE, MOD_TIME, INV_NUMBER, MOD_ID, INV_TYPE, INSTRUMENT, FACILITY_CYCLE " +
              /*  "FROM (SELECT DISTINCT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.LAST_NAME  " +
                "FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE t2.facility_user_id = t1.facility_user_id " +
                "AND t2.federal_id = ?1 AND t0.id = t1.investigation_id UNION " +
                "SELECT t0.ID, t0.PREV_INV_NUMBER, t0.BCAT_INV_STR, t0.VISIT_ID, t0.GRANT_ID, t0.INV_ABSTRACT, t0.RELEASE_DATE, t0.TITLE, t0.MOD_TIME, t0.INV_NUMBER, t0.MOD_ID, t0.INV_TYPE, t0.INSTRUMENT, t0.FACILITY_CYCLE, t2.LAST_NAME  FROM INVESTIGATION t0, INVESTIGATOR t1, FACILITY_USER t2 WHERE id NOT IN (SELECT investigation_id from investigator)) WHERE LAST_NAME LIKE ?2";*/
        
        //test code here
        log.info("Testing");
        Collection<java.math.BigDecimal> investigations =  em.createQuery(INVESTIGATIONS_BY_USER_SQL).setParameter("userId","JAMES").getResultList();
        log.info("Results: "+investigations.size());
        for(java.math.BigDecimal investigation : investigations){
            log.info(investigation);
        }
        
       /* Collection<Investigation> investigations =  em.createNativeQuery(INVESTIGATIONS_BY_USER_SQL,Investigation.class).setParameter(1,"JAMES").setMaxResults(100).getResultList();
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId()+" "+investigation.getTitle());
        }*/
        log.info("Results: "+investigations.size());
        tearDown();
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestSearch ts = new TestSearch();
        
        // ts.seachByKeyword("JAMES", "ccw");
        
        Collection<String> keywords  =   new ArrayList<String>();
        
        //isis
        keywords.add("copper");
        keywords.add("isis");
        ts.seachByKeywords("gjd37", keywords, LogicalOperator.AND, false, InvestigationIncludes.ALL);
        
        //keywords.add("ccw");
        //  ts.seachByKeywords("JAMES", keywords);
        
        // ts.seachBySurname("JAMES", "HEALY");
        
        //    ts.seachByUserID("JAMES", "JAMES");
        
        // ts.seachByUserID("JAMES", "JAMES");
        
        
        /* Collection<String> in  =   new ArrayList<String>();
        in.add("alf");
         
        ts.seachByRunNumber("JAMES", in, 0L,2000L);*/
        
        /* AdvancedSearchDTO dto = new AdvancedSearchDTO();
         
        dto.setInvestigationName("RROT=-85");
        dto.setInvestigators("HEALY");
        dto.setYearRangeStart(new Date(120,1,1));  //120 = 2020
        dto.setYearRangeEnd(new Date(120,1,1));
         
        ts.seachByAdvanced("JAMES",dto);*/
        
        // ts.getAllKeywords("JAMES");
        
        // ts.getUserKeywords("JAMES", "alf");
        
        // ts.getUserInvestigations("JAMES");
        
        //ts.test();
    }
    
}
