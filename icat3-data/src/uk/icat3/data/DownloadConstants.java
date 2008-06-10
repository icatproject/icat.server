/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.data;

import java.net.URL;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author gjd37
 */
public class DownloadConstants {

    static Logger log = Logger.getLogger(DownloadConstants.class);
    static Properties props;
    public static String HOST_NAME = "data.isis.rl.ac.uk";
    public static String CGI_NAME = "download";
    public static String DATAFILEID_NAME = "datafileId";
    public static String DATASETID_NAME = "datasetId";
    public static String SESSIONID_NAME = "sessionId";
    public static String DOWNLOAD_SCHEME = "http";

    public static enum ACTION {
        ZIP, DOWNLOAD, ZIPCOMP, TAR ;
        
        @Override
        public String toString(){
            //action strings need to be lower case
            return super.toString().toLowerCase();
        }
    };

    //read in config 
    static {  
        try {            
            URL url = DownloadConstants.class.getResource("/download.properties");
            if (url != null) {
                props = new Properties();
                props.load(url.openStream());

                //read in all props
                HOST_NAME = props.getProperty("host.name", HOST_NAME);
                CGI_NAME = props.getProperty("cgi.name", CGI_NAME);
                DATAFILEID_NAME = props.getProperty("datafile.id", DATAFILEID_NAME);
                DATASETID_NAME = props.getProperty("dataset.id", DATASETID_NAME);
                SESSIONID_NAME = props.getProperty("session.id", SESSIONID_NAME);
                DOWNLOAD_SCHEME = props.getProperty("download.scheme", DOWNLOAD_SCHEME);
            } else {
                log.info("could not locate resource: download.properties");
            }
        } catch (Exception ex) {           
            log.error("Unable to read download.properties", ex);            
        }
    }
    
    //Test
    public static void main(String[] args){
        System.out.println(HOST_NAME);
    }
}

