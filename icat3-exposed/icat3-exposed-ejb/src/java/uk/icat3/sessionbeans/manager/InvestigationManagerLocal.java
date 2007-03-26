
package uk.icat3.sessionbeans.manager;

import javax.ejb.Local;
import uk.icat3.entity.Dataset;
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
    
    public Investigation getInvestigation(String sessionId, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
        
    public Investigation getInvestigation(String sessionId, Long investigationId, InvestigationInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
        
    public  boolean addDataSet(String sessionId, Long investigationId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;    
    
}
