/*
 * TestUser.java
 *
 * Created on 20 February 2007, 12:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.test;

import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.SessionException;
import uk.icat3.user.UserManager;
import uk.icat3.userdefault.entity.User;


/**
 *
 * @author gjd37
 */
public class TestUser {
    
    protected  static Logger log = Logger.getLogger(TestUser.class);
    
    // TODO code application logic here
    static EntityManagerFactory  emf = null;
    // Create new EntityManager
    static EntityManager  em = null;
    
    /**
     * Creates a new instance of TestUser
     */
    public TestUser() {
    }
    
    protected static void setUp(){
        
        emf = Persistence.createEntityManagerFactory("icat3-user-defaultPU");
        em = emf.createEntityManager();
        
        // Begin transaction
        em.getTransaction().begin();
    }
    
    protected static void tearDown(){
        // Commit the transaction
        em.getTransaction().commit();
        
        em.close();
    }
    
    public void login(String username, String password, int lifetime) throws SessionException {
        
        setUp();
        long time  = System.currentTimeMillis();
        UserManager userManager = new UserManager("uk.icat3.userdefault.facility.DefaultUser", em);
        
        userManager.login(username,password,lifetime);
        System.out.println((System.currentTimeMillis() - time)/1000f);
        
        
        
        tearDown();
    }
    
    public void loginAdmin(String username, String password, String runAs) throws SessionException {
        
        setUp();
        long time  = System.currentTimeMillis();
        UserManager userManager = new UserManager("uk.icat3.userdefault.facility.DefaultUser", em);
        
        userManager.login(username,password,runAs);
        System.out.println((System.currentTimeMillis() - time)/1000f);
        
        
        
        tearDown();
    }
    
    
    public void logout(String sessionId) throws SessionException {
        
        setUp();
        long time  = System.currentTimeMillis();
        UserManager userManager = new UserManager("uk.icat3.userdefault.facility.DefaultUser", em);
        
        userManager.logout(sessionId);
        System.out.println((System.currentTimeMillis() - time)/1000f);
        
        
        
        tearDown();
    }
    
    public void getUserId(String sessionId) throws SessionException {
        
        setUp();
        long time  = System.currentTimeMillis();
        UserManager userManager = new UserManager("uk.icat3.userdefault.facility.DefaultUser", em);
        
        String userId =  userManager.getUserIdFromSessionId(sessionId);
        System.out.println(userId);
        System.out.println((System.currentTimeMillis() - time)/1000f);
        
        
        
        tearDown();
    }
    
    
    public void insertSession(){
        
        setUp();
        uk.icat3.userdefault.entity.Session se = new uk.icat3.userdefault.entity.Session();
        se.setCredential("dd");
        
        User user = em.find(User.class, 1L);
        se.setUserId(user);
        se.setUserSessionId("df");
        se.setExpireDateTime(new Date());
        se.setCredential("-----BEGIN CERTIFICATE-----MIIB7TCCAZegAwIBAgICIhswDQYJKoZIhvcNAQEEBQAwdjELMAkGA1UEBhMCVUsxETAPBgNVBAoTCGVTY2llbmNlMQ0wCwYDVQQLEwRDTFJDMQswCQYDVQQHEwJETDEYMBYGA1UEAxMPZ2xlbiBkcmlua3dhdGVyMQ4wDAYDVQQDEwVwcm94eTEOMAwGA1UEAxMFcHJveHkwHhcNMDcwMzIwMTEwMTA3WhcNMDcwMzIwMTMwNjA3WjCBhjELMAkGA1UEBhMCVUsxETAPBgNVBAoTCGVTY2llbmNlMQ0wCwYDVQQLEwRDTFJDMQswCQYDVQQHEwJETDEYMBYGA1UEAxMPZ2xlbiBkcmlua3dhdGVyMQ4wDAYDVQQDEwVwcm94eTEOMAwGA1UEAxMFcHJveHkxDjAMBgNVBAMTBXByb3h5MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKd9NUYTkWTvVRyj0CPQ3netMoDO94Y4+RakiN7sn0st6fmZxG4Tn6/MStDb+8S4COFiUCxRLEcHkc8PYLjDbX8CAwEAATANBgkqhkiG9w0BAQQFAANBAJwE93hderi2+LlY/wdpEzwLUUumv+YLkq8/cY2Tz3NY/PwxuEuHnFulK836PDEiN2THjTnQ4BeNZwYlhyf/Lag=-----END CERTIFICATE----------BEGIN RSA PRIVATE KEY-----MIIBOQIBAAJBAKd9NUYTkWTvVRyj0CPQ3netMoDO94Y4+RakiN7sn0st6fmZxG4Tn6/MStDb+8S4COFiUCxRLEcHkc8PYLjDbX8CAwEAAQJANmbl8fATLszKQXY3+hoy2H6KQ+p13cQNrDwfNQF4sCsgI9tP2xPj8vxvgV5HZOuCoHoaLO18l6y7dzv1TUus0QIhANNXpA6m3Lg86SZEAo3f05k21SjpPErCoh/SqMK9WMPJAiEAyuFeO4cqmd6pJZxgrYiwcNIlBfxDiTguzxSdyVhg+wcCIBEwJYWTiIvrWELmcRa8x2bEkN186oxh+/F+nn8ICzoRAiAeeg6C2MJAjR9RxBUN3IvM1vSy2nt2wJbfitRNK9advwIgeSQorhb0TXXFdAKDR20TeoHyPIPh/urjrfi1DUCmdN8=-----END RSA PRIVATE KEY----------BEGIN CERTIFICATE-----" +
                "MIIBzDCCAXagAwIBAgICIhswDQYJKoZIhvcNAQEFBQAwZjELMAkGA1UEBhMCVUsxETAPBgNVBAoTCGVTY2llbmNlMQ0wCwYDVQQLEwRDTFJDMQswCQYDVQQHEwJETDEYMBYGA1UEAxMPZ2xlbiBkcmlua3dhdGVyMQ4wDAYDVQQDEwVwcm94eTAeFw0wNzAzMTkxMTQ1MDZaFw0wNzAzMjYxMTUwMDRaMHYxCzAJBgNVBAYTAlVLMREwDwYDVQQKEwhlU2NpZW5jZTENMAsGA1UECxMEQ0xSQzELMAkGA1UEBxMCREwxGDAWBgNVBAMTD2dsZW4gZHJpbmt3YXRlcjEOMAwGA1UEAxMFcHJveHkxDjAMBgNVBAMTBXByb3h5MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAPA+WbQhEZR9pVrGMlQnlOVNeqjxUTt2fhFrka/tfXeE/vtP343916yBno1210UDWmCnpx52J6u4zcrlYpJ/hncCAwEAATANBgkqhkiG9w0BAQUFAANBAIxvvz982pvNFv//v0rEVMKFVMLYJ7T9eJaKxFEjnymtbSJ2yc4AwSIbAROzx7KXRZ6/NVAQvueMxx2Rl6mF9mA=-----END CERTIFICATE----------BEGIN CERTIFICATE-----MIIB7TCCAVagAwIBAgICIhswDQYJKoZIhvcNAQEFBQAwVjELMAkGA1UEBhMCVUsxETAPBgNVBAoTCGVTY2llbmNlMQ0wCwYDVQQLEwRDTFJDMQswCQYDVQQHEwJETDEYMBYGA1UEAxMPZ2xlbiBkcmlua3dhdGVyMB4XDTA3MDMxOTExNDUwNFoXDTA3MDMyNjExNTAwNFowZjELMAkGA1UEBhMCVUsxETAPBgNVBAoTCGVTY2llbmNlMQ0wCwYDVQQLEwRDTFJDMQswCQYDVQQHEwJETDEYMBYGA1UEAxMPZ2xlbiBkcmlua3dhdGVyMQ4wDAYDVQQDEwVwcm94eTBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQDAgD8Q9sV1Jg8fk2PW85WhSTklfsFdFdCBjLXm7cClx3/tQyI3FpvohMPCVVlIUCRYhtgDE5W0WBKIAm7nEDdhAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAVBA0Qcg5WR0Ouie9lIZZ" +
                "kFaM2ViBLx4JlXF4E9EoLFx22BN7hF/ArNQpU2A84Hn/ANTTPbAqpf4bZUAftfie/3Ieuw6bBjyDWHW/sInh9eUoojh17jSDPEeFXBX4Fs5s2XTis4XJimDeB/za4oLGmCG3/NyQXQaj0HxYx7HhWwc=-----END CERTIFICATE----------BEGIN CERTIFICATE-----MIIE2DCCA8CgAwIBAgICIhswDQYJKoZIhvcNAQEFBQAwQzELMAkGA1UEBhMCVUsxEzARBgNVBAoTCmVTY2llbmNlQ0ExEjAQBgNVBAsTCUF1dGhvcml0eTELMAkGA1UEAxMCQ0EwHhcNMDYxMDI2MTM0ODA3WhcNMDcxMTI1MTM0ODA3WjBWMQswCQYDVQQGEwJVSzERMA8GA1UEChMIZVNjaWVuY2UxDTALBgNVBAsTBENMUkMxCzAJBgNVBAcTAkRMMRgwFgYDVQQDEw9nbGVuIGRyaW5rd2F0ZXIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAK4Qn/pJ1dGY860J8B9YMioAUojmzkQMG/7RAIhAs9W/ASn2vhyJeWFHYKw2kBsSbPDUDfWMiG3fGLbUqhUe433oEqaQ1VV/LSFfaNWxILMbqHPX9DIAkanT+SgPKLf/sAYFKw5u4C8QKgsqeJuh0l6ntEpOxOiqasmE/Uvf8RwLAgMBAAGjggJFMIICQTAMBgNVHRMBAf8EAjAAMBEGCWCGSAGG+EIBAQQEAwIFoDAOBgNVHQ8BAf8EBAMCA+gwLAYJYIZIAYb4QgENBB8WHVVLIGUtU2NpZW5jZSBVc2VyIENlcnRpZmljYXRlMB0GA1UdDgQWBBRukPmb9KIFFFiMzXUkYDuK4tB2mjB8BgNVHSMEdTBzgBT/pakabg9vlOk30cClRh2oIZNqS6FYpFYwVDELMAkGA1UEBhMCVUsxFTATBgNVBAoTDGVTY2llbmNlUm9vdDESMBAGA1UECxMJQXV0aG9yaXR5MQ0wCwYDVQQHEwRS" +
                "b290MQswCQYDVQQDEwJDQYIBATAiBgNVHREEGzAZgRdnLmouZHJpbmt3YXRlckBkbC5hYy51azAlBgNVHRIEHjAcgRpzdXBwb3J0QGdyaWQtc3VwcG9ydC5hYy51azAZBgNVHSAEEjAQMA4GDCsGAQQB2S8BAQEBBzBJBglghkgBhvhCAQQEPBY6aHR0cDovL2NhLmdyaWQtc3VwcG9ydC5hYy51ay9wdWIvY3JsL2VzY2llbmNlLXJvb3QtY3JsLmNybDBHBglghkgBhvhCAQMEOhY4aHR0cDovL2NhLmdyaWQtc3VwcG9ydC5hYy51ay9wdWIvY3JsL2VzY2llbmNlLWNhLWNybC5jcmwwSQYDVR0fBEIwQDA+oDygOoY4aHR0cDovL2NhLmdyaWQtc3VwcG9ydC5hYy51ay9wdWIvY3JsL2VzY2llbmNlLWNhLWNybC5jcmwwDQYJKoZIhvcNAQEFBQADggEBAJhns3MgypmEGkg367mlbi6eJlDi3XWCHNrxAZiwXgun38ClEmU83+rd8akQ4+mNFe3RHgcPXlu0qBC3w9s+ZX6Gw11Q5DpJn1+bZIFrxZFEt1BmJEPgwwAvbjFa4EUpulk05Hl4nA25eo9x6Sxc3TV+pxBW" +
                "sOFJMwNqkftzDn4NviTDvatyi+2czTFkig4GNYP6usOenfmRL5nPbWPptdpy3hIUKKenULAB0qr4kIMzdEtgYnqelJB1zIu+Af6YF/Trq+R/WYOJIZyxVTtZp6OASBPRkTQfNFUQWJ93Md3b2gDFST1UWH4+abla4Y38yuOcuQ02geB35XItI24whes=---" +
                "--END CERTIFICATE-----");
     
        em.persist(se);
        
        tearDown();
    }
    
    
    public static void main(String[] args)throws SessionException {
        TestUser tu = new TestUser();
        
        for(int i = 0; i < 1; i++){
                 tu.login("glen","kkkkkk",2);
        }
        // tu.insertSession();
       // tu.logout("18c7f182-031f-42c2-a351-bf3ef3f7484e");
        
       // tu.getUserId("ad38ced4-6e7d-4df6-b6b4-a893d3826d8c");
        //tu.loginAdmin("admin","password","bob");
    }
}


