/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.reporting.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import org.apache.log4j.Logger;

/**
 *
 * @author scb24683
 */
public class ReportServiceImpl extends RemoteServiceServlet {

    static Logger log;

    /**
     * Generate a byte[] for outputting the report in pdf
     * @param sourceFile the name of the jrxml file
     * @param params the report parameter names and values input by the user
     * @throws IOException if it fails to retrieve the properties file
     * @return byte[] for pdf
     */
    public byte[] getPdfFile(String sourceFile, HashMap params) throws IOException {
        log = Logger.getLogger(ReportServiceImpl.class);

        Properties props = new Properties();
        props.load(new FileInputStream("Images.properties"));

        try {
            //Get logos for report
            params.put("LOGO_LEFT", props.getProperty("logoLeft"));
            params.put("LOGO_RIGHT", props.getProperty("logoRight"));
            Connection con = createConnection(props);
            //Get folder jrxml is stored in and create full file path
            String source = props.getProperty("sourceFolder") + sourceFile;
            JasperReport jasperReport = JasperCompileManager.compileReport(
                    source);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, con);
            byte[] report = JasperExportManager.exportReportToPdf(jasperPrint);
            return report;
        } catch (JRException e) {
            log.fatal("Error outputing file to byte[]", e);
            return null;
        }
    }

    /**
     * Generate the report in xml
     * @param sourceFile the name of the jrxml file
     * @param params the report parameter names and values input by the user
     * @throws IOException if it fails to retrieve the properties file
     * @return String of xml
     */
    public String getXmlFile(String sourceFile, HashMap params) throws IOException {
        log = Logger.getLogger(ReportServiceImpl.class);
        Properties props = new Properties();
        props.load(new FileInputStream("Images.properties"));

        try {
            params.put("LOGO_LEFT", props.getProperty("logoLeft"));
            params.put("LOGO_RIGHT", props.getProperty("logoRight"));
            Connection con = createConnection(props);
            String source = props.getProperty("sourceFolder") + sourceFile;
            JasperReport jasperReport = JasperCompileManager.compileReport(
                    source);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, con);
            String xml = JasperExportManager.exportReportToXml(jasperPrint);
            return xml;
        } catch (JRException e) {
            log.fatal("Error in outputing xml file", e);
            return null;
        }
    }

    /**
     * Generate the report as csv file
     * @param sourceFile the name of the jrxml file
     * @param params the report parameter names and values input by the user
     * @throws IOException if it fails to retrieve the properties file
     * @return ByteArrayOutputStream for outputting the file as a csv
     */
    public ByteArrayOutputStream getCsvFile(String sourceFile, HashMap params) throws IOException {
        log = Logger.getLogger(ReportServiceImpl.class);
        Properties props = new Properties();
        props.load(new FileInputStream("Images.properties"));

        try {
            params.put("LOGO_LEFT", props.getProperty("logoLeft"));
            params.put("LOGO_RIGHT", props.getProperty("logoRight"));
            Connection con = createConnection(props);
            String source = props.getProperty("sourceFolder") + sourceFile;
            JasperReport jasperReport = JasperCompileManager.compileReport(
                    source);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, params, con);
            JRCsvExporter exporter = new JRCsvExporter();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
            exporter.exportReport();

            return baos;
        } catch (JRException e) {
            log.fatal("Error generating csv", e);
            return null;
        }
    }

    public Connection createConnection(Properties props) {
        try {
            String username = props.getProperty("dbUsername");
            String password = props.getProperty("dbPassword");
            String DATABASE_USER = "user";
            String DATABASE_PASSWORD = "password";
            String AUTO_RECONNECT = "autoReconnect";
            String MAX_RECONNECTS = "maxReconnects";


            String driver = "oracle.jdbc.driver.OracleDriver";
            // load the driver
            Class.forName(driver);
            String dbURL = props.getProperty("dbURL");

            Properties connProperties = new Properties();
            connProperties.put(DATABASE_USER, username);
            connProperties.put(DATABASE_PASSWORD, password);

            // set additional connection properties:
            // if connection stales, then make automatically
            // reconnect; make it alive again;
            // if connection stales, then try for reconnection;
            connProperties.put(AUTO_RECONNECT, "true");
            connProperties.put(MAX_RECONNECTS, "4");
            Connection conn = DriverManager.getConnection(dbURL, connProperties);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
