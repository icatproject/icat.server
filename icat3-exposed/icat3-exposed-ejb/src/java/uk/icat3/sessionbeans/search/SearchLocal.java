
package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.util.InvestigationInclude;



/**
 * This is the business interface for NewSession enterprise bean.
 */
@Local
public interface SearchLocal {
     public Collection<Long> searchByKeywordsRtnId(String sessionId, String keyword, InvestigationInclude include)  ;
  
     public Collection<uk.icat3.entity.Investigation> searchByKeywords(String sessionId, Collection<String> keywords, InvestigationInclude include);   
    // public ArrayList<Investigation> searchByKeywords()  ;
 
}
