
package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileFormat;
import uk.icat3.exceptions.SessionException;


/**
 * This is the business interface for DatafileSearch enterprise bean.
 */
@Local
public interface DatafileSearchLocal {
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun) throws SessionException ;    
    
    public Collection<Datafile> searchByRunNumber(String sessionId, Collection<String> instruments, float startRun, float endRun, int startIndex, int number_results) throws SessionException ;    

     public Collection<DatafileFormat> listDatafileFormats(String sessionId) throws SessionException ;
   
}
