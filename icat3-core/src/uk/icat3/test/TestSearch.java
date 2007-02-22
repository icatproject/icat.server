/*
 * TestSearch.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Investigation;
import uk.icat3.search.AdvancedSearchDTO;
import uk.icat3.search.DatafileSearch;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.util.EntityManagerResource;

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
    
    
    public  void seachByKeyword(String userId, String keyword) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByKeyword(userId,keyword,em);
        log.info("Results: "+investigations.size());
        for(Investigation investigation : investigations){
            log.info(investigation.getId());
        }
        
        tearDown();
        
    }
    
    public  void seachBySurname(String userId, String surname) throws Exception {
        
        setUp();
        
        //test code here
        log.info("Testing");
        Collection<Investigation> investigations = InvestigationSearch.searchByUser(userId,surname,em);
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
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestSearch ts = new TestSearch();
        
        // ts.seachByKeyword("885", "Applicatio");
        
        //  ts.seachBySurname("885", "Stuar,");
        
        //   ts.seachByUserID("885", "dis79");
        // ts.seachByUserID("885", "gjd37");
        
        
       /* Collection<String> in  =   new ArrayList<String>();
        in.add("alf");
        
        ts.seachByRunNumber("JAMES", in, 0L,2000L);*/
        
        AdvancedSearchDTO dto = new AdvancedSearchDTO();
        
        dto.setInvestigationName("RROT=-85");
        dto.setInvestigators("HEALY");
        dto.setYearRangeStart(new Date(120,1,1));  //120 = 2020
        dto.setYearRangeEnd(new Date(120,1,1));
        
        ts.seachByAdvanced("JAMES",dto);
    }
    
}
