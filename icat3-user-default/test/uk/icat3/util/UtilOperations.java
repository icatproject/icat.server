/*
 * UtilOperations.java
 *
 * Created on 21 March 2007, 16:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import uk.icat3.userdefault.entity.*;
/**
 *
 * @author gjd37
 */
public class UtilOperations {
    
    private static Logger log = Logger.getLogger(UtilOperations.class);
    
    public static Session putInvalidSession(EntityManager em){
        Session session = null;
        try {
            session = (Session) em.createNamedQuery("Session.findByUserSessionId").setParameter("userSessionId", "expiredsession").getSingleResult();
            log.trace("Already got expired session");
            Calendar cal =  GregorianCalendar.getInstance();
            cal.add(GregorianCalendar.HOUR,-15); //minus 15 hours, expired
            session.setExpireDateTime(cal.getTime());
            
        } catch(NoResultException ex) {
            //create one
            log.trace("Creating expired session");
            //input invalid one
            session = new Session();
            Calendar cal =  GregorianCalendar.getInstance();
            cal.add(GregorianCalendar.HOUR,-5); //minus 5 hours, expired
            session.setExpireDateTime(cal.getTime());
            session.setUserSessionId("expiredsession");
            
            session.setCredential("expiredsession_CREDENTIAL");
            em.persist(session);
            
        }
        return session;
    }
    
    public static Session getSession(String sid, EntityManager em) throws SessionException {
        Session session = null;
        try {
            session = (Session) em.createNamedQuery("Session.findByUserSessionId").setParameter("userSessionId", sid).getSingleResult();
            
        } catch(NoResultException ex) {
            throw new SessionException("Invalid session thrown from unit test!");
        }
        
        return session;
    }
    
    public static Session putValidSession(EntityManager em){
        Session session = null;
        
        //input invalid one
        session = new Session();
        
        Calendar cal =  GregorianCalendar.getInstance();
        cal.add(GregorianCalendar.HOUR,2); //add 5 hours, valid
        session.setExpireDateTime(cal.getTime());
        session.setUserSessionId("testcorrectsid"+Math.random());
        
        session.setCredential("testcorrectsid_CREDENTIAL");
        
        //create user
        User user = new User();
        user.setDn("test correct dn");
        user.setUserId("correctuserid"+Math.random());
        
        user.addSession(session);
        
        log.info("persisting session: "+session.getUserSessionId());
        em.persist(user);
        
        return session;
    }
    
    public static Session putValidAdminSession(String runAs, EntityManager em) throws Exception {
        try {
            Session session = null;
            
            //input invalid one
            session = new Session();
            
            Calendar cal =  GregorianCalendar.getInstance();
            cal.add(GregorianCalendar.HOUR,2); //add 5 hours, valid
            session.setExpireDateTime(cal.getTime());
            session.setUserSessionId("testcorrect"+runAs+"sid"+Math.random());
            
            session.setCredential("testcorrect"+runAs+"sid_CREDENTIAL");
            session.setRunAs(runAs);
            
            User adminUser = null;
            try {
                log.trace("Finding user "+runAs+" in user tables");
                adminUser = (User) em.createNamedQuery("User.findByUserId").setParameter("userId", runAs).getSingleResult();
                
            } catch(NoResultException ex) {
                log.info("Creating "+runAs+" user");
                //create user
                adminUser = new User();
                adminUser.setDn("test correct "+runAs+" dn");
                adminUser.setUserId(runAs);
                adminUser.setPassword(runAs+"password");
            }
            
            adminUser.addSession(session);
            
            log.info("persisting "+runAs+" session: "+session.getUserSessionId());
            em.persist(adminUser);
            
            return session;
        } catch(Exception e) {
            log.error(e);
            e.printStackTrace();
            throw e;
        }
    }
    
    public static ProxyServers loadMyProxyServer(EntityManager em){
        List<ProxyServers> servers = em.createQuery("SELECT p from ProxyServers p").getResultList();
        
        if(servers == null || servers.size() == 0){
            log.debug("Creating proxy servers");
            ProxyServers server  = new ProxyServers();
            server.setCaRootCertificate("/C=UK/O=eScience/OU=CLRC/L=DL/CN=host/myproxy.grid-support.ac.uk/E=a.j.richards@dl.ac.uk");
            server.setProxyServerAddress("myproxy.grid-support.ac.uk");
            server.setPortNumber(7512);
            server.setActive(true);
            em.persist(server);
            return server;
        }
        return null;        
    }
}
