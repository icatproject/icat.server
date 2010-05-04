/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.sessionbeans.manager;

import javax.ejb.Local;
import uk.icat3.entity.FacilityUser;
import uk.icat3.exceptions.NoSuchObjectFoundException;

/**
 *
 * @author tang76
 */
@Local
public interface FacilityManagerLocal {

    public java.util.Collection<uk.icat3.entity.FacilityCycle> listAllFacilityCycles(java.lang.String sessionId) throws uk.icat3.exceptions.SessionException;

    public uk.icat3.entity.FacilityUser getFacilityUserByFacilityUserId(java.lang.String sessionId, java.lang.String facilityUserId) throws uk.icat3.exceptions.SessionException;

    public FacilityUser getFacilityUserByFederalId(String sessionId, String federalId) throws uk.icat3.exceptions.SessionException, NoSuchObjectFoundException;
    
}
