/*
 * DatasetSearchBean.java
 *
 * Created on 27 March 2007, 10:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.search;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import uk.icat3.entity.Sample;
import uk.icat3.exceptions.DatevalueException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ParameterSearchException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.restriction.RestrictionLogicalCondition;
import uk.icat3.search.DatasetSearch;
import uk.icat3.search.parameter.ParameterComparisonCondition;
import uk.icat3.search.parameter.ParameterCondition;
import uk.icat3.search.parameter.ParameterLogicalCondition;
import uk.icat3.search.parameter.util.ParameterSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;
import uk.icat3.util.DatasetInclude;
import uk.icat3.util.LogicalOperator;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
@Stateless
@PermitAll
//@WebService(targetNamespace="client.icat3.uk")
//this interceptor check no nulls passed in and logs the method arguments
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DatasetSearchBean extends EJBObject implements DatasetSearchLocal {
    
    /** Creates a new instance of DatasetSearchBean */
    public DatasetSearchBean() {
    }
    
    /**
     * From a sample name, return all the samples a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sampleName
     * @throws uk.icat3.exceptions.SessionException
     * @return collection
     */
    @WebMethod()
    public Collection<Sample> searchSamplesBySampleName(String sessionId, String sampleName) throws SessionException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.getSamplesBySampleName(userId, sampleName, manager);
    }
    
    /**
     * From a sample, return all the datafiles a user can view asscoiated with the sample name
     *
     * @param sessionId
     * @param sample
     * @throws uk.icat3.exceptions.SessionException
     * @throws uk.icat3.exceptions.NoSuchObjectFoundException 
     * @throws uk.icat3.exceptions.InsufficientPrivilegesException 
     * @return collection
     */
    @WebMethod()
    public Collection searchDataSetsBySample(String sessionId, Sample sample) throws SessionException, NoSuchObjectFoundException, NoSuchObjectFoundException, InsufficientPrivilegesException {
        
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.getDatasetsBySample(userId, sample, manager);
    }
    
    /**
     *  List all the valid avaliable types for datasets
     *
     * @param sessionId
     * @return collection of types
     * @throws uk.icat3.exceptions.SessionException 
     */
    @WebMethod()
    public Collection<String> listDatasetTypes(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.listDatasetTypes(manager);
    }
    
    /**
     * List all the valid avaliable status for datasets
     *
     * @param sessionId
     * @return collection of status
     */
    @WebMethod()
    public Collection<String> listDatasetStatus(String sessionId) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return DatasetSearch.listDatasetStatus(manager);
    }

    @WebMethod()
    @Override
    public Collection searchByParameterCondition(String sessionId, ParameterCondition logicalCondition) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return DatasetSearch.searchByParameterCondition(userId, logicalCondition, Queries.NO_RESTRICTION, DatasetInclude.NONE, manager);
    }

    @WebMethod()
    @Override
    public Collection searchByParameterComparison(String sessionId, ParameterComparisonCondition... comparison) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        List<ParameterComparisonCondition> list = new ArrayList<ParameterComparisonCondition>();
        for (ParameterComparisonCondition p : comparison) {
            list.add(p);
        }

        return DatasetSearch.searchByParameterComparisonList(userId, list, Queries.NO_RESTRICTION, DatasetInclude.NONE, manager);
    }

    @WebMethod()
    @Override
    public Collection searchByParameter(String sessionId, ParameterSearch... parameters) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        List<ParameterSearch> list = new ArrayList<ParameterSearch>();
        for (ParameterSearch p : parameters)
            list.add(p);

        return DatasetSearch.searchByParameterList(userId, list, Queries.NO_RESTRICTION, DatasetInclude.NONE, manager);
    }


    @WebMethod()
    @Override
    public Collection searchByParameterCondition(String sessionId, ParameterCondition paramLogCond, RestrictionCondition... restriction) throws SessionException, ParameterSearchException, RestrictionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restriction) {
            restLogCond.add(r);
            // TODO - these properties which apply to the whole query should not be part of the restriction mechanism
            if (r.hasMaxResults()) {
            	restLogCond.setMaxResults(r.getMaxResults());
            }
            if (r.isReturnLongId()) {
            	restLogCond.setReturnLongId(true);
            }
        }

        return DatasetSearch.searchByParameterCondition(userId, paramLogCond, restLogCond, DatasetInclude.NONE, manager);
    }

    @WebMethod()
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

        return DatasetSearch.searchByParameterComparisonList(userId, list, restLogCond, DatasetInclude.NONE, manager);
    }

    @WebMethod()
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

        return DatasetSearch.searchByParameterList(userId, list, restLogCond, DatasetInclude.NONE, manager);
    }

    @Override
    public Collection searchByRestriction(String sessionId, RestrictionCondition... restricion) throws SessionException, RestrictionException, DatevalueException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        RestrictionLogicalCondition restLogCond = new RestrictionLogicalCondition(LogicalOperator.AND);
        for (RestrictionCondition r : restricion)
            restLogCond.add(r);

        return DatasetSearch.searchByRestriction(userId, restLogCond, manager);
    }

    @Override
    public Collection searchByRestriction(String sessionId, RestrictionCondition restricion) throws SessionException, RestrictionException, DatevalueException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return DatasetSearch.searchByRestriction(userId, restricion, DatasetInclude.NONE, manager);
    }
}
