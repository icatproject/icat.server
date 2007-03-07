/*
 * AdvancedSearchDTO.java
 *
 * Created on 21 February 2007, 16:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import java.util.Date;

/**
 *
 * @author gjd37
 */
public class AdvancedSearchDTO {
       
    private String investigationName;
    private Collection<String> investigators; //surname
    private String experimentNumber; //inv_number
    private String instrument;
    private Long runStart;
    private Long runEnd;
 //   ParameterSearchTerm[] parameterSearchTerms;
    private String sampleName;
    private String sampleType;
    private String datafileName;
    //private Timestamp year;
    private Date yearRangeStart;
    private Date yearRangeEnd;
    private Collection<String> datafileTypes;
    private Collection<String> keywords;
    private String searchFilterType;
    
    /** Creates a new instance of AdvancedSearchDTO */
    public AdvancedSearchDTO() {
    }

       
    public String getInvestigationName() {
        return investigationName;
    }

    public void setInvestigationName(String investigationName) {
        this.investigationName = investigationName;
    }

    public Collection<String> getInvestigators() {
        return investigators;
    }

    public void setInvestigators(Collection<String> investigators) {
        this.investigators = investigators;
    }

    public String getExperimentNumber() {
        return experimentNumber;
    }

    public void setExperimentNumber(String experimentNumber) {
        this.experimentNumber = experimentNumber;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
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

    
    //can do that with start and end
    /*public Timestamp getYear() {
        return year;
    }

    public void setYear(Timestamp year) {
        this.year = year;
    }*/

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

    public Collection<String> getDatafileTypes() {
        return datafileTypes;
    }

    public void setDatafileTypes(Collection<String> datafileTypes) {
        this.datafileTypes = datafileTypes;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Collection<String> keywords) {
        this.keywords = keywords;
    }

    public String getSearchFilterType() {
        return searchFilterType;
    }

    public void setSearchFilterType(String searchFilterType) {
        this.searchFilterType = searchFilterType;
    }    
}
