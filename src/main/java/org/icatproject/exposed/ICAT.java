package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;
import org.icatproject.authentication.Authenticator;
import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Application;
import org.icatproject.core.entity.DataCollection;
import org.icatproject.core.entity.DataCollectionDatafile;
import org.icatproject.core.entity.DataCollectionDataset;
import org.icatproject.core.entity.DataCollectionParameter;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.DatafileParameter;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.FacilityCycle;
import org.icatproject.core.entity.Grouping;
import org.icatproject.core.entity.Instrument;
import org.icatproject.core.entity.InstrumentScientist;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.InvestigationUser;
import org.icatproject.core.entity.Job;
import org.icatproject.core.entity.Keyword;
import org.icatproject.core.entity.Log;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.PublicStep;
import org.icatproject.core.entity.Publication;
import org.icatproject.core.entity.RelatedDatafile;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleParameter;
import org.icatproject.core.entity.Shift;
import org.icatproject.core.entity.Study;
import org.icatproject.core.entity.StudyInvestigation;
import org.icatproject.core.entity.StudyStatus;
import org.icatproject.core.entity.User;
import org.icatproject.core.entity.UserGroup;
import org.icatproject.core.manager.AccessType;
import org.icatproject.core.manager.CreateResponse;
import org.icatproject.core.manager.EntityBeanManager;
import org.icatproject.core.manager.EntityInfo;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.NotificationMessage;
import org.icatproject.core.manager.PropertyHandler;
import org.icatproject.core.manager.Transmitter;

@Stateless
@WebService(targetNamespace = "http://icatproject.org")
@TransactionManagement(TransactionManagementType.BEAN)
public class ICAT {

	private static Logger logger = Logger.getLogger(ICAT.class);

	private Map<String, Authenticator> authPlugins = new HashMap<String, Authenticator>();

	@EJB
	EntityBeanManager beanManager;

	@EJB
	GateKeeper gatekeeper;

	private int lifetimeMinutes;

	@PersistenceContext(unitName = "icat")
	private EntityManager manager;

	@EJB
	PropertyHandler propertyHandler;

	private Set<String> rootUserNames;

	@EJB
	Transmitter transmitter;

	@Resource
	private UserTransaction userTransaction;

	@Resource
	WebServiceContext webServiceContext;

	private void checkRoot(String sessionId) throws IcatException {
		String userId = beanManager.getUserName(sessionId, manager);
		if (!rootUserNames.contains(userId)) {
			throw new IcatException(IcatExceptionType.INSUFFICIENT_PRIVILEGES,
					"user must be in rootUserNames");
		}
	}

	@WebMethod
	public long create(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			CreateResponse createResponse = beanManager.create(userId, bean, manager,
					userTransaction, false);
			transmitter.processMessage(createResponse.getNotificationMessage());
			return createResponse.getPk();
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public List<Long> createMany(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "beans") List<EntityBaseBean> beans) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			List<CreateResponse> createResponses = beanManager.createMany(userId, beans, manager,
					userTransaction);
			List<Long> lo = new ArrayList<Long>();
			for (CreateResponse createResponse : createResponses) {
				transmitter.processMessage(createResponse.getNotificationMessage());
				lo.add(createResponse.getPk());
			}
			return lo;
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void delete(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			transmitter.processMessage(beanManager.delete(userId, bean, manager, userTransaction));
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void deleteMany(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "beans") List<EntityBaseBean> beans) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			List<NotificationMessage> nms = beanManager.deleteMany(userId, beans, manager,
					userTransaction);
			for (NotificationMessage nm : nms) {
				transmitter.processMessage(nm);
			}
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}

	}

	@WebMethod
	public void dummy(@WebParam Datafile datafile, @WebParam DatafileFormat datafileFormat,
			@WebParam DatafileParameter datafileParameter, @WebParam Dataset dataset,
			@WebParam DatasetParameter datasetParameter, @WebParam DatasetType datasetType,
			@WebParam Facility facility, @WebParam FacilityCycle facilityCycle,
			@WebParam InstrumentScientist facilityInstrumentScientist, @WebParam User user,
			@WebParam Instrument instrument, @WebParam Investigation investigation,
			@WebParam InvestigationType investigationType,
			@WebParam InvestigationUser investigator, @WebParam Keyword keyword,
			@WebParam ParameterType parameter, @WebParam Publication publication,
			@WebParam RelatedDatafile relatedDatafile, @WebParam Sample sample,
			@WebParam SampleParameter sampleParameter, @WebParam Shift shift,
			@WebParam Study study, @WebParam StudyInvestigation studyInvestigation,
			@WebParam StudyStatus studyStatus, @WebParam Application application,
			@WebParam Job job, @WebParam DataCollection dataCollection,
			@WebParam DataCollectionParameter dataCollectionParameter,
			@WebParam DataCollectionDataset dataCollectionDataset,
			@WebParam DataCollectionDatafile dataCollectionDatafile, @WebParam Grouping group,
			@WebParam UserGroup userGroup, @WebParam Log log, @WebParam PublicStep publicStep) {
	}

	@WebMethod
	public EntityBaseBean get(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query, @WebParam(name = "primaryKey") long primaryKey)
			throws IcatException {
		try {
			String userId = getUserName(sessionId);
			return beanManager.get(userId, query, primaryKey, manager, userTransaction);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod()
	public String getApiVersion() throws IcatException {
		return Constants.API_VERSION;
	}

	@WebMethod
	public EntityInfo getEntityInfo(@WebParam(name = "beanName") String beanName)
			throws IcatException {
		return beanManager.getEntityInfo(beanName);
	}

	@WebMethod()
	public List<String> getEntityNames() throws IcatException {
		return EntityInfoHandler.getEntityNamesList();
	}

	@WebMethod
	public List<String> getProperties(@WebParam(name = "sessionId") String sessionId)
			throws IcatException {
		checkRoot(sessionId);
		return beanManager.getProperties();
	}

	@WebMethod()
	public double getRemainingMinutes(@WebParam(name = "sessionId") String sessionId)
			throws IcatException {
		return beanManager.getRemainingMinutes(sessionId, manager);
	}

	@WebMethod
	public String getUserName(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		return beanManager.getUserName(sessionId, manager);
	}

	@PostConstruct
	private void init() {
		authPlugins = propertyHandler.getAuthPlugins();
		lifetimeMinutes = propertyHandler.getLifetimeMinutes();
		rootUserNames = gatekeeper.getRootUserNames();
	}

	@WebMethod
	public boolean isAccessAllowed(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean,
			@WebParam(name = "accessType") AccessType accessType) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			return beanManager.isAccessAllowed(userId, bean, manager, userTransaction, accessType);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public String login(@WebParam(name = "plugin") String plugin,
			@WebParam(name = "credentials") Map<String, String> credentials) throws IcatException {
		MessageContext msgCtxt = webServiceContext.getMessageContext();
		HttpServletRequest req = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
		Authenticator authenticator = authPlugins.get(plugin);
		if (authenticator == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Authenticator mnemonic " + plugin + " not recognised");
		}
		logger.debug("Using " + plugin + " to authenticate");
		String userName = authenticator.authenticate(credentials, req.getRemoteAddr())
				.getUserName();
		return beanManager.login(userName, lifetimeMinutes, manager, userTransaction);
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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logger.debug("Other exception " + baos.toString());
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, baos.toString());
		} finally {
			long time = System.currentTimeMillis() - start;
			logger.debug("Method " + target + " took " + time / 1000f + "s to execute");
		}
	}

	@WebMethod
	public void logout(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		beanManager.logout(sessionId, manager, userTransaction);
	}

	@WebMethod
	public void luceneClear(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		try {
			checkRoot(sessionId);
			beanManager.luceneClear();
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void luceneCommit(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		try {
			checkRoot(sessionId);
			beanManager.luceneCommit();
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public List<String> luceneGetPopulating(@WebParam(name = "sessionId") String sessionId)
			throws IcatException {
		try {
			checkRoot(sessionId);
			return beanManager.luceneGetPopulating();
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void lucenePopulate(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "entityName") String entityName) throws IcatException {
		try {
			checkRoot(sessionId);
			beanManager.lucenePopulate(entityName, manager);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public List<String> luceneSearch(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query, @WebParam(name = "maxCount") int maxCount,
			@WebParam(name = "entityName") String entityName) throws IcatException {
		try {
			checkRoot(sessionId);
			return beanManager.luceneSearch(query, maxCount, entityName, manager);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void refresh(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		beanManager.refresh(sessionId, lifetimeMinutes, manager, userTransaction);
	}

	private void reportIcatException(IcatException e) throws IcatException {
		if (e.getType() == IcatExceptionType.INTERNAL) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logger.debug("Internal exception " + baos.toString());
		} else {
			logger.debug("IcatException " + e.getType() + " " + e.getMessage()
					+ (e.getOffset() >= 0 ? " at offset " + e.getOffset() : ""));
		}
	}

	private void reportThrowable(Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(baos);
		e.printStackTrace(s);
		s.close();
		logger.error("Unexpected failure in Java "
				+ System.getProperties().getProperty("java.version") + " " + baos);
	}

	@WebMethod
	public List<?> search(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			return beanManager.search(userId, query, manager, userTransaction);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public List<?> searchText(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query, @WebParam(name = "maxCount") int maxCount,
			@WebParam(name = "entityName") String entityName) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			return beanManager.searchText(userId, query, maxCount, entityName, manager,
					userTransaction);
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void update(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			transmitter.processMessage(beanManager.update(userId, bean, manager, userTransaction,
					false));
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

}
