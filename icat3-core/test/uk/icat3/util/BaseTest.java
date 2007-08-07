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
        
        emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
        em = emf.createEntityManager();
        log.trace("");
        log.debug("setUp(), creating entityManager");
        
        
        // Begin transaction
        log.debug("beginning transaction on entityManager");
        
        em.getTransaction().begin();
        
    }
    
    public static void setUpEntityManagerOnly(){
        
        // emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
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
        
        emf = Persistence.createEntityManagerFactory("icat3-scratch-testing-PU");
        
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
    
    protected Collection<Long> addInvestigationAuthorisation(Long id, String user, IcatRoles role){
        //add entry for a user who can delete this
        IcatAuthorisation icat = new IcatAuthorisation();
        icat.setElementId(id);
        icat.setElementType(ElementType.INVESTIGATION);
        icat.setUserId(user);
        icat.setModId(user);
        IcatRole icatRole =  new IcatRole(role.toString());
        icatRole.setActionRootRemove("Y");
        icatRole.setActionRemove("Y");
        icat.setRole(icatRole);
        
        //add child
        IcatAuthorisation child = new IcatAuthorisation();
        child.setElementId(null);
        child.setElementType(ElementType.DATASET);
        child.setParentElementId(id);
        child.setParentElementType(ElementType.INVESTIGATION);
        child.setUserId(user);
        child.setModId(user);
        IcatRole role2 =  new IcatRole(role.toString());
        role2.setActionRootRemove("Y");
        child.setRole(role2);
        em.persist(child);
        log.trace("Saving: "+child);
        
        icat.setUserChildRecord(child.getId());
        em.persist(icat);
        log.trace("Saving: "+icat);
        
        Collection longs = new ArrayList<Long>();
        longs.add(icat.getId());
        longs.add(child.getId());
        
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
