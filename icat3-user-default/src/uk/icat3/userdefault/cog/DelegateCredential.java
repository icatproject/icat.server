/*
 * DelegateCredential.java
 *
 * Created on 24 March 2004, 09:57
 */

package uk.icat3.userdefault.cog;

import org.apache.log4j.Logger;
import org.ietf.jgss.*;
import org.globus.myproxy.*;

/**
 *
 * @author  gjd37
 */
public abstract class DelegateCredential {
// Data specific to MyProxyServer
    
    static Logger log = Logger.getLogger(DelegateCredential.class);
    
    public static GSSCredential getProxy( String aUsername, String aPassPhrase,int lifetime, GSSCredential thePortalProxy , String host, Integer port, String dn)
    throws GSSException,  MyProxyException {
        GSSCredential portalProxy = thePortalProxy;
        String username = aUsername;
        String userPassphrase = aPassPhrase;
        
        // check that portal proxy hasn't expired
        if( portalProxy == null || portalProxy.getRemainingLifetime() <= 0 ) {
            throw new MyProxyException( "Invalid server portal proxy: "+portalProxy.getName().toString() );
        }
        
        log.trace("Server Proxy Ok");
        log.info("Connecting to "+host+":"+port+", with DN: "+dn);
        
        org.globus.myproxy.MyProxy proxy = new org.globus.myproxy.MyProxy( host, port );
        GSSCredential delegateUserProxy = proxy.get( host, port, portalProxy, username, userPassphrase, lifetime *3600 /*turn into seconds*/, dn );
        log.trace("Retrieved user proxy.");
        return delegateUserProxy;
    }
}

//*********************************************************************


