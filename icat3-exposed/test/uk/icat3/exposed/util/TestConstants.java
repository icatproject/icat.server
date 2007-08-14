/*
 * TestConstants.java
 *
 * Created on 22 February 2007, 12:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.exposed.util;

/**
 *
 * @author gjd37
 */
public class TestConstants {
    
    public final static String VALID_SESSION =  "validSession";
    public final static String VALID_SESSION_ICAT_ADMIN =  "validSessionIcatAdmin";
    public final static String INVALID_SESSION =  "invalidSession";
    public final static String VALID_USER_FOR_INVESTIGATION =  "test";
    public final static String VALID_ICAT_ADMIN_FOR_INVESTIGATION =  "test_icatadmin";
    public final static String VALID_FACILITY_USER_FOR_INVESTIGATION =  VALID_USER_FOR_INVESTIGATION;
    public final static String VALID_FACILITY_USER_FOR_PROPS_INVESTIGATION =  "FIRST PROPAGATION";
    public final static Long VALID_DATASET_ID_FOR_INVESTIGATION =  3L;
    public final static Long VALID_INVESTIGATION_ID =  3L;
    public final static Long VALID_DATA_SET_ID =  3L;
     public final static Long VALID_DATA_SET_FA_ID =  5L;
    public final static Long VALID_DATA_FILE_ID =  3L;
    public final static Long VALID_SAMPLE_ID_FOR_INVESTIGATION_ID =  3L;
     
    
    public final static String INVALID_USER =  "invalidUser" +Math.random();
    public final static String VALID_INVESTIGATION_SURNAME  = "Drinkwater";
    
    
    public final static String PERSISTENCE_UNIT = "icat3-scratch-testing-PU";
    // public final static String PERSISTENCE_UNIT = "icat3-unit-testing-PU";
}
