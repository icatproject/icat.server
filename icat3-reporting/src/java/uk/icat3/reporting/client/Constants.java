/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.reporting.client;

import com.google.gwt.core.client.GWT;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scb24683
 */
public class Constants {

    public static final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    //File format
    public static final String jrxmlExt = ".jrxml";
    //Report locations
    //Login reports
    public static final String LOGIN_DAY_CHART = "LoginBarChartDay";
    public static final String LOGIN_MONTH_CHART = "LoginBarChartMonth";
    public static final String LOGIN_DAY_REPORT = "LoginsDay";
    public static final String LOGIN_MONTH_REPORT = "LoginsMonth";
    //Names
    public static final String LOGIN_DAY_CHART_NAME = "Logins Per Day Chart";
    public static final String LOGIN_MONTH_CHART_NAME = "Logins Per Month Chart";
    public static final String LOGIN_DAY_REPORT_NAME = "Logins Per Day";
    public static final String LOGIN_MONTH_REPORT_NAME = "Logins Per Month";
    //Download reports
    public static final String USER_DOWNLOADS = "DownloadsForUser";
    public static final String ALL_DOWNLOADS = "Downloads";
    public static final String DOWNLOADS_BY_AGE = "DownloadsByAge";
    public static final String DOWNLOADS_BY_SIZE = "DownloadsBySize";
    public static final String DOWNLOADS_FOR_INSTRUMENT = "DownloadsForInstrument";
    //Names
    public static final String USER_DOWNLOADS_NAME = "Downloads For User";
    public static final String ALL_DOWNLOADS_NAME = "All Downloads";
    public static final String DOWNLOADS_BY_AGE_NAME = "Downloads By File Age Chart";
    public static final String DOWNLOADS_BY_SIZE_NAME = "Downloads By File Size Chart";
    public static final String DOWNLOADS_FOR_INSTRUMENT_NAME = "Downloads For Instrument";
    //Search reports
    public static final String SEARCH_TYPES = "Searches";
    public static final String KEYWORDS = "Keywords";
    //Names
    public static final String SEARCH_TYPES_NAME = "Searches By Type";
    public static final String KEYWORDS_NAME = "Keywords";
    //View reports
    public static final String FILE_VIEWS_BY_INSTRUMENT = "DatafileViewsByInstrument";
    public static final String SET_VIEWS_BY_INSTRUMENT = "DatasetViewsByInstrument";
    public static final String INVESTIGATION_VIEWS_BY_INSTRUMENT = "InvestigationViewsByInstrument";
    //Names
    public static final String FILE_VIEWS_BY_INSTRUMENT_NAME = "Datafile Views By Instrument";
    public static final String SET_VIEWS_BY_INSTRUMENT_NAME = "Dataset Views By Instrument";
    public static final String INVESTIGATION_VIEWS_BY_INSTRUMENT_NAME = "Investigation Views By Instrument";
    //Available formats
    public static final String PDF = "pdf";
    public static final String XML = "xml";
    public static final String CSV = "csv";

    public static ReportInfo[] createReportObjects() {
        ReportInfo[] reports = new ReportInfo[14];
        List<String> fromUntil = new ArrayList<String>();
        fromUntil.add("FROM");
        fromUntil.add("UNTIL");
        List<String> user = new ArrayList<String>();
        user.add("USERID");
        user.addAll(fromUntil);
        List<String> instrument = new ArrayList<String>();
        instrument.add("INSTRUMENT");
        instrument.addAll(fromUntil);
        List<String> monthList = new ArrayList<String>();
        monthList.add("MONTH_FROM");
        monthList.add("MONTH_UNTIL");
        reports[0] = new ReportInfo(1, LOGIN_DAY_CHART_NAME, "Bar chart of logins per days", LOGIN_DAY_CHART + jrxmlExt, LOGIN_DAY_CHART, fromUntil);
        reports[1] = new ReportInfo(2, LOGIN_MONTH_CHART_NAME, "Bar chart of logins per month", LOGIN_MONTH_CHART + jrxmlExt, LOGIN_MONTH_CHART, monthList);
        reports[2] = new ReportInfo(3, LOGIN_DAY_REPORT_NAME, "Table & chart of all logins per day", LOGIN_DAY_REPORT + jrxmlExt, LOGIN_DAY_REPORT, fromUntil);
        reports[3] = new ReportInfo(4, LOGIN_MONTH_REPORT_NAME, "Table & chart of all logins per months", LOGIN_MONTH_REPORT + jrxmlExt, LOGIN_MONTH_REPORT, monthList);
        reports[4] = new ReportInfo(5, USER_DOWNLOADS_NAME, "All downloads by specified user, grouped by instrument", USER_DOWNLOADS + jrxmlExt, USER_DOWNLOADS, user);
        reports[5] = new ReportInfo(6, ALL_DOWNLOADS_NAME, "Table of all downloads made, ordered by date of download", ALL_DOWNLOADS + jrxmlExt, ALL_DOWNLOADS, fromUntil);
        reports[6] = new ReportInfo(7, DOWNLOADS_BY_AGE_NAME, "Bar chart of all downloads made, grouped by age (days)", DOWNLOADS_BY_AGE + jrxmlExt, DOWNLOADS_BY_AGE, fromUntil);
        reports[7] = new ReportInfo(8, DOWNLOADS_BY_SIZE_NAME, "Table of total size of downloads per month, and chart of number of downloads of each file size", DOWNLOADS_BY_SIZE + jrxmlExt, DOWNLOADS_BY_SIZE, monthList);
        reports[8] = new ReportInfo(9, DOWNLOADS_FOR_INSTRUMENT_NAME, "Table & chart of all files downloaded for the given instrument, grouped by month", DOWNLOADS_FOR_INSTRUMENT + jrxmlExt, DOWNLOADS_FOR_INSTRUMENT, instrument);
        reports[9] = new ReportInfo(10, SEARCH_TYPES_NAME, "Pie chart of types of search carried out", SEARCH_TYPES + jrxmlExt, SEARCH_TYPES, null);
        reports[10] = new ReportInfo(11, KEYWORDS_NAME, "List of all keywords used and number of usages", KEYWORDS + jrxmlExt, KEYWORDS, null);
        reports[11] = new ReportInfo(12, FILE_VIEWS_BY_INSTRUMENT_NAME, "Table & chart of all datafiles viewed, grouped by instrument", FILE_VIEWS_BY_INSTRUMENT + jrxmlExt, FILE_VIEWS_BY_INSTRUMENT, fromUntil);
        reports[12] = new ReportInfo(13, SET_VIEWS_BY_INSTRUMENT_NAME, "Table & chart of all datasets viewed, grouped by instrument", SET_VIEWS_BY_INSTRUMENT + jrxmlExt, SET_VIEWS_BY_INSTRUMENT, fromUntil);
        reports[13] = new ReportInfo(14, INVESTIGATION_VIEWS_BY_INSTRUMENT_NAME, "Table & chart of all investigations viewed, grouped by instrument", INVESTIGATION_VIEWS_BY_INSTRUMENT + jrxmlExt, INVESTIGATION_VIEWS_BY_INSTRUMENT, fromUntil);

        return reports;
    }
}
