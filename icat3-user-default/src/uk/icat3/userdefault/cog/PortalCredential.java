/*
 * PortalCredential.java
 *
 * Created on 24 March 2004, 09:37
 */

package uk.icat3.userdefault.cog;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.ietf.jgss.*;
import org.globus.gsi.*;
import org.globus.gsi.bc.*;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;

import org.globus.common.CoGProperties;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import org.apache.log4j.*;

/**
 *
 * @author  gjd37
 */
public class PortalCredential {
    
    private static GSSCredential portalProxy;
    private static Logger log = Logger.getLogger(PortalCredential.class);
    
    public static  GSSCredential getPortalProxy() throws Exception{
        if( portalProxy == null ) {
            createPortalProxy();
        } else    // proxy already exists
        {
            if( portalProxy.getRemainingLifetime() <= 120 )    // has proxy expired?
            {
                createPortalProxy();
            }
        }
        
        return portalProxy;
        
        
    }
    
    
    private static synchronized void createPortalProxy() throws Exception {
        X509Certificate portalCert = null;
        PrivateKey portalPrivateKey = null;
        X509Certificate caCerts[] = null;
        
        try {
            CoGProperties cog  = CoGProperties.getDefault();
            String cert = cog.getUserCertFile();
            String userkey = cog.getUserKeyFile();
            portalCert = CertUtil.loadCertificate(cert);
            OpenSSLKey key = new BouncyCastleOpenSSLKey(userkey);
            log.debug("Cert "+cert);
            log.debug("Key "+userkey);
            
            if (key.isEncrypted()) {
                //decrypt
                Properties prop = new Properties();
                prop.load(new FileInputStream(System.getProperty("user.home")+File.separator+".globus"+File.separator+"pass"));
                //log.trace("password : "+prop.getProperty("pass"));
                String password = prop.getProperty("pass");
                key.decrypt(password);
            }
            portalPrivateKey = key.getPrivateKey();
            
        }
        
        catch( Exception e ) {
            log.error("Cog properties not set correctly",e);
            throw e;
        }
        
        try {
            BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();
            GlobusCredential proxy = factory.createCredential(new X509Certificate[] {portalCert},portalPrivateKey, 512, 3600, GSIConstants.DELEGATION_FULL );
            
            // Create delegate credential
            portalProxy = new GlobusGSSCredentialImpl(proxy, GSSCredential.INITIATE_AND_ACCEPT);
            
            // X509Certificate [] portalCertChain = new X509Certificate[] { portalCert };
            //portalProxy = new GlobusProxy( portalPrivateKey, portalCertChain, caCerts );
        }
        
        catch( Exception e ) {
            log.fatal("Unable to create proxy",e);
            throw e;
        }
    }
    
}
