package org.icatproject.exposed;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

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
import org.icatproject.exposed.interceptor.LogoutInterceptor;
import org.icatproject.exposed.manager.BeanManagerLocal;
import org.icatproject.exposed.util.Constants;

@Stateless
@WebService(targetNamespace = "http://icatproject.org")
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class ICAT extends EJBObject {

	@EJB
	private BeanManagerLocal beanManagerLocal;

	@Resource
	WebServiceContext webServiceContext;

	public ICAT() {
	}

	@WebMethod
	public List<?> search(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query) throws IcatException {
		return beanManagerLocal.search(sessionId, query);
	}

	@WebMethod
	public long create(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		return beanManagerLocal.create(sessionId, bean);
	}

	@WebMethod
	public List<Long> createMany(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "beans") List<EntityBaseBean> beans) throws IcatException {
		return beanManagerLocal.createMany(sessionId, beans);
	}

	@WebMethod
	public void delete(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		beanManagerLocal.delete(sessionId, bean);
	}

	@WebMethod
	public void update(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean) throws IcatException {
		beanManagerLocal.update(sessionId, bean);
	}

	@WebMethod
	public EntityBaseBean get(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query, @WebParam(name = "primaryKey") long primaryKey)
			throws IcatException {
		return beanManagerLocal.get(sessionId, query, primaryKey);
	}

	@WebMethod
	public EntityInfo getEntityInfo(@WebParam(name = "beanName") String beanName)
			throws IcatException {
		return beanManagerLocal.getEntityInfo(beanName);
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
		beanManagerLocal.dummy(facility);
	}

	@WebMethod
	@ExcludeClassInterceptors
	public String login(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password) throws IcatException {
		MessageContext msgCtxt = webServiceContext.getMessageContext();
		HttpServletRequest req = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
		return user.login(username, password, req);
	}

	@WebMethod
	@Interceptors(LogoutInterceptor.class)
	public void logout(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		user.logout(sessionId);
	}

	@WebMethod
	public String getUserName(@WebParam(name = "sessionId") String sessionId) throws IcatException {
		return this.user.getUserName(sessionId);

	}

	@WebMethod()
	public double getRemainingMinutes(@WebParam(name = "sessionId") String sessionId)
			throws IcatException {
		return this.user.getRemainingMinutes(sessionId);
	}

	@WebMethod()
	public String getApiVersion() throws IcatException {
		return Constants.API_VERSION;
	}

}
