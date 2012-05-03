package uk.icat3.sessionbeans;

import java.util.Collection;
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
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import uk.icat3.data.DownloadInfo;
import uk.icat3.entity.Application;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.Facility;
import uk.icat3.entity.FacilityCycle;
import uk.icat3.entity.Group;
import uk.icat3.entity.InputDatafile;
import uk.icat3.entity.InputDataset;
import uk.icat3.entity.Instrument;
import uk.icat3.entity.InstrumentScientist;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.InvestigationUser;
import uk.icat3.entity.Job;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.NotificationRequest;
import uk.icat3.entity.OutputDatafile;
import uk.icat3.entity.OutputDataset;
import uk.icat3.entity.ParameterType;
import uk.icat3.entity.Publication;
import uk.icat3.entity.RelatedDatafile;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.Shift;
import uk.icat3.entity.Study;
import uk.icat3.entity.Study.StudyStatus;
import uk.icat3.entity.StudyInvestigation;
import uk.icat3.entity.User;
import uk.icat3.entity.UserGroup;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.ICATAPIException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.jaxb.gen.DatasetStatus;
import uk.icat3.manager.EntityInfo;
import uk.icat3.sessionbeans.data.DownloadManagerLocal;
import uk.icat3.sessionbeans.interceptor.DownloadInterceptor;
import uk.icat3.sessionbeans.interceptor.LogoutInterceptor;
import uk.icat3.sessionbeans.manager.BeanManagerLocal;
import uk.icat3.sessionbeans.manager.XMLIngestionManagerLocal;
import uk.icat3.sessionbeans.util.Constants;
import uk.icat3.user.UserDetails;

@Stateless
@WebService(targetNamespace = "http://icatproject.org")
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class ICAT extends ICATCompat {

	@EJB
	private XMLIngestionManagerLocal xmlIngestionManagerLocal;
	@EJB
	private DownloadManagerLocal downloadManagerLocal;
	@EJB
	private BeanManagerLocal beanManagerLocal;

	@Resource
	WebServiceContext webServiceContext;

	public ICAT() {
	}

	@WebMethod
	public List<?> search(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query)
			throws BadParameterException, IcatInternalException,
			InsufficientPrivilegesException, SessionException {
		return beanManagerLocal.search(sessionId, query);
	}

	@WebMethod
	public Object create(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean)
			throws SessionException, InsufficientPrivilegesException,
			NoSuchObjectFoundException, ValidationException,
			ObjectAlreadyExistsException, IcatInternalException {
		return beanManagerLocal.create(sessionId, bean);
	}
	
	@WebMethod
	public List<Object> createMany(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "beans") List<EntityBaseBean> beans)
			throws SessionException, InsufficientPrivilegesException,
			NoSuchObjectFoundException, ValidationException,
			ObjectAlreadyExistsException, IcatInternalException {
		return beanManagerLocal.createMany(sessionId, beans);
	}

	@WebMethod
	public void delete(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean)
			throws SessionException, InsufficientPrivilegesException,
			NoSuchObjectFoundException, ValidationException,
			ObjectAlreadyExistsException, IcatInternalException {
		beanManagerLocal.delete(sessionId, bean);
	}

	@WebMethod
	public void update(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "bean") EntityBaseBean bean)
			throws SessionException, InsufficientPrivilegesException,
			NoSuchObjectFoundException, ValidationException,
			IcatInternalException {
		beanManagerLocal.update(sessionId, bean);
	}

	@WebMethod
	public EntityBaseBean get(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "query") String query,
			@WebParam(name = "primaryKey") Object primaryKey)
			throws SessionException, NoSuchObjectFoundException,
			InsufficientPrivilegesException, BadParameterException,
			IcatInternalException {
		return beanManagerLocal.get(sessionId, query, primaryKey);
	}

	@WebMethod
	public EntityInfo getEntityInfo(@WebParam(name = "beanName") String beanName)
			throws BadParameterException, IcatInternalException {
		return beanManagerLocal.getEntityInfo(beanName);
	}

	@WebMethod
	public void dummy(@WebParam Datafile datafile,
			@WebParam DatafileFormat datafileFormat,
			@WebParam DatafileParameter datafileParameter,
			@WebParam Dataset dataset,
			@WebParam DatasetParameter datasetParameter,
			@WebParam DatasetStatus datasetStatus,
			@WebParam DatasetType datasetType, @WebParam Facility facility,
			@WebParam FacilityCycle facilityCycle,
			@WebParam InstrumentScientist facilityInstrumentScientist,
			@WebParam User user, @WebParam Instrument instrument,
			@WebParam Investigation investigation,
			@WebParam InvestigationType investigationType,
			@WebParam InvestigationUser investigator,
			@WebParam Keyword keyword, @WebParam ParameterType parameter,
			@WebParam Publication publication,
			@WebParam RelatedDatafile relatedDatafile, @WebParam Sample sample,
			@WebParam SampleParameter sampleParameter, @WebParam Shift shift,
			@WebParam Study study,
			@WebParam StudyInvestigation studyInvestigation,
			@WebParam StudyStatus studyStatus,
			@WebParam Application application, @WebParam Job job,
			@WebParam InputDataset inputDataset,
			@WebParam OutputDataset outputDataset,
			@WebParam InputDatafile inputDatafile,
			@WebParam OutputDatafile outputDatafile,
			@WebParam NotificationRequest notificationRequest,
			@WebParam Group group, @WebParam UserGroup userGroup) {
		beanManagerLocal.dummy(facility);
	}

	@WebMethod(operationName = "loginAdmin")
	@ExcludeClassInterceptors
	public String loginAdmin(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "runAsUserFedId") String runAsUserFedId)
			throws SessionException {
		return user.loginAdmin(sessionId, runAsUserFedId);
	}

	@WebMethod
	@ExcludeClassInterceptors
	public String login(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password)
			throws SessionException, IcatInternalException {
		MessageContext msgCtxt = webServiceContext.getMessageContext();
		HttpServletRequest req = (HttpServletRequest) msgCtxt
				.get(MessageContext.SERVLET_REQUEST);
		return user.login(username, password, req);
	}

	@WebMethod(operationName = "loginLifetime")
	@ExcludeClassInterceptors
	@RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.loginLifetime")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.loginLifetimeResponse")
	public String login(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password,
			@WebParam(name = "lifetime") int lifetime) throws SessionException,
			IcatInternalException {
		MessageContext msgCtxt = webServiceContext.getMessageContext();
		HttpServletRequest req = (HttpServletRequest) msgCtxt
				.get(MessageContext.SERVLET_REQUEST);
		return user.login(username, password, lifetime, req);
	}

	@WebMethod
	@Interceptors(LogoutInterceptor.class)
	public boolean logout(@WebParam(name = "sessionId") String sessionId) {
		return user.logout(sessionId);
	}

	@WebMethod(operationName = "getUserDetails")
	public UserDetails getUserDetails(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "usersName") String usersName)
			throws SessionException, NoSuchUserException {
		return this.user.getUserDetails(sessionId, usersName);

	}

	@WebMethod()
	public boolean isSessionValid(@WebParam(name = "sessionId") String sessionId) {
		try {
			return this.user.isSessionValid(sessionId);
		} catch (Exception e) {
			return false;
		}
	}

	@WebMethod
	public Long[] ingestMetadata(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "xml") String xml) throws SessionException,
			ValidationException, InsufficientPrivilegesException,
			ICATAPIException {
		return xmlIngestionManagerLocal.ingestMetadata(sessionId, xml);
	}

	/**
	 * Downloads a datafile
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @param datafileId
	 *            datafile id that is to be downloaded
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return Url of the file
	 * @throws IcatInternalException
	 */
	@WebMethod
	@Interceptors(DownloadInterceptor.class)
	public @WebResult(name = "URL")
	String downloadDatafile(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "datafileId") Long datafileId)
			throws SessionException, NoSuchObjectFoundException,
			InsufficientPrivilegesException, IcatInternalException {
		return downloadManagerLocal.downloadDatafile(sessionId, datafileId);
	}

	/**
	 * Downloads a dataset
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @param datasetId
	 *            dataset id that is to be downloaded
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return Url of the zipped dataset
	 * @throws IcatInternalException
	 */
	@WebMethod
	@Interceptors(DownloadInterceptor.class)
	public @WebResult(name = "URL")
	String downloadDataset(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "datasetId") Long datasetId)
			throws SessionException, NoSuchObjectFoundException,
			InsufficientPrivilegesException, IcatInternalException {
		return downloadManagerLocal.downloadDataset(sessionId, datasetId);
	}

	/**
	 * Downloads a collection of datafiles
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @param datafileIds
	 *            Collection of the datafile ids that are to be downloaded
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return Url of the zipped datafiles
	 * @throws IcatInternalException
	 */
	@WebMethod
	@Interceptors(DownloadInterceptor.class)
	public @WebResult(name = "URL")
	String downloadDatafiles(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "datafileIds") Collection<Long> datafileIds)
			throws SessionException, NoSuchObjectFoundException,
			InsufficientPrivilegesException, IcatInternalException {
		return downloadManagerLocal.downloadDatafiles(sessionId, datafileIds);
	}

	/**
	 * Checks if user has access to download the files. This will be called from
	 * the data service to check that the request coming in is valid with ICAT
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @param datafileIds
	 *            ids of the files that are to be downloaded
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return DownloadInfo downloadinfo
	 * @throws IcatInternalException
	 */
	@WebMethod
	public @WebResult(name = "downloadInfo")
	DownloadInfo checkDatafileDownloadAccess(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "datafileIds") Collection<Long> datafileIds)
			throws SessionException, NoSuchObjectFoundException,
			InsufficientPrivilegesException, IcatInternalException {
		return downloadManagerLocal.checkDatafileDownloadAccess(sessionId,
				datafileIds);
	}

	/**
	 * Checks if user has access to download the dataset. This will be called
	 * from the data service to check that the request coming in is valid with
	 * ICAT
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @param datasetId
	 *            id of the dataset that are to be downloaded
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	 *             if entity does not exist in database
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return DownloadInfo downloadinfo
	 * @throws IcatInternalException
	 */
	@WebMethod
	public @WebResult(name = "downloadInfo")
	DownloadInfo checkDatasetDownloadAccess(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "datasetId") Long datasetId)
			throws SessionException, NoSuchObjectFoundException,
			InsufficientPrivilegesException, IcatInternalException {
		return downloadManagerLocal.checkDatasetDownloadAccess(sessionId,
				datasetId);
	}

	/**
	 * Returns the current version of the ICAT API in use
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	 *             if user has insufficient privileges to the object
	 * @return String the Current ICAT API Version (manually updated)
	 */
	@WebMethod(operationName = "getICATAPIVersion")
	public String getICATAPIVersion(
			@WebParam(name = "sessionId") String sessionId)
			throws SessionException, InsufficientPrivilegesException {
		return Constants.CURRENT_ICAT_API_VERSION;
	}

}
