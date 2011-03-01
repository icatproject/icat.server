/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 30 aug 2010
 */

package uk.icat3.manager;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import org.apache.log4j.Logger;
import uk.icat3.entity.Parameter;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.ParameterValueType;

/**
 * This is the manager class for all operations for parameter.
 * @author cruzcruz
 * @author Srikanth Nagella
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
     * This method creates a new parameter in the parameters table. name and the units
     * have to be unique otherwise throws an exception.
     * @param userId : The user id requested to add the parameter
     * @param name : The name of parameter
     * @param units : The units of parameter
     * @param valueType : Value type (Numeric, String etc..)
     * @param isSearchable : Is parameter searchable
     * @param isDatasetParameter : is the parameter is used for dataset
     * @param isDatafileParameter : is the parameter is used for datafile
     * @param isSampleParameter : is the parameter is used for sample
     * @param manager : persistance entity manager
     */
    public static Parameter createParameter(String userId, String name, String units, ParameterValueType valueType, boolean isSearchable, boolean isDatasetParameter, boolean isDatafileParameter, boolean isSampleParameter, EntityManager manager) throws ValidationException{
//    public Parameter(ParameterPK parameterPK, String searchable, String valueType, String isSampleParameter, String isDatasetParameter, String isDatafileParameter, String modId, Date modTime) {
        Parameter param = new Parameter( units, name);
        param.setValueType(valueType);
        param.setSampleParameter(isSampleParameter);
        param.setDatasetParameter(isDatasetParameter);
        param.setDatafileParameter(isDatafileParameter);
        if(isSearchable)
            param.setSearchable("Y");
        else
            param.setSearchable("N");
        createParameter(userId,param,manager);
        return param;
    }

    /**
     * This method also creates a new parameter in the parameter table. Parameter needs to be
     * unique otherwise it will fail.
     * @param userId : the user id requested to add the parameter
     * @param param  : the parameter to be persisted
     * @param manager : entity manager
     */
    public static void createParameter(String userId, Parameter param, EntityManager manager) throws ValidationException{
        //TODO: Check whether user has permission to add Parameters
        //Check all the values are set
        param.setCreateId(userId);
        param.setModId(userId);
        //TODO: Check whether the SUPER is inserting and set it to verified true
        param.setVerified(false);
        param.isValid(manager);

        try{
            manager.persist(param);
            //TODO: Change this so that the commit returns if there is an error
            manager.flush();
        }catch(PersistenceException ex){
            System.out.println("Caught Pesistance Exception"+ex);
            throw new ValidationException("Parameter Already Exists "+ex.toString());
        }catch(Exception ex){
            System.out.println("Caught unknown exception "+ex);
            throw new ValidationException("Parameter Already Exists "+ex.toString());
        }
    }

    /**
     * This method updates a parameter in the parameters table. name and the units
     * is used for searching the parameter and the other options can be updated.
     * @param userId : The user id requested to update the parameter
     * @param name : The name of parameter
     * @param units : The units of parameter
     * @param valueType : Value type (Numeric, String etc..)
     * @param isSearchable : Is parameter searchable
     * @param isDatasetParameter : is the parameter is used for dataset
     * @param isDatafileParameter : is the parameter is used for datafile
     * @param isSampleParameter : is the parameter is used for sample
     * @param manager : persistance entity manager
     */
    public static Parameter updateParameter(String userId, String name, String units, boolean isSearchable, boolean isDatasetParameter, boolean isDatafileParameter, boolean isSampleParameter, EntityManager manager) throws ValidationException{
        try{
        //Find the parameter matching name and units
            Parameter param = (Parameter)manager.createNamedQuery("Parameter.findByNameAndUnits").setParameter("name", name).setParameter("units", units).getSingleResult();
        //TODO: Check user has permission to remove the parameter

        //update the parameter from database
           if(isSearchable)
               param.setSearchable("Y");
           else
               param.setSearchable("N");
           param.setDatasetParameter(isDatasetParameter);
           param.setDatafileParameter(isDatafileParameter);
           param.setSampleParameter(isSampleParameter);
           param.setModId(userId);
           param.setModTime(new Date());
           manager.merge(param);
           return param;
        }catch(NoResultException ex){
            throw new ValidationException("Parameter "+name+","+units+" doesn't exsist");
        }catch(NonUniqueResultException ex){
            throw new ValidationException("Database is not setup properly");
        }catch(Exception ex){ //Cannot be removed
            throw new ValidationException("Parameter is used by other entities and cannot be removed",ex);
        }
    }

    /**
     * This method is used for removing a parameter that is not required any more. It the
     * parameter is used then an exception is thrown. it uses name an units to find the parameter.
     * @param userId : userId requested to remove the parameter
     * @param name : name of the parameter
     * @param units : units of the parameter
     * @param manager : entity manager
     */
    public static void removeParameter(String userId, String name, String units,EntityManager manager) throws ValidationException{
        try{
        //Find the parameter matching name and units
            Parameter param = (Parameter)manager.createNamedQuery("Parameter.findByNameAndUnits").setParameter("name", name).setParameter("units", units).getSingleResult();

        //TODO: Check user has permission to remove the parameter

        //Remove the parameter from database
          if(param.isVerified()) throw new Exception("Parameter is verfied and can only be deleted manually from database");
          manager.remove(param);
        }catch(NoResultException ex){
            throw new ValidationException("Parameter "+name+","+units+" doesn't exsist");
        }catch(NonUniqueResultException ex){
            throw new ValidationException("Database is not setup properly");
        }catch(Exception ex){ //Cannot be removed
            throw new ValidationException("Parameter is used by other entities and cannot be removed",ex);
        }
    }
}