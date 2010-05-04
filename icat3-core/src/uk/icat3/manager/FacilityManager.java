/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.manager;

import java.util.Collection;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import uk.icat3.entity.FacilityCycle;
import uk.icat3.entity.FacilityUser;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import static uk.icat3.util.Queries.*;

/**
 * This is the manager class for all operations for facility related
 * objects - facility_users, facility_instrument_scientists and facility_cycles
 *
 * These are update, remove, delete, create on these objects.
 * <br /><br />
 * @author tang76
 */
public class FacilityManager extends ManagerUtil {

    // Global class logger
    static Logger log = Logger.getLogger(FacilityManager.class);

    /**
     * Lists all the facility cyles in the database
     *
     * @param manager manager object that will facilitate interaction with underlying database
     * @return List of {@link Facilty_Cycles}s
     */
    public static Collection<FacilityCycle> listAllFacilityCycles(EntityManager manager) {
        log.trace("listAllFacilityCycles(EntityManager)");
        return manager.createNamedQuery(ALL_FACILITYCYCLES)/*.setMaxResults(MAX_QUERY_RESULTSET)*/.getResultList();
    }

     /**
     * Get a facility user by their federalId
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return {@link Facilty_User}
     */
    public static FacilityUser getFacilityUserByFederalId(String federalId, EntityManager manager) throws NoSuchObjectFoundException {
        log.trace("getFacilityUserByFederalId(String, EntityManager)");

        try
        {
            FacilityUser user = (FacilityUser) manager.createNamedQuery("FacilityUser.findByFederalId").setParameter("fedId", federalId).getSingleResult();
            return user;
        }
        catch(Exception e)
        {
            throw new NoSuchObjectFoundException();
        }

    }

    /**
     * Get a facility user by their facilityUserId
     *
     * @param userId federalId of the user.
     * @param manager manager object that will facilitate interaction with underlying database
     * @return {@link Facilty_User}
     */
    public static FacilityUser getFacilityUserByFacilityUserId(String facilityUserId, EntityManager manager) {
        log.trace("getFacilityUserByFacilityUserId(String, EntityManager)");
        //return manager.createNamedQuery(ALL_FACILITYCYCLES)/*.setMaxResults(MAX_QUERY_RESULTSET)*/.getResultList();
        return manager.find(FacilityUser.class, facilityUserId);
    }

}
