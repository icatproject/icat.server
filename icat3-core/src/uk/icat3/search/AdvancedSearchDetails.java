/*
 * AdvancedSearchDetails.java
 *
 * Created on 21 February 2007, 16:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.search;

import java.util.Collection;
import java.util.Date;
import uk.icat3.util.InvestigationInclude;

/**
 *
 * @author gjd37
 */
public class AdvancedSearchDetails {
    
    /**
     * Investigation Name
     */
    private String investigationName; //inv title
    /**
     * Visit Id
     */
    private String visitId; //visit id 
    /**
     * Investigation abstract
     */
    private String investigationAbstract;    
    /**
     * Investigation type
     */
    private String investigationType;
    /**
     * Investigation grant Id
     */
    private Long grantId;
    /**
     * Investigator Name
     */
    private String backCatalogueInvestigatorString; //back catalogue investigator string
    /**
     * List of surnames
     */
    private Collection<String> investigators; //surname
    /**
     * Investigation number
     */
    private String experimentNumber; //inv_number
    /**
     * List of instruments
     */
    private Collection<String> instruments;
    /**
     * Long value of start date in datafile parameter
     */
    private float runStart; // data file parameter,  run_number datafile_parameter
    /**
     * Long value of start end in datafile parameter
     */
    private float runEnd = 90000000000f;// data file parameter, run_number datafile_parameter
    /**
     * Sample name
     */
    private String sampleName; // sample
    /**
     * Datafile name
     */
    private String datafileName; // data file name
    /**
     * datafile create date
     */
    private Date yearRangeStart; // (datafile_CREATE_time)
    /**
     * datafile create date
     */
    private Date yearRangeEnd;// (datafile_CREATE_time)
    /**
     * List of keywords
     */
    private Collection<String> keywords;
    /**
     * InvestigationInclude in the data returned. {@link InvestigationInclude}
     */
    private InvestigationInclude investigationInclude;
    
    /**
     * Creates a new instance of AdvancedSearchDetails
     */
    public AdvancedSearchDetails() {
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
    
    public float getRunStart() {
        return runStart;
    }
    
    public void setRunStart(Long runStart) {
        this.runStart = runStart;
    }
    
    public float getRunEnd() {
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
    
    public String getDatafileName() {
        return datafileName;
    }
    
    public void setDatafileName(String datafileName) {
        this.datafileName = datafileName;
    }
    
    public Date getYearRangeStart() {
        //if null, pass in 1901
        if(yearRangeStart == null) return new Date(1,1,1); // 1901/1/1
        else return yearRangeStart;
    }
    
    public void setYearRangeStart(Date yearRangeStart) {
        this.yearRangeStart = yearRangeStart;
    }
    
    public Date getYearRangeEnd() {
        //if null, pass in todays date
        if(yearRangeEnd == null) return new Date();
        return yearRangeEnd;
    }
    
    public void setYearRangeEnd(Date yearRangeEnd) {
        this.yearRangeEnd = yearRangeEnd;
    }
    
    public Collection<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(Collection<String> keywords) {
        this.keywords = keywords;
    }
    
    public Collection<String> getInstruments() {
        return instruments;
    }
    
    public void setInstruments(Collection<String> instruments) {
        this.instruments = instruments;
    }
    
    public String getBackCatalogueInvestigatorString() {
        return backCatalogueInvestigatorString;
    }
    
    public void setBackCatalogueInvestigatorString(String backCatalogueInvestigatorString) {
        this.backCatalogueInvestigatorString = backCatalogueInvestigatorString;
    }
    
     public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }
    
    public String getInvestigationAbstract() {
        return investigationAbstract;
    }

    public void setInvestigationAbstract(String investigationAbstract) {
        this.investigationAbstract = investigationAbstract;
    }
        
    public String getInvestigationType() {
        return investigationType;
    }

    public void setInvestigationType(String investigationType) {
        this.investigationType = investigationType;
    }
        
    public Long getGrantId() {
        return grantId;
    }

    public void setGrantId(Long grantId) {
        this.grantId = grantId;
    }
    
    /////////////  Util methods for AdvancedSearch creation in InvestigationSearch    /////////////////
    public boolean isOtherParameters(){
        if(investigators != null && investigators.size() != 0) return true;
        if(keywords != null && keywords.size() != 0) return true;
        if(sampleName != null) return true;
        if(datafileName != null) return true;
        if(runEnd != 90000000000f || runStart != 0f) return true;
        else return false;
    }
    
    public boolean isInstruments(){
        if(getInstruments() != null && getInstruments().size() != 0) return true;
        else return false;
    }
    
    public boolean isInvestigators(){
        if(getInvestigators() != null && getInvestigators().size() != 0)return true;
        else return false;
    }
    
    public boolean isKeywords(){
        if(getKeywords() != null && getKeywords().size() != 0) return true;
        else return false;
    }
    
      public boolean isAbstract(){
        if(getInvestigationAbstract() != null && getInvestigationAbstract().length() != 0) return true;
        else return false;
    }
    
    public boolean isRunNumber(){
        if(runEnd != 90000000000f || runStart != 0f) return true;
        else return false;
    }
    
    public boolean isDatFileParameters(){
        if(yearRangeEnd != null || yearRangeStart != null || datafileName != null) return true;
        else return false;
    }
        
    /////////////  End of methods    /////////////////
    
    public InvestigationInclude getInvestigationInclude() {
        if(investigationInclude == null) return InvestigationInclude.NONE;
        return investigationInclude;
    }
    
    public void setInvestigationInclude(InvestigationInclude investigationInclude) {
        this.investigationInclude = investigationInclude;
    }
}
