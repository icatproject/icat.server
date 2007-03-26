
package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.InvestigationInclude;


/**
 * This is the business interface for InvestigationSearch enterprise bean.
 */
@Local
public interface InvestigationSearchLocal {
    
    Collection<Investigation> searchByUserID(String sessionId, String userSearch) throws SessionException;
    
    Collection<Investigation> getMyInvestigations(String sessionId) throws SessionException;
    
    Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException;
    
}
