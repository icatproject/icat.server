/*
 * DownloadServlet.java
 *
 * Created on 22 October 2007, 14:56
 */
package uk.icat3.download;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.PropertyConfigurator;
import uk.icat3.exceptions.SessionException;

/**
 *
 * @author gjd37
 * @version
 */
public class DownloadServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(DownloadServlet.class);
    private static String FACILITY;
    private static String PATH;
    private static Concurrent concurrent = new Concurrent();

    // @Override

    public void init() {
        //load log4j configuration
        //load resource bundle
        ResourceBundle facilityResources = ResourceBundle.getBundle("uk.icat3.download.facility");

        String facilityLogFile = null;
        try {
            facilityLogFile = facilityResources.getString("facility.name");
        } catch (Exception mre) {
            facilityLogFile = "ISIS";
        }

        FACILITY = facilityLogFile;

        //first section, reload log4j
        if (new File(System.getProperty("user.home") + File.separator + "." + facilityLogFile + "-icatapi.xml").exists()) {
            PropertyConfigurator.configure(System.getProperty("user.home") + File.separator + "." + facilityLogFile + "-icatapi.xml");
        //log.info("Loading "+System.getProperty("user.home")+File.separator+"log4j.xml");
        } else {
            PropertyConfigurator.configure(System.getProperty("user.home") + File.separator + "." + facilityLogFile + "-icatapi.properties");
        //log.info("Loading "+System.getProperty("user.home")+File.separator+"log4j.properties");
        }

        PATH = System.getProperty("java.io.tmpdir") + File.separator + "srbapi_downloads" + File.separator + FACILITY;

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                
        log.trace("DownloadServlet");

        concurrent.add();
        
        long time = System.currentTimeMillis();
        float totalTime = 0;
        ServletOutputStream out = response.getOutputStream();
        File downloadFile = null; // file to download
        String user = null;

        ////  Get all the information regarding the download ////

        String sid = request.getParameter("sid"); //get sessionId

        if (sid == null) {
            log.warn("Bad request from " + request.getRemoteAddr() + ": no sid parameter given");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return; //error, no session
        } else {
            //check if session valid
            try {
                user = SessionDelegate.getInstance().getUserFromSessionId(sid);
            } catch (SessionException sessionException) {
                log.error("Someone trying to download a file with invalid sessionId " + sid + ", IP Addess: " + request.getRemoteAddr());
                //session invalid
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        String name = request.getParameter("name"); //get name of download
        if (name == null) {
            log.warn("Bad request from " + request.getRemoteAddr() + ": no name parameter given");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String file = request.getParameter("file");  //get file
        if (file == null) {
            log.warn("Bad request from " + request.getRemoteAddr() + ": no file parameter given");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return; //error, no file
        } else {
            Date today = new Date();
            downloadFile = new File(PATH + File.separator + today.getDate() + "-" + (today.getMonth() + 1) + File.separator + file);
            if (!downloadFile.exists()) {
                log.error("FileNotFound from " + request.getRemoteAddr() + ": File not found " + downloadFile.getAbsolutePath());
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        log.trace("Downloading " + downloadFile.getAbsolutePath() + " for user " + user);
        ////  End of getting all the info, download ok, start to download ////

        totalTime = (System.currentTimeMillis() - time) / 1000f;
        log.trace("Time taken to get all data: " + totalTime + " seconds");

        //downloaded, set input out streams

        FileInputStream filein = null;
        DataInputStream datain = null;
        try {
            String contenttype = getContentType(downloadFile.getAbsolutePath());
            response.setContentType(contenttype);
            response.setContentLength((int) downloadFile.length());
            // response.setBufferSize((10 * 1024));
            response.setHeader("Content-disposition", "attachment; filename=" + name.replaceAll(" ", "_") + ""); //replace all spaces

            //download
            int length = 0;
            byte[] bbuf = new byte[4 * 1024];
            filein = new FileInputStream(downloadFile);
            datain = new DataInputStream(filein);

            while ((datain != null) && ((length = datain.read(bbuf)) != -1)) {
                out.write(bbuf, 0, length);
            }
            log.debug("CONCURRENT " + concurrent.get() + " : MAX: " + concurrent.getMax());

            filein.close();
            filein = null;

            datain.close();
            datain = null;

            out.flush();
            out.close();

        } catch (Throwable ex) {
            log.warn("Error downloading " + ex);
            filein.close();
            filein = null;

            datain.close();
            datain = null;

            out.flush();
            out.close();
        } finally {

            concurrent.minus();
            totalTime = (System.currentTimeMillis() - time) / 1000f;
            log.trace("Time taken to download: " + totalTime + " seconds");
            log.debug("deleting " + downloadFile.getAbsolutePath() + "?:" + downloadFile.delete());
            if (!downloadFile.getAbsolutePath().endsWith(".zip")) {
                //remove parent
                log.debug("deleting " + downloadFile.getAbsoluteFile().getParentFile().getAbsolutePath() + "?:" + downloadFile.getParentFile().delete());
            }
        }
        /*
    concurrent.add();
        
        File f = new File(System.getProperty("java.io.tmpdir")+File.separator+"large");
        int length  = 0;
        ServletOutputStream op  = response.getOutputStream();
        ServletContext  context  = getServletConfig().getServletContext();
        String  mimetype = context.getMimeType( f.getName() );
               
        response.setContentType( (mimetype != null) ? mimetype : "application/octet-stream" );
        response.setContentLength( (int)f.length() );
        response.setHeader( "Content-Disposition", "attachment; filename=\"" + f.getName() + "\"" );
        
        byte[] bbuf = new byte[1024];
        FileInputStream fil  =new FileInputStream(f);
        DataInputStream in = new DataInputStream(fil);
        System.out.println("opened buffer");
        while ((in != null) && ((length = in.read(bbuf)) != -1))
        {          
            op.write(bbuf,0,length);           
        }

       log.trace("Concurrent "+concurrent.get()+" : MAX: "+concurrent.getMax());          
         
        fil.close();
        in.close();
        op.flush();
        op.close();        
                 
        concurrent.minus();
        */
    }

    /**
     * Uses the file extension to determine the type of file.
     *
     * @param filename  name of the file whose type to determine
     * @return          the content type of the given file
     */
    public String getContentType(String filename) {
        int idx = filename.lastIndexOf(".");
        if (idx == -1) {
            return new String("application/octet-stream");
        }

        String ext = filename.substring(idx + 1, filename.length());

        if (ext.equals("ps")) {
            return new String("application/postscript");
        } else if (ext.equalsIgnoreCase("pdf")) {
            return new String("application/pdf");
        } else if (ext.equalsIgnoreCase("zip")) {
            return new String("application/zip");
        } else if (ext.equalsIgnoreCase("jpg")) {
            return new String("image/jpeg");
        } else if (ext.equalsIgnoreCase("jpeg")) {
            return new String("image/jpeg");
        } else if (ext.equalsIgnoreCase("gif")) {
            return new String("image/gif");
        } else if (ext.equalsIgnoreCase("tif")) {
            return new String("image/tiff");
        } else if (ext.equalsIgnoreCase("mpg")) {
            return new String("video/mpeg");
        } else if (ext.equalsIgnoreCase("mov")) {
            return new String("video/quicktime");
        } else if (ext.equalsIgnoreCase("htm")) {
            return new String("text/html");
        } else if (ext.equalsIgnoreCase("html")) {
            return new String("text/html");
        } else if (ext.equalsIgnoreCase("xml")) {
            return new String("text/xml");
        } else if (ext.equalsIgnoreCase("doc")) {
            return new String("application/msword");
        } else if (ext.equalsIgnoreCase("txt") || ext.equalsIgnoreCase("out") || ext.equalsIgnoreCase("err")) {
            return new String("text/plain");
        }

        return new String("application/octet-stream");
    }

    /**
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Download icat data";
    }
    // </editor-fold>

}
