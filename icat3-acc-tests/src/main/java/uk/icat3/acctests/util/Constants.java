/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.acctests.util;

/**
 *
 * @author df01
 */
public class Constants {
    
    
    public static String ISIS_GUARDIAN = "ISIS_GUARDIAN";
    public static String USER1 = "dwf64";
    public static String USER2 = "ks82";
    public static String USER3 = "lba63";
    public static String USER4 = "tgp98";
    public static String USER5 = "smk78";    
    public static String USER6 = "1927";    //armstrong
    public static String USER7 = "11260";    //Schmaljohann
    public static String USER8 = "6727";    //Bull DJ
    public static String USER9 = "10139";    //Chakhalian Jaques
    
    public static final String ICAT_ADMIN_USER = "ISIS-admin";
    public static final String ICAT_ADMIN_PASSWORD = "TaunWuOd5";
    
    public static final String ICAT_F_1_KEYWORD1 = "SXD";
    
    public static final String ICAT_F_2_KEYWORD1 = "structural";
    public static final String ICAT_F_2_KEYWORD2 = "electrochemical";
    public static final String ICAT_F_2_KEYWORD3 = "cycling";    
    public static final String ICAT_F_2_KEYWORD4 = "Shankland";
    public static final String ICAT_F_2_KEYWORD5 = "powder";
    public static final String ICAT_F_2_KEYWORD6 = "magnetic";
    public static final String ICAT_F_2_KEYWORD7 = "Furnace";
    public static final String ICAT_F_2_KEYWORD8 = "SXD";
    public static final String ICAT_F_2_KEYWORD9 = "Empty";
                    
    public static final double ICAT_F_3_RUN_START = 16000;
    public static final double ICAT_F_3_RUN_END = 16099;
    public static final String ICAT_F_3_INSTRUMENT = "SXD";
    public static final String ICAT_F_3_RUN_NUMBER = "RUN_NUMBER";
    public static final String ICAT_F_3_SAMPLE = "*Telephone*"; //GD 
    public static final int ICAT_F_3_YEAR_START = 1999;
    public static final int ICAT_F_3_YEAR_END = 1999; //GD
    public static final int ICAT_F_3_MONTH_START = 2;
    public static final int ICAT_F_3_MONTH_END = 2;
    public static final int ICAT_F_3_DAY_START = 1;
    public static final int ICAT_F_3_DAY_END = 28;
    public static final int ICAT_F_3_HOUR = 1;
    public static final int ICAT_F_3_MINUTE = 1;
    public static final int ICAT_F_3_SECOND = 1; //GD no data in icat between these dates
    public static final String ICAT_F_3_EXPERIMENT_NUMBER = "720568";
    public static final String ICAT_F_3_ABSTRACT_KEYWORD_1 = "*nano-actuators*";    //GD        
    public static final String ICAT_F_3_ABSTRACT_KEYWORD_2 = "*Li3N*";           //GD  
    public static final String ICAT_F_3_ABSTRACT_KEYWORD_3 = "*Deuterium*";      //GD       
    public static final String ICAT_F_3_INVESTIGATOR_1 = "Armstrong";            
    public static final String ICAT_F_3_INVESTIGATOR_2 = "Bruce";            
    public static final String ICAT_F_3_BCAT_INV_STR = "*Shankland*";       
    public static final String ICAT_F_3_DATAFILE_NAME = "GEM35639_STATUS.TXT";       
    public static final String ICAT_F_3_DATAFILE_NAME_2 = "SXD04465.RAW"; 
    
    /**
     * ICAT_S_1_DATAFILE_NAME refers to LOQ24803.RAW which is one of
     * the commercial loq data files as listed within file 
     * "LOQ Commercial Data - for ICAT.xls"
     */    
    public static final String ICAT_S_1_DATAFILE_NAME = "LOQ24803.RAW";               
    
    public static final String ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_1 = "720544";       
    public static final String ICAT_S_2_NON_PUBLIC_EXPERIMENT_NUMBER_2 = "720568";  
        
    public static final String ICAT_S_3_NON_PUBLIC_EXPERIMENT_NUMBER_1 = "720544"; //Schmaljohann D  
    
    /**
     * Please note that the investigation below is an existing 
     * non-public investigation of type experiment that I 
     * have manually changed to type commercial experiment using the following SQL
     * "update investigation set inv_type = 'commercial_experiment' where inv_number = '720207'"
     */    
    public static final String ICAT_S_3_COMMERCIAL_EXPERIMENT_NUMBER_1 = "720207"; //Chalhalian (USER9)
    
    
    public static final String ICAT_S_4_PUBLIC_EXPERIMENT_NUMBER_1 = "10268"; //Chalhalian (USER9)
    
    public static final double ICAT_P_1_MAX_TIME = 5000; //GD 1000ms = 1s
    public static final double ICAT_P_3_MAX_TIME = 10000;
    
    public static final String ICAT_P_3_INSTRUMENT = "OSIRIS";
    public static final double ICAT_P_3_START_RUN = 100;
    public static final double ICAT_P_3_END_RUN = 199;
    
    public static final String ICAT_DATA_URL = "http://data.nd.rl.ac.uk/icat/getfiles.py/run?sessionId=";        
    public static final String ICAT_DOWNLOAD_ZIP = "action=zip";       
    public static final String ICAT_DOWNLOAD_DATAFILE = "action=download";        
    
}
