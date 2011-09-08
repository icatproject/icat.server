package uk.icat3.sessionbeans.manager;

import java.util.Collection;

import javax.ejb.Local;

import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.InvestigationInclude;

/**
 * This is the business interface for InvestigationManager enterprise bean.
 */
@Local
public interface InvestigationManagerLocal {
    
    Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

    Collection<Investigation> getInvestigations(String sessionId, Collection<Long> investigationIds, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

}
