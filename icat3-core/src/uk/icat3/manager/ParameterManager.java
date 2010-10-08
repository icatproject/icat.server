/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 aug 2010
 */

package uk.icat3.manager;

import java.util.List;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.Parameter;

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
}