/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 20 oct. 2010
 */

package uk.icat3.search;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import uk.icat3.entity.Parameter;
import uk.icat3.util.Queries;

/**
 * This class implements functions to make searchs in the parameter table.
 *
 * @author cruzcruz
 */
public class ParameterSearch {

    // Global class logger
    static Logger log = Logger.getLogger(ParameterSearch.class);

    /**
     * Returns parameters matched by name and units. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     * 
     * @param userId User login identification
     * @param name Parameter name
     * @param units Parameter units
     * @param manager Entity manager which handles database
     * @return Paremeter collection matched by name and units
     */
    public static Collection<Parameter> getParameterByNameUnits (String userId, String name, String units, EntityManager manager) {
        log.trace("getParameterByNameUnits(" + userId + ", " + name + ", " + units + ", EntityManager)");

        if (name == null)
            name = "";
        else if (units == null)
            units = "";
        // Select type of search
        boolean sensitive = false;
        boolean eager = true;
        name = sensitiveEager (name, sensitive, eager);
        units = sensitiveEager(units, sensitive, eager);
        // Select query (no sensitive = all fields to lower)
        String namedQuery = Queries.PARAMETER_SEARCH_BY_NAME_UNITS;
        if (sensitive)
            namedQuery = Queries.PARAMETER_SEARCH_BY_NAME_UNITS_SENSITIVE;
        
        return manager.createNamedQuery(namedQuery)
                .setParameter("name", "" + name + "")
                .setParameter("units", "" + units + "").getResultList();
    }

    /**
     * Returns parameters matched by name. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     *
     * @param userId User login identification
     * @param name Parameter name
     * @param manager Entity manager which handles database
     * @return Paremeter collection matched by name
     */
    public static Collection<Parameter> getParameterByName (String userId, String name, EntityManager manager) {
        log.trace("getParameterByName(" + userId + ", " + name + ", EntityManager)");

        if (name == null)
            name = "";
        // Select of type of search
        boolean sensitive = false;
        boolean eager = true;
        name = sensitiveEager(name, sensitive, eager);
        // Select query (no sensitive = all fields to lower)
        String namedQuery = Queries.PARAMETER_SEARCH_BY_NAME;
        if (sensitive)
            namedQuery = Queries.PARAMETER_SEARCH_BY_NAME_SENSITIVE;
        return manager.createNamedQuery(namedQuery)
                .setParameter("name", "" + name + "").getResultList();
    }

    /**
     * Returns parameters matched by units. The search parameters are
     * insensitive (no different between lowercase or uppercase) and eager (match
     * the word, LIKE '%name%' behavior).
     *
     * @param userId User login identification
     * @param units Parameter units
     * @param manager Entity manager which handles database
     * @return
     */
    public static Collection<Parameter> getParameterByUnits (String userId, String units, EntityManager manager) {
        log.trace("getParameterByName(" + userId + ", " + units + ", EntityManager)");

        if (units == null)
            units = "";

         // Select of type of search
        boolean sensitive = false;
        boolean eager = true;
        units = sensitiveEager(units, sensitive, eager);
        // Select query (no sensitive = all fields to lower)
        String namedQuery = Queries.PARAMETER_SEARCH_BY_UNITS;
        if (sensitive)
            namedQuery = Queries.PARAMETER_SEARCH_BY_UNITS_SENSITIVE;
        return manager.createNamedQuery(namedQuery)
                .setParameter("units", "" + units + "").getResultList();
    }

    /**
     * Transform the string (JPQL parameter) to handle in
     * sensitive (different between lowercase or uppercase) and/or eager (match
     * the word, LIKE '%name%' behavior)
     * 
     * @param value JPQL parameter to transform
     * @param sensitive If parameter is sensitive
     * @param eager If parmaeter is eager
     * @return
     */
    private static String sensitiveEager (String value, boolean sensitive, boolean eager) {
        if (!sensitive)
            value = value.toLowerCase();
        if (eager)
            value = "%" + value + "%";
        return value;
    }

  
 
}
