/*
 * Constants.java
 *
 * Created on 15-Aug-2007, 12:58:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package icat3wstest;

/**
 *
 * @author gjd37
 */
public class Constants {
        
    // CLF
   //public static String END_POINT_ADDRESS =  "https://facilities01.esc.rl.ac.uk:8182/ICATCLFService/ICAT?wsdl";
    //public static String END_POINT_ADMIN_ADDRESS = "https://facilities01.esc.rl.ac.uk:8182/ICATAdminCLFService/ICATAdmin?wsdl";
   
    
    //my machine
    //public static String END_POINT_ADMIN_ADDRESS = "https://escvig6.dc.dl.ac.uk:8181/ICATAdminService/ICATAdmin";
    public static String END_POINT_ADDRESS = "https://localhost:8181/ICATService/ICAT";
    
    // volga
    public static String END_POINT_ADMIN_ADDRESS = "https://volga.dl.ac.uk:8181/ICATAdminService/ICATAdmin";
   // public static String END_POINT_ADDRESS = "https://volga.dl.ac.uk:8181/ICATService/ICAT";
    
 //  public static String END_POINT_ADMIN_ADDRESS = "https://volga.dl.ac.uk:9181/ICATAdminService/ICATAdmin";
  // public static String END_POINT_ADDRESS = "https://volga.dl.ac.uk:9181/ICATService/ICAT";
    
    /// ISIS NEW
    // public static String END_POINT_ADDRESS = "https://isras123.nd.rl.ac.uk:8181/ICATService/ICAT?wsdl";
    //public static String END_POINT_ADMIN_ADDRESS = "https://facilities01.esc.rl.ac.uk:8182/ICATAdminISISService/ICATAdmin?wsdl";
   
   // ISIS
   // public static String END_POINT_ADDRESS = "https://facilities01.esc.rl.ac.uk:8182/ICATISISService/ICAT?wsdl";
    //public static String END_POINT_ADMIN_ADDRESS = "https://facilities01.esc.rl.ac.uk:8182/ICATAdminISISService/ICATAdmin?wsdl";
    
    //public static String SID = "64112098-db95-4b31-a0f9-efceb7c94132"; //isis
    public static String SID = "67a0348f-efd2-49ed-b9a7-67fbefa89845"; //clf
    
    public static String KEYWORD = "calibration";
    public static String USER_ID = "gjd37";
    public static String SURNAME = "Drinkwater";
    public static String INSTRUMENT = "SXD";
    
    public static String PARAMETER_NAME = "finish_date";
    public static String PARAMETER_UNITS = "yyyy-MM-dd HH:mm:ss";
    
    public static String DATAFILE_FORMAT = "nexus";
    
    public static String INVESTIGATOR = "test_creator_investigation_facility_user";
    
    public static Long INVESTIGATION_ID = 2L;
    public static Long DATASET_ID = 2L;
    public static Long DATAFILE_ID = 2L;
    public static Long SAMPLE_ID = 2L;
    
    
}
