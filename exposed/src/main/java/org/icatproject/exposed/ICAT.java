package org.icatproject.exposed;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.Application;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.DatafileParameter;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.FacilityCycle;
import org.icatproject.core.entity.Group;
import org.icatproject.core.entity.InputDatafile;
import org.icatproject.core.entity.InputDataset;
import org.icatproject.core.entity.Instrument;
import org.icatproject.core.entity.InstrumentScientist;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.InvestigationUser;
import org.icatproject.core.entity.Job;
import org.icatproject.core.entity.Keyword;
import org.icatproject.core.entity.NotificationRequest;
import org.icatproject.core.entity.OutputDatafile;
import org.icatproject.core.entity.OutputDataset;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.Publication;
import org.icatproject.core.entity.RelatedDatafile;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleParameter;
import org.icatproject.core.entity.Shift;
import org.icatproject.core.entity.Study;
import org.icatproject.core.entity.Study.StudyStatus;
import org.icatproject.core.entity.StudyInvestigation;
import org.icatproject.core.entity.User;
import org.icatproject.core.entity.UserGroup;
import org.icatproject.core.manager.EntityInfo;

@Stateless
@WebService(targetNamespace = "http://icatproject.org")
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class ICAT {

	private static Logger logger = Logger.getLogger(ICAT.class);

	@EJB
	private BeanManagerBean beanManagerBean;

	@Resource
	WebServiceContext webServiceContext;

	public ICAT() {
	}

	@WebMethod
	public List<?> search(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query) throws IcatException {
		return beanManagerBean.search(sessionId, query);
	}

	@WebMethod
	public long create(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		return beanManagerBean.create(sessionId, bean);
	}

	@WebMethod
	public List<Long> createMany(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "beans") List<EntityBaseBean> beans) throws IcatException {
		return beanManagerBean.createMany(sessionId, beans);
	}

	@WebMethod
	public void deleteMany(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "beans") List<EntityBaseBean> beans) throws IcatException {
		beanManagerBean.deleteMany(sessionId, beans);
	}

	@WebMethod
	public void delete(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		beanManagerBean.delete(sessionId, bean);
	}

	@WebMethod
	public void update(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		beanManagerBean.update(sessionId, bean);
	}

	@WebMethod
	public EntityBaseBean get(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query, @WebParam(name = "primaryKey") long primaryKey)
			throws IcatException {
		return beanManagerBean.get(sessionId, query, primaryKey);
	}

	@WebMethod
	public EntityInfo getEntityInfo(@WebParam(name = "beanName") String beanName)
			throws IcatException {
		return beanManagerBean.getEntityInfo(beanName);
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
			@WebParam Job job, @WebParam InputDataset inputDataset,
			@WebParam OutputDataset outputDataset, @WebParam InputDatafile inputDatafile,
			@WebParam OutputDatafile outputDatafile,
			@WebParam NotificationRequest notificationRequest, @WebParam Group group,
			@WebParam UserGroup userGroup) {
		beanManagerBean.dummy(facility);
	}

	@WebMethod
	public String login(@WebParam(name = "plugin") String plugin,
			@WebParam(name = "credentials") Map<String, String> credentials) throws IcatException {
		MessageContext msgCtxt = webServiceContext.getMessageContext();
		HttpServletRequest req = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
		return beanManagerBean.login(plugin, credentials, req);
	}

	@WebMethod
	public void logout(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		beanManagerBean.logout(sessionId);
	}

	@WebMethod
	public String getUserName(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		return beanManagerBean.getUserName(sessionId);

	}

	@WebMethod()
	public double getRemainingMinutes(@WebParam(name = "sessionId") String sessionId)
			throws IcatException {
		return beanManagerBean.getRemainingMinutes(sessionId);
	}

	@WebMethod()
	public String getApiVersion() throws IcatException {
		return Constants.API_VERSION;
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

}
