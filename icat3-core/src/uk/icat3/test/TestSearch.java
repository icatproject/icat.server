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
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
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
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
public class TestSearch {

    protected  static Logger log = Logger.getLogger(TestSearch.class);
    // TODO code application logic here

    static EntityManagerFactory emf = null;
    // Create new EntityManager

    static EntityManager em = null;

    /** Creates a new instance of TestSearch */
    public TestSearch() {
    }

    protected static void setUp() {
        emf = Persistence.createEntityManagerFactory("icat3-unit-testing-PU");
        //emf = Persistence.createEntityManagerFactory("icatisis");
        em = emf.createEntityManager();


        // Begin transaction
        em.getTransaction().begin();


    }

    protected static void tearDown() {
        // Commit the transaction
        em.getTransaction().commit();

        em.close();
    }

    public void searchByRun() {
        setUp();

        long time = System.currentTimeMillis();
        
        List<String> in = new ArrayList<String>();
        in.add("sxd");
        Collection<Datafile> files = DatafileSearch.searchByRunNumber("gjd37", in, 8374f, 8400f, em);
       
        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        tearDown();
        
        for (Datafile datafile : files) {
            System.out.println(datafile.getIcatRole());
        } 
    }

    public void getInvestigations(String userId, Collection<Long> ids) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        setUp();

        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationManager.getInvestigations(userId, ids, em);

        for (Investigation investigation : investigations) {
            log.info(investigation.getId());
        }

        tearDown();

    }

    public void getMyInvestigations(String userId) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        setUp();

        //test code here
        log.info("getMyInvestigations");
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(userId, em);

        for (Investigation investigation : investigations) {
            log.info(investigation.getId());
        }

        tearDown();

    }

    public void getInvestigation(String userId, Long id) throws NoSuchObjectFoundException, InsufficientPrivilegesException {
        setUp();

        //test code here
        log.info("Testing");
        Investigation investigation = InvestigationManager.getInvestigation(userId, id, em);


        log.info(investigation.getId());


        tearDown();

    }

    public void seachByKeyword(String userId, String keyword) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(userId, keyword, em);

        for (Investigation investigation : investigations) {
            log.info(investigation.getId());
        }
        log.info("Results: " + investigations.size());
        tearDown();

    }

    public void seachByKeywordRnId(String userId, String keyword) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<Long> investigations = InvestigationSearch.searchByKeywordRtnId(userId, keyword, em);

        for (Object investigation : investigations) {
            log.info(investigation);
        }
        log.info("Results: " + investigations.size());
        tearDown();

    }

    public void seachByKeywords(String userId, Collection<String> keywords, LogicalOperator operator, boolean fuzzy, InvestigationInclude includes) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(userId, keywords, operator, includes, fuzzy, false, -1, -1, em);

        for (Investigation investigation : investigations) {
            log.info(investigation.getId() + " " + investigation.getTitle());
        }
        log.info("Results: " + investigations.size());
        tearDown();

    }

    public void seachByKeywords(String userId, Collection<String> keywords) throws Exception {

        setUp();

        long time = System.currentTimeMillis();
        //test code here
        log.info("Testing");
       // Collection<Investigation> investigations = InvestigationSearch.searchByKeywords(userId, keywords, LogicalOperator.AND, InvestigationInclude.NONE, false, true, 0, 500, em);

        Collection<Investigation> investigations  = InvestigationSearch.searchByKeywords(userId,keywords,em);

        for (Investigation investigation : investigations) {
            log.info(investigation.getId() + " " + investigation.getFacility());
        }
        log.info("Results: " + investigations.size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        tearDown();

    }

    public void seachBySurname(String userId, String surname) throws Exception {

        setUp();
        long time = System.currentTimeMillis();
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByUserSurname(userId, surname, em);
        log.info("Results: " + investigations.size());
        for (Investigation investigation : investigations) {
            log.info(investigation.getId());
        }
        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        tearDown();

    }

    public void seachByUserID(String userId, String searchUserID) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByUserID(userId, searchUserID, em);
        log.info("Results: " + investigations.size());
        for (Investigation investigation : investigations) {
            log.info(investigation.getId());
        }

        tearDown();

    }

    public void seachByRunNumber(String userId, Collection<String> instruments, Long startRun, Long endRun) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<Datafile> datafiles = DatafileSearch.searchByRunNumber(userId, instruments, startRun, endRun, 0, 300, em);
        log.info("Results: " + datafiles.size());
        for (Datafile datafile : datafiles) {
            log.info(datafile.getId());
        }

        tearDown();

    }

    public void seachByAdvanced(String userId, AdvancedSearchDetails dto) throws Exception {

        setUp();

        long time = System.currentTimeMillis();

        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByAdvanced(userId, dto, 0, 300, em);
        log.info("Results: " + investigations.size());
        for (Investigation investigation : investigations) {
            log.info(investigation.getId() + " " + investigation.getTitle() + " " + investigation.getInstrument());
        }

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        tearDown();
    }

    public void getAllKeywords(String userId) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<String> keywords = KeywordSearch.getAllKeywords(userId, KeywordType.ALPHA, em);
        log.info("Results: " + keywords.size());
        log.info(keywords.getClass());
        for (String keyword : keywords) {
        //     log.info(keyword);
        }

        tearDown();

    }

    public void getUserKeywords(String userId, String startkeyword) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<String> keywords = KeywordSearch.getKeywordsForUser(userId, KeywordType.ALL, startkeyword, 300, em);
        log.info("Results: " + keywords.size());
        for (String keyword : keywords) {
        //  log.info(keyword);
        }

        tearDown();

    }

    public void getUserInvestigations(String userId) throws Exception {

        setUp();

        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.getUsersInvestigations(userId, em);

        for (Investigation investigation : investigations) {
            log.info(investigation.getId() + " " + investigation.getTitle());
        }
        log.info("Results: " + investigations.size());

        tearDown();

    }

    public void getAllInvestigations(String userId) throws Exception {

        setUp();

        long time = System.currentTimeMillis();

        //test code here
        log.info("Testing");
        Collection<Long> investigations = em.createQuery(Queries.LIST_ALL_USERS_INVESTIGATION_IDS_JPQL).
                setParameter("objectType", ElementType.INVESTIGATION).setParameter("userId", userId).getResultList();

        log.info("Results: " + investigations.size());

        System.out.println((System.currentTimeMillis() - time) / 1000f + " seconds");

        tearDown();

    }

    public void testModify() throws Exception {

        setUp();


        Investigation investigation = InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);

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

        Investigation investigation2 = InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);
        System.out.println("Investigation name is: " + investigation2.getTitle());

        Thread.sleep(20000);

        Investigation investigation3 = InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);
        System.out.println("Investigation name is now: " + investigation3.getTitle());

        tearDown();
        setUp();

        Investigation investigation31 = InvestigationManager.getInvestigation("JAMES-JAMES", 11915480L, em);
        System.out.println("Investigation name is now with new entity manager: " + investigation31.getTitle());


        Investigation investigation4 = (Investigation) em.createQuery("select i from Investigation i where i.id = 11915480").setHint("toplink.refresh", "true").getSingleResult();
        System.out.println("Investigation name is after a select hine: " + investigation4.getTitle());

        //  em.refresh(investigation3);

        // System.out.println("Investigation name is now after refresh: "+investigation3.getTitle());

        tearDown();

    }

    public void test2() throws Exception {

        setUp();
        Collection<Long> ids = new ArrayList<Long>();
        ids.add(2L);
        ids.add(2L);
        Collection<Investigation> investigations = InvestigationManager.getInvestigations("dwf64", ids, em);

        for (Investigation investigation : investigations) {
            log.info(investigation.getId() + " " + investigation.getTitle() + " " + investigation.getCreateId());
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

        String LIST_ALL = "SELECT k.keywordPK.name from Keyword k where LOWER(k.keywordPK.name) LIKE '%s%'";

        System.out.println(em.createQuery(LIST_ALL).getResultList());

        tearDown();
    }

    public void testDelete() throws Exception {
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
        Collection<String> keywords = new ArrayList<String>();

        //isis
        //keywords.add("ccwilson");
        keywords.add("*ccw*");

        // ts.seachByKeywords("gjd37", keywords, LogicalOperator.AND, false, InvestigationUtil.ALL);
        ts.seachByKeywords("gjd37", keywords);
        ///  get Investigations ////

        Collection<Long> ids = new ArrayList<Long>();

        //    ids.add(9525280L);
        //  ts.getInvestigations("gjd37",ids);

        //ts.getInvestigation("gjd37",9525454280L);
        //////////////////////////


        keywords.add("a");
        // keywords.add("calibration");
        // ts.seachByKeywords("gjd37", keywords);
        // ts.seachByKeywords("gjd37", keywords);

        // log.info("Hello");
        // ts.getMyInvestigations("gjd37");

        // ts.seachBySurname("gjd37", "Shankland");

        // ts.seachByUserID("JAMES", "JAMES");

        // ts.seachByUserID("JAMES", "JAMES");


        Collection<String> ins = new ArrayList<String>();
        // ins.add("scan");
        //ins.add("crisp");

        // ts.seachByRunNumber("gjd37", ins, 11757L,11759L);

          AdvancedSearchDetails dto = new AdvancedSearchDetails();

        // dto.setInvestigationName("multidetector");
        /*Collection<String> inv  =   new ArrayList<String>();
        inv.add("JAMES-JAMES");*/
        //  dto.setInvestigators(inv);
        //dto.setDateRangeStart(new Date(1,1,1/*System.currentTimeMillis()-900000000)*/));  //120 = 2020
        //dto.setYearRangeEnd(new Date());
       // dto.setSampleName("multidetecto");
       // dto.setInstruments(ins);
        //dto.setExperimentNumber("3684");
       //  dto.setVisitId("40");
       //dto.setInvestigationType("experiment");
        //dto.setBackCatalogueInvestigatorString("ab etc - oxford,");
        //
        //dto.setDatafileName("a");
        Collection<String> keywords2 = new ArrayList<String>();

        //isis
        keywords2.add("*ccw*");
        // dto.setInvestigationName("SrF2 calibration  w=-25.3");
        dto.setKeywords(keywords2);
       // dto.setDatafileName("CSP11758.");
       // dto.setRunEnd(11759L);
       // dto.setRunStart(11757L);
       // dto.setFuzzy(false);
       ts.seachByAdvanced("gjd37",dto);
       
        //  ts.getAllKeywords("gjd37");
        //
        // ts.getUserKeywords("gjd37", null);
        // ts.getAllInvestigations("gjd37");
        //ts.getUserInvestigations("gjd37");

        //ts.test();

       // ts.searchByRun();
       // ts.searchByRun(); 
       // ts.searchByRun();
    //ts.test2();
        //  ts.testModify();

    //  ts.testDelete();


    }
}
