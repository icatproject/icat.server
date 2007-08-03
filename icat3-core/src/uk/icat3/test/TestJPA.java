/*
 * TestSearch.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.DatafileFormatPK;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.manager.DataFileManager;
import uk.icat3.manager.DataSetManager;
import uk.icat3.manager.InvestigationManager;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

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
        emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
        //emf = Persistence.createEntityManagerFactory("icat3-dls_dev_new");
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
        
        Investigation investigation = new Investigation();
        
        investigation.setTitle("investigation "+new Random().nextInt());
        investigation.setInvNumber(""+new Random().nextInt());
        investigation.setInvType(new InvestigationType("experiment"));
        
        InvestigationManager.createInvestigation("test_admin_investigation", investigation, em);
        
        tearDown();
    }
    
    public void createDS() throws Exception{
        setUp();
        
        Dataset ds = new Dataset();
        DatasetType type = new DatasetType();
        type.setName("analyzed");
        type.setDescription("Analyzed data");
        ds.setDatasetType(type);
        ds.setName("unit test create data set");
        
        DataSetManager.createDataSet("test_admin_investigation", ds, 100L, em);
        
        tearDown();
    }
    
    public void createDF() throws Exception{
        setUp();
        
        Datafile df = new Datafile();
        DatafileFormat type = new DatafileFormat();
        DatafileFormatPK pk = new DatafileFormatPK("3.0.0", "nexus");
        type.setDatafileFormatPK(pk);
        
        df.setDatafileFormat(type);
        df.setName("name of df");
        
        DataFileManager.createDataFile("test_admin_investigation", df, 100L, em);
        
        tearDown();
    }
    
    public void changeRole() throws Exception {
        setUp();
        
        InvestigationManager.updateAuthorisation("test_admin_investigation", "CREATOR", 103L, em);
        
        tearDown();
    }
    
    public void testJPA() throws Exception {
        setUp();
        
        Query nullQuery = em.createQuery( "SELECT i FROM IcatAuthorisation i WHERE " +
                "i.parentElementType = :parentElementType OR :parentElementType IS NULL");
        
        //try and find user with null as investigation
        //nullQuery.setParameter("elementType", ElementType.DATASET).
        //  nullQuery.setParameter("userId", "test_admin_investigation");
        
        
        nullQuery.setParameter("parentElementType", ElementType.DATASET);
        // nullQuery.setParameter("parentElementId", null);
        
        System.out.println(nullQuery.getResultList());
        //System.out.println(em.createQuery("SELECT i FROM IcatAuthorisation i WHERE i.elementType = :type1  AND (i.parentElementType = :type OR :type IS NULL)").setParameter("type1", ElementType.INVESTIGATION).setParameter("type", null).getResultList());
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestJPA ts = new TestJPA();
        
       // ts.createDF();
        // ts.createDS();
        ts.addRole();
        // ts.getRoles();
        //  ts.createInv();
        // ts.testJPA();
        //  ts.changeRole();
    }
    
    
    
}
