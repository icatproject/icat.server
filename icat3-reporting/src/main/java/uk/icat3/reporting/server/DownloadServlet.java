/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.reporting.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.icat3.reporting.client.Constants;
import uk.icat3.reporting.client.ReportInfo;

/**
 *
 * @author scb24683
 */
public class DownloadServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processPDFRequest(String name, byte[] bytes, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ServletOutputStream stream = null;
            stream = response.getOutputStream();
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=" + name);
            response.setContentLength((int) bytes.length);
            stream.write(bytes);
            stream.close();
        } finally {
        }
    }

    protected void processXMLRequest(String name, String xml, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ServletOutputStream stream = null;
            stream = response.getOutputStream();
            response.setContentType("text/xml");
            response.addHeader("Content-Disposition", "attachment; filename=" + name);
            response.setContentLength((int) xml.length());
            stream.print(xml);
            stream.close();
        } finally {
        }
    }

    protected void processCSVRequest(String name, ByteArrayOutputStream baos, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ServletOutputStream stream = null;
            stream = response.getOutputStream();
            response.setContentType("text/csv");
            response.addHeader("Content-Disposition", "attachment; filename=" + name);
            response.setContentLength((int)baos.size());
            stream.write(baos.toByteArray());
            stream.close();
        } finally {
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ReportServiceImpl reportServ = new ReportServiceImpl();

        int reportId = Integer.parseInt(request.getParameter("reportId"));
        String format = request.getParameter("format");

        ReportInfo[] reports = Constants.createReportObjects();
        HashMap params = new HashMap();
        ReportInfo report = new ReportInfo();

        //find rest of report info from id
        for (int i = 0; i < reports.length; i++) {
            if (reportId == reports[i].getId()) {
                report = reports[i];
                break;
            }
        }

        //check if report has parameters. If so, retrieve them and add to the parameters HashMap
        if (report.getParameters() != null) {
            for (String name : report.getParameters()) {
                if ((name.equals("FROM")) || (name.equals("UNTIL")) || (name.equals("MONTH_FROM")) || (name.equals("MONTH_UNTIL"))) {
                    Timestamp date = Timestamp.valueOf(request.getParameter(name));
                    params.put(name, date);
                } else {
                    String value = request.getParameter(name);
                    params.put(name, value);
                }
            }
        }

        //Based on the file format given, generate the report in ReportServiceImpl
        if (format.equals(".pdf")) {
            byte[] file = reportServ.getPdfFile(report.getSource(), params);
            processPDFRequest(report.getTarget() + format, file, request, response);
        } else if (format.equals(".xml")) {
            String xml = reportServ.getXmlFile(report.getSource(), params);
            processXMLRequest(report.getTarget() + format, xml, request, response);
        } else if (format.equals(".csv")) {
            ByteArrayOutputStream baos = reportServ.getCsvFile(report.getSource(), params);
            processCSVRequest(report.getTarget() + format, baos, request, response);
        }
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
