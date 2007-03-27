/*
 * EJBObject.java
 *
 */
package uk.icat3.sessionbeans;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.util.Constants;

/**
 *
 * @author gjd37
 */

public abstract class EJBObject {
    
    static Logger log = Logger.getLogger(EJBObject.class);
    
    @PersistenceContext(unitName="icat3-exposed")
    protected EntityManager manager;
    
    @EJB
    protected UserSessionLocal user;
    
    // For junit testing only
    protected void setEntityManager(EntityManager manager){
        this.manager = manager;
    }
    
    protected Object mergeEntity(Object entity) {
        return manager.merge(entity);
    }
    
    protected Object persistEntity(Object entity) {
        manager.persist(entity);
        return entity;
    }
    
    protected Object refreshEntity(Object entity) {
        manager.refresh(entity);
        return entity;
    }
    
    protected void removeEntity(Object entity) {
        manager.remove(manager.merge(entity));
    }
    
    @PostConstruct
    protected void init(){
        
        //load config from user.home
        PropertyConfigurator.configure(System.getProperty("user.home")+File.separator+".log4j.properties");
       // PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
        
        log.info("Loaded log4j properties from : "+System.getProperty("user.home")+File.separator+".log4j.properties");
        
        //check if overrides default session implementation
        File sessionConf =  new File(System.getProperty("user.home")+File.separator+".session.conf");
        if(sessionConf.exists()){
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(sessionConf));
                
                String sessionImplementation = props.getProperty("session.impl.class");
                
                log.info("Setting session implementation as: "+sessionImplementation);
                Constants.DEFAULT_USER_IMPLEMENTATION = sessionImplementation;
            } catch (Exception ex) {
                log.fatal("Session configuration is set up incorrectly.",ex);
                return ;
            } 
        }
    }
    
    /**
     * AOP all method, log time of method call.
     */
    @AroundInvoke   
    protected Object logMethods(InvocationContext ctx)
    throws Exception {
        
        String className = ctx.getTarget().getClass().getName();
        String methodName = ctx.getMethod().getName();
        String target = className + "." + methodName + "()";
        
        long start = System.currentTimeMillis();
        
        log.debug("Invoking " + target);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw e;
        } finally {
            long time = System.currentTimeMillis() - start;
            log.debug("Exiting " + target +" , This method takes " +
                    time/1000f + "s to execute\n");
            log.debug("\n");
        }
    }
}
