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
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.KeywordSearch;
import uk.icat3.util.ElementType;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.KeywordType;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */
public class TestSearch {
    
    protected  static Logger log = Logger.getLogger(TestSearch.class);
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    /** Creates a new instance of TestSearch */
    public TestSearch() {
    }
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
        //emf = Persistence.createEntityManagerFactory("icat3-dls_dev_new");
        em = emf.createEntityManager();
        
        
        // Begin transaction
        //em.getTransaction().begin();
        
        
    }
    
    protected static void tearDown(){
        // Commit the transaction
        // em.getTransaction().commit();
        
        em.close();
    }
    
    
    public void getInvestigations(String userId, Collection<Long> ids) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationManager.getInvestigations(userId, ids, em);
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    public void getMyInvestigations(String userId) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        setUp();
        
        //test code here
        log.info("getMyInvestigations");
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(userId,  em);
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    
    public void getInvestigation(String userId, Long id) throws NoSuchObjectFoundException, InsufficientPrivilegesException{
        setUp();
        
        //test code here
        log.info("Testing");
        Investigation investigation = InvestigationManager.getInvestigation(userId, id, em);
        
        
        log.info(investigation.getId());
        
        
        tearDown();
        
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
    
    public  void seachByKeywordRnId(String userId, String keyword ) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Long> investigations = InvestigationSearch.searchByKeywordRtnId(userId,keyword, em);
        
        for(Object investigation : investigations){
            log.info(investigation);
        }
        log.info("Results: "+investigations.size());
        tearDown();
        
    }
    
    public void seachByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, boolean fuzzy, InvestigationInclude includes) throws Exception {
        
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
    
    public  void seachByKeywords(String userId, Collection<String> keywords) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(userId, keywords,LogicalOperator.AND, InvestigationInclude.NONE, false, true, 0, 500, em);
        
        //Collection<Investigation> investigations  = InvestigationSearch.searchByKeywords(userId,keywords,em);
        
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
    
    public void seachByAdvanced(String userId, AdvancedSearchDetails dto) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations =  InvestigationSearch.searchByAdvanced(userId,dto, 0,300,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId()+" "+investigation.getTitle()+" "+investigation.getInstrument());
        }
        
        
        tearDown();
        
    }
    
    public void getAllKeywords(String userId) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<String> keywords =  KeywordSearch.getAllKeywords(userId, KeywordType.ALPHA, em);
        log.info("Results: "+keywords.size());
        log.info(keywords.getClass());
        for(String keyword : keywords){
            //     log.info(keyword);
        }
        
        tearDown();
        
    }
    
    public void getUserKeywords(String userId, String startkeyword) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<String> keywords =  KeywordSearch.getKeywordsForUser(userId,startkeyword,300, em);
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
    
    
    public void testModify() throws Exception {
        
        setUp();
        
        
        Investigation investigation =  InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);
        
       /*
        Testing PSD  - old title
        *
        investigation.setId(1L);
        investigation.setDatasetCollection(null);
        investigation.setInvestigationLevelPermissionCollection(null);
        investigation.setKeywordCollection(null);
        investigation.setInvestigatorCollection(null);
        investigation.setPublicationCollection(null);
        investigation.setSampleCollection(null);
        investigation.setStudyInvestigationCollection(null);
        investigation.setShiftCollection(null);
        
        investigation.setTitle("New Test Investigation");
        
        em.merge(investigation);
        */
        
        Investigation investigation2 =  InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);
        System.out.println("Investigation name is: "+investigation2.getTitle());
        
        Thread.sleep(20000);
        
        Investigation investigation3 =  InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);
        System.out.println("Investigation name is now: "+investigation3.getTitle());
        
        tearDown();
        setUp();
        
        Investigation investigation31 =  InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);
        System.out.println("Investigation name is now with new entity manager: "+investigation31.getTitle());
        
        
        Investigation investigation4 = (Investigation) em.createQuery("select i from Investigation i where i.id = 11915480").setHint("toplink.refresh", "true").getSingleResult();
        System.out.println("Investigation name is after a select hine: "+investigation4.getTitle());
        
        //  em.refresh(investigation3);
        
        // System.out.println("Investigation name is now after refresh: "+investigation3.getTitle());
        
        tearDown();
        
    }
    
    public void test2() throws Exception {
        
        setUp();
        Collection<Long> ids = new ArrayList<Long>();
        ids.add(2L);
        ids.add(2L);
        Collection<Investigation> investigations = InvestigationManager.getInvestigations("dwf64",ids,em);
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId()+" "+investigation.getTitle()+" "+investigation.getCreateId());
        }
        //System.out.println(investigation.isValid(em));
        /*for(Dataset dataset : investigation.getDatasetCollection()){
         
            Collection<DatasetParameter> params = dataset.getDatasetParameterCollection();
            log.trace(investigation.getDatasetCollection().size()+" "+params.size());
            for(DatasetParameter param : params){
                param.isValid(em);
            }
         
            for(Datafile datafile : dataset.getDatafileCollection()){
                Collection<DatafileParameter> paramsfile = datafile.getDatafileParameterCollection();
                log.trace(dataset.getDatafileCollection().size()+" "+params.size());
                for(DatafileParameter param : paramsfile){
                    param.isValid(em);
                }
            }
        }*/
        
        /*log.trace("Searching all");
        Collection<DatafileParameter> params = (Collection<DatafileParameter>)em.createQuery("select dsp from DatafileParameter dsp").setMaxResults(1000).getResultList();
         
        for(DatafileParameter param : params){
            param.isValid(em);
        }*/
        
        tearDown();
    }
    
    public void test() throws Exception {
        
        
        setUp();
        /*String LIST_ALL = "SELECT DISTINCT i from Investigation i, IcatAuthorisation ia WHERE" +
            " i.id = ia.investigationId AND i.markedDeleted = 'N' " +
            " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
            " AND ia.markedDeleted = 'N' AND ia.role.actionSelect = 'Y'";*/
        String LIST_ALL = "SELECT DISTINCT ds from Dataset ds, IcatAuthorisation ia WHERE" +
            " ds.id = ia.elementId AND ia.elementType = '"+ElementType.DATASET+"' AND ds.markedDeleted = 'Y' " +
            " AND ia.userId = :userId " +
            " AND ia.markedDeleted = 'N' AND ia.role.actionSelect = 'Y'";
        //String TEST_SQL = LIST_ALL+ "AND d.datasetId.investigationId.instrument.name IN('alf','lad') AND d.datafileParameterCollection.datafileParameterPK.name = 'run_number' AND d.datafileParameterCollection.datafileParameterPK.name BETWEEN 2620 AND 2631";
        
        // String TEST_SQL =  "SELECT DISTINCT k.keywordPK.name from Keyword k, IcatAuthorisation ia WHERE" +
        //    " k.investigation.id = ia.icatAuthorisationPK.investigationId AND " +
        //  "(ia.icatAuthorisationPK.userId = :userId OR ia.icatAuthorisationPK.userId = 'ANY')" +
        //" AND ia.markedDeleted = 'N' AND (k.keywordPK.name LIKE :startKeyword OR :startKeyword IS NULL) ORDER BY k.keywordPK.name";
        //     String TEST_SQL = "SELECT i from Investigation i WHERE i.id = '283'";
        //String TEST_SQL = "SELECT i from Keyword i";
        //test code here
        // em.createQuery(INVESTIGATIONS_BY_USER_SQL2).setMaxResults(2).getResultList();
        
        System.out.println(em.createQuery(LIST_ALL).setParameter("userId", "test").getResultList());
        //log.info("Testing");
        /*Collection<Long> investigations = em.createQuery(INVESTIGATIONS_BY_USER_SQL).setParameter("userId","JAMES-JAMES").setParameter("instrument","alf").setParameter("lowerRunNumber",0).setParameter("upperRunNumber",10000).getResultList();
        log.info("Results: "+investigations.size());
        for(Long investigation : investigations){
            log.info(investigation);
        }*/
        
       /* Collection<Investigation> investigations =  em.createNativeQuery(INVESTIGATIONS_BY_USER_SQL,Investigation.class).setParameter(1,"JAMES").setMaxResults(100).getResultList();
        
        for(Investigation investigation : investigations){
            log.info(investigation.getId()+" "+investigation.getTitle());
        }*/
        //log.info("Results: "+investigations.size());
        
        
       /// Dataset dataset = em.find(Dataset.class, 2L);
        
     //   Datafile datafile = em.find(Datafile.class, 2L);
        
        tearDown();
        
      //  System.out.println(datafile.getId());
        
       // System.out.println(dataset.getInvestigationId());
        
    }
    
    
    public void testDelete() throws Exception{
        setUp();
        
        //DataFileManager.deleteDataFile("dwf64", 101L, em);
        //DataSetManager.deleteDataSet("dwf64", 2L, em);
        InvestigationManager.deleteInvestigation("dwf64", 2L, em);
        tearDown();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestSearch ts = new TestSearch();
        
        //  ts.seachByKeyword("JAMES", "ccw");
        
        //ts.seachByKeywordRnId("JAMES-JAMES", "a");
        Collection<String> keywords  =   new ArrayList<String>();
        
        //isis
        keywords.add("isis");
        
        keywords.add("calibration");
        // ts.seachByKeywords("gjd37", keywords, LogicalOperator.AND, false, InvestigationUtil.ALL);
        // ts.seachByKeywords("JAMES-JAMES", keywords);
        ///  get Investigations ////
        
        Collection<Long> ids  =   new ArrayList<Long>();
        
        //    ids.add(9525280L);
        //  ts.getInvestigations("gjd37",ids);
        
        //ts.getInvestigation("gjd37",9525454280L);
        //////////////////////////
        
        
        
        //keywords.add("ccw");
        // ts.seachByKeywords("JAMES-JAMES", keywords);
        
        log.info("Hello");
        // ts.getMyInvestigations("JAMES-JAMES");
        
        // ts.seachBySurname("JAMES", "HEALY");
        
        //ts.seachByUserID("JAMES", "JAMES");
        
        // ts.seachByUserID("JAMES", "JAMES");
        
        
        Collection<String> ins  =   new ArrayList<String>();
        ins.add("scan");
        ins.add("crisp");
        
        //   ts.seachByRunNumber("JAMES-JAMES", ins, 2620L,2631L);
        
        AdvancedSearchDetails dto = new AdvancedSearchDetails();
        
        dto.setInvestigationName("angle scan 0.44");
        Collection<String> inv  =   new ArrayList<String>();
        //inv.add("JAMES-JAMES");
        dto.setInvestigators(inv);
        dto.setYearRangeStart(new Date(1,1,1));  //120 = 2020
        dto.setYearRangeEnd(new Date());
        dto.setSampleName("angle scan 0.44");
        dto.setInstruments(ins);
        dto.setExperimentNumber("0");
        //
        dto.setDatafileName("a");
        Collection<String> keywords2  =   new ArrayList<String>();
        
        //isis
        keywords2.add("angle");
        
        dto.setKeywords(keywords2);
        dto.setRunEnd(19624L);
        dto.setRunStart(19622L);
        
        //  ts.seachByAdvanced("JAMES-JAMES",dto);
        
        //       ts.getAllKeywords("JAMES");
        //
        //    ts.getUserKeywords("JAMES-JAMES", "alf");
        
        // ts.getUserInvestigations("JAMES");
        
        ts.test();
        
        
        //ts.test2();
        //  ts.testModify();
        
        //  ts.testDelete();
    }
    
    
    
}
