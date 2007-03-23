
package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import javax.ejb.Remote;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.InvestigationInclude;



/**
 * This is the business interface for NewSession enterprise bean.
 */

@Remote
public interface SearchRemote {
    
    public Collection<Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include, boolean fuzzy) throws SessionException ;
    
    public Collection<Investigation> searchByKeyword(String sessionId, String keywords) throws SessionException ;
    
    public Collection<Long> searchByKeywordsRtnId(String sessionId, String keyword) throws SessionException ;
    
    public Collection<String> listInstruments(String sessionId) throws SessionException ;
    
    public Collection<Investigation> searchMyInvestigations(String sessionId) throws SessionException ;
    
    public Collection<Investigation> searchUser(String sessionId, String userSearch) throws SessionException ;
    
}