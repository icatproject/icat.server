
package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.IcatAuthorisation;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.KeywordPK;
import uk.icat3.entity.Publication;
import uk.icat3.entity.Sample;
import uk.icat3.entity.SampleParameter;
import uk.icat3.entity.SampleParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.InvestigationInclude;


/**
 * This is the business interface for InvestigationManager enterprise bean.
 */
@Local
public interface InvestigationManagerLocal {
    
    public Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public Collection<Investigation> getInvestigations(String sessionId, Collection<Long> investigationIds, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public Keyword addKeyword(String sessionId, Keyword keyword, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void removeKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void deleteKeyword(String sessionId, KeywordPK keywordPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public Investigator addInvestigator(String sessionId, Investigator investigator, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void modifyInvestigator(String sessionId, Investigator investigator) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public Sample addSample(String sessionId, Sample sample, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void removeSample(String sessionId, Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void deleteSample(String sessionId, Long sampleId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void modifySample(String sessionId, Sample sample) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public SampleParameter addSampleParameter(String sessionId, SampleParameter sampleParameter, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException;
    
    public void removeSampleParameter(String sessionId,SampleParameterPK sampleParameterPK) throws SessionException,  InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void deleteSampleParameter(String sessionId,SampleParameterPK sampleParameterPK) throws SessionException,  InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void modifySampleParameter(String sessionId,SampleParameter sampleParameter) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void deleteInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void removeInvestigator(String sessionId, InvestigatorPK investigatorPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public Publication addPublication(String sessionId, Publication publication, Long investigationId) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void removePublication(String sessionId, Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void modifyPublication(String sessionId, Publication publication) throws SessionException, ValidationException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    public void deletePublication(String sessionId, Long publicationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
    //auth bit
    public Collection<IcatAuthorisation> getAuthorisations(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public IcatAuthorisation addAuthorisation(String sessionId, String toAddUserId, String toAddRole, Long investigationId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException;
    
    public void deleteAuthorisation(String sessionId, Long authorisationId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;
    
    public void removeAuthorisation(String sessionId, Long authorisationId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException;
    
    public void updateAuthorisation(String sessionId, String toChangetoRole, Long authorisationId) throws SessionException, NoSuchObjectFoundException, InsufficientPrivilegesException, ValidationException;
    
}
