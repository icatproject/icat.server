/*
 * EJBObject.java
 *
 */
package uk.icat3.sessionbeans;

import java.io.File;
import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author gjd37
 */
public abstract class EJBObject {
    
    static Logger log = Logger.getLogger(EJBObject.class);
    
    @PersistenceContext(unitName="icat3-exposed")
    protected EntityManager manager;
    
    @PersistenceContext(unitName="icat3-exposed-user")
    protected EntityManager managerUser;
    
    // For junit testing only
    public void setEntityManager(EntityManager manager){
        this.manager = manager;
    }
    
    public void setEntityManagerUser(EntityManager manager){
        this.managerUser = manager;
    }
    
    public Object mergeEntity(Object entity) {
        return manager.merge(entity);
    }
    
    public Object persistEntity(Object entity) {
        manager.persist(entity);
        return entity;
    }
    
    public Object refreshEntity(Object entity) {
        manager.refresh(entity);
        return entity;
    }
    
    public void removeEntity(Object entity) {
        manager.remove(manager.merge(entity));
    }
    
    @PostConstruct
    public void init(){
        
        //load config from user.home
        PropertyConfigurator.configure(System.getProperty("user.home")+File.separator+"log4j.properties");
        //PropertyConfigurator.configure(ClassLoader.getSystemResource("log4j.properties"));
        
        log.debug("Loaded log4j properties from : "+System.getProperty("user.home")+File.separator+"log4j.properties");
        
    }
    
    /**
     * AOP all method, log time of method call.
     */
    @AroundInvoke
    public Object logMethods(InvocationContext ctx)
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
