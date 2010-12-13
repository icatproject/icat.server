/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 20 oct. 2010
 */

package uk.icat3.sessionbeans.search;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restriction.RestrictionCondition;

/**
 * This interface is in charge of provide methods to retrive parameters according
 * to parameter name and/or units.
 * 
 * @author cruzcruz
 */
@Local
public interface ParameterSearchLocal {

    /**
     * Returns parameters matched by name and units. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     * 
     * @param sessionId Session identification
     * @param name Parameter name
     * @param units Parameter units
     * @param manager Entity manager which handles database
     * @return Paremeter collection matched by name and units
     * 
     * @throws SessionException
     */
    Collection<Parameter> getParameterByNameUnits (String sessionId, String name, String units) throws SessionException;

    /**
     * Returns parameters matched by name. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     *
     * @param sessionId Session identification
     * @param name Parameter name
     * @param manager Entity manager which handles database
     * @return Paremeter collection matched by name
     *
     * @throws SessionException
     */
    Collection<Parameter> getParameterByName (String sessionId, String name) throws SessionException;

    /**
     * Returns parameters matched by RestrictionCondition. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     *
     * @param sessionId Session identification
     * @param name Parameter name
     * @param manager Entity manager which handles database
     * @return Paremeter collection matched by name
     *
     * @throws SessionException
     */
    Collection getParameterByRestriction (String sessionId, RestrictionCondition condition) throws SessionException, RestrictionException;

    /**
     * Returns parameters matched by units. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     *
     * @param sessionId Session identification
     * @param units Parameter units
     * @param manager Entity manager which handles database
     * @return Paremeter collection matched by units
     *
     * @throws SessionException
     */
    Collection<Parameter> getParameterByUnits (String sessionId, String units) throws SessionException;
}
