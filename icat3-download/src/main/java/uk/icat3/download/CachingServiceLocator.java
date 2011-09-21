/*
 * CachingServiceLocator.java
 *
 * Created on 22 October 2007, 14:52
 */
package uk.icat3.download;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.*;

/**
 *
 * @author gjd37
 * @version
 */
public class CachingServiceLocator {
    
     private static Logger log = Logger.getLogger(CachingServiceLocator.class);
  
    private InitialContext ic;
    private Map cache;
    
    private static CachingServiceLocator me;
    
    static {
        try {
            me = new CachingServiceLocator();
        } catch(NamingException se) {
            throw new RuntimeException(se);
        }
    }
    
    private CachingServiceLocator() throws NamingException  {
        ic = new InitialContext();
        cache = Collections.synchronizedMap(new HashMap());
    }
    
    public static CachingServiceLocator getInstance() {
        return me;
    }
    
    public Object lookup(String jndiName) throws NamingException {
        Object cachedObj = cache.get(jndiName);
        if (cachedObj == null) {
            log.trace(jndiName+" not in lookup. Looking up now");
            cachedObj = ic.lookup(jndiName);
            cache.put(jndiName, cachedObj);
        }
        return cachedObj;
    } 
}

