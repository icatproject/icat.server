/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.reporting.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Main entry point.
 *
 * @author scb24683
 */
public class reportsEntryPoint implements EntryPoint {

    /**
     * Creates a new instance of reportsEntryPoint
     */
    public reportsEntryPoint() {
    }

    public ListBox createListBox() {
        final ListBox dropDown = new ListBox(false);
        dropDown.setVisibleItemCount(1);
        dropDown.addItem(Constants.PDF);
        dropDown.addItem(Constants.XML);
        return dropDown;
    }

    public FlexTable createReportTable() {
        FlexTable table = new FlexTable();
        table.setBorderWidth(2);
        table.setWidget(0, 0, new HTML("<font size='3'><b>Report Name</b></font>"));
        table.setWidget(0, 1, new HTML("<font size='3'><b>Description</b></font>"));
        table.setWidget(0, 2, new HTML("<font size='3'><b>Format</b></font>"));
        table.setWidget(0, 3, new HTML("<font size='3'><b>Download</b></font>"));
        final ReportInfo[] reports = Constants.createReportObjects();

        //Add report info to the table
        for (int i = 0; i < reports.length; i++) {
            Label label = new Label(reports[i].getName());
            table.setWidget(i + 1, 0, label);
            Label desc = new Label(reports[i].getDescription());
            table.setWidget(i + 1, 1, desc);

            ListBox formats = createListBox();
            //Not all reports are output as csv
            if (((i + 1) != 1) && ((i + 1) != 2) && ((i + 1) != 7)) {
                formats.addItem(Constants.CSV);
            }
            table.setWidget(i + 1, 2, formats);

            Button download = new Button("Download");
            download.setTitle(reports[i].getName());
            download.addClickHandler(new DownloadHandler());
            table.setWidget(i + 1, 3, download);
        }

        //Table style
        RowFormatter rowFormat = table.getRowFormatter();
        rowFormat.setStylePrimaryName(0, "white");
        for (int i = 0; i < reports.length;) {
            i = i + 2;
            rowFormat.setStylePrimaryName(i, "white");
        }
        rowFormat.setStylePrimaryName(1, "grey");
        for (int i = 1; i < reports.length - 1;) {
            i = i + 2;
            rowFormat.setStylePrimaryName(i, "grey");
        }

        return table;
    }

    /**
     * The entry point method, called automatically by loading a module
     * that declares an implementing class as an entry-point
     */
    public void onModuleLoad() {

        RootPanel titlePanel = RootPanel.get("TitlePanel");
        Label label = new HTML("<b><font size='5'>ICAT Usage Reports</font></b>");
        titlePanel.add(label);
        RootPanel root = RootPanel.get("MainPanel");

        root.setTitle("Root Panel");
        FlexTable table = createReportTable();
        root.add(table);

    }
}
