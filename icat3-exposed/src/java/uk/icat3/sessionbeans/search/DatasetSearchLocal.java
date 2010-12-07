package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.DatasetInclude;

/**
 * This is the business interface for DatasetSearch enterprise bean.
 */
@Local
public interface DatasetSearchLocal extends ParameterSearchInterface {

    Collection<Sample> searchSamplesBySampleName(String sessionId, String sampleName) throws SessionException;

    Collection<Dataset> searchDataSetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    Collection<String> listDatasetTypes(String sessionId) throws SessionException;

    Collection<String> listDatasetStatus(String sessionId) throws SessionException;
}
