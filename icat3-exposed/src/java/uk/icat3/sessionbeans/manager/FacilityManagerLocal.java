/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import java.util.Collection;
import javax.ejb.Local;
import uk.icat3.entity.FacilityUser;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.RestrictionException;
import uk.icat3.exceptions.SessionException;
import uk.icat3.restriction.RestrictionCondition;

/**
 *
 * @author tang76
 */
@Local
public interface FacilityManagerLocal {

    public java.util.Collection<uk.icat3.entity.FacilityCycle> listAllFacilityCycles(java.lang.String sessionId) throws uk.icat3.exceptions.SessionException;

    public uk.icat3.entity.FacilityUser getFacilityUserByFacilityUserId(java.lang.String sessionId, java.lang.String facilityUserId) throws uk.icat3.exceptions.SessionException;

    public FacilityUser getFacilityUserByFederalId(String sessionId, String federalId) throws uk.icat3.exceptions.SessionException, NoSuchObjectFoundException;

    /**
     * Search FacilityUser which match with restriction conditions
     *
     * @param userId User identifaction
     * @param restriction Restriction condition
     * 
     * @return Collection of investigation
     *
     * @throws SessionException
     * @throws RestrictionException
     */
    Collection searchByRestriction(String sessionId, RestrictionCondition restricion) throws SessionException, RestrictionException;
    
}
