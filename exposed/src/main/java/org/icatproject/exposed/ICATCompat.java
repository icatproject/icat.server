package org.icatproject.exposed;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.Investigation;
import org.icatproject.exposed.compatibility.AdvancedSearchDetails;

@Stateless
@WebService(targetNamespace = "client.icat3.uk")
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class ICATCompat {

	@SuppressWarnings("serial")
	public class SessionException extends Exception {

		public SessionException(IcatException icatException) {
			super(icatException.getMessage());
		}
	}

	@EJB
	private BeanManagerBean beanManagerLocal;

	public ICATCompat() {
	}

	private String getIN(List<String> ele) {
		final StringBuilder infield = new StringBuilder("(");
		for (final String t : ele) {
			if (infield.length() != 1) {
				infield.append(',');
			}
			infield.append('\'').append(t).append('\'');
		}
		infield.append(')');
		return infield.toString();
	}

	/**
	 * This gets all the keywords available to the user, they can only see keywords associated with
	 * their investigations or public investigations
	 */
	@SuppressWarnings("unchecked")
	@WebMethod
	public Collection<String> getKeywordsForUser(@WebParam(name = "sessionId") String sessionId)
			throws SessionException {
		try {
			return (List<String>) this.beanManagerLocal.search(sessionId, "DISTINCT Keyword.name");
		} catch (IcatException e) {
			throw new SessionException(e);
		}
	}

	/**
	 * This gets all the keywords available to the user - limited by count
	 */
	@SuppressWarnings("unchecked")
	@WebMethod(operationName = "getKeywordsForUserMax")
	@RequestWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserMax")
	@ResponseWrapper(className = "uk.icat3.sessionbeans.jaxws.getKeywordsForUserMaxResponse")
	public Collection<String> getKeywordsForUser(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "limit") int limit) throws SessionException {
		try {
			return (List<String>) this.beanManagerLocal.search(sessionId, "0," + limit
					+ " DISTINCT Keyword.name");
		} catch (IcatException e) {
			throw new SessionException(e);
		}
	}

	/**
	 * Lists all the investigations for the current user, ie who he is an investigator of
	 */
	@SuppressWarnings("unchecked")
	@WebMethod
	public Collection<Investigation> getMyInvestigations(
			@WebParam(name = "sessionId") String sessionId) throws SessionException {
		try {
			return (List<Investigation>) this.beanManagerLocal.search(sessionId, "Investigation");
		} catch (IcatException e) {
			throw new SessionException(e);
		}
	}

	/**
	 * SearchManager by a collection of keywords for investigations that user has access to view,
	 * with AND been operator, fuzzy false, no includes
	 * 
	 */
	@SuppressWarnings("unchecked")
	@WebMethod(operationName = "searchByKeywords")
	public Collection<Investigation> searchByKeywords(
			@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "keywords") List<String> keywords) throws SessionException {
		final String query = "DISTINCT Investigation <-> Keyword[name IN " + this.getIN(keywords)
				+ "]";
		try {
			return (List<Investigation>) this.beanManagerLocal.search(sessionId, query);
		} catch (IcatException e) {
			throw new SessionException(e);
		}
	}

	/**
	 * This searches all DB for investigations with the advanced search criteria
	 * 
	 */
	@SuppressWarnings("unchecked")
	@WebMethod
	public List<Investigation> searchByAdvanced(@WebParam(name = "sessionId") String sessionId,
			@WebParam(name = "advancedSearchDetails") AdvancedSearchDetails advancedSearch)
			throws SessionException {
		final StringBuilder query = new StringBuilder();
		if (advancedSearch.hasExperimentNumber()) {
			augmentQuery(query, "name", advancedSearch.getExperimentNumber());
		}
		if (advancedSearch.hasInvestigationType()) {
			augmentQuery(query, "type.name", advancedSearch.getInvestigationType());
		}
		// TODO add rest of these ...
		if (query.length() == 0) {
			query.append("DISTINCT Investigation");
		} else {
			query.append("]");
		}
		try {
			return (List<Investigation>) this.beanManagerLocal.search(sessionId, query.toString());
		} catch (IcatException e) {
			throw new SessionException(e);
		}
	}

	private void augmentQuery(StringBuilder query, String field, String value) {
		if (query.length() == 0) {
			query.append("DISTINCT Investigation [");
		} else {
			query.append(" AND ");
		}
		query.append(field + " = '" + value + "'");
	}

	/**
	 * This searches all DB for investigations with the advanced search criteria
	 * 
	 * @param sessionId
	 *            session id of the user.
	 * @param advancedSearch
	 *            advanced SearchManager details to search with
	 * @param startIndex
	 *            start index of the results found, default 0
	 * @param numberOfResults
	 *            number of results found from the start index, default {@link Queries}
	 *            .MAX_QUERY_RESULTSET
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return collection of {@link Investigation} investigation objects
	 * @throws IcatInternalException
	 */
	// @WebMethod(operationName = "searchByAdvancedPagination")
	// public List<Investigation> searchByAdvanced(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "advancedSearchDetails") AdvancedSearchDetails
	// advancedSearch,
	// @WebParam(name = "startIndex") int startIndex, @WebParam(name =
	// "numberOfResults") int numberOfResults)
	// throws SessionException, IcatInternalException {
	// return investigationSearchLocal.searchByAdvanced(sessionId,
	// advancedSearch, startIndex, numberOfResults);
	// }

	/**
	 * Lists all the investigations for the current user, ie who he is an investigator of
	 * 
	 * @param sessionId
	 * @param investigationIncludes
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return collection
	 * @throws IcatInternalException
	 */
	// @WebMethod(operationName = "getMyInvestigationsIncludes")
	// public List<Investigation> getMyInvestigations(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "investigationInclude") InvestigationInclude
	// investigationIncludes)
	// throws SessionException, IcatInternalException {
	// return investigationSearchLocal.getMyInvestigations(sessionId,
	// investigationIncludes);
	// }

	/**
	 * Lists all the investigations for the current user, ie who he is an investigator of
	 * 
	 * @param sessionId
	 * @param investigationIncludes
	 * @param startIndex
	 *            start index of the results found
	 * @param number_results
	 *            number of results found from the start index
	 * @throws uk.icat3.exceptions.SessionException
	 *             if the session id is invalid
	 * @return collection
	 * @throws IcatInternalException
	 */
	// @WebMethod(operationName = "getMyInvestigationsIncludesPagination")
	// public List<Investigation> getMyInvestigations(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "investigationInclude") InvestigationInclude
	// investigationIncludes,
	// @WebParam(name = "startIndex") int startIndex, @WebParam(name =
	// "numberOfResults") int number_results)
	// throws SessionException, IcatInternalException {
	// return investigationSearchLocal.getMyInvestigations(sessionId,
	// investigationIncludes, startIndex,
	// number_results);
	// }

	// /**
	// * This gets all the keywords available for that user, beginning with a
	// * keyword, they can only see keywords associated with their
	// investigations
	// * or public investigations
	// *
	// * @param sessionId
	// * federalId of the user.
	// * @param startKeyword
	// * start keyword to search
	// * @param numberReturned
	// * number of results found returned
	// * @return list of keywords
	// * @throws uk.icat3.exceptions.SessionException
	// */
	// @WebMethod(operationName = "getKeywordsForUserStartWithMax")
	// public List<String> getKeywordsForUser(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "startKeyword") String startKeyword, @WebParam(name =
	// "numberReturned") int numberReturned)
	// throws SessionException {
	// return keywordSearchLocal.getKeywordsForUser(sessionId, startKeyword,
	// numberReturned);
	// }

	// /**
	// * This gets all the keywords avaliable for that user, beginning with a
	// * keyword, they can only see keywords associated with their
	// investigations
	// * or public investigations
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param type
	// * ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
	// * @throws uk.icat3.exceptions.SessionException
	// * @return list of keywords
	// */
	// @WebMethod(operationName = "getKeywordsForUserType")
	//
	// public List<String> getKeywordsForUser(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "keywordType") KeywordType type) throws SessionException
	// {
	// return keywordSearchLocal.getKeywordsForUser(sessionId, type);
	// }

	// /**
	// * This gets all the unique keywords in the database
	// *
	// * Types, ALPHA, ALPHA_NUMERIC only work with oracle DBs
	// *
	// * @param sessionId
	// * sessionId of the user.
	// * @param type
	// * ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType} throws IcatException
	// * @return list of keywords
	// * @throws uk.icat3.exceptions.SessionException
	// */
	// @WebMethod
	// public List<String> getAllKeywords(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "type") KeywordType type) throws SessionException {
	// return keywordSearchLocal.getAllKeywords(sessionId, type);
	// }

	// =================================================================

	//
	// /**
	// * Searches the investigations the user has access to view by user id
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param userSearch
	// * Could be DN , username or federal ID
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of {@link Investigation} investigation objects
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// public Collection<Investigation> searchByUserID(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "userSearch") String userSearch) throws
	// SessionException, IcatInternalException {
	// return investigationSearchLocal.searchByUserID(sessionId, userSearch);
	// }
	//
	// /**
	// * Searches the investigations the user has access to view by user id
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param userSearch
	// * Could be DN , username or federal ID
	// * @param startIndex
	// * start index of the results found
	// * @param number_results
	// * number of results found from the start index
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of {@link Investigation} investigation objects
	// * @throws IcatInternalException
	// */
	// @WebMethod(operationName = "searchByUserIDPagination")
	// @RequestWrapper(className =
	// "uk.icat3.sessionbeans.jaxws.searchByUserIDPagination")
	// @ResponseWrapper(className =
	// "uk.icat3.sessionbeans.jaxws.searchByUserIDPaginationResponse")
	// public Collection<Investigation> searchByUserID(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "userSearch") String userSearch, @WebParam(name =
	// "startIndex") int startIndex,
	// @WebParam(name = "numberOfResults") int number_results) throws
	// SessionException, IcatInternalException {
	// return investigationSearchLocal.searchByUserID(sessionId, userSearch,
	// startIndex, number_results);
	// }
	//
	// /**
	// * Searches the investigations the user has access to view by surname
	// *
	// * @param sessionId
	// * @param surname
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// public Collection<Investigation> searchByUserSurname(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "surname") String surname) throws SessionException,
	// IcatInternalException {
	// return investigationSearchLocal.searchByUserSurname(sessionId, surname);
	// }
	//
	// /**
	// * Searches the investigations the user has access to view by surname
	// *
	// * @param sessionId
	// * @param surname
	// * @param startIndex
	// * start index of the results found
	// * @param number_results
	// * number of results found from the start index
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of
	// * @throws IcatInternalException
	// */
	// @WebMethod(operationName = "searchByUserSurnamePagination")
	// @RequestWrapper(className =
	// "uk.icat3.sessionbeans.jaxws.searchByUserSurnamePagination")
	// @ResponseWrapper(className =
	// "uk.icat3.sessionbeans.jaxws.searchByUserSurnamePaginationResponse")
	// public Collection<Investigation> searchByUserSurname(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "surname") String surname, @WebParam(name =
	// "startIndex") int startIndex,
	// @WebParam(name = "numberOfResults") int number_results) throws
	// SessionException, IcatInternalException {
	// return investigationSearchLocal.searchByUserSurname(sessionId, surname,
	// startIndex, number_results);
	// }
	//
	// /**
	// * Lists all the instruments in the DB
	// *
	// * @param sessionId
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of instruments
	// */
	// @WebMethod
	// public Collection<String> listInstruments(@WebParam(name = "sessionId")
	// String sessionId) throws SessionException {
	// return investigationSearchLocal.listInstruments(sessionId);
	// }
	//
	// /**
	// * Lists all the instruments in the DB
	// *
	// * @param sessionId
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of instruments
	// */
	// @WebMethod
	// public Collection<Instrument> getAllInstruments(@WebParam(name =
	// "sessionId") String sessionId)
	// throws SessionException {
	// return investigationSearchLocal.getAllInstruments(sessionId);
	// }
	//
	// /**
	// * Lists all the parameters in the DB
	// *
	// * @param sessionId
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of rols
	// */
	// @WebMethod
	// public Collection<ParameterType> listParameters(@WebParam(name =
	// "sessionId") String sessionId)
	// throws SessionException {
	// return investigationSearchLocal.listParameters(sessionId);
	// }
	//
	// /**
	// * Lists all the FacilityCycles in the DB
	// *
	// * @param sessionId
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of FacilityCycles
	// */
	// @WebMethod
	// public Collection<FacilityCycle> listFacilityCycles(@WebParam(name =
	// "sessionId") String sessionId)
	// throws SessionException {
	// return facilityManagerLocal.listAllFacilityCycles(sessionId);
	//
	// }
	//
	// /**
	// * Lists all the inv types in the DB
	// *
	// * @param sessionId
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of rols
	// */
	// @WebMethod
	// public Collection<String> listInvestigationTypes(@WebParam(name =
	// "sessionId") String sessionId)
	// throws SessionException {
	// return investigationSearchLocal.listInvestigationTypes(sessionId);
	// }
	//
	// /**
	// * From a sample name, return all the samples a user can view asscoiated
	// * with the sample name
	// *
	// * @param sessionId
	// * @param sampleName
	// * @throws uk.icat3.exceptions.SessionException
	// * @return collection of sample
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// @Interceptors(SampleSearchInterceptor.class)
	// public Collection<Sample> searchSamplesBySampleName(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "sampleName") String sampleName) throws
	// SessionException, IcatInternalException {
	// return datasetSearchLocal.searchSamplesBySampleName(sessionId,
	// sampleName);
	// }
	//
	// /**
	// * From a sample, return all the datafiles a user can view asscoiated with
	// * the sample name
	// *
	// * @param sessionId
	// * @param sample
	// * @throws uk.icat3.exceptions.SessionException
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * @return collection of Data sets
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// @Interceptors(SampleSearchInterceptor.class)
	// public Collection<Dataset> searchDatasetsBySample(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "sample") Sample sample) throws SessionException,
	// NoSuchObjectFoundException,
	// InsufficientPrivilegesException, IcatInternalException {
	// return datasetSearchLocal.searchDataSetsBySample(sessionId, sample);
	// }
	//
	// /**
	// * List all the valid avaliable types for datasets
	// *
	// * @param sessionId
	// * @return collection of types
	// * @throws uk.icat3.exceptions.SessionException
	// */
	// @WebMethod
	// public Collection<String> listDatasetTypes(@WebParam(name = "sessionId")
	// String sessionId) throws SessionException {
	// return datasetSearchLocal.listDatasetTypes(sessionId);
	// }
	//
	// /**
	// * List all the valid avaliable status for datasets
	// *
	// * @param sessionId
	// * @return collection of status
	// * @throws uk.icat3.exceptions.SessionException
	// */
	// @WebMethod
	// public Collection<String> listDatasetStatus(@WebParam(name = "sessionId")
	// String sessionId) throws SessionException {
	// return datasetSearchLocal.listDatasetStatus(sessionId);
	// }
	//
	// // ///////////////////////// End of Dataset SearchManager methods
	// // /////////////////////////////////////////
	//
	// // ///////////////////////// Datafile SearchManager methods
	// // /////////////////////////////////////////
	// /**
	// * Searchs database for data files from a start and end run on an
	// instrument
	// * for which the userId has permission to read the data files
	// investigation
	// *
	// * @param sessionId
	// * sessionId of the user.
	// * @param instruments
	// * collection of instruments
	// * @param startRun
	// * lower range of run number
	// * @param endRun
	// * upper range of run number
	// * @return collection of datafiles returned from search
	// * @throws uk.icat3.exceptions.SessionException
	// */
	// @WebMethod
	// @Interceptors(RunNumberSearchInterceptor.class)
	// public Collection<Datafile> searchByRunNumber(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "instruments") Collection<String> instruments,
	// @WebParam(name = "startRun") float startRun, @WebParam(name = "endRun")
	// float endRun)
	// throws SessionException {
	// return datafileSearchLocal.searchByRunNumber(sessionId, instruments,
	// startRun, endRun);
	// }
	//
	// /**
	// * Searches database for data files from a start and end run on an
	// * instrument for which the userId has permission to read the data files
	// * investigation
	// *
	// * @param sessionId
	// * sessionId of the user.
	// * @param instruments
	// * collection of instruments
	// * @param startRun
	// * lower range of run number
	// * @param endRun
	// * upper range of run number
	// * @param startIndex
	// * start index of the results found
	// * @param number_results
	// * number of results found from the start index
	// * @return collection of datafiles returned from search
	// * @throws uk.icat3.exceptions.SessionException
	// */
	// @WebMethod(operationName = "searchByRunNumberPagination")
	// @RequestWrapper(className =
	// "uk.icat3.sessionbeans.jaxws.searchByRunNumberPagination")
	// @ResponseWrapper(className =
	// "uk.icat3.sessionbeans.jaxws.searchByRunNumberPaginationResponse")
	// @Interceptors(RunNumberSearchInterceptor.class)
	// public Collection<Datafile> searchByRunNumber(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "instruments") Collection<String> instruments,
	// @WebParam(name = "startRun") float startRun, @WebParam(name = "endRun")
	// float endRun,
	// @WebParam(name = "startIndex") int startIndex, @WebParam(name =
	// "numberOfResults") int number_results)
	// throws SessionException {
	// return datafileSearchLocal.searchByRunNumber(sessionId, instruments,
	// startRun, endRun, startIndex,
	// number_results);
	// }
	//
	// /**
	// * List all the valid avaliable formats for datafiles
	// *
	// * @param sessionId
	// * @return collection of types
	// * @throws uk.icat3.exceptions.SessionException
	// */
	// @WebMethod
	// public Collection<DatafileFormat> listDatafileFormats(@WebParam(name =
	// "sessionId") String sessionId)
	// throws SessionException {
	// return datafileSearchLocal.listDatafileFormats(sessionId);
	// }
	//
	// /**
	// * Returns a {@link Investigation} investigation from a
	// * {@link Investigation} id if the user has access to the investigation.
	// *
	// * @param sessionId
	// * sessionid of the user.
	// * @param investigationId
	// * Id of investigations
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return {@link Investigation} object
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// @Interceptors(ViewInvestigationsInterceptor.class)
	// public Investigation getInvestigation(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "investigationId") Long investigationId) throws
	// SessionException,
	// InsufficientPrivilegesException, NoSuchObjectFoundException,
	// IcatInternalException {
	// return investigationManagerLocal.getInvestigation(sessionId,
	// investigationId);
	// }
	//
	// /**
	// * Returns a {@link Investigation} investigation from a
	// * {@link Investigation} id if the user has access to the investigation.
	// * Also gets extra information regarding the investigation. See
	// * {@link InvestigationInclude}
	// *
	// * @param sessionId
	// * sessionid of the user.
	// * @param investigationId
	// * Id of investigations
	// * @param includes
	// * information that is needed to be returned with the
	// * investigation
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return {@link Investigation} object
	// * @throws IcatInternalException
	// */
	// @WebMethod(operationName = "getInvestigationIncludes")
	// @RequestWrapper(className =
	// "uk.icat3.sessionbeans.getInvestigationIncludes")
	// @ResponseWrapper(className =
	// "uk.icat3.sessionbeans.getInvestigationIncludesResponse")
	// @Interceptors(ViewInvestigationsInterceptor.class)
	// public Investigation getInvestigation(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "investigationId") Long investigationId,
	// @WebParam(name = "investigationInclude") InvestigationInclude includes)
	// throws SessionException,
	// InsufficientPrivilegesException, NoSuchObjectFoundException,
	// IcatInternalException {
	// return investigationManagerLocal.getInvestigation(sessionId,
	// investigationId, includes);
	// }
	//
	// /**
	// * Returns a list of {@link Investigation} investigations from a list of
	// * {@link Investigation} investigation ids if the user has access to the
	// * investigations.
	// *
	// * @param userId
	// * federalId of the user.
	// * @param investigationIds
	// * Ids of investigations
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @return collection of {@link Investigation} investigation objects
	// * @throws IcatInternalException
	// */
	// @Interceptors(ViewInvestigationsInterceptor.class)
	// public Collection<Investigation> getInvestigations(@WebParam(name =
	// "userId") String userId,
	// @WebParam(name = "investigationIds") Collection<Long> investigationIds)
	// throws SessionException,
	// InsufficientPrivilegesException, NoSuchObjectFoundException,
	// IcatInternalException {
	// return investigationManagerLocal.getInvestigations(userId,
	// investigationIds, InvestigationInclude.NONE);
	// }
	//
	// /**
	// * Returns a list of {@link Investigation} investigations from a list of
	// * {@link Investigation} investigation ids if the user has access to the
	// * investigations. Also gets extra information regarding the
	// investigation.
	// * See {@link InvestigationInclude}
	// *
	// * @param userId
	// * federalId of the user.
	// * @param investigationIds
	// * Ids of investigations
	// * @param includes
	// * information that is needed to be returned with the
	// * investigation
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @return collection of {@link Investigation} investigation objects
	// * @throws IcatInternalException
	// */
	// @WebMethod(operationName = "getInvestigationsIncludes")
	// @RequestWrapper(className =
	// "uk.icat3.sessionbeans.getInvestigationsIncludes")
	// @ResponseWrapper(className =
	// "uk.icat3.sessionbeans.getInvestigationsIncludesResponse")
	// @Interceptors(ViewInvestigationsInterceptor.class)
	// public Collection<Investigation> getInvestigations(@WebParam(name =
	// "userId") String userId,
	// @WebParam(name = "investigationIds") Collection<Long> investigationIds,
	// @WebParam(name = "investigationInclude") InvestigationInclude includes)
	// throws SessionException,
	// InsufficientPrivilegesException, NoSuchObjectFoundException,
	// IcatInternalException {
	// return investigationManagerLocal.getInvestigations(userId,
	// investigationIds, includes);
	// }
	//
	// /**
	// * Gets the data set object from a data set id, depending if the user has
	// * access to read the data set.
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param datasetId
	// * Id of object
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return {@link Dataset}
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// @Interceptors(ViewDatasetsInterceptor.class)
	// public Dataset getDataset(@WebParam(name = "sessionId") String sessionId,
	// @WebParam(name = "datasetId") Long datasetId) throws SessionException,
	// InsufficientPrivilegesException,
	// NoSuchObjectFoundException, IcatInternalException {
	// return datasetManagerLocal.getDataset(sessionId, datasetId);
	// }
	//
	// /**
	// * Gets the data set object from a data set id, depending if the user has
	// * access to read the data set. Also gets extra information regarding the
	// * data set. See {@link DatasetInclude}
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param datasetId
	// * Id of object
	// * @param includes
	// * other information wanted with the data set
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return {@link Dataset}
	// * @throws IcatInternalException
	// */
	// @WebMethod(operationName = "getDatasetIncludes")
	// @RequestWrapper(className =
	// "uk.icat3.sessionbeans.manager.getDatasetIncludes")
	// @ResponseWrapper(className =
	// "uk.icat3.sessionbeans.manager.getDatasetIncludesResponse")
	// @Interceptors(ViewDatasetsInterceptor.class)
	// public Dataset getDataset(@WebParam(name = "sessionId") String sessionId,
	// @WebParam(name = "datasetId") Long datasetId, @WebParam(name =
	// "datasetInclude") DatasetInclude includes)
	// throws SessionException, InsufficientPrivilegesException,
	// NoSuchObjectFoundException, IcatInternalException {
	// return datasetManagerLocal.getDataset(sessionId, datasetId, includes);
	// }
	//
	// /**
	// * Gets the data set object from a from a list of data set ids, depending
	// if
	// * the user has access to read the data sets.
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param datasetIds
	// * Id of object
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of {@link Dataset}s
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// @Interceptors(ViewDatasetsInterceptor.class)
	// public Collection<Dataset> getDatasets(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "datasetIds") Collection<Long> datasetIds) throws
	// SessionException,
	// InsufficientPrivilegesException, NoSuchObjectFoundException,
	// IcatInternalException {
	// return datasetManagerLocal.getDatasets(sessionId, datasetIds);
	// }
	//
	// /**
	// * Gets a data file object from a data file id, depending if the user has
	// * access to read the data file
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param datafileId
	// * Id of data file
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return {@link Datafile}
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// @Interceptors(ViewDatafilesInterceptor.class)
	// public Datafile getDatafile(@WebParam(name = "sessionId") String
	// sessionId,
	// @WebParam(name = "datafileId") Long datafileId) throws SessionException,
	// InsufficientPrivilegesException,
	// NoSuchObjectFoundException, IcatInternalException {
	// return datafileManagerLocal.getDatafile(sessionId, datafileId);
	// }
	//
	// /**
	// * Gets a collection of data file object from a collection of data file
	// ids,
	// * depending if the user has access to read the data file
	// *
	// * @param sessionId
	// * session id of the user.
	// * @param datafileIds
	// * collection of data file ids
	// * @throws uk.icat3.exceptions.NoSuchObjectFoundException
	// * if entity does not exist in database
	// * @throws uk.icat3.exceptions.InsufficientPrivilegesException
	// * if user has insufficient privileges to the object
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return collection of {@link Datafile} objects
	// * @throws IcatInternalException
	// */
	// @WebMethod
	// @Interceptors(ViewDatafilesInterceptor.class)
	// public Collection<Datafile> getDatafiles(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "datafileIds") Collection<Long> datafileIds) throws
	// SessionException,
	// InsufficientPrivilegesException, NoSuchObjectFoundException,
	// IcatInternalException {
	// return datafileManagerLocal.getDatafiles(sessionId, datafileIds);
	// }
	//
	// /**
	// * Returns the User for the given userId
	// *
	// * @param sessionId
	// * sessionId of the user
	// * @param facilityUserId
	// * the id of the user to retrieve
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return User the User requested
	// * @throws NoSuchObjectFoundException
	// */
	// @WebMethod(operationName = "getFacilityUserByFacilityUserId")
	// public User getFacilityUserByFacilityUserId(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "facilityUserId") String facilityUserId) throws
	// SessionException,
	// NoSuchObjectFoundException {
	// return facilityManagerLocal.getFacilityUserByFacilityUserId(sessionId,
	// facilityUserId);
	// }
	//
	// /**
	// * Returns the User for the given userId
	// *
	// * @param sessionId
	// * sessionId of the user
	// * @param facilityUserId
	// * the id of the user to retrieve
	// * @throws uk.icat3.exceptions.SessionException
	// * if the session id is invalid
	// * @return User the User requested
	// */
	// @WebMethod(operationName = "getFacilityUserByFederalId")
	// public User getFacilityUserByFederalId(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "federalId") String federalId) throws SessionException,
	// NoSuchObjectFoundException {
	// return facilityManagerLocal.getFacilityUserByFederalId(sessionId,
	// federalId);
	// }
	//
	// //
	// ///////////////////////////////////////////////////////////////////
	// // GET PARAMETER METHODS //
	// //
	// ///////////////////////////////////////////////////////////////////
	// /**
	// * Returns parameters matched by name and units. The search parameters are
	// * insensitive (no different between lowercase or uppercase) and eager
	// * (match the word, LIKE '%name%' behavior).
	// *
	// * @param sessionId
	// * Session identification
	// * @param name
	// * ParameterType name
	// * @param units
	// * ParameterType units
	// * @param manager
	// * Entity manager which handles database
	// * @return Paremeter collection matched by name and units
	// *
	// * @throws SessionException
	// */
	// @WebMethod
	// public Collection<ParameterType> getParameterByNameUnits(@WebParam(name =
	// "sesssionId") String sesssionId,
	// @WebParam(name = "name") String name, @WebParam(name = "units") String
	// units) throws SessionException {
	// return parameterSearchLocal.getParameterByNameUnits(sesssionId, name,
	// units);
	// }
	//
	// /**
	// * Returns parameters matched by name. The search parameters are
	// insensitive
	// * (no different between lowercase or uppercase) and eager (match the
	// word,
	// * LIKE '%name%' behavior).
	// *
	// * @param sessionId
	// * Session identification
	// * @param name
	// * ParameterType name
	// * @return Paremeter collection matched by name
	// * @throws SessionException
	// */
	// @WebMethod
	// public Collection<ParameterType> getParameterByName(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "name") String name) throws SessionException {
	// return parameterSearchLocal.getParameterByName(sessionId, name);
	// }
	//
	// /**
	// * Returns parameters matched by units. The search parameters are
	// * insensitive (no different between lowercase or uppercase) and eager
	// * (match the word, LIKE '%units%' behavior).
	// *
	// * @param sessionId
	// * Session identification
	// * @param units
	// * ParameterType units
	// * @return Paremeter collection matched by units
	// * @throws SessionException
	// */
	// @WebMethod(operationName = "getParameterByUnits")
	// public Collection<ParameterType> getParameterByUnits(@WebParam(name =
	// "sessionId") String sessionId,
	// @WebParam(name = "units") String units) throws SessionException {
	// return parameterSearchLocal.getParameterByUnits(sessionId, units);
	// }
	// /**
	// * SearchManager by a collection of keywords for investigations that user
	// * has access to view
	// *
	// * @param sessionId
	// * sessionId of the user.
	// * @param keywordsDetails
	// * details of keyword search
	// * @param startIndex
	// * start index of the results found, default 0
	// * @param numberOfResults
	// * number of results found from the start index, default
	// * {@link Queries}.MAX_QUERY_RESULTSET
	// * @return collection of {@link Investigation} investigation objects
	// * @throws uk.icat3.exceptions.SessionException
	// * @throws IcatInternalException
	// */
	// @WebMethod(operationName = "searchByKeywordsAll")
	// public List<Investigation> searchByKeywords(@WebParam(name = "sessionId")
	// String sessionId,
	// @WebParam(name = "keywordDetails") KeywordDetails details, @WebParam(name
	// = "startIndex") int startIndex,
	// @WebParam(name = "numberOfResults") int numberOfResults) throws
	// SessionException, IcatInternalException {
	// return investigationSearchLocal.searchByKeywords(sessionId, details,
	// startIndex, numberOfResults);
	// }

}