/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 20 oct. 2010
 */

package uk.icat3.sessionbeans.search;

import java.util.Collection;

import javax.ejb.Stateless;

import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.SessionException;
import uk.icat3.search.ParameterSearch;
import uk.icat3.sessionbeans.EJBObject;

@Stateless
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

}
