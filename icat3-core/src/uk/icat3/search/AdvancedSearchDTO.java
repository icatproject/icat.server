/*
 * AdvancedSearchDTO.java
 *
 * Created on 21 February 2007, 16:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author gjd37
 */
public class AdvancedSearchDTO {
    
    private String experimentTitle;
    private String investigationName;
    private String investigators;
    private String experimentNumber;
    private String[] instruments;
    private Long runStart;
    private Long runEnd;
 //   ParameterSearchTerm[] parameterSearchTerms;
    private String sampleName;
    private String sampleType;
    private String datafileName;
    private Timestamp year;
    private Date yearRangeStart;
    private Date yearRangeEnd;
    private String[] datafileTypes;
    private String[] keywords;
    private String searchFilterType;
    
    /** Creates a new instance of AdvancedSearchDTO */
    public AdvancedSearchDTO() {
    }

    public String getExperimentTitle() {
        return experimentTitle;
    }

    public void setExperimentTitle(String experimentTitle) {
        this.experimentTitle = experimentTitle;
    }

    public String getInvestigationName() {
        return investigationName;
    }

    public void setInvestigationName(String investigationName) {
        this.investigationName = investigationName;
    }

    public String getInvestigators() {
        return investigators;
    }

    public void setInvestigators(String investigators) {
        this.investigators = investigators;
    }

    public String getExperimentNumber() {
        return experimentNumber;
    }

    public void setExperimentNumber(String experimentNumber) {
        this.experimentNumber = experimentNumber;
    }

    public String[] getInstruments() {
        return instruments;
    }

    public void setInstruments(String[] instruments) {
        this.instruments = instruments;
    }

    public Long getRunStart() {
        return runStart;
    }

    public void setRunStart(Long runStart) {
        this.runStart = runStart;
    }

    public Long getRunEnd() {
        return runEnd;
    }

    public void setRunEnd(Long runEnd) {
        this.runEnd = runEnd;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getDatafileName() {
        return datafileName;
    }

    public void setDatafileName(String datafileName) {
        this.datafileName = datafileName;
    }

    public Timestamp getYear() {
        return year;
    }

    public void setYear(Timestamp year) {
        this.year = year;
    }

    public Date getYearRangeStart() {
        return yearRangeStart;
    }

    public void setYearRangeStart(Date yearRangeStart) {
        this.yearRangeStart = yearRangeStart;
    }

    public Date getYearRangeEnd() {
        return yearRangeEnd;
    }

    public void setYearRangeEnd(Date yearRangeEnd) {
        this.yearRangeEnd = yearRangeEnd;
    }

    public String[] getDatafileTypes() {
        return datafileTypes;
    }

    public void setDatafileTypes(String[] datafileTypes) {
        this.datafileTypes = datafileTypes;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String getSearchFilterType() {
        return searchFilterType;
    }

    public void setSearchFilterType(String searchFilterType) {
        this.searchFilterType = searchFilterType;
    }
        
    
}
