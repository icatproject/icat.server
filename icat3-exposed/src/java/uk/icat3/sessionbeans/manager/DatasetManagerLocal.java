
package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.DatasetParameterPK;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.DatasetInclude;


/**
 * This is the business interface for DatasetManager enterprise bean.
 */
@Local
public interface DatasetManagerLocal {
    
    public Dataset getDataset(String sessionId, Long datasetId)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public Collection<Dataset> getDatasets(String sessionId, Collection<Long> datasetIds)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public Dataset getDataset(String sessionId, Long datasetId, DatasetInclude includes)  throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public Dataset createDataSet(String sessionId, Dataset dataSet, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    public Collection<Dataset> createDataSets(String sessionId, Collection<Dataset> dataSets, Long investigationId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    public DatasetParameter addDataSetParameter(String sessionId, DatasetParameter dataSetParameter, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    public void modifyDataSetParameter(String sessionId, DatasetParameter dataSetParameter) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    public void removeDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public void deleteDataSetParameter(String sessionId, DatasetParameterPK datasetParameterPK) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public void setDataSetSample(String sessionId, Long sampleId, Long datasetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
    public void deleteDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public void removeDataSet(String sessionId, Long dataSetId) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException ;
    
    public void modifyDataSet(String sessionId, Dataset dataSet) throws SessionException, InsufficientPrivilegesException, NoSuchObjectFoundException, ValidationException ;
    
}
