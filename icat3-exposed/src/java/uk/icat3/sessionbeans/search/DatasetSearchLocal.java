
package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;


/**
 * This is the business interface for DatasetSearch enterprise bean.
 */
@Local
public interface DatasetSearchLocal {
    public Collection<Sample> searchSamplesBySampleName(String sessionId, String sampleName) throws SessionException ;
    
    public Collection<Dataset> searchDataSetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, NoSuchObjectFoundException, InsufficientPrivilegesException ;
    
    public Collection<String> listDatasetTypes(String sessionId) throws SessionException ;
    
    public Collection<String> listDatasetStatus(String sessionId) throws SessionException ;
}
