
package uk.icat3.sessionbeans.manager;

import javax.ejb.Local;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.DatasetInclude;


/**
 * This is the business interface for DatasetManager enterprise bean.
 */
@Local
public interface DatasetManagerLocal {
   
   public Dataset getDataset(String sessionId, Long datasetId, DatasetInclude includes) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;

   public Dataset getDataset(String sessionId, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException;
    
}
