package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restrictions.RestrictionCondition;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.util.DatasetInclude;

/**
 * This is the business interface for DatasetSearch enterprise bean.
 */
@Local
public interface DatasetSearchLocal {

    Collection<Sample> searchSamplesBySampleName(String sessionId, String sampleName) throws SessionException;

    Collection<Dataset> searchDataSetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, NoSuchObjectFoundException, InsufficientPrivilegesException;

    Collection<String> listDatasetTypes(String sessionId) throws SessionException;

    Collection<String> listDatasetStatus(String sessionId) throws SessionException;

    /**
     * Return datasets matched by a logical condition.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    public Collection searchByParameterCondition(String sessionId, ParameterLogicalCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException;

    /**
     * Return datasets matched by comparison(s).
     *
     * @param sessionId Session identification
     * @param comparison Comparison
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison) throws SessionException, ParameterSearchException, RestrictionException;

    /**
     * Return datasets matched by parameter(s).
     *
     * @param sessionId Session identification
     * @param parameters Parameters
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    public Collection searchByParameter(String sessionId, ParameterSearch[] parameters) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameterCondition(String sessionId, ParameterLogicalCondition logicalCondition, DatasetInclude include, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison, DatasetInclude include, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameter(String sessionId, ParameterSearch[] parameters, DatasetInclude include, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameterCondition(String sessionId, ParameterLogicalCondition logicalCondition, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameter(String sessionId, ParameterSearch[] parameters, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameterCondition(String sessionId, ParameterLogicalCondition logicalCondition, DatasetInclude include) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison, DatasetInclude include) throws SessionException, ParameterSearchException, RestrictionException;

    public Collection searchByParameter(String sessionId, ParameterSearch[] parameters, DatasetInclude include) throws SessionException, ParameterSearchException, RestrictionException;
}
