/*
 * TestGateKeeperUtil.java
 *
 * Created on 27-Jul-2007, 08:15:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.security;

import uk.icat3.entity.IcatRole;
import uk.icat3.entity.Investigation;
import uk.icat3.util.AccessType;
import uk.icat3.util.BaseTestClassTX;
import uk.icat3.util.IcatRoles;
import static uk.icat3.util.TestConstants.*;
import static uk.icat3.util.Util.*;
import static org.junit.Assert.*;
/**
 *
 * @author gjd37
 */
public class TestGateKeeperUtil extends BaseTestClassTX {
    
    
    protected Investigation getInvestigation(boolean valid){
        if(valid){
            Investigation investigation = em.find(Investigation.class, VALID_INVESTIGATION_ID_FOR_READER);
            
            return investigation;
        } else {
            //create invalid investigation
            Investigation investigation = new Investigation();
            return investigation;
        }
    }
}
