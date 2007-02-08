/*
 * CachingServiceLocator.java
 *
 * Created on 20 June 2006, 14:36
 */
package uk.icat3.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;

/**
 *
 * @author gjd37
 * @version
 */
public class CachingServiceLocator {
    
    
    private Map cache;
    private  static Logger log = Logger.getLogger(CachingServiceLocator.class);
    
    private static CachingServiceLocator me;
    
    static {
        try {
            me = new CachingServiceLocator();
        } catch(NamingException se) {
            throw new RuntimeException(se);
        }
    }
    
    private CachingServiceLocator() throws NamingException  {
        
        cache = Collections.synchronizedMap(new HashMap());
    }
    
    public static CachingServiceLocator getInstance() {
        return me;
    }
    
    
    public void put(EntityManagerFactory em){
        log.trace("inserting EntityManagerFactory");
        cache.put("EntityManagerFactory",em);
    }
    
    public EntityManagerFactory getEntityManagerFactory(){
        log.trace("Looking up EntityManagerFactory");
        return (EntityManagerFactory)cache.get("EntityManagerFactory");
    }
    
}

