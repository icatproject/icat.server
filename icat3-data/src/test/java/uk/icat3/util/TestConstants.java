/*
 * TestConstants.java
 *
 * Created on 22 February 2007, 12:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 *
 * @author gjd37
 */
public class TestConstants {
    
    
    //searching
    public final static String VALID_KEYWORD = "calibration";
    public final static String VALID_KEYWORD2 = "ccwilson";
    
    public final static String VALID_SAMPLE_NAME = "SrF2 calibration  w=-25.3";
    
    public final static String VALID_FEDID_FOR_INVESTIGATION =  "gjd37";
    public final static String VALID_SURNAME_FOR_INVESTIGATION =  "User"; //from test user
    
    public final static String VALID_FACILITY_USER_FOR_INVESTIGATION =  "test";
    public final static String VALID_USER_FOR_INVESTIGATION =  VALID_FACILITY_USER_FOR_INVESTIGATION;
    public final static String VALID_FACILITY_USER_FOR_PROPS_INVESTIGATION =  "FIRST PROPAGATION";
    public final static long VALID_DATASET_ID_FOR_INVESTIGATION =  3;
    public final static long VALID_DATASET_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED =  3;
    public final static long VALID_DATAFILE_ID_FOR_INVESTIGATION_FOR_NOT_FACILITY_ACQURED =  3;
    public final static long VALID_INVESTIGATION_ID_FOR_NOT_FACILITY_ACQURED = 5;
    public final static long VALID_INVESTIGATION_ID =  3;
    public final static long VALID_INVESTIGATION_ID_TO_DE_DELETED =  7;
    public final static long VALID_DATASET_ID_TO_DE_DELETED =  6;
    public final static long VALID_DATA_SET_ID =  3;
    public final static long VALID_DATA_FILE_ID =  3;
    public final static long VALID_SAMPLE_ID_FOR_INVESTIGATION_ID =  3;
    
    public final static String INVALID_USER =  "invalidUser" +Math.random();
    public final static String VALID_INVESTIGATION_SURNAME  = "HEALY";
    
    
    //testing gatekeeper
    public final static String CREATOR_USER = "test_creator";
    public final static String READER_USER = "test_reader";
    public final static String DOWNLOADER_USER = "test_downloader";
    public final static String UPDATER_USER = "test_updater";
    public final static String DELETER_USER = "test_deleter";
    public final static String ADMIN_USER = "test_admin";
    public final static String ICAT_ADMIN_USER = "test_icatadmin";
    
    public final static long VALID_DATA_SET_ID_FOR_ICAT_ADMIN =  3;
    public final static long VALID_DATA_FILE_ID_FOR_ICAT_ADMIN  =  3;
    public final static long VALID_INVESTIGATION_ID_FOR_ICAT_ADMIN  =  3;
    
    public final static long VALID_DATA_SET_ID_GATEKEEPER_TEST =  5;
    public final static long VALID_DATA_FILE_ID_GATEKEEPER_TEST =  57;
    public final static long VALID_INVESTIGATION_ID_FOR_GATEKEEPER_TEST =  3;
        
    //public final static String PERSISTENCE_UNIT = "icat3-scratch-testing-PU";
    //My One
    public final static String PERSISTENCE_UNIT = "icat3-unit-testing-PU";       
    //public final static String PERSISTENCE_UNIT = "icat3-apitest";
}
