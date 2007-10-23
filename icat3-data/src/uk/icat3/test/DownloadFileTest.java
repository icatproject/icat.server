/*
 * DownloadFileTest.java
 *
 * Created on 18-Oct-2007, 09:56:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.io.File;
import javax.activation.DataHandler;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ietf.jgss.GSSCredential;
import uk.ac.dl.srbapi.cog.CogUtil;
import uk.icat3.data.DownloadManager;




/**
 *
 * @author gjd37
 */
public class DownloadFileTest {
    
    protected  static Logger log = Logger.getLogger(DownloadFileTest.class);
    
    /** Creates a new instance of DownloadFileTest */
    public DownloadFileTest() {
        PropertyConfigurator.configure(this.getClass().getResource("log4j.properties"));
    }
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    
    protected static void setUp(){
        emf = Persistence.createEntityManagerFactory("icat3-apitest");
        // emf = Persistence.createEntityManagerFactory("icatisis_dev");
        em = emf.createEntityManager();
        
        // Begin transaction
        //em.getTransaction().begin();
        
    }
    
    protected static void tearDown(){
        // Commit the transaction
        //em.getTransaction().commit();
        
        em.close();
    }
    
    public void testDownloadDatafile() throws Exception{
        
        setUp();
        
        System.out.println("Testing download datafile 2");
        
        GSSCredential proxy = CogUtil.loadProxy(new File(System.getProperty("user.home")+File.separator+"sso.cert"));
        
        DownloadManager.downloadDatafile("gjd37", 2L, proxy, em);
        
        tearDown();
    }
    
    public void testDownloadDataset() throws Exception{
        
        setUp();
        
        System.out.println("Testing download datafile 2");
        
        GSSCredential proxy = CogUtil.loadProxy(new File(System.getProperty("user.home")+File.separator+"sso.cert"));
        
        File file = DownloadManager.downloadDataset("gjd37", 106L, proxy, em);
                     
        System.out.println(file.getName()); 
        
        tearDown();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //
        // TODO code application logic here
        DownloadFileTest test  = new DownloadFileTest();
       // test.testDownloadDatafile();
        
       test.testDownloadDataset();
    }
    
}
