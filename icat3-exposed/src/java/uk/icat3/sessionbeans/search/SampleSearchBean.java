/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 7 oct. 2010
 */

package uk.icat3.sessionbeans.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import org.apache.log4j.Logger;
import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.search.AdvancedSearchDetails;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.search.KeywordDetails;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author cruzcruz
 */
@Stateless()
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SampleSearchBean extends EJBObject implements SampleSearchLocal {

    @Override
    public Collection<Sample> searchByParameterCondition(String sessionId, ParameterLogicalCondition logicalCondition) throws SessionException, ParameterSearchException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return SampleSearch.searchByParameterCondition(userId, logicalCondition, manager);
    }

    @Override
    public Collection<Sample> searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison) throws SessionException, ParameterSearchException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        List<ParameterComparisonCondition> list = new ArrayList<ParameterComparisonCondition>();
        for (ParameterComparisonCondition p : comparison) {
            list.add(p);
        }

        return SampleSearch.searchByParameterComparisonList(userId, list, manager);
    }

    @Override
    public Collection<Sample> searchByParameter(String sessionId, ParameterSearch[] parameters) throws SessionException, ParameterSearchException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        List<ParameterSearch> list = new ArrayList<ParameterSearch>();
        for (ParameterSearch p : parameters)
            list.add(p);

        return SampleSearch.searchByParameterList(userId, list, manager);
    }
}
