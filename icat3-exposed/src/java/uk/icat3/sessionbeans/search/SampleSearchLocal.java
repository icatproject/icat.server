/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 7 oct. 2010
 */

package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.util.ParameterSearch;

/**
 * This is the business interface for SampleSearch enterprise bean.
 * @author cruzcruz
 */
@Local
public interface SampleSearchLocal {
    
    public Collection<Sample> searchByParameterCondition(String sessionId, ParameterLogicalCondition logicalCondition) throws SessionException, ParameterSearchException;

    public Collection<Sample> searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison) throws SessionException, ParameterSearchException;

    public Collection<Sample> searchByParameter(String sessionId, ParameterSearch[] parameters) throws SessionException, ParameterSearchException;
}
