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
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.search.SampleSearch;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;
import uk.icat3.util.SampleInclude;

/**
 *
 * @author cruzcruz
 */
@Stateless()
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method sarguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SampleSearchBean extends EJBObject implements SampleSearchLocal {

    @Override
    public Collection<Sample> searchByParameterCondition(String sessionId, ParameterCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return SampleSearch.searchByParameterCondition(userId, logicalCondition, Queries.NO_RESTRICTION, SampleInclude.NONE, manager);
    }

    @Override
    public Collection<Sample> searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        List<ParameterComparisonCondition> list = new ArrayList<ParameterComparisonCondition>();
        for (ParameterComparisonCondition p : comparison) {
            list.add(p);
        }

        return SampleSearch.searchByParameterComparisonList(userId, list, Queries.NO_RESTRICTION, SampleInclude.NONE, manager);
    }

    @Override
    public Collection<Sample> searchByParameter(String sessionId, ParameterSearch[] parameters) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        List<ParameterSearch> list = new ArrayList<ParameterSearch>();
        for (ParameterSearch p : parameters)
            list.add(p);

        return SampleSearch.searchByParameterList(userId, list, Queries.NO_RESTRICTION, SampleInclude.NONE, manager);
    }


    @Override
    public Collection searchByParameterCondition(String sessionId, ParameterCondition logicalCondition, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restriction) {
            restLogCond.add(r);
        }

        return SampleSearch.searchByParameterCondition(userId, logicalCondition, restLogCond, SampleInclude.NONE, manager);
    }

    @Override
    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition[] comparison, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restriction) {
            restLogCond.add(r);
        }

        List<ParameterComparisonCondition> list = new ArrayList<ParameterComparisonCondition>();
        for (ParameterComparisonCondition p : comparison) {
            list.add(p);
        }

        return SampleSearch.searchByParameterComparisonList(userId, list, restLogCond, SampleInclude.NONE, manager);
    }

    @Override
    public Collection searchByParameter(String sessionId, ParameterSearch[] parameters, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException {
         //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restriction) {
            restLogCond.add(r);
        }

        List<ParameterSearch> list = new ArrayList<ParameterSearch>();
        for (ParameterSearch p : parameters)
            list.add(p);

        return SampleSearch.searchByParameterList(userId, list, restLogCond, SampleInclude.NONE, manager);
    }


    @Override
    public Collection searchByRestriction(String sessionId, RestrictionCondition... restricion) throws SessionException, RestrictionException, DatevalueException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restricion)
            restLogCond.add(r);

        return SampleSearch.searchByRestriction(userId, restLogCond, manager);
    }

    @Override
    public Collection searchByRestriction(String sessionId, RestrictionCondition restricion) throws SessionException, RestrictionException, DatevalueException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        return SampleSearch.searchByRestriction(userId, restricion, SampleInclude.NONE, manager);
    }
}
