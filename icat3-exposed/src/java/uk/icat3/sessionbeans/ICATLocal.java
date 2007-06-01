
package uk.icat3.sessionbeans;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.NoSuchUserException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.user.UserDetails;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.KeywordType;
import uk.icat3.util.LogicalOperator;


/**
 * This is the business interface for AllOperations enterprise bean.
 */
@Local
public interface ICATLocal {
    
  /*  public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException ;
   
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords) throws SessionException ;
   
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, int startIndex, int numberOfResults) throws SessionException ;
   
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException ;
   
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include,  int startIndex, int numberOfResults) throws SessionException ;
   
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, LogicalOperator operator, InvestigationInclude include, boolean fuzzy, int startIndex, int numberOfResults) throws SessionException ;
   
    public Collection<Investigation> getMyInvestigations(String sessionId) throws SessionException ;
   
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch) throws SessionException ;
   
    public Collection<Investigation> searchByUserID(String sessionId, String userSearch, int startIndex, int number_results) throws SessionException ;
   
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname) throws SessionException ;
   
    public Collection<Investigation> searchByUserSurname(String sessionId, String surname, int startIndex, int number_results) throws SessionException ;
   
    public Collection<String> listAllInstruments(String sessionId) throws SessionException ;
   
   
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, Long startRun, Long endRun) throws SessionException ;
   
   
    public Collection<Dataset> searchBySampleName(String sessionId, String sampleName) throws SessionException ;
   
    public Collection<DatasetType> listDatasetTypes(String sessionId) throws SessionException ;
   
      public Collection<DatasetType> listDatasetTypes2(String sessionId) throws SessionException ;
   
   
    public Collection<DatasetStatus> listDatasetStatus(String sessionId) throws SessionException ;
   
   
    public Collection<String> getKeywordsForUser(String sessionId) throws SessionException;
   
    public Collection<String> getKeywordsForUser(String sessionId, String startKeyword, int numberReturned) throws SessionException;
   
    public Collection<String> getAllKeywords(String sessionId, KeywordType type) throws SessionException;
   */
   // public Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
         /*public Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
     
    public void addKeyword(String sessionId, Keyword keyword, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void removeKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void deleteKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void addInvestigator(String sessionId, Investigator investigator, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void modifyInvestigator(String sessionId, Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void addSample(String sessionId, Sample sample, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void removeSample(String sessionId, Long sampleId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void deleteSample(String sessionId, Long sampleId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void modifySample(String sessionId, Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void removeSampleParameter(String sessionId, SampleParameter sampleParameterPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void deleteSampleParameter(String sessionId, SampleParameter sampleParameterPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void modifySampleParameter(String sessionId, SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void deleteInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
    public void removeInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
     
     
    public String login(String username, String password) throws SessionException;
     
    public String login(String username, String password, int lifetime) throws SessionException;
     
    public String login(String adminUsername, String AdminPassword, String runAsUser) throws SessionException;
     
    public String login(String credential) throws SessionException;
     
    public boolean logout(String sessionId);
     
    // public UserDetails getUserDetails(String sessionId, String user) throws SessionException, NoSuchUserException;
     */
}


