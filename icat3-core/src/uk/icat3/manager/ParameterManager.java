/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 aug 2010
 */

package uk.icat3.manager;

import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.NoElementTypeException;
import uk.icat3.exceptions.NoParameterTypeException;
import uk.icat3.exceptions.NoSearchableParameterException;
import uk.icat3.exceptions.ParameterNoExistsException;
import uk.icat3.search.parameter.ParameterType;

/**
 * This is the manager class for all operations for parameter.
 * @author cruzcruz
 */
public class ParameterManager extends ManagerUtil {

    static Logger log = Logger.getLogger(ParameterManager.class);

    /**
     * Return a parameter by its name and units
     * 
     * @param name Name of parameter
     * @param units Units of parameter
     * @param manager Entity Manager
     * @return parameter or null if parameter doesn't exists.
     */
    public static Parameter getParameter (String name, String units, EntityManager manager) {
        log.trace("getParameter(" + name + ", " + units + ", EntityManager)");
        
        List lp = manager.createQuery("select p from Parameter p where " +
                "p.parameterPK.name = '" + name + "' and " +
                "p.parameterPK.units = '" + units + "'").getResultList();

        if (lp.size() == 0)
            return null;

        return (Parameter) lp.get(0);
    }

    /**
     * Check the collection of parameter exists in the database and are searchable
     * for the type it belongs.
     * 
     * @param collecParam Collection of parameters
     * @param type Type
     * @param manager Entity Manager
     * @throws ParameterNoExistsException
     * @throws NoSearchableParameterException
     * @throws NoElementTypeException
     */
    public static void existsSearchableParameters (Collection<Parameter> collecParam, ParameterType type, EntityManager manager) throws ParameterNoExistsException, NoSearchableParameterException, NoParameterTypeException {
        log.trace("existsSearchableParameters (collecParam, " + type.name() + ", manager)");
        
        Parameter param = null;
        for (Parameter p : collecParam) {
            param = ParameterManager.getParameter(p.getParameterPK().getName(),
                        p.getParameterPK().getUnits(), manager);

            if (param == null)
                 throw new ParameterNoExistsException(p);
            else if (!param.getSearchable().equalsIgnoreCase("Y"))
                throw new NoSearchableParameterException(param);
            else if (type == ParameterType.DATAFILE && !param.isDatafileParameter())
                throw new NoSearchableParameterException(param, "Parameter not relevant for Datafile");
            else if (type == ParameterType.DATASET && !param.isDatasetParameter())
                throw new NoSearchableParameterException(param, "Parameter not relevant for Dataset");
            else if (type == ParameterType.SAMPLE && !param.isSampleParameter())
                throw new NoSearchableParameterException(param, "Parameter not relevant for Sample");
        }
    }
}