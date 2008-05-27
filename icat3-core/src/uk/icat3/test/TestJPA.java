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
import java.util.Random;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.DatafileFormatPK;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.search.KeywordSearch;
import uk.icat3.util.AccessType;
import uk.icat3.util.ElementType;
import uk.icat3.util.KeywordType;
import uk.icat3.util.Queries;
import uk.icat3.util.Util;

/**
 *
 * @author gjd37
 */
public class TestJPA {
    
    protected  static Logger log = Logger.getLogger(TestSearch.class);
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    /** Creates a new instance of TestSearch */
    public TestJPA() {
    }
    
    protected static void setUp(){
        //emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
          emf = Persistence.createEntityManagerFactory("icat3-unit-testing-PU");
        //emf = Persistence.createEntityManagerFactory("icatisis_dev");
        em = emf.createEntityManager();
        // Begin transaction
        em.getTransaction().begin();
        
    }
    
    protected static void tearDown(){
        // Commit the transaction
        em.getTransaction().commit();
        
        em.close();
    }
    
    
    public void createInv() throws Exception{
        setUp();
        
        System.out.println(KeywordSearch.getAllKeywords("gjd37", KeywordType.ALPHA, em));
        
        tearDown();
    }
    
    public void createDS() throws Exception{
        setUp();
        
        Dataset ds = new Dataset();
        DatasetType type = new DatasetType();
        type.setName("analyzed");
        type.setDescription("Analyzed data");
        ds.setDatasetType("analyzed");
        ds.setName("unit test create data setds");
        
        DataSetManager.createDataSet("test_icatadmin", ds, 3L, em);
        
        tearDown();
    }
    
    public void createDF() throws Exception{
        setUp();
        
        Datafile df = new Datafile();
        df.setDatafileVersion("1");
        DatafileFormat type = new DatafileFormat();
        DatafileFormatPK pk = new DatafileFormatPK("3.0.0", "nexus");
        type.setDatafileFormatPK(pk);
        
        df.setDatafileFormat(type);
        df.setName("name of dffdf");
        
        df.isValid(em);
        //create datafile parameter
        
       /* DatafileParameterPK dfppk = new DatafileParameterPK("uAmp hours","good_proton_charge", null);
        DatafileParameter dfp = new DatafileParameter(dfppk);
        
        df.addDataFileParameter(dfp);*/
        
        //
        DataFileManager.createDataFile("test_icatadmin", df, 3L, em);
        
        tearDown();
    }
    
    public void changeRole() throws Exception {
        setUp();
        
        InvestigationManager.updateAuthorisation("test_admin_investigation", "CREATOR", 103L, em);
        
        tearDown();
    }
    
    public void testJPA() throws Exception {
        setUp();
        
        long time = System.currentTimeMillis();
        String QUERY = Queries.LIST_ALL_USERS_INVESTIGATIONS_JPQL ;
        
        
        Query nullQuery = em.createQuery(QUERY);
        
        //  QUERY = "SELECT DISTINCT t0.ID, t0.GRANT_ID, t0.MOD_TIME, t0.RELEASE_DATE, t0.CREATE_ID, t0.TITLE, t0.MOD_ID, t0.INV_ABSTRACT, t0.PREV_INV_NUMBER, t0.VISIT_ID, t0.BCAT_INV_STR, t0.INV_NUMBER, t0.CREATE_TIME, t0.FACILITY_ACQUIRED, t0.DELETED, t0.FACILITY_CYCLE, t0.INSTRUMENT, t0.INV_TYPE, t0.FACILITY FROM KEYWORD t5, KEYWORD t4, KEYWORD t3, ICAT_ROLE t2, ICAT_AUTHORISATION t1, INVESTIGATION t0 WHERE (((((((((t0.ID = t1.ELEMENT_ID) AND (t1.ELEMENT_TYPE = 'INVESTIGATION')) AND (t0.DELETED = 'N')) AND ((t1.USER_ID = ?userId) OR (t1.USER_ID = 'ANY'))) AND (t1.DELETED = 'N')) AND (t2.ACTION_SELECT = 'Y')) AND ((t3.NAME LIKE '%ccw%') OR (t4.NAME LIKE '%shu%'))) AND (t5.DELETED = 'N')) AND ((((t2.ROLE = t1.ROLE) AND (t3.INVESTIGATION_ID = t0.ID)) AND (t4.INVESTIGATION_ID = t0.ID)) AND (t5.INVESTIGATION_ID = t0.ID)))";
        
        // Query nullQuery = em.createNativeQuery(QUERY);
        //nullQuery.setParameter("userId", "gjd37");
        // nullQuery.setParameter("userId", "SUPER_USER");
        //  nullQuery.setParameter("userId", "facility_scientist");
        nullQuery.setParameter("userId", "none_facility_scientist");
        nullQuery.setParameter("objectType", ElementType.INVESTIGATION);
        //try and find user with null as investigation
        
        nullQuery.setMaxResults(50);
        
                
        
        Collection<Investigation> invs = nullQuery.getResultList();
                
        System.out.println((System.currentTimeMillis() - time)/1000f+" seconds for "+invs.size());
        
        for (Investigation investigation : invs) {
                 System.out.println(investigation.getInvStartDate());
        }

       
        tearDown();
    }
    
    public void getRoles() throws Exception {
        setUp();
        
        System.out.println(InvestigationManager.getAuthorisations("test_admin_investigation", 100L, em));
        
        tearDown();
    }
    
    public void addRole() throws Exception {
        setUp();
        
        System.out.println(DataFileManager.addAuthorisation("test_admin_investigation","addedDatafileUser", "CREATOR", 100L, em));
        
        tearDown();
    }
    
    public void testP1() throws Exception {
        setUp();
        String QUERY  = "SELECT DISTINCT i from Investigation i , IcatAuthorisation ia, FacilityInstrumentScientist fis WHERE  " +
                "((:userId = 'SUPER_USER') OR  (:userId = fis.facilityInstrumentScientistPK.federalId AND " +
                " (fis.facilityInstrumentScientistPK.instrumentName = i.instrument) AND fis.markedDeleted = 'N') OR  " +
                "(i.id = ia.elementId AND ia.elementType = :objectType  AND (ia.userId = :userId OR ia.userId = 'ANY') " +
                "AND ia.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y')) AND i.markedDeleted = 'N'  AND " +
                "EXISTS (SELECT DISTINCT df FROM Datafile df, FacilityInstrumentScientist fis2, IcatAuthorisation iadf3 WHERE " +               
                " df.dataset = i.datasetCollection AND (df.datafileCreateTime > :lowerTime OR :lowerTime IS NULL) " +
                "AND (df.datafileCreateTime < :upperTime OR :upperTime IS NULL) AND  df.dataset.markedDeleted = 'N' " +
                "AND df.markedDeleted = 'N' AND (df.name  =  :datafileName OR :datafileName IS NULL) AND" +
                " iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND df.dataset.markedDeleted = 'N'  " +
                "AND ((:userId = 'SUPER_USER') OR  (:userId = fis2.facilityInstrumentScientistPK.federalId AND " +
                " fis2.facilityInstrumentScientistPK.instrumentName = i.instrument AND fis2.markedDeleted = 'N') " +
                "OR  (df.dataset.id = iadf3.elementId AND iadf3.elementType = :dataSetType  " +
                "AND (iadf3.userId = :userId OR iadf3.userId = 'ANY')  AND iadf3.role.actionCanSelect = 'Y')))";
                

               
        System.out.println(QUERY);
               
        Query nullQuery = em.createQuery(QUERY);
                        
        
        
        nullQuery.setParameter("objectType", ElementType.DATASET);
   
        nullQuery.setParameter("userId", "ALL");       
       

        nullQuery.setParameter("dataSetType", ElementType.DATASET);
        nullQuery.setParameter("lowerTime", new Date(1, 1, 1));
        nullQuery.setParameter("upperTime", new Date());
        nullQuery.setParameter("datafileName", null);

        
        
        System.out.println(nullQuery.getResultList());
        System.out.println("dfdf");
        tearDown();
    }
    
    public void testP() throws Exception {
        setUp();
        
        String QUERY = "SELECT i FROM Investigation i, IcatAuthorisation ia WHERE i.id = ia.elementId AND ia.elementType = :investigationType AND i.markedDeleted = 'N' " +
                " AND (ia.userId = :userId OR ia.userId = 'ANY')" +
                " AND ia.markedDeleted = 'N' AND i.markedDeleted = 'N' AND ia.role.actionCanSelect = 'Y' AND "+
                
                " (i.visitId = :visitId OR  :visitId IS NULL) AND" +
                " (i.invType.name = :invType  OR :invType IS NULL) AND " +
                " (i.invAbstract LIKE :invAbstract OR :invAbstract IS NULL) AND" +
                " (i.grantId = :grantId OR :grantId IS NULL) AND" +
                " (i.title = :title OR :title IS NULL) AND" +
                " (i.bcatInvStr = :bcatInvStr OR :bcatInvStr IS NULL) AND " +
                " (i.invNumber = :invNumber  OR :invNumber IS NULL) " +
                
                " AND i.instrument.name IN(:instrument)  AND i.instrument.markedDeleted = 'N' "+ //expand IN, remove this if instrument null
                
                " AND EXISTS (SELECT sample FROM i.sampleCollection sample WHERE sample.name LIKE :sampleName AND " +
                "sample.markedDeleted = 'N') "+//iterate, remove if no sample is null
                
                " AND EXISTS (SELECT kw FROM i.keywordCollection kw WHERE kw.keywordPK.name LIKE :keyword AND " +
                " kw.markedDeleted = 'N')  "+ //iterate, remove if no keyword is null
                
                " AND EXISTS ( SELECT inv FROM i.investigatorCollection inv WHERE " +
                "LOWER(inv.facilityUser.lastName) LIKE :surname AND inv.markedDeleted = 'N')  "+ //iterate, remove this if instrument null
                
                " AND EXISTS (SELECT df FROM Datafile df, IcatAuthorisation iadf3 WHERE " +
                " df.id = iadf3.elementId AND iadf3.elementType = :dataFileType AND df.markedDeleted = 'N' " +
                " AND (iadf3.userId = :userId OR iadf3.userId = 'ANY')" +
                " AND iadf3.markedDeleted = 'N' AND df.markedDeleted = 'N' AND iadf3.role.actionCanSelect = 'Y' " +
                " AND df.dataset.investigation = i AND (df.createTime > :lowerTime OR :lowerTime IS NULL AND df.createTime < :upperTime OR :upperTime IS NULL) AND " +
                " df.markedDeleted = 'N' AND (df.name = :datafileName OR :datafileName IS NULL))  " + //remove if all are null
                
                " AND EXISTS (SELECT dfp FROM DatafileParameter dfp, IcatAuthorisation ia2 " +
                " WHERE dfp.datafile.id = ia2.elementId AND ia2.elementType = :dataFileType AND dfp.markedDeleted = 'N' " +
                " AND (ia2.userId = :userId OR ia2.userId = 'ANY')" +
                " AND ia2.markedDeleted = 'N' AND dfp.datafile.markedDeleted = 'N' AND ia2.role.actionCanSelect = 'Y' AND dfp.datafile.dataset.investigation = i AND dfp.numericValue BETWEEN :lower AND :upper AND " +
                " dfp.datafileParameterPK.name = 'run_number' AND dfp.markedDeleted = 'N')"; //remove this if run number null
        
        
        
        Query nullQuery = em.createQuery(QUERY);
        
        
        nullQuery.setParameter("userId", "test");
        nullQuery.setParameter("investigationType", ElementType.INVESTIGATION);
        nullQuery.setParameter("invType", "experiment");
        nullQuery.setParameter("visitId", "12");
        nullQuery.setParameter("invNumber", "12345");
        nullQuery.setParameter("bcatInvStr", "damian");
        nullQuery.setParameter("title", "Investigation without any investigators");
        nullQuery.setParameter("grantId", null);
        nullQuery.setParameter("invAbstract", null);
        
        nullQuery.setParameter("instrument", "SXD");
        
        nullQuery.setParameter("surname", "user");
        
        nullQuery.setParameter("keyword", "shull");
        
        nullQuery.setParameter("sampleName", "SrF2 calibration  w=-25.3");
        
        nullQuery.setParameter("dataFileType", ElementType.DATAFILE);
        nullQuery.setParameter("datafileName", "SXD015554.RAW");
        nullQuery.setParameter("upperTime", new Date());
        nullQuery.setParameter("lowerTime", new Date(System.currentTimeMillis()-900000000));
        
        nullQuery.setParameter("dataFileType", ElementType.DATAFILE);
        nullQuery.setParameter("upper", 11257f);
        nullQuery.setParameter("lower", 100f);
        
        
        System.out.println(nullQuery.getResultList());
        tearDown();
    }
    
    public void testSurname(){
        setUp();
        String SAMPLES_BY_NAME_JPQL = Queries.SAMPLES_BY_NAME_JPQL;
        
        Query query = em.createQuery(SAMPLES_BY_NAME_JPQL).
                setParameter("userId","test").
                setParameter("name","%calibration%").
                setParameter("objectType",ElementType.INVESTIGATION);
        
        //setParameter("objectType",ElementType.INVESTIGATION).
        //setParameter("userId","test").
        
        System.out.println(query.getResultList());
        
        tearDown();
    }
    
    public void testDelete(){
        try {
            setUp();
            Datafile kw = em.find(Datafile.class, 2L);
            
            kw.setCreateId("test");
            kw.setFacilityAcquired("N");
            
            DataFileManager.deleteDataFile("test", kw, em);
            tearDown();
        } catch (InsufficientPrivilegesException ex) {
            java.util.logging.Logger.getLogger("global").log(Level.SEVERE, null, ex);
        } catch (NoSuchObjectFoundException ex) {
            java.util.logging.Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }
    
    public void testDeleteKeyword(){
        try {
            setUp();
            uk.icat3.entity.Keyword kw = em.find(uk.icat3.entity.Keyword.class, new uk.icat3.entity.KeywordPK("shull", 3L));
            
            kw.setCreateId("test");
            kw.setFacilityAcquired("N");
            
            uk.icat3.manager.InvestigationManager.deleteInvestigationObject("test", kw, uk.icat3.util.AccessType.DELETE, em);
            tearDown();
        } catch (InsufficientPrivilegesException ex) {
            java.util.logging.Logger.getLogger("global").log(Level.SEVERE, null, ex);
        } catch (NoSuchObjectFoundException ex) {
            java.util.logging.Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }
    
    public void addParameter() throws Exception{
        
        setUp();
        
        DatafileParameterPK PK = new DatafileParameterPK("newffy", "newffy", 2L);
        DatafileParameter pa = new DatafileParameter(PK);
        pa.setNumericValue(3d);
        
        DataFileManager.addDataFileParameter("test_icatadmin", pa, 105L, em);
        
        tearDown();
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestJPA ts = new TestJPA();
        // ts.createInv();
        //   ts.createDF();
        //    ts.createDS();
        //ts.addRole();
        // ts.getRoles();
        //  ts.createInv();
        //ts.testJPA();
        //  ts.changeRole();
         ts.testP1();
        // ts.testSurname();
        
        //  ts.testDelete();
        //  ts.addParameter();
        
    }
    
    
    
}
