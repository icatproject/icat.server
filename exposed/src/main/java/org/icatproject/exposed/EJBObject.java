package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.icatproject.core.IcatException;
import org.icatproject.exposed.user.UserSessionLocal;


public abstract class EJBObject {

	private static Logger logger = Logger.getLogger(EJBObject.class);

	@PersistenceContext(unitName = "icat")
	protected EntityManager manager;
	@EJB
	protected UserSessionLocal user;

	@PostConstruct
	protected void init() {
		/*
		 * Set up log4j. Note that even if the requested log4j.properties file
		 * is not found or is corrupt log4j will do its best to produce some
		 * output. The file will be checked for changes every minute. Existing
		 * properties will NOT be removed - so to reduce logging you may need to
		 * specify a logging level of INHERIT to take values from further up the
		 * tree.
		 */
		String log4jFile = "log4j.properties";
		LogManager.resetConfiguration();
		PropertyConfigurator.configureAndWatch(log4jFile);
		logger = Logger.getLogger(EJBObject.class);
		logger.info("Loaded log4j properties from : " + log4jFile + " and will watch it.");
	}

	@AroundInvoke
	protected Object logMethods(InvocationContext ctx) throws IcatException {

		String className = ctx.getTarget().getClass().getName();
		String methodName = ctx.getMethod().getName();
		String target = className + "." + methodName + "()";

		long start = System.currentTimeMillis();

		logger.debug("Invoking " + target);
		try {
			return ctx.proceed();
		} catch (IcatException e) {
			throw e;
		} catch (Exception e) {
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		} finally {
			long time = System.currentTimeMillis() - start;
			logger.debug("Method " + target + " took " + time / 1000f + "s to execute");
		}
	}

	protected void reportThrowable(Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(baos);
		e.printStackTrace(s);
		s.close();
		logger.error("Unexpected failure in Java " + System.getProperties().getProperty("java.version") + " " + baos);
	}

}
