
package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.exceptions.SessionException;


/**
 * This is the business interface for DatasetSearch enterprise bean.
 */
@Local
public interface DatasetSearchLocal {
    public Collection<Dataset> searchBySampleName(String sessionId, String sampleName) throws SessionException ;
    
    public Collection<DatasetType> listDatasetTypes(String sessionId) throws SessionException ;
    
    public Collection<DatasetStatus> listDatasetStatus(String sessionId) throws SessionException ;
}
