/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.reporting.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 *
 * @author scb24683
 */
public class DownloadHandler implements ClickHandler {

    

    @Override
    public void onClick(ClickEvent event) {
        Button button = (Button) event.getSource();
        String title = button.getTitle();

        FlexTable grid = (FlexTable) button.getParent();
        Cell cellForEvent = grid.getCellForEvent(event);
        int cellIndex = cellForEvent.getCellIndex() - 1;
        int row = cellForEvent.getRowIndex();
        ListBox formatList = (ListBox) grid.getWidget(row, cellIndex);
        int formatIndex = formatList.getSelectedIndex();
        String format = formatList.getItemText(formatIndex);
        final String fileExt = "." + format;

        ReportInfo reportToCreate = new ReportInfo();
        ReportInfo[] reports = Constants.createReportObjects();
        for (int i = 0; i < reports.length; i++) {
            if (title.equals(reports[i].getName())) {
                reportToCreate = reports[i];
                break;
            }
        }

        final int reportId = reportToCreate.getId();
        List<String> reportParams = reportToCreate.getParameters();

        //Report does not require any parameters, download now
        if (reportParams == null) {
            Window.open(GWT.getHostPageBaseURL() + "DownloadServlet?reportId=" + reportId + "&format=" + fileExt, "Download", "");
        } else {
            //Report requires parameters, create box to enter these
            final DialogBox dialog = new DialogBox();
            dialog.setGlassEnabled(true);
            dialog.setAnimationEnabled(true);
            FlexTable paramTable = createParameterTable(reportParams);
            dialog.add(paramTable);
            Button go = new Button("OK");

            //ClickHandler for retrieving parameters from page and downloading report
            go.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    Button button = (Button) event.getSource();
                    FlexTable table = (FlexTable) button.getParent();

                    int rows = table.getRowCount();
                    String params = "";

                    for (int i = 1; i < rows - 1; i++) {
                        Label name = (Label) table.getWidget(i, 0);
                        String paramName = name.getText();
                        String paramValue = "";
                        if (paramName.equals("FROM")) {
                            DateBox dateBox = (DateBox) table.getWidget(i, 1);
                            paramValue = new Timestamp(dateBox.getValue().getTime()).toString();
                        } else if (paramName.equals("UNTIL")) {
                            DateBox dateBox = (DateBox) table.getWidget(i, 1);
                            int dateNo = dateBox.getValue().getDate();
                            Date date1 = dateBox.getValue();
                            date1.setDate(dateNo + 1);
                            paramValue = new Timestamp(date1.getTime()).toString();
                        } else if ((paramName.equals("MONTH_FROM")) || (paramName.equals("MONTH_UNTIL"))) {
                            paramValue = getDateFromMonthSelect(i, table, paramName).toString();
                        } else {
                            TextBox value = (TextBox) table.getWidget(i, 1);
                            if (value.getText().equals("%")) {
                            paramValue = value.getText() + "25";
                            } else {
                                paramValue = value.getText();
                            }
                        }

                        //Create string of parameters for url
                        params += "&" + paramName + "=" + paramValue;
                    }
                    Window.open(GWT.getHostPageBaseURL() + "DownloadServlet?reportId=" + reportId + "&format=" + fileExt + params, "Download", "");
                    dialog.hide();
                }
            });

            Button cancel = new Button("Cancel");
            cancel.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    dialog.hide();
                }
            });

            dialog.center();
            paramTable.setWidget(reportParams.size() + 1, 0, go);
            paramTable.setWidget(reportParams.size() + 1, 1, cancel);
        }
    }

    //Creates the table to take parameters for report
    public FlexTable createParameterTable(List<String> reportParams) {
        Reporting props = GWT.create(Reporting.class);
        FlexTable table = new FlexTable();
        table.setWidget(0, 0, new HTML("<b>Parameters</b>"));

        for (int i = 0; i < reportParams.size(); i++) {
            table.setWidget(i + 1, 0, new Label(reportParams.get(i)));
            if ((reportParams.get(i).equals("FROM")) || (reportParams.get(i).equals("UNTIL"))) {
                DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd/MM/yyyy");
                DateBox dateBox = new DateBox();
                dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
                table.setWidget(i + 1, 1, dateBox);
            } else if ((reportParams.get(i).equals("MONTH_FROM")) || (reportParams.get(i).equals("MONTH_UNTIL"))) {

                ListBox monthBox = new ListBox();
                for (String m : Constants.months) {
                    monthBox.addItem(m);
                }
                table.setWidget(i + 1, 1, monthBox);
                ListBox yearBox = new ListBox();

                int[] years = new int[props.numberYears()];
                int currentYear = new Date().getYear() + 1900;
                years[0] = currentYear;
                for (int n = 1; n < props.numberYears(); n++) {
                    years[n] = currentYear - n;
                }

                for (int y : years) {
                    yearBox.addItem(String.valueOf(y));
                }
                table.setWidget(i + 1, 2, yearBox);

            } else {
                table.setWidget(i + 1, 1, new TextBox());
            }
        }
        return table;
    }

    //Generates a timestamp from the month and year input for report parameter
    public Timestamp getDateFromMonthSelect(int rowNumber, FlexTable table, String paramName) {
        ListBox monthBox = (ListBox) table.getWidget(rowNumber, 1);
        int month = monthBox.getSelectedIndex() + 1;
        ListBox yearBox = (ListBox) table.getWidget(rowNumber, 2);
        int yearNo = yearBox.getSelectedIndex();
        int year = Integer.parseInt(yearBox.getItemText(yearNo));

        DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd/MM/yyyy");
        Date parsedDate = null;
        if (paramName.equals("MONTH_FROM")) {
            parsedDate = dateFormat.parse("01/" + month + "/" + year);
        } else {
            parsedDate = dateFormat.parse("01/" + (month + 1) + "/" + year);
        }

        Timestamp timestamp = new Timestamp(parsedDate.getTime());
        return timestamp;
    }
}
