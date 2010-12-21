/*
 * EJBObject.java
 *
 */
package uk.icat3.sessionbeans;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uk.icat3.exceptions.SessionException;
import uk.icat3.sessionbeans.user.UserSessionLocal;
import uk.icat3.sessionbeans.util.Constants;

/**
 *
 * @author gjd37
 */
public abstract class EJBObject {

    static Logger log = Logger.getLogger(EJBObject.class);
    protected static String FACILITY;
    protected static Properties facilityProps;
    @PersistenceContext(unitName = "icat3-exposed")
    protected EntityManager manager;
    @EJB
    protected UserSessionLocal user;

    // For junit testing only
    public void setUserSession(UserSessionLocal localUserSession) {
        this.user = localUserSession;
    }

    // For junit testing only
    public void setEntityManager(EntityManager manager) {
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
    protected void init() {

        //load resource bundle
        URL url = this.getClass().getResource("/uk/icat3/sessionbeans/facility.properties");
        facilityProps = new Properties();
        String facilityLogFile = null;
        try {
            facilityProps.load(url.openStream());
            facilityLogFile = facilityProps.getProperty("facility.name");
        } catch (Exception mre) {
            facilityLogFile = "ISIS";
            System.out.println("Unable to load props file, setting log as  " + facilityLogFile + "\n" + mre);
        }
        FACILITY = facilityLogFile;

		/*
		 * Set up log4j. Note that even if the requested log4j.properties file is not found or is
		 * corrupt log4j will do its best to produce some output. The file will be checked for
		 * changes every minute. Existing properties will NOT be removed - so to reduce logging you
		 * may need to specify a logging level of INHERIT to take values from further up the tree.
		 */
		String log4jFile = "log4j.properties";
		LogManager.resetConfiguration();
		PropertyConfigurator.configureAndWatch(log4jFile);
		log = Logger.getLogger(EJBObject.class);
		log.info("Loaded log4j properties from : " + log4jFile + " and will watch it.");

        //check if overrides default session implementation
        File sessionConf = new File(System.getProperty("user.home") + File.separator + ".session.conf");
        if (sessionConf.exists()) {
            try {
                Properties props2 = new Properties();
                props2.load(new FileInputStream(sessionConf));

                String sessionImplementation = props2.getProperty("session.impl.class");

                log.info("Setting session implementation as: " + sessionImplementation);
                Constants.DEFAULT_USER_IMPLEMENTATION = sessionImplementation;
            } catch (Exception ex) {
                log.fatal("Session configuration is set up incorrectly.", ex);
                return;
            }
        }
    }

    /**
     * AOP all method, log time of method call.
     */
    @AroundInvoke
    protected Object logMethods(InvocationContext ctx) throws Exception {

        String className = ctx.getTarget().getClass().getName();
        String methodName = ctx.getMethod().getName();
        String target = className + "." + methodName + "()";

        long start = System.currentTimeMillis();

        log.debug("Invoking " + target);
        try {
            return ctx.proceed();
        } catch (IllegalArgumentException e) {
            throw new SessionException(e.getMessage());
        } catch (Exception e) {
            throw e;
        } finally {
            long time = System.currentTimeMillis() - start;
            log.debug("Exiting " + target + " , This method takes " +
                    time / 1000f + "s to execute");
        }
    }
}
