/*
 * SearchKeyword.java
 *
 * Created on 15-Aug-2007, 12:57:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package icat3wstest;

import uk.icat3.client.*;
import java.util.ArrayList;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import static icat3wstest.Constants.*;

/**
 *
 * @author gjd37
 */
public class InvestigationSearch {

    /** Creates a new instance of SearchKeyword */
    public static void searchMyInvestigations(String sid) throws Exception {

        try {
            long time = System.currentTimeMillis();

            // TODO process result here
            java.util.List<uk.icat3.client.Investigation> result = ICATSingleton.getInstance().getMyInvestigationsIncludes(sid,
                    InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES); //get my investigations, default limit to 500, include no other info
            //java.util.List<client.Investigation> result =
            //      ICATSingleton.getInstance().getMyInvestigationsIncludesPagination(sid, InvestigationInclude.ALL, 3, 10);
            // Find my investigations, include all info, bring back 3 to 10

            float totalTime = (System.currentTimeMillis() - time) / 1000f;

            System.out.println("Number of MyInvestigations is " + result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: " + investigation.getId() + ", TITLE: " + investigation.getTitle() + " FACILITY: " + investigation.getFacility());
            }
            System.out.println("\nTime taken: " + totalTime + " seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for MyInvestigations with SID " + sid);
            System.out.println(ex);
            assert false;

        // TODO handle custom exceptions here
        }
    }

    public static void searchAdvanced(String sid, AdvancedSearchDetails asd) throws Exception {

        try {
            long time = System.currentTimeMillis();

            // TODO process result here
            java.util.List<uk.icat3.client.Investigation> result =
                    ICATSingleton.getInstance().searchByAdvancedPagination(sid, asd, 0, 440); //bring back first 440 results

            float totalTime = (System.currentTimeMillis() - time) / 1000f;

            System.out.println("Number of Advanced is " + result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: " + investigation.getId() + ", TITLE: " + investigation.getTitle());
            }
            System.out.println("\nTime taken: " + totalTime + " seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for Advanced with SID " + sid);
            System.out.println(ex);
            assert false;
        // TODO handle custom exceptions here
        }
    }

    /** Creates a new instance of SearchKeyword */
    public static void searchKeyword(String sid, String keyword) throws Exception {

        try {
            long time = System.currentTimeMillis();

            java.util.List<java.lang.String> keywords = new ArrayList<String>();
            keywords.add(keyword);

            // TODO process result here
            java.util.List<uk.icat3.client.Investigation> result = ICATSingleton.getInstance().searchByKeywords(sid, keywords);
            //java.util.List<client.Investigation> result =
            //      ICATSingleton.getInstance().searchByKeywordsPaginationFuzzyAndInclude(sid, keywords,
            //    InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES, true, 0, 20);
            // Search by keywords, include only investigation info (ie, investigators, keywords etc not datasets/datafiles)
            // using LIKE operator on keywords, bring back first 20 results

            float totalTime = (System.currentTimeMillis() - time) / 1000f;

            System.out.println("Number of investigations with " + keyword + " as a keyword is " + result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: " + investigation.getId() + ", TITLE: " + investigation.getTitle());
            }
            System.out.println("\nTime taken: " + totalTime + " seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for keyword: " + keyword + " with SID " + sid);
            System.out.println(ex);
            assert false;
        // TODO handle custom exceptions here
        }
    }

    public static void searchKeywordAll(String sid, String keyword) throws Exception {

        try {
            long time = System.currentTimeMillis();

            KeywordDetails details = new KeywordDetails();

            details.getKeywords().add(keyword);
            
            //details.setCaseSensitve(false); //false default
            //details.setFuzzy(true); //false default
            //details.setInvestigationIncludes(InvestigationInclude.NONE); // NONE default
            //details.setOperator(LogicalOperator.AND); //AND default

            // TODO process result here
            java.util.List<uk.icat3.client.Investigation> result = ICATSingleton.getInstance().searchByKeywordsAll(sid, details, 0, 200);

            float totalTime = (System.currentTimeMillis() - time) / 1000f;

            System.out.println("Number of investigations with " + keyword + " as a keywordAll is " + result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
             //   System.out.println("  ID: " + investigation.getId() + ", TITLE: " + investigation.getTitle());
            }
            System.out.println("\nTime taken: " + totalTime + " seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for keywordAll: " + keyword + " with SID " + sid);
            System.out.println(ex);
            assert false;
        // TODO handle custom exceptions here
        }
    }

    /** Creates a new instance of SearchKeyword */
    public static void searchUserId(String sid, String userId) throws Exception {

        try {
            long time = System.currentTimeMillis();

            // TODO process result here
            java.util.List<uk.icat3.client.Investigation> result = ICATSingleton.getInstance().searchByUserID(sid, userId);

            float totalTime = (System.currentTimeMillis() - time) / 1000f;

            System.out.println("Number of investigations with " + userId + " as a federalId is " + result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: " + investigation.getId() + ", TITLE: " + investigation.getTitle());
            }
            System.out.println("\nTime taken: " + totalTime + " seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for federalId: " + userId + " with SID " + sid);
            System.out.println(ex);
            assert false;
        // TODO handle custom exceptions here
        }

    }

    /** Creates a new instance of SearchKeyword */
    public static void searchSurname(String sid, String surname) throws Exception {

        try {
            long time = System.currentTimeMillis();

            // TODO process result here
            java.util.List<uk.icat3.client.Investigation> result = ICATSingleton.getInstance().searchByUserSurname(sid, surname);

            float totalTime = (System.currentTimeMillis() - time) / 1000f;

            System.out.println("Number of investigations with " + surname + " as a surname is " + result.size());
            System.out.println("Results:");
            for (Investigation investigation : result) {
                System.out.println("  ID: " + investigation.getId() + ", TITLE: " + investigation.getTitle());
            }
            System.out.println("\nTime taken: " + totalTime + " seconds");
            System.out.println("--------------------------------------------------\n");
            assert true;
        } catch (Exception ex) {
            System.out.println("Unable to search for surname: " + surname + " with SID " + sid);
            System.out.println(ex);
            assert false;
        // TODO handle custom exceptions here
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        // searchKeyword(SID, "hrpd");
        ICATSingleton.getInstance();
        
        searchKeywordAll(SID, "hrpd");
        searchKeywordAll(SID, "hrpd");

        // searchSurname(SID, "in");
        // searchUserId(SID, "gjd37");
        //searchMyInvestigations(SID);
        AdvancedSearchDetails asd = new AdvancedSearchDetails();
        // asd.getKeywords().add("calibration");
        // asd.getInvestigators().add(SURNAME);
        //asd.setInvestigationName("SrF2 calibration  w=-25.3d");
        //asd.getInstruments().add("sxd");
        // asd.setRunStart(8374d);
        //asd.setRunEnd(8400d);
        XMLGregorianCalendar xmlCal = null;
        XMLGregorianCalendar xmlCalEnd = null;
        try {
            xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            xmlCalEnd = DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } catch (Exception e) {
        }

        xmlCal.setDay(20);
        xmlCal.setMonth(11);
        xmlCal.setYear(2007);

        xmlCalEnd.setDay(21);
        xmlCalEnd.setMonth(11);
        xmlCalEnd.setYear(2007);

        asd.setDateRangeStart(xmlCal);
        asd.setDateRangeEnd(xmlCalEnd);

    //InvestigationSearch.searchAdvanced(SID, asd);
    }
}
