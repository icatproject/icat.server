/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 20 oct. 2010
 */

package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restriction.RestrictionCondition;
import uk.icat3.search.ParameterSearch;
import uk.icat3.sessionbeans.ArgumentValidator;
import uk.icat3.sessionbeans.EJBObject;

/**
 * The bean is in charge to make search in parameter table
 * 
 * @author cruzcruz
 */
@Stateless
@Interceptors(ArgumentValidator.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ParameterSearchBean extends EJBObject implements ParameterSearchLocal {

    @Override
    public Collection<Parameter> getParameterByNameUnits(String sessionId, String name, String units) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return ParameterSearch.getParameterByNameUnits(userId, name, units, manager);
    }

    @Override
    public Collection<Parameter> getParameterByName(String sessionId, String name) throws SessionException {
         //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);

        return ParameterSearch.getParameterByName(userId, name, manager);
    }

    @Override
    public Collection<Parameter> getParameterByUnits(String sessionId, String units) throws SessionException {
        //for user bean get userId
        String userId = user.getUserIdFromSessionId(sessionId);
        
        return ParameterSearch.getParameterByUnits(userId, units, manager);
    }

    @Override
    public Collection getParameterByRestriction(String sessionId, RestrictionCondition condition) throws SessionException, RestrictionException {
        String userId = user.getUserIdFromSessionId(sessionId);

        return ParameterSearch.getParameterByRestriction(userId, condition, manager);
    }
}
