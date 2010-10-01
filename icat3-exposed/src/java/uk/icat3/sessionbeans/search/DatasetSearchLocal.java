package uk.icat3.sessionbeans.search;

import java.util.Collection;
import java.util.List;
import javax.ejb.Local;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetStatus;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;

/**
 * This is the business interface for DatasetSearch enterprise bean.
 */
@Local
public interface DatasetSearchLocal {

    Collection<Sample> searchSamplesBySampleName(String sessionId, String sampleName) throws SessionException;

    Collection<Dataset> searchDatasetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    Collection<Dataset> searchDatasetsByName(String sessionId, String datasetName) throws SessionException;

    Collection<Dataset> searchDatasetsByParameterOperable (String sessionId, ParameterCondition parameterOperable) throws SessionException, ParameterSearchException;

    Collection<Dataset> searchDatasetsByParameter(String sessionId, List<ParameterComparisonCondition> parameterComparator) throws SessionException, ParameterSearchException;
    
    Collection<String> listDatasetTypes(String sessionId) throws SessionException;

    Collection<String> listDatasetStatus(String sessionId) throws SessionException;
}
