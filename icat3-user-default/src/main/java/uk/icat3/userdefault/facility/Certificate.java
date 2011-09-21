/*
 * Certificate.java
 *
 * Created on 13 April 2006, 10:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.facility;

// Globus certificate
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import org.apache.log4j.Logger;
import org.globus.gsi.*;
import org.globus.gsi.gssapi.*;
import java.security.cert.CertificateException;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;

import org.ietf.jgss.*;

// Streams
import java.io.ByteArrayInputStream;

// Testing
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This is a util class used to manipulate the GSSCredential from MyProxy
 *
 * @author gjd37
 */

public class Certificate {
    
    private String certificate;
    private GSSCredential credential;
    static Logger log = Logger.getLogger(Certificate.class);
    
    
    /**
     * Creates a new instance of certificate from a string representation of a GSSCredential
     *
     * @param certificate
     * @throws java.security.cert.CertificateException
     */
    public Certificate(String certificate) throws CertificateException {
        this.certificate = certificate;
        loadCredential(this.certificate);
    }
    
    /**
     * Creates a new instance of certificate from a GSSCredential
     *
     * @param credential
     * @throws java.security.cert.CertificateException
     */
    public Certificate(GSSCredential credential) throws CertificateException {
        log.debug("new Certificate(credential)");
        try {
            this.certificate =  turnintoString(credential);
        } catch (IOException ioe) {
            throw new CertificateException("Unable to read in credential: "+ioe.getMessage(),ioe);
        } catch (GSSException ex) {
            throw new CertificateException("Unable to turn credential into string: "+ex.getMessage(),ex);
        }
        this.credential = credential;
    }
    
    /**
     * Creates a new instance of certificate from a string
     *
     * @param certificate
     * @throws java.security.cert.CertificateException
     * @throws java.security.cert.CertificateExpiredException
     */
    public Certificate(java.io.InputStream certificate) throws CertificateException , CertificateExpiredException{
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(certificate));
            String inputLine;
            StringBuffer cert = new StringBuffer();
            while ((inputLine = in.readLine()) != null){
                cert.append(inputLine);
                cert.append("\n");
            }
            in.close();
            
            this.certificate = cert.toString();
        } catch (IOException ex) {
            throw new CertificateException("Unable to read in credential: "+ex.getMessage(),ex);
            
        }
        loadCredential(this.certificate);
    }
    
    /**
     * Creates a new instance of certificate from a file
     *
     * @param certificate
     * @throws java.security.cert.CertificateException
     * @throws java.security.cert.CertificateExpiredException
     */
    public Certificate(java.io.File certificate) throws CertificateException , CertificateExpiredException{
        try {
            BufferedReader in = new BufferedReader(new java.io.FileReader(certificate));
            String inputLine;
            StringBuffer cert = new StringBuffer();
            while ((inputLine = in.readLine()) != null){
                cert.append(inputLine);
                cert.append("\n");
            }
            in.close();
            
            this.certificate = cert.toString();
        } catch (FileNotFoundException ex) {
            throw new CertificateException("Unable to find in credential: "+ex.getMessage(),ex);
            
        } catch (IOException ex) {
            throw new CertificateException("Unable to read in credential: "+ex.getMessage(),ex);
            
        }
        loadCredential(this.certificate);
    }
    
    /**
     * Creates a new instance of certificate from a file
     * e.g. file:///E:/cog-1.1/build/cog-1.1/bin/x509up_36855.pem
     *
     * @param url
     * @throws java.security.cert.CertificateException
     */
    public Certificate(URL url) throws CertificateException {
        try {
            URLConnection con = url.openConnection();
            InputStream in2 = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(in2));
            String inputLine;
            StringBuffer cert = new StringBuffer();
            while ((inputLine = in.readLine()) != null){
                cert.append(inputLine);
                cert.append("\n");
            }
            in.close();
            
            this.certificate = cert.toString();
        } catch (IOException ex) {
            throw new CertificateException("Unable to read in credential: "+ex.getMessage(),ex);
        }
        loadCredential(this.certificate);
    }
    
    private void loadCredential(String cred) throws CertificateException, CertificateExpiredException{
        try {
            
            byte [] data = cred.getBytes();
            
            ExtendedGSSManager manager = (ExtendedGSSManager)ExtendedGSSManager.getInstance();
            credential = (GSSCredential)manager.createCredential(data,ExtendedGSSCredential.IMPEXP_OPAQUE,  GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.INITIATE_AND_ACCEPT);
            
        } catch (GSSException ex) {
            throw new CertificateException("Unable to load credential: "+ex.getMessage(),ex);
        }
        int lifetime = 0;
        try {
            lifetime = credential.getRemainingLifetime();
        } catch (GSSException ex) {
            
        }
        if(lifetime < 60*2 /*secs*/) throw new CertificateExpiredException("Credential for "+getDn()+" has expired");
        
        
    }
    
    /**
     * Returns the  number of seconds life of the credential
     * 
     * @throws java.security.cert.CertificateException 
     * @return number seconds left
     */
    public long getLifetime() throws CertificateException {
        try {
            // Get remaining lifetime in seconds
            long lifetimeLeft = credential.getRemainingLifetime();
            return lifetimeLeft;
        } catch (GSSException ex) {
            throw new CertificateException("Unable to get remaining lifetime: "+ex.getMessage(),ex);
        }
    }
    
    /**
     * Checks to see if there are more than 10 mins left of a credential
     *
     * @throws java.security.cert.CertificateException 
     * @return 
     */
    public boolean isLifetimeLeft() throws CertificateException {
        boolean result = false;
        // Check some lifetime left on proxy certificate (more than 10 mins left)
        if (getLifetime() > 60*10) {
            result = true;
        }
        return result;
    }
    
    /**
     * Gets the DN of the credential 
     *
     * @throws java.security.cert.CertificateException 
     * @return Dn of GSSCredential object
     */
    public String getDn() throws CertificateException {
        try {
            String DN =  credential.getName().toString();
            return DN;
        } catch (GSSException ex) {
            throw new CertificateException("Unable to get DN: "+ex.getMessage(),ex);
        }
    }
    
    /**
     * Gets the credential in the form of a string.  This makes it easier to pass over EJBs and Web Services
     * 
     * @return string of GSSCredential object
     */
    public String getStringRepresentation(){
        return this.certificate;
    }
    
    @Override
    public String toString(){
        try {
            return getDn()+" has lifetime "+getLifetime()+" seconds";
        } catch (Exception ex) {
            return "UNKNOWN has lifetime UNKNOWN seconds";
        }
    }
    
    /**
     * Returns the GSSCredential object
     *
     * @return GSSCredential object
     */
    public GSSCredential getCredential(){
        return credential;
    }
    
   /* public boolean isValid(){
    
    }
    
    private Session getSessionImpl() throws CertificateException{
        try {
            User user  = (User) em.createNamedQuery("User.findByDn").setParameter("dn",getDName()).getSingleResult();
            user.getSession()
        } catch (GSSException ex) {
            throw new CertificateException("Unable to get DN from credential: "+ex.getMessage());
        }
    
    }
    
    public Session getSession(){
    
    }*/
    
    // Testing stub
    private static void prt(Certificate c) throws Exception {
        System.out.println("Certificate: "+c.toString());
        System.out.println("Lifetime: "+c.getLifetime());
        System.out.println("Any life left? "+c.isLifetimeLeft());
        System.out.println("DN: "+c.getDn());
    }
    
    private static synchronized String turnintoString(GSSCredential cred) throws IOException, GSSException{
        log.debug("turning credential into string");
        // File file = new File(Config.getContextPath()+"globuscred.txt");
        //FileOutputStream out = new FileOutputStream(file);
        
        ExtendedGSSCredential extendcred = (ExtendedGSSCredential)cred;
        byte [] data = extendcred.export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        File file = new File(System.getProperty("java.io.tmpdir")+File.separator+"globuscred.txt");
        FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();
        
        
        URL url1  = new URL("file:///"+file.getAbsolutePath());
        // System.out.println(url);
        URLConnection con = url1.openConnection();
        InputStream in2 = con.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(in2));
        String inputLine;
        // String certt;
        StringBuffer cert = new StringBuffer();
        while ((inputLine = in.readLine()) != null){
            //System.out.println(inputLine);
            cert.append(inputLine);
            cert.append("\n");
            //  if(!inputLine.equals("-----END CERTIFICATE-----"))  cert.append("\n");
            
        }
        in.close();
        in2.close();
        out.close();
        //end of file save
        
        
        log.trace("Deleted file: "+file.getAbsolutePath()+" ? "+file.delete());
        //System.out.println(cert);
        return cert.toString();
        
    }
    
    public static void main(String args[]) throws Exception {
        
        Certificate cert;
        
        // Name of proxy that has expired
        cert = new Certificate(new URL("file:///E:/cog-1.1/build/cog-1.1/bin/x509up_36855.pem"));
        prt(cert);
        
        // Name of current proxy file
        cert = new Certificate(new URL("file:///E:/cog-1.1/build/cog-1.1/bin/x509up_47677.pem"));
        prt(cert);
        
        // test with certificate as string
        //Certificate cert2 = new Certificate(cert.toString());
        //prt(cert2);
    }
    
}