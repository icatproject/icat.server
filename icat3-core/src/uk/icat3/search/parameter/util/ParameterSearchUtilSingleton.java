/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 4 July 2010
 */

package uk.icat3.search.parameter.util;

/**
 * Singleton for the class ParmeterSearchUtils
 *
 * @author cruzcruz
 * @see ParameterSearchUtil
 */
public class ParameterSearchUtilSingleton {

    private static ParameterSearchUtil util = null;

    public static ParameterSearchUtil getInstance () {
        if (util == null) {
            util = new ParameterSearchUtil();
        }
        else
            // Everytime the object is access has to be reset.
            util.reset();

        return util;
    }
}
