/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.xmlingest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.ws.BindingProvider;

/**
 *
 * @author df01
 */
public class ICATXMLIngestClient {


    public static void printHelp() {
        System.out.println();
        System.out.println("Usage: java -jar ICATIngestClient.jar [username] [filename]");
        System.out.println("e.g.   java -jar ICATIngestClient.jar /tmp/ASTRA00001.XML");
        System.out.println("e.g.   java -jar ICATIngestClient.jar C:/tmp/MAP00001.XML");
        System.out.println("Instructions for changing properties file...");
    }

    public static String readXMLFile(String _filename) {
        String _buffer = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_filename)));
            String line = "";
            while((line = br.readLine()) != null) {
                _buffer += line;
            }//end while
        } catch (Exception e) {
            System.err.println("[ERROR] - Supplied filename does not exist or cannot be read");
            printHelp();
            System.exit(-1);
        }//end try/catch

        return _buffer;
    }

    public static Properties readConfigFile() {
        ResourceBundle bundle = null;
        Properties properties = null;
        InputStream is = null;
        boolean usernameVerified = false;
        boolean passwordVerified = false;
        boolean icatVerified = false;
        boolean icatAdminVerified = false;

        try {            
            bundle = new PropertyResourceBundle(ICATXMLIngestClient.class.getResourceAsStream("/icatclient.properties"));
            properties = new Properties();
            Enumeration keys = bundle.getKeys();
            System.out.println("keys.size" + bundle.toString());

            while(keys.hasMoreElements( ) ) {
                String prop = (String)keys.nextElement( );
                String val = bundle.getString(prop);

                //check to make sure that all required keys and (non null) values are present
                if ((prop != null) && (prop.equals("username"))) { if ((val != null) && (val.length() >0)) usernameVerified = true;  }
                if ((prop != null) && (prop.equals("password"))) { if ((val != null) && (val.length() >0)) passwordVerified = true;  }
                if ((prop != null) && (prop.equals("icat_endpoint"))) { if ((val != null) && (val.length() >0)) icatVerified = true; }
                if ((prop != null) && (prop.equals("icatadmin_endpoint"))) { if ((val != null) && (val.length() >0)) icatAdminVerified = true; }

                properties.setProperty(prop, val);
            }//end while

            //check that each key is accounted for
            if (!usernameVerified) throw new Exception("Please check icatclient.properties to ensure that username key is supplied e.g. 'username=icatAdmin'");
            if (!passwordVerified) throw new Exception("Please check icatclient.properties to ensure that password key is supplied e.g. 'password=rumpelstiltskin'");
            if (!icatVerified) throw new Exception("Please check icatclient.properties to ensure that icat_endpoint key is supplied e.g. 'icat_endpoint=https://facilities01.esc.rl.ac.uk/ICATService/ICAT?wsdl'");
            if (!icatAdminVerified) throw new Exception("Please check icatclient.properties to ensure that icatadmin_endpoint key is supplied e.g. 'icatadmin_endpoint=https://facilities01.esc.rl.ac.uk/ICATAdminService/ICATAdmin?wsdl'");

        } catch (Exception io) {
            io.printStackTrace();
            System.exit(-1);
        }//end try/catch

        return properties;
    }

    public static void main(String[] args) {

        //Check that correct number of arguments are supplied
        if ((args == null) || (args.length <2)) {
            System.err.println("[ERROR] - Incorrect number of arguments supplied");
            printHelp();
            System.exit(-1);
        }//end if

        //read username
        String username = args[0];

        //Check that user supplied xml file exists and can be read
        String buffer = readXMLFile(args[1]);

        //Check that configuration file is present and correct
        Properties properties = readConfigFile();

        
        System.out.println("[runas] " + username);
        System.out.println("[xml] " + buffer);
        System.out.println("[username] " + properties.getProperty("username"));
        System.out.println("[password] " + properties.getProperty("password"));
        System.out.println("[icatadmin_endpoint] " + properties.getProperty("icatadmin_endpoint"));
        System.out.println("[icat_endpoint] " + properties.getProperty("icat_endpoint"));


        //uk.icat3.client.ICATAdminISISService icatAdminService = null;
        uk.icat3.client.admin.ICATAdmin icatAdminPort = null;
        //uk.icat3.client.ICATISISService icatService = null;
        uk.icat3.client.ICAT icatPort = null;

        try {

            // Call Web Service Operation
            icatAdminPort = new uk.icat3.client.admin.ICATAdminService().getICATAdminPort();

            ((BindingProvider)icatAdminPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, properties.getProperty("icatadmin_endpoint"));
            ((BindingProvider)icatAdminPort).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, properties.getProperty("username"));
            ((BindingProvider)icatAdminPort).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, properties.getProperty("password"));            


            icatPort = new uk.icat3.client.ICATService().getICATPort();
            ((BindingProvider)icatPort).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, properties.getProperty("icat_endpoint"));

            System.out.println("Logging in...");
            java.lang.String sessionId = icatAdminPort.loginAdmin(username);
            //java.lang.String sessionId = icatPort.login("dwf64", "sadsadas");

            System.out.println("SessionId = " + sessionId);

            //ingest here

            System.out.println("Logging out...");
            icatPort.logout(sessionId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
