package org.icatproject.exposed;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

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
import org.icatproject.core.entity.FieldSet;
import org.icatproject.core.manager.AccessType;
import org.icatproject.core.manager.AuthenticatorInfo;
import org.icatproject.core.manager.CreateResponse;
import org.icatproject.core.manager.EntityBeanManager;
import org.icatproject.core.manager.EntityInfo;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.manager.NotificationTransmitter;
import org.icatproject.core.manager.PropertyHandler;
import org.icatproject.core.manager.PropertyHandler.ExtendedAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@WebService(targetNamespace = "http://icatproject.org")
@TransactionManagement(TransactionManagementType.BEAN)
public class ICAT {

	private static Logger logger = LoggerFactory.getLogger(ICAT.class);

	private Map<String, ExtendedAuthenticator> authPlugins = new HashMap<>();

	@EJB
	EntityBeanManager beanManager;

	private int lifetimeMinutes;

	@EJB
	PropertyHandler propertyHandler;

	private Set<String> rootUserNames;

	@EJB
	NotificationTransmitter transmitter;

	@Resource
	WebServiceContext webServiceContext;

	private void checkRoot(String sessionId) throws IcatException {
		String userId = beanManager.getUserName(sessionId);
		if (!rootUserNames.contains(userId)) {
			throw new IcatException(IcatExceptionType.INSUFFICIENT_PRIVILEGES, "user must be in rootUserNames");
		}
	}

	@WebMethod
	public long create(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "bean") EntityBaseBean bean)
			throws IcatException {
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		try {
			String userId = getUserName(sessionId);
			CreateResponse createResponse = beanManager.create(userId, bean, false, ip);
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
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		try {
			String userId = getUserName(sessionId);
			List<CreateResponse> createResponses = beanManager.createMany(userId, beans, ip);
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
	public void delete(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "bean") EntityBaseBean bean)
			throws IcatException {
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		try {
			String userId = getUserName(sessionId);
			ArrayList<EntityBaseBean> beans = new ArrayList<EntityBaseBean>();
			beans.add(bean);
			beanManager.delete(userId, beans, ip);
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
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		try {
			String userId = getUserName(sessionId);
			beanManager.delete(userId, beans, ip);
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
			@WebParam DatasetParameter datasetParameter, @WebParam DatasetType datasetType, @WebParam Facility facility,
			@WebParam FacilityCycle facilityCycle, @WebParam InstrumentScientist facilityInstrumentScientist,
			@WebParam User user, @WebParam Instrument instrument, @WebParam Investigation investigation,
			@WebParam InvestigationType investigationType, @WebParam InvestigationUser investigator,
			@WebParam Keyword keyword, @WebParam ParameterType parameter, @WebParam Publication publication,
			@WebParam RelatedDatafile relatedDatafile, @WebParam Sample sample,
			@WebParam SampleParameter sampleParameter, @WebParam Shift shift, @WebParam Study study,
			@WebParam StudyInvestigation studyInvestigation, @WebParam StudyStatus studyStatus,
			@WebParam Application application, @WebParam Job job, @WebParam DataCollection dataCollection,
			@WebParam DataCollectionParameter dataCollectionParameter,
			@WebParam DataCollectionDataset dataCollectionDataset,
			@WebParam DataCollectionDatafile dataCollectionDatafile, @WebParam Grouping group,
			@WebParam UserGroup userGroup, @WebParam PublicStep publicStep, @WebParam FieldSet fieldSet) {
	}

	@WebMethod
	public EntityBaseBean get(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "query") String query,
			@WebParam(name = "primaryKey") long primaryKey) throws IcatException {
		try {
			String userId = getUserName(sessionId);
			String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
					.getRemoteAddr();
			return beanManager.get(userId, query, primaryKey, ip);
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
	public List<AuthenticatorInfo> getAuthenticatorInfo() throws IcatException {
		List<AuthenticatorInfo> infos = new ArrayList<>();
		for (Entry<String, ExtendedAuthenticator> entry : authPlugins.entrySet()) {
			String mnemonic = entry.getKey();
			ExtendedAuthenticator auth = entry.getValue();
			infos.add(new AuthenticatorInfo(mnemonic, auth));
		}
		return infos;
	}

	@WebMethod
	public EntityInfo getEntityInfo(@WebParam(name = "beanName") String beanName) throws IcatException {
		return beanManager.getEntityInfo(beanName);
	}

	@WebMethod()
	public List<String> getEntityNames() throws IcatException {
		return EntityInfoHandler.getEntityNamesList();
	}

	@WebMethod
	public List<String> getProperties(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		checkRoot(sessionId);
		return beanManager.getProperties();
	}

	@WebMethod()
	public double getRemainingMinutes(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		return beanManager.getRemainingMinutes(sessionId);
	}

	@WebMethod
	public String getUserName(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		return beanManager.getUserName(sessionId);
	}

	@WebMethod()
	public String getVersion() throws IcatException {
		return Constants.API_VERSION;
	}

	@PostConstruct
	private void init() {
		authPlugins = propertyHandler.getAuthPlugins();
		lifetimeMinutes = propertyHandler.getLifetimeMinutes();
		rootUserNames = propertyHandler.getRootUserNames();
	}

	@WebMethod
	public boolean isAccessAllowed(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean, @WebParam(name = "accessType") AccessType accessType)
			throws IcatException {
		try {
			String userId = getUserName(sessionId);
			return beanManager.isAccessAllowed(userId, bean, accessType);
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
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		ExtendedAuthenticator extendedAuthenticator = authPlugins.get(plugin);
		if (extendedAuthenticator == null) {
			throw new IcatException(IcatException.IcatExceptionType.SESSION,
					"Authenticator mnemonic " + plugin + " not recognised");
		}
		Authenticator authenticator = extendedAuthenticator.getAuthenticator();
		logger.debug("Using " + plugin + " to authenticate");
		String userName = authenticator.authenticate(credentials, ip).getUserName();
		return beanManager.login(userName, lifetimeMinutes, ip);
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
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		beanManager.logout(sessionId, ip);
	}

	@WebMethod
	public void refresh(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		beanManager.refresh(sessionId, lifetimeMinutes, ip);
	}

	private void reportIcatException(IcatException e) throws IcatException {
		if (e.getType() == IcatExceptionType.INTERNAL) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logger.error("Internal exception " + baos.toString());
		} else {
			logger.error("IcatException " + e.getType() + " " + e.getMessage()
					+ (e.getOffset() >= 0 ? " at offset " + e.getOffset() : ""));
		}
	}

	private void reportThrowable(Throwable e) {
		logger.error("Unexpected failure in Java " + System.getProperties().getProperty("java.version"), e);
	}

	@WebMethod
	public List<?> search(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "query") String query)
			throws IcatException {
		try {
			String userId = getUserName(sessionId);
			String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
					.getRemoteAddr();
			List<?> result = beanManager.search(userId, query, ip);
			// special handling for fieldsets so they can be marshalled 
			if (!result.isEmpty() && result.get(0) instanceof Object[]) {
				List<FieldSet> newResults = new ArrayList<FieldSet>();
				for (int i = 0; i < result.size(); i++) {
					if (result.get(i) instanceof Object[]) {
						FieldSet fieldSet = new FieldSet((Object[]) result.get(i));
						newResults.add(fieldSet);
					} else {
						logger.warn("Something in the fieldset isn't an Object[]");
					}
				}
				result = newResults;
			}
			return result;
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

	@WebMethod
	public void update(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "bean") EntityBaseBean bean)
			throws IcatException {
		String ip = ((HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST))
				.getRemoteAddr();
		try {
			String userId = getUserName(sessionId);
			transmitter.processMessage(beanManager.update(userId, bean, false, ip));
		} catch (IcatException e) {
			reportIcatException(e);
			throw e;
		} catch (Throwable e) {
			reportThrowable(e);
			throw new IcatException(IcatException.IcatExceptionType.INTERNAL, e.getMessage());
		}
	}

}
