
package uk.icat3.sessionbeans.manager;

import javax.ejb.Local;
import uk.icat3.entity.Datafile;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;

/**
 * This is the business interface for DatasetManager enterprise bean.
 */
@Local
public interface DatafileManagerLocal {
   
   public Datafile getDatafile(String sessionId, Long datafileId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
   
}
