package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.PropertyHandler;
import org.icatproject.core.manager.BeanManager;
import org.icatproject.core.manager.LuceneSingleton;

@Stateless
@WebService(targetNamespace = "http://icatproject.org")
@TransactionManagement(TransactionManagementType.BEAN)
@RolesAllowed("ICATAdmin")
public class ICATAdmin {

	private static Logger logger = Logger.getLogger(ICATAdmin.class);

	@PersistenceContext(unitName = "icat")
	protected EntityManager manager;

	@EJB
	Transmitter transmitter;

	@Resource
	private UserTransaction userTransaction;

	@Resource
	WebServiceContext webServiceContext;

	private LuceneSingleton lucene;

	@PostConstruct
	private void init() {
		PropertyHandler p = PropertyHandler.getInstance();
		if (p.getLuceneDirectory() != null) {
			lucene = LuceneSingleton.getInstance();
		}
	}

	@PreDestroy
	private void exit() {
		if (lucene != null) {
			lucene.close();
		}
	}

	@WebMethod
	public void luceneCommit() throws IcatException {
		try {
			BeanManager.luceneCommit(lucene);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}
	
	@WebMethod
	public List<String> props() {
		return BeanManager.props();
	}

	@WebMethod
	public void luceneClear() throws IcatException {
		try {
			BeanManager.luceneClear(lucene);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void lucenePopulate(@WebParam(name = "entityName") String entityName)
			throws IcatException {
		try {
			BeanManager.lucenePopulate(entityName, manager, lucene);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public List<String> luceneSearch(@WebParam(name = "query") String query,
			@WebParam(name = "maxCount") int maxCount,
			@WebParam(name = "entityName") String entityName) throws IcatException {
		try {
			return BeanManager.luceneSearch(query, maxCount, entityName, manager, lucene);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@AroundInvoke
	private Object logMethods(InvocationContext ctx) throws IcatException {

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

	private void reportIcatException(IcatException e) throws IcatException {
		logger.debug("IcatException " + e.getType() + " " + e.getMessage()
				+ (e.getOffset() >= 0 ? " at offset " + e.getOffset() : ""));
	}

	private void reportThrowable(Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(baos);
		e.printStackTrace(s);
		s.close();
		logger.error("Unexpected failure in Java "
				+ System.getProperties().getProperty("java.version") + " " + baos);
	}

}
