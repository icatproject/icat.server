/*
 * EntityManagerFactoryTest.java
 *
 * Created on 01-Jun-2007, 13:01:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exposed.util;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author gjd37
 */
public class EntityManagerFactoryTest {
    
    private String URL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(HOST=elektra.dl.ac.uk)(PROTOCOL=tcp)(PORT=1521))(CONNECT_DATA=(SID=minerva2)))";
    private String username = "icat_scratch";
    private String password = "c1sco";
    private EntityManagerFactory emf;
    
    private static EntityManagerFactoryTest emft;
    
    private  EntityManagerFactoryTest() {
        createFactory();
    }
    
    public EntityManager getEntityManager(){
        return emf.createEntityManager();
    }
        
     public EntityManagerFactory getEntityManagerFactory(){
        return emf;
    }
        
    
    public static EntityManagerFactoryTest getInstance(){
        synchronized(EntityManagerFactoryTest.class){
            if(emft == null){
                try {
                    emft = new EntityManagerFactoryTest();
                } catch(Exception se) {
                    throw new RuntimeException(se);
                }
            }
            return emft;
        }
    }
    
    private void createFactory(){
        if(emf == null){
            Map props = new HashMap();
            
            props.put("toplink.jdbc.user", username);
            props.put("toplink.jdbc.password", password);
            props.put("toplink.jdbc.url", URL);
            
            
            emf = Persistence.createEntityManagerFactory("icat3-scratch", props);
        }
    }
}
