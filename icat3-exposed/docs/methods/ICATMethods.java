import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.DatafileParameterPK;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Instrument;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.SampleParameterPK;
import java.util.Collection;
import javax.xml.bind.ValidationException;


/**
 *
 * @author Glen Drinkwater
 */
public interface ICATMethods {
    
    
    //////////////        ICAT API Web Service method signatures          ////////////////////
    
    
    ///// User Session methods
    
    /**
     * Logs in, defaults to 2 hours
     */
    public String login(String username, String password) throws SessionException;
    
    /**
     * Logs in for a certain ammount of time
     */
    public String login(String username, String password, int lifetime) throws SessionException;
    
    /**
     * Logs out
     */
    public boolean logout( String sessionId);
      
     ///////////////////////////////////   All Searches  ///////////////////////////////////////////
    
    ///////////////////////////     KeywordSearch methods  /////////////////////////////////////////
    /**
     *  This gets all the keywords avaliable for that user, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId federalId of the user.
     * @return list of keywords
     */
    
    public Collection<String> getKeywordsForUser(String sessionId) throws SessionException;
        
    /**
     * This gets all the keywords avaliable for that user, beginning with a keyword, they can only see keywords associated with their
     * investigations or public investigations
     *
     * @param sessionId federalId of the user.
     * @param startKeyword start keyword to search
     * @param numberReturned number of results found returned
     * @return list of keywords
     */
    public Collection<String> getKeywordsForUser(String sessionId, String startKeyword, int numberReturned) throws SessionException;
    
        /**
     * This gets all the unique keywords in the database
     *
     * Types,  ALPHA, ALPHA_NUMERIC only work with oracle DBs
     *
     * @param sessionId sessionId of the user.
     * @param type ALL, ALPHA, ALPHA_NUMERIC, {@link KeywordType}
     * @return list of keywords
     */
    public Collection<String> getAllKeywords(String sessionId, KeywordType type) throws SessionException;
        
    
    ///////////////////////////     Investigation Search methods  /////////////////////////////////////////
    /**
     * This searches all DB for investigations with the advanced search criteria
     *
     * @param sessionId session id of the user.
     * @param advancedSearch advanced Search details to search with
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByAdvanced(String sessionId, AdvancedSearchDetails advancedSearch) throws SessionException ;
    
    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param include {@link InvestigationInclude}
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include,  boolean fuzzy) throws SessionException ;
    
    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator, fuzzy false, no includes
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords) throws SessionException ;
    
    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator, fuzzy false, no includes
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, int startIndex, int numberOfResults) throws SessionException ;
    
    /**
     * Search by a collection of keywords for investigations that user has access to view, with AND been operator
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param include {@link InvestigationInclude}
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException ;
    
    /**
     * Search by a collection of keywords for investigations that user has access to view, searching by fuzzy is true, with AND been operator
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param include {@link InvestigationInclude}
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, int startIndex, int numberOfResults) throws SessionException ;
    
    /**
     * Search by a collection of keywords for investigations that user has access to view
     *
     * @param sessionId sessionId of the user.
     * @param keywords Collection of keywords to search on
     * @param operator {@link LogicalOperator}, either AND or OR, default AND
     * @param include {@link InvestigationInclude}
     * @param fuzzy search with wildcards, e.g like copper searches for %copper% i.e anything with copper in keyword, default false
     * @param startIndex start index of the results found, default 0
     * @param numberOfResults number of results found from the start index, default {@link Queries}.MAX_QUERY_RESULTSET
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException ;
    
    /**
     * Lists all the investigations for the current user, ie who he is an investigator of
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    public Collection<Investigation> getMyInvestigations(String sessionId) throws SessionException ;
    
    /**
     * Lists all the investigations for the current user, ie who he is an investigator of
     *
     * @param sessionId
     * @param include
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    public Collection<Investigation> getMyInvestigations(String sessionId, InvestigationInclude include) throws SessionException ;
    
    /**
     * Lists all the investigations for the current user, ie who he is an investigator of
     *
     * @param sessionId
     * @param include
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection
     */
    public Collection<Investigation> getMyInvestigations(String sessionId, InvestigationInclude include, int startIndex, int number_results) throws SessionException ;
    
    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param userSearch Could be DN , username or federal ID
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch) throws SessionException ;
    
    /**
     * Searches the investigations the user has access to view by user id
     *
     * @param sessionId session id of the user.
     * @param userSearch Could be DN , username or federal ID
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> searchByUserID(String sessionId,String userSearch, int startIndex, int number_results) throws SessionException ;
    
    /**
     * Searches the investigations the user has access to view by surname
     *
     * @param sessionId
     * @param surname
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of
     */
    
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname) throws SessionException ;
    
    /**
     * Searches the investigations the user has access to view by surname
     *
     * @param sessionId
     * @param surname
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of
     */
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname, int startIndex, int number_results) throws SessionException ;
    
    /**
     *  Lists all the instruments in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of instruments
     */
    public Collection<Instrument> listInstruments(String sessionId) throws SessionException ;
    
    /**
     *  Lists all the rols in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    public Collection<IcatRole> listRoles(String sessionId) throws SessionException ;
    
    /**
     *  Lists all the parameters in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    public Collection<Parameter> listParameters(String sessionId) throws SessionException ;
    
    /**
     *  Lists all the inv types in the DB
     *
     * @param sessionId
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @return collection of rols
     */
    public Collection<InvestigationType> listInvestigationTypes(String sessionId) throws SessionException ;
       
    
    ///////////////////////////     Dataset Search methods  /////////////////////////////////////////
    /**
     * From a sample name, return all the samples a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sampleName
     * @throws uk.icat3.exceptions.SessionException
     * @return collection of sample
     */
    public Collection<Sample> searchSamplesBySampleName( String sessionId, String sampleName) throws SessionException ;
    
    
    /**
     * From a sample, return all the datafiles a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sample
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException
     * @return collection of Data sets
     */
    public Collection<Dataset> searchDatasetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException ;
        
    /**
     *  List all the valid avaliable types' for datasets
     *
     * @param sessionId
     * @return collection of types'
     * @throws uk.icat3.exceptions.SessionException
     */
    public Collection<DatasetType> listDatasetTypes(String sessionId) throws SessionException ;
        
    /**
     * List all the valid avaliable status' for datasets
     *
     * @param sessionId
     * @return collection of status'
     */
    public Collection<DatasetStatus> listDatasetStatus(String sessionId) throws SessionException ;
        
    ///////////////////////////     Datafile Search methods  /////////////////////////////////////////
    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param sessionId sessionId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @return collection of datafiles returned from search
     */
    
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun) throws SessionException ;
    
    
    /**
     * Searchs database for data files from a start and end run on an instrument for which the userId has permission to read
     * the data files investigation
     *
     * @param sessionId sessionId of the user.
     * @param instruments collection of instruments
     * @param startRun lower range of run number
     * @param endRun upper range of run number
     * @param startIndex start index of the results found
     * @param number_results number of results found from the start index
     * @return collection of datafiles returned from search
     */
    
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results) throws SessionException ;
        
    /////      Manager methods
    
    // Investigation manager methods
    
    /**
     * Returns a {@link Investigation} investigation from a {@link Investigation} id
     * if the user has access to the investigation.
     *
     * @param sessionId sessionid of the user.
     * @param investigationId Id of investigations
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     * @return {@link Investigation} object
     */
    public Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Returns a {@link Investigation} investigation from a {@link Investigation} id
     * if the user has access to the investigation.
     * Also gets extra information regarding the investigation.  See {@link InvestigationInclude}
     *
     * @param sessionId sessionid of the user.
     * @param investigationId Id of investigations
     * @param includes information that is needed to be returned with the investigation
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     * @return {@link Investigation} object
     */
    public Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Returns a list of {@link Investigation} investigations from a list of {@link Investigation} investigation ids
     * if the user has access to the investigations.
     * Also gets extra information regarding the investigation.  See {@link InvestigationInclude}
     *
     * @param userId federalId of the user.
     * @param investigationIds Ids of investigations
     * @param includes information that is needed to be returned with the investigation
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws SessionException if the session id is invalid
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @return collection of {@link Investigation} investigation objects
     */
    public Collection<Investigation> getInvestigations(String userId, Collection<Long> investigationIds, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Adds keyword to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keyword {@link Keyword} object to be updated
     * @param investigationId id of the investigation
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     */
    public void addKeyword(String sessionId, Keyword keyword, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Adds investigator to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigator {@link Investigator} object to be updated
     * @param investigationId id of the investigation
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     */
    public void addInvestigator(String sessionId, Investigator investigator, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Adds a sample to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sample {@link Sample} object to be updated
     * @param investigationId id of the investigation
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     */
    public void addSample(String sessionId, Sample sample, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Adds publication to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publication {@link Publication} object to be updated
     * @param investigationId id of the investigation
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     * @return publication that was added
     */
    public Publication addPublication(String sessionId, Publication publication, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Adds a sample parameter to investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameter {@link SampleParameter} object to be updated
     * @param investigationId id of the investigation
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     * @return sampleparameter that was added
     */
    public SampleParameter addSampleParamater(String sessionId,  SampleParameter sampleParameter, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Deletes/Undeletes the investigator from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigatorPK {@link InvestigatorPK} object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deleteInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Deletes/Undeletes the keyword from investigation, depending on whether the user has permission to delete this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keywordPK {@link KeywordPK} object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deleteKeyword(String sessionId, KeywordPK keywordPK) throws SessionException,  InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Deleted the publication from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publicationId ID of object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deletePublication(String sessionId, Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Deletes/Undeletes the sample from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleId primary key object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deletesample(String sessionId, Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Removes the sample parameter from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameterPK {@link SampleParameterPK} primary key object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deletesampleParameter(String sessionId, SamleParameterPK sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Modifies the investigator of the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigator {@link Investigator} object to be updated
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     */
    public void modifyInvestigator(String sessionId,  Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Modifies the sample from the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sample {@link Sample} object to be updated
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     */
    public void modifySample(String sessionId, Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Modifies the publication of the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publication {@link Publication} object to be updated
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     */
    public void modifyPublication(String sessionId, Publication publication) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Modifies the sample parameter from the investigation, depending on whether the user has permission to update this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameter {@link SampleParameter} object to be updated
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the investigation object is invalid
     * @throws SessionException if the session id is invalid
     */
    public void modifySampleParameter(String sessionId, SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Removes the keyword from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param keywordPK {@link KeywordPK} object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeKeyword(String sessionId, KeywordPK keywordPK) throws SessionException,  InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Removes the investigator from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param investigatorPK {@link InvestigatorPK} object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Removes the publication from investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param publicationId id of object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removePublication(String sessionId, Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Removes the sample from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleId primary key object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeSample(String sessionId, Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    /**
     * Deletes/Undeletes the sample parameter from the investigation, depending on whether the user has permission to remove this Investigation object.
     *
     * @param sessionId sessionid of the user.
     * @param sampleParameterPK {@link SampleParameterPK} primary key object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeSampleParameter(String sessionId,  SampleParameterPK sampleParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    
//   Dataset manager methods
    
    /**
     * Gets the data set object from a data set id, depending if the user has access to read the data set.
     *
     * @param sessionId session id of the user.
     * @param datasetId Id of object
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     * @return {@link Dataset}
     */
    public Dataset getDataset(String sessionId, Long datasetId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Gets the data set object from a data set id, depending if the user has access to read the data set.
     * Also gets extra information regarding the data set.  See {@link DatasetInclude}
     *
     * @param sessionId session id of the user.
     * @param datasetId Id of object
     * @param includes other information wanted with the data set
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     * @return {@link Dataset}
     */
    public Dataset getDataset(String sessionId,  Long datasetId, DatasetInclude includes)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Gets the data set object from a from a list of data set ids, depending if the user has access to read the data sets.
     *
     * @param sessionId session id of the user.
     * @param datasetIds Id of object
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     * @return collection of {@link Dataset}s
     */
    public Collection<Dataset> getDatasets(String sessionId,  Collection<Long> datasetIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Creates a data set, depending if the user has create permission on the data set associated with the investigation
     *
     * @param sessionId session id of the user.
     * @param dataSet object to be created
     * @param investigationId id of investigations to added the dataset to
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data set is invalid
     * @throws SessionException if the session id is invalid
     * @return {@link Dataset} that was created
     */
    public Dataset createDataSet(String sessionId, Long investigationId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Creates a collection of data sets, depending if the user has update permission on the data set associated with the investigation
     *
     * @param sessionId session id of the user.
     * @param dataSets collection of the datasets
     * @param investigationId id of investigations to added the datasets to
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data set is invalid
     * @throws SessionException if the session id is invalid
     * @return collection of {@link Dataset}s that were created
     */
    public Collection<Dataset> createDataSets( String sessionId, Collection<Dataset> dataSets,  Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Deletes/Undeletes the data set for a user depending if the users id has delete permissions to delete the data set from the
     * data set ID. Deleting the set marks it, and all of its paramters and data files as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deleteDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Deleted the data set paramter, depending if the users has access to remove the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deleteDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Updates a data set depending on whether the user has permission to update this data set or its investigation
     *
     * @param sessionId session id of the user.
     * @param dataSet object to be updated
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws ValidationException if the data set is invalid
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void modifyDataSet(String sessionId,  Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Modifies a data set paramter, depending if the users has access to update the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameter object to be created
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data set is invalid
     * @throws SessionException if the session id is invalid
     */
    public void modifyDataSetParameter(String sessionId,  DatasetParameter dataSetParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Sets the dataset sample id, depending if the users has access to update the data set
     *
     * @param sessionId session id of the user.
     * @param sampleId Id of sample
     * @param datasetId Id of dataset
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data set is invalid
     * @throws SessionException if the session id is invalid
     */
    public void setDataSetSample(String sessionId,  Long sampleId, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Adds a data set paramter to a dataset, depending if the users has access to create the data set paramter
     *
     * @param sessionId session id of the user.
     * @param dataSetParameter object to be created
     * @param datasetId id of dataset to add to
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data set is invalid
     * @throws SessionException if the session id is invalid
     * @return DatasetParameter that was added
     */
    public DatasetParameter addDataSetParameter(String sessionId, DatasetParameter dataSetParameter, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Removes (from the database) the data set, and its dataset paramters and data files for a user depending if the
     * users id has remove permissions to delete the data set from the data set ID.
     *
     * @param sessionId session id of the user.
     * @param dataSetId primary key object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Removes the data set paramter, depending if the users has access to delete the data set paramter
     *
     * @param sessionId session id of the user.
     * @param datasetParameterPK {@link DatasetParameterPK} object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
//  Datafile manager methods
    
    /**
     * Gets a data file object from a data file id, depending if the user has access to read the data file
     *
     * @param sessionId session id of the user.
     * @param datafileId Id of data file
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     * @return {@link Datafile}
     */
    public Datafile getDatafile(String sessionId, Long datafileId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Gets a collection of data file object from a collection of data file ids, depending if the user has access to read the data file
     *
     * @param sessionId session id of the user.
     * @param datafileIds collection of data file ids
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     * @return collection of {@link Datafile} objects
     */
    public Collection<Datafile> getDatafiles( String sessionId, Collection<Long> datafileIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Creates a data file, depending if the user has update permission on the data set associated with the data file
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be created
     * @param datasetId Id of data set
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data file is invalid
     * @throws SessionException if the session id is invalid
     * @return the created {@link Datafile} primary key
     */
    public Datafile createDataFile(String sessionId,  Datafile dataFile, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Creates a collection of data files, depending if the user has update permission on the data set associated with the data file
     *
     * @param sessionId session id of the user.
     * @param dataFiles collection of objects to be created
     * @param datasetId Id of data set
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data file is invalid
     * @throws SessionException if the session id is invalid
     * @return the collection of created {@link Datafile} primary keys
     */
    public Collection<Datafile> createDataFiles( String sessionId, Collection<Datafile> dataFiles,  Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Deletes/Undeletes a data file for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param datafileId id to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deleteDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Deletes/Undeletes a data file for a users depending if the users id has delete permissions to
     * delete the data file. Deleting the file marks it, and all of its paramters as deleted but does not remove it from the database.
     *
     * @param sessionId session id of the user.
     * @param dataFile objectto be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deleteDataFile(String sessionId,  Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Updates data file depending on whether the user has permission to update this data file.
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data file is invalid
     * @throws SessionException if the session id is invalid
     */
    public void updateDataFile(String sessionId, Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Adds a data file paramter object to a data file, depending if the user has access to create the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameter object to be added
     * @param datafileId the data file id that you want a add the paramter to
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data file is invalid
     * @throws SessionException if the session id is invalid
     * @return {@link DatafileParameter} created
     */
    public DatafileParameter addDataFileParameter(String sessionId, DatafileParameter dataFileParameter, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Updates the data file paramter object, depending if the user has access to update the data file parameter.
     *
     * @param sessionId session id of the user.
     * @param dataFileParameter object to be updated
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws ValidationException if the data file is invalid
     * @throws SessionException if the session id is invalid
     */
    public void modifyDataFileParameter(String sessionId, DatafileParameter dataFileParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    /**
     * Deletes/Undeletes a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be deleted
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void deleteDataFileParameter(String sessionId, DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param datafileId id be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeDataFile(String sessionId, Long datafileId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Removes (from the database) the data file with ID, for a users depending if the users id has remove permissions to remove the data file from
     * the ID.
     *
     * @param sessionId session id of the user.
     * @param dataFile object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeDataFile(String sessionId,  Datafile dataFile) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    /**
     * Removes (from the database) a data file paramter object, depending if the user has access to remove the data file parameter from
     * the associated data file id.
     *
     * @param sessionId session id of the user.
     * @param datafileParameterPK {@link DatafileParameterPK} object to be removed
     * @throws NoSuchObjectFoundException if entity does not exist in database
     * @throws InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws SessionException if the session id is invalid
     */
    public void removeDataFileParameter(String sessionId,  DatafileParameterPK datafileParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    
    
////////////////////////////////////   Authorisation Section methods //////////////////////////////////////////////
    /**
     * Gets all the IcatAuthorisations for a dataFile id of all of the users
     *
     * @param sessionId session id of the user.
     * @param elementId elementId of the authorisations (ie either Investigation, Dataset, Datafile
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid     *
     * @return Collection of {@link IcatAuthorisation}>s of the datafile id
     */
    public Collection<IcatAuthorisation> getAuthorisations(String sessionId, Long elementId, ElementType elementType) throws InsufficientPrivilegesException, NoSuchObjectFoundException, SessionException ;
    
    
    /**
     * Adds a role to a datafile Id for a user (fedId) depending weather user  from session id has permission to do it
     *
     * @param sessionId session id of the user.
     * @param toAddUserId federal Id of user to add
     * @param toAddRole new role for federal Id
     * @param elementId elementId Id of the item to add the role to
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding
     * @return {@link IcatAuthorisation}s of the datafile id
     */
    public IcatAuthorisation addAuthorisation(String sessionId, String toAddUserId, String toAddRole, Long elementId , ElementType elementType) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException;
    
    
    /**
     * Deletes/Undeletes a IcatAuthorisation
     *
     * @param sessionId session id of the user.
     * @param authorisationId id of the authorisation to delete
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    public void deleteAuthorisation(String sessionId, Long authorisationId, ElementType elementType) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException;
    
    /**
     * Removes a IcatAuthorisation
     *
     * @param sessionId session id of the user.
     * @param authorisationId id of the authorisation to remove
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     */
    public void removeAuthorisation(String sessionId, Long authorisationId, ElementType elementType) throws NoSuchObjectFoundException, InsufficientPrivilegesException, SessionException;
    
    
    /**
     * Changes a IcatAuthorisation role for a authorisation id
     *
     * @param sessionId session id of the user.
     * @param toChangetoRole role to change to
     * @param authorisationId id of the authorisation to remove
     * @param elementType {@link ElementType} INVESTIGATION, DATASET, DATAFILE
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException if entity does not exist in database
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException if user has insufficient privileges to the object
     * @throws uk.icat3.exceptions.SessionException if the session id is invalid
     * @throws uk.icat3.exceptions.ValidationException if the added role is higher than the persons role adding
     */
    public void updateAuthorisation(String sessionId, String toChangetoRole, Long authorisationId, ElementType elementType) throws NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException, SessionException;
    
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////
}


