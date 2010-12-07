/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 25 nov. 2010
 */

package uk.icat3.sessionbeans.search;

import java.util.Collection;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.search.parameter.util.ParameterSearch;

/**
 * This interface defines functions for parameter search.
 * T class is a Enum Object and should be one of the type of search
 * we are goint to make (DatasetInclude, DatafileInclude, InvestigationInclude,
 * SampleInclude)
 * 
 * @author cruzcruz
 */
public interface ParameterSearchInterface {

    /**
     * Search investigation which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @return Collection of investigation
     *
     * @throws SessionException
     * @throws RestrictionException
     * @throws DatevalueException
     */
    Collection searchByRestriction(String sessionId, RestrictionCondition... restricion) throws SessionException, RestrictionException, DatevalueException;
    /**
     * Search investigation which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * @return Collection of investigation
     *
     * @throws SessionException
     * @throws RestrictionException
     * @throws DatevalueException
     */
    Collection searchByRestriction(String sessionId, RestrictionCondition restricion) throws SessionException, RestrictionException, DatevalueException;
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
    public Collection searchByParameterCondition(String sessionId, ParameterCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException;

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
    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition... comparison) throws SessionException, ParameterSearchException, RestrictionException;

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
    public Collection searchByParameter(String sessionId, ParameterSearch... parameters) throws SessionException, ParameterSearchException, RestrictionException;
    /**
     * Return datasets matched by a logical condition and restriction.
     *
     * @param sessionId Session identification
     * @param logicalCondition Logial condition
     * @param restriction Restrictions
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    public Collection searchByParameterCondition(String sessionId, ParameterCondition logicalCondition, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;
    /**
     * Return datasets matched by comparison(s) and restrictions.
     *
     * @param sessionId Session identification
     * @param comparison Comparison
     * @param restriction Restrictions
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;
    /**
     * Return datasets matched by parameter(s) and restrictions.
     *
     * @param sessionId Session identification
     * @param parameters Parameters
     * @param restriction Restrictions
     * @return Collection of datasets
     *
     * @throws SessionException
     * @throws ParameterSearchException
     */
    public Collection searchByParameter(String sessionId, ParameterSearch[] parameters, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException;
}
