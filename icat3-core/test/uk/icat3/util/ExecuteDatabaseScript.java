/*
 * ExecuteDatabaseScript.java
 *
 * Created on 05 March 2007, 16:33
 *
 * Class that will execute all statements in a database script (location
 * of script and statement terminator e.g. ';' are supplied as parameters
 * to execute method.
 *
 * Database Connection is established in constructor, allowing multiple 
 * scripts (via execute method) to be executed on the same pre-configured
 * datasource.
 *
 * @author Damian Flannery
 * @version 1.0
 */
package uk.icat3.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.apache.log4j.Logger;

public class ExecuteDatabaseScript {

    private String jdbcURL = "";
    private String username = "";
    private String password = "";
    private static Logger log = Logger.getLogger(BaseTestMethod.class);

    /** 
     * Creates a new instance of ExecuteDatabaseScript and sets up
     * database connection
     *
     * @param jdbcURL       jdbc connection string
     * @param username      username for database schema
     * @param password      password for user
     */
    public ExecuteDatabaseScript(String jdbcURL, String username, String password) {
        this.jdbcURL = jdbcURL;
        this.username = username;
        this.password = password;
    }

    /**
     * Executes each statement in an sql script specified by <code>location</code>.
     * Script is parsed into individual statements separated by <code>terminator</code>.
     * Each statement is then executed individually, if a statement fails control is 
     * passed on to the next statement until the entire script has been executed.
     *
     * @param location      location of script to be executed
     * @param terminator    character delimiter for each statement
     *     
     */
    public void execute(String location, String terminator) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;

        try {

            //read entire sql script into StringBuffer
            br = new BufferedReader(new InputStreamReader(new FileInputStream(location)));
            String line = "";
            while ((line = br.readLine()) != null) {
                String lineTrimmed = line.trim();
                if (!lineTrimmed.startsWith("--")) {
                    sb.append(line);
                }
            }//end while

            //split each statement as delimited by terminator
            String[] myCommands = sb.toString().split(terminator);

            //Create connection to database with details that were passed into constructor
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            Connection con = DriverManager.getConnection(jdbcURL, username, password);

            //try to execute each statement whether pass or fail
            for (int i = 0; i < myCommands.length; i++) {
                log.trace("-" + myCommands[i]);
                try {
                    PreparedStatement updateSales = con.prepareStatement(myCommands[i]);
                    updateSales.execute();
                } catch (Exception e) {
                    log.error("Failed to execute statement", e);
                } //end try/catch
            } //end for

        } catch (Exception e1) {
            log.fatal("Failed to read database script", e1);
        } //end try/catch        
    }
}
