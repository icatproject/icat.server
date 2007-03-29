/*
 * TestDataFileManager.java
 *
 * Created on 27 March 2007, 11:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.EntityManagerResource;

/**
 *
 * @author gjd37
 */
public class TestDataFileManager {
    
    static String userId = "JAMES-JAMES";
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
        //emf = Persistence.createEntityManagerFactory("icat3-isis");
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
    
    public Datafile createDataFile(String userId, Long datasetId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        
        setUp();
        
        Datafile file = new Datafile();
        file.setName("test name");
        
        Dataset dataset = em.find(Dataset.class, datasetId);
        file.setDatasetId(dataset);
        file.setModId(userId);
        
        Collection<DatafileFormat> datafileFormats = (Collection<DatafileFormat>)em.createQuery("select d from DatafileFormat d").getResultList();
        if(datafileFormats.size() == 0) throw new NoSuchObjectFoundException("No DatafileFormats found");
        
        file.setDatafileFormat(datafileFormats.iterator().next());
        
        em.persist(file);
        
        tearDown();
        
        return file;
    }
    
    public Datafile addDataFile(String userId, Long datasetId) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException{
        
        setUp();
        
        Datafile file = new Datafile();
        file.setName("new Test with long id");
        
        file.setModId(userId);
        Dataset dataset = em.find(Dataset.class, 4l);
        
        
        dataset.addDataFile(file);
        
        DatasetParameter parma = new DatasetParameter("yyyy-MM-dd HH:mm:ss","finish_date", 43l);
        
        dataset.addDataSetParamaeter(parma);
        
        dataset.isValid(em);
        
                
        
      
        
        tearDown();
        
        return file;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        TestDataFileManager tdfm = new TestDataFileManager();
        // tdfm.createDataFile(userId, 2L);
        tdfm.addDataFile(userId, 2L);
    }
    
}
