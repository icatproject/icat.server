/*
 * BaseTest.java
 *
 * Created on 22 February 2007, 12:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.IcatRole;

/**
 *
 * @author gjd37
 */
public class BaseTest {
    
    private static Logger log = Logger.getLogger(BaseTest.class);
    
    
    // TODO code application logic here
    static protected  EntityManagerFactory  emf = null;
    // Create new EntityManager
    static protected EntityManager  em = null;
    
    public static void setUp(){
        
        emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
        em = emf.createEntityManager();
        log.trace("");
        log.debug("setUp(), creating entityManager");
        
        
        // Begin transaction
        log.debug("beginning transaction on entityManager");
        
        em.getTransaction().begin();
        
    }
    
    public static void setUpEntityManagerOnly(){
        
        // emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
        em = emf.createEntityManager();
        log.trace("");
        log.debug("setUp(), creating entityManager");
        
    }
    
    public static void tearDownEntityManagerOnly(){
        
        log.debug("tearDown(), closing entityManager");
        log.trace("");
        em.close();
    }
    
    public static void setUpEntityManagerFactoryOnly(){
        
        emf = Persistence.createEntityManagerFactory(TestConstants.PERSISTENCE_UNIT);
        
    }
    
    public static void tearDownEntityManagerFactoryOnly(){
        
        emf.close();
    }
    
    public static void tearDown(){
        
        // Commit the transaction
        log.debug("commiting transaction on entityManager");
        em.getTransaction().commit();
        log.debug("tearDown(), closing entityManager");
        log.trace("");
        em.close();
    }
    
    protected Collection<Long> addAuthorisation(Long id, Long parent, String user, ElementType type , IcatRoles role){
        //add entry for a user who can delete this
        IcatAuthorisation icat = new IcatAuthorisation();
        IcatAuthorisation child = new IcatAuthorisation();
        
        icat.setElementId(id);
        icat.setElementType(type);
        icat.setUserId(user);
        icat.setModId(user);
        IcatRole icatRole =  new IcatRole(role.toString());
        icatRole.setActionCanRootRemove("Y");
        icatRole.setActionCanRemove("Y");
        icat.setRole(icatRole);
        
        if(type == ElementType.INVESTIGATION){
            icat.setParentElementId(null);
            icat.setParentElementType(null);
        } else if(type == ElementType.DATASET){
             icat.setParentElementId(parent);
            icat.setParentElementType(ElementType.INVESTIGATION);
        } else if(type == ElementType.DATAFILE){
             icat.setParentElementId(parent);
            icat.setParentElementType(ElementType.DATASET);
        }
        
        //add child
        if(type != ElementType.DATAFILE){
            
            if(type == ElementType.INVESTIGATION){
                child.setElementType(ElementType.DATASET);
                child.setParentElementType(ElementType.INVESTIGATION);
            } else {
                child.setElementType(ElementType.DATAFILE);
                child.setParentElementType(ElementType.DATASET);
            }
            child.setElementId(null);
            child.setParentElementId(id);
            child.setUserId(user);
            child.setModId(user);
            IcatRole role2 =  new IcatRole(role.toString());
            role2.setActionCanRootRemove("Y");
            child.setRole(role2);
            em.persist(child);
            log.trace("Saving: "+child);
            
            icat.setUserChildRecord(child.getId());
        }
        
        em.persist(icat);
        log.trace("Saving: "+icat);
        
        Collection longs = new ArrayList<Long>();
        longs.add(icat.getId());
        if(type != ElementType.DATAFILE){
            longs.add(child.getId());
        }
        
        return longs;
    }
    
    public static Collection<?> executeListResultCmd(String sql){
        return em.createQuery(sql).getResultList();
    }
    
    public static Object executeSingleResultCmd(String sql){
        return em.createQuery(sql).getSingleResult();
    }
    
    public static Collection<?> executeNativeListResultCmd(String sql, Class className){
        return em.createNativeQuery(sql).getResultList();
    }
    
    public static Object executeNativeSingleResultCmd(String sql, Class className){
        return em.createNativeQuery(sql, className).getSingleResult();
    }
}
