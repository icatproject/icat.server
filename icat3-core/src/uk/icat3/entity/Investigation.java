/*
 * Investigation.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.log4j.Logger;
import uk.icat3.entity.Dataset;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.security.GateKeeper;
import uk.icat3.util.AccessType;
import uk.icat3.util.Cascade;
import uk.icat3.util.ElementType;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.Queries;

/**
 * Entity class Investigation
 *
 * @author gjd37
 */
@Entity
@Table(name = "INVESTIGATION", uniqueConstraints={@UniqueConstraint(columnNames={"INV_NUMBER","VISIT_ID", "FACILITY_CYCLE","INSTRUMENT"})})
@NamedQueries( {
    @NamedQuery(name = "Investigation.findById", query = "SELECT i FROM Investigation i WHERE i.id = :id"),
    @NamedQuery(name = "Investigation.findByInvNumber", query = "SELECT i FROM Investigation i WHERE i.invNumber = :invNumber"),
    @NamedQuery(name = "Investigation.findByVisitId", query = "SELECT i FROM Investigation i WHERE i.visitId = :visitId"),
    @NamedQuery(name = "Investigation.findByTitle", query = "SELECT i FROM Investigation i WHERE i.title = :title"),
    @NamedQuery(name = "Investigation.findByInvAbstract", query = "SELECT i FROM Investigation i WHERE i.invAbstract = :invAbstract"),
    @NamedQuery(name = "Investigation.findByPrevInvNumber", query = "SELECT i FROM Investigation i WHERE i.prevInvNumber = :prevInvNumber"),
    @NamedQuery(name = "Investigation.findByBcatInvStr", query = "SELECT i FROM Investigation i WHERE i.bcatInvStr = :bcatInvStr"),
    @NamedQuery(name = "Investigation.findByGrantId", query = "SELECT i FROM Investigation i WHERE i.grantId = :grantId"),
    @NamedQuery(name = "Investigation.findByReleaseDate", query = "SELECT i FROM Investigation i WHERE i.releaseDate = :releaseDate"),
    @NamedQuery(name = "Investigation.findByModTime", query = "SELECT i FROM Investigation i WHERE i.modTime = :modTime"),
    @NamedQuery(name = "Investigation.findByModId", query = "SELECT i FROM Investigation i WHERE i.modId = :modId"),
    @NamedQuery(name = "Investigation.findByUnique", query = "SELECT i FROM Investigation i WHERE (i.invNumber = :invNumber OR i.invNumber is NULL) AND (i.visitId = :visitId OR i.visitId IS NULL) AND (i.facilityCycle = :facilityCycle OR i.facilityCycle IS NULL) AND (i.instrument = :instrument OR i.instrument IS NULL)"),
    
    
    //Added searches for ICAT3 API
    @NamedQuery(name = Queries.INVESTIGATIONS_BY_USER, query = Queries.INVESTIGATIONS_BY_USER_JPQL),
    @NamedQuery(name = Queries.INVESTIGATION_LIST_BY_SURNAME, query= Queries.INVESTIGATIONS_LIST_BY_USER_SURNAME_JPQL),
    @NamedQuery(name = Queries.INVESTIGATION_LIST_BY_USERID,  query= Queries.INVESTIGATION_LIST_BY_USERID_JPQL),
    @NamedQuery(name = Queries.INVESTIGATION_LIST_BY_USERID_RTID,  query= Queries.INVESTIGATION_LIST_BY_USERID_RTID_JPQL),  
    @NamedQuery(name = Queries.INVESTIGATION_LIST_BY_KEYWORD_RTN_ID, query= Queries.INVESTIGATION_LIST_BY_KEYWORD_RTN_ID_JPQL),
    @NamedQuery(name = Queries.INVESTIGATION_LIST_BY_KEYWORD, query= Queries.INVESTIGATION_LIST_BY_KEYWORD_JPQL)
})
        
        @SqlResultSetMappings({
 //   @SqlResultSetMapping(name="investigationMapping",entities={@EntityResult(entityClass=Investigation.class)}),
  //  @SqlResultSetMapping(name="investigationIdMapping",columns={@ColumnResult(name="ID")})
})
        @XmlRootElement
        @SequenceGenerator(name="INVESTIGATION_SEQ",sequenceName="INVESTIGATION_ID_SEQ",allocationSize=1)
        public class Investigation extends EntityBaseBean implements Serializable {
    
    /**
     * Override logger
     */
    protected static Logger log = Logger.getLogger(Investigation.class);
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="INVESTIGATION_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "INV_NUMBER", nullable = false)
    private String invNumber;
    
    @Column(name = "VISIT_ID")
    private String visitId;
    
    @Column(name = "TITLE", nullable = false)
    private String title;
    
    @Column(name = "INV_ABSTRACT")
    @ICAT(max=4000)
    private String invAbstract;
    
    @Column(name = "PREV_INV_NUMBER")
    private String prevInvNumber;
    
    @Column(name = "BCAT_INV_STR")
    private String bcatInvStr;
    
    @Column(name = "INV_PARAM_NAME")     
    private String invParamName;
       
    @Column(name = "INV_PARAM_VALUE")   
    private String invParamValue;
    
    @Column(name = "GRANT_ID")
    private Long grantId;
    
    @Column(name = "FACILITY", nullable = false)
    private String facility;
       
    @Column(name = "RELEASE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date releaseDate;
    
    @Column(name = "INV_START_DATE")   
    @Temporal(TemporalType.TIMESTAMP)    
    private Date invStartDate;
        
    @Column(name = "INV_END_DATE")
    @Temporal(TemporalType.TIMESTAMP)   
    private Date invEndDate;
    
    /*@JoinColumn(name = "FACILITY", referencedColumnName = "FACILITY_SHORT_NAME", nullable= false)
    @ManyToOne()
    @ICAT(merge=false)
    private Facility facility;*/
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigationId")
    private Collection<Publication> publicationCollection;
    
    @OneToMany(cascade = CascadeType.ALL , mappedBy = "investigationId")
    private Collection<Sample> sampleCollection;
    
    @JoinColumn(name = "FACILITY_CYCLE", referencedColumnName = "NAME")
    @ManyToOne
    private FacilityCycle facilityCycle;
    
    @Column(name = "INSTRUMENT")
    private String instrument;
    
    public String getInstrument() {
        return instrument;
    }
    
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
    
    /*@JoinColumn(name = "INSTRUMENT", referencedColumnName = "NAME")
    @ManyToOne
    private Instrument instrument;*/
    
    @Column(name = "INV_TYPE", nullable = false)
    private String invType;
    
    public String getInvType() {
        return invType;
    }
    
    public void setInvType(String invType) {
        this.invType = invType;
    }
    
    /*@JoinColumn(name = "INV_TYPE", referencedColumnName = "NAME", nullable= false)
    @ManyToOne
    private InvestigationType invType;*/
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<Dataset> datasetCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<Shift> shiftCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<Keyword> keywordCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<StudyInvestigation> studyInvestigationCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<Investigator> investigatorCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<TopicList> topicListCollection;
    
    /**
     * InvestigationIncludes that needs to be added to the investigation
     */
    private transient InvestigationInclude investigationInclude = InvestigationInclude.NONE;
    
    
    /** Creates a new instance of Investigation */
    public Investigation() {
    }
    
    /**
     * Creates a new instance of Investigation with the specified values.
     * @param id the id of the Investigation
     */
    public Investigation(Long id) {
            this.id = id;
    }

    /**
     * Creates a new instance of Investigation with the specified values.
     * @param id the id of the Investigation
     * @param invNumber the invNumber of the Investigation
     * @param title the title of the Investigation
     * @param modTime the modTime of the Investigation
     * @param modId the modId of the Investigation
     */
    public Investigation(Long id, String invNumber, String title, Date modTime, String modId) {
        this.id = id;
        this.invNumber = invNumber;
        this.title = title;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the id of this Investigation.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this Investigation to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the invNumber of this Investigation.
     * @return the invNumber
     */
    public String getInvNumber() {
        return this.invNumber;
    }
    
    /**
     * Sets the invNumber of this Investigation to the specified value.
     * @param invNumber the new invNumber
     */
    public void setInvNumber(String invNumber) {
        this.invNumber = invNumber;
    }
    
    /**
     * Gets the visitId of this Investigation.
     * @return the visitId
     */
    public String getVisitId() {
        return this.visitId;
    }
    
    /**
     * Sets the visitId of this Investigation to the specified value.
     * @param visitId the new visitId
     */
    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }
    
    /**
     * Gets the title of this Investigation.
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Sets the title of this Investigation to the specified value.
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the invAbstract of this Investigation.
     * @return the invAbstract
     */
    public String getInvAbstract() {
        return this.invAbstract;
    }
    
    /**
     * Sets the invAbstract of this Investigation to the specified value.
     * @param invAbstract the new invAbstract
     */
    public void setInvAbstract(String invAbstract) {
        this.invAbstract = invAbstract;
    }
    
    /**
     * Gets the prevInvNumber of this Investigation.
     * @return the prevInvNumber
     */
    public String getPrevInvNumber() {
        return this.prevInvNumber;
    }
    
    /**
     * Sets the prevInvNumber of this Investigation to the specified value.
     * @param prevInvNumber the new prevInvNumber
     */
    public void setPrevInvNumber(String prevInvNumber) {
        this.prevInvNumber = prevInvNumber;
    }
    
    /**
     * Gets the bcatInvStr of this Investigation.
     * @return the bcatInvStr
     */
    public String getBcatInvStr() {
        return this.bcatInvStr;
    }
    
    /**
     * Sets the bcatInvStr of this Investigation to the specified value.
     * @param bcatInvStr the new bcatInvStr
     */
    public void setBcatInvStr(String bcatInvStr) {
        this.bcatInvStr = bcatInvStr;
    }
    
    /**
     * Gets the grantId of this Investigation.
     * @return the grantId
     */
    public Long getGrantId() {
        return this.grantId;
    }
    
    /**
     * Sets the grantId of this Investigation to the specified value.
     * @param grantId the new grantId
     */
    public void setGrantId(Long grantId) {
        this.grantId = grantId;
    }
    
    /**
     * Gets the releaseDate of this Investigation.
     * @return the releaseDate
     */
    public Date getReleaseDate() {
        return this.releaseDate;
    }
    
    /**
     * Sets the releaseDate of this Investigation to the specified value.
     * @param releaseDate the new releaseDate
     */
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    /**
     * Gets the publicationCollection of this Investigation.
     * @return the publicationCollection
     */
    @XmlTransient
    public Collection<Publication> getPublicationCollection() {
        return this.publicationCollection;
    }
    
    /**
     * Adds a Publication to the investigation,
     * also adds the investigation to the Publication.
     */
    public void addPublication(Publication publication){
        publication.setInvestigationId(this);
        
        Collection<Publication> publications = this.getPublicationCollection();
        if(publications == null) publications = new ArrayList<Publication>();
        publications.add(publication);
        
        this.setPublicationCollection(publications);
    }
    
    /**
     * This method is used by JAXWS to map to datasetCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="publicationCollection")
    private Collection<Publication> getPublicationCollection_() {
        if(investigationInclude.isPublications()){
            return this.publicationCollection;
        } else return null;
    }
    
    /**
     * Sets the publicationCollection of this Investigation to the specified value.
     * @param publicationCollection the new publicationCollection
     */
    public void setPublicationCollection(Collection<Publication> publicationCollection) {
        this.publicationCollection = publicationCollection;
    }
    
    /**
     * Gets the sampleCollection of this Investigation.
     * @return the sampleCollection
     */
    @XmlTransient
    public Collection<Sample> getSampleCollection() {
        return this.sampleCollection;
    }
    
    /**
     * Adds a Sample to the investigation,
     * also adds the investigation to the Sample.
     */
    public void addSample(Sample sample){
        sample.setInvestigationId(this);
        
        Collection<Sample> samples = this.getSampleCollection();
        if(samples == null) samples = new ArrayList<Sample>();
        
        samples.add(sample);
        
        this.setSampleCollection(samples);
    }
    
    /**
     * This method is used by JAXWS to map to datasetCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="sampleCollection")
    private Collection<Sample> getSampleCollection_() {
        if(investigationInclude.isSamples()){
            return this.sampleCollection;
        } else return null;
    }
    
    /**
     * Sets the sampleCollection of this Investigation to the specified value.
     * @param sampleCollection the new sampleCollection
     */
    public void setSampleCollection(Collection<Sample> sampleCollection) {
        this.sampleCollection = sampleCollection;
    }
    
    /**
     * Gets the facilityCycle of this Investigation.
     * @return the facilityCycle
     */
    
    public FacilityCycle getFacilityCycle() {
        return this.facilityCycle;
    }
    
    /**
     * Sets the facilityCycle of this Investigation to the specified value.
     * @param facilityCycle the new facilityCycle
     */
    public void setFacilityCycle(FacilityCycle facilityCycle) {
        this.facilityCycle = facilityCycle;
    }
    
    /**
     * Gets the instrument of this Investigation.
     * @return the instrument
     */
    /*public Instrument getInstrument() {
        return this.instrument;
    }*/
    
    /**
     * Sets the instrument of this Investigation to the specified value.
     * @param instrument the new instrument
     */
    /*public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }*/
    
    /**
     * Gets the invType of this Investigation.
     * @return the invType
     */
    /*public InvestigationType getInvType() {
        return this.invType;
    }*/
    
    /**
     * Sets the invType of this Investigation to the specified value.
     * @param invType the new invType
     */
    /*public void setInvType(InvestigationType invType) {
        this.invType = invType;
    }*/
    
    /**
     * Gets the datasetCollection of this Investigation.
     * @return the datasetCollection
     */
    @XmlTransient
    public Collection<Dataset> getDatasetCollection() {
        return this.datasetCollection;
    }
    
    /**
     * This method is used by JAXWS to map to datasetCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="datasetCollection")
    private Collection<Dataset> getDatasetCollection_() {
        if(investigationInclude.isDatasets()){
            return this.datasetCollection;
        } else return null;
    }
    
    private void setDatasetCollection_(Collection<Dataset> datasetCollection) {
        this.datasetCollection = datasetCollection;
    }
    
    /**
     * Sets the datasetCollection of this Investigation to the specified value.
     * @param datasetCollection the new datasetCollection
     */
    public void setDatasetCollection(Collection<Dataset> datasetCollection) {
        this.datasetCollection = datasetCollection;
    }
    
    
    /**
     * Adds a DataSet to the investigation,
     * also adds the investigation to the DataSet.
     */
    public void addDataSet(Dataset dataSet){
        dataSet.setInvestigation(this);
        
        Collection<Dataset> datasets = this.getDatasetCollection();
        if(datasets == null) datasets = new ArrayList<Dataset>();
        datasets.add(dataSet);
        
        this.setDatasetCollection(datasets);
    }
    
    /**
     * Gets the shiftCollection of this Investigation.
     * @return the shiftCollection
     */
    @XmlTransient //do not turn into XML
    public Collection<Shift> getShiftCollection() {
        return this.shiftCollection;
    }
    
    /**
     * Sets the shiftCollection of this Investigation to the specified value.
     * @param shiftCollection the new shiftCollection
     */
    public void setShiftCollection(Collection<Shift> shiftCollection) {
        this.shiftCollection = shiftCollection;
    }
    
    /**
     * This method is used by JAXWS to map to shiftCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="shiftCollection")
    private Collection<Shift> getShiftCollection_() {
        if(investigationInclude.isShifts()){
            return this.shiftCollection;
        } else return null;
    }
    
    private void setShiftCollection_(Collection<Shift> shiftCollection) {
        this.shiftCollection = shiftCollection;
    }
    
    /**
     * Gets the keywordCollection of this Investigation.
     * @return the keywordCollection
     */
    @XmlTransient //do not turn into XML
    public Collection<Keyword> getKeywordCollection() {
        return this.keywordCollection;
    }
    
    /**
     * This method is used by JAXWS to map to keywordCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="keywordCollection")
    private Collection<Keyword> getKeywordCollection_() {
        if(investigationInclude.isKeywords()){
            return this.keywordCollection;
        } else return null;
    }
    
    private void setKeywordCollection_(Collection<Keyword> keywordCollection) {
        this.keywordCollection = keywordCollection;
    }
    
    /**
     * Sets the keywordCollection of this Investigation to the specified value.
     * @param keywordCollection the new keywordCollection
     */
    public void setKeywordCollection(Collection<Keyword> keywordCollection) {
        this.keywordCollection = keywordCollection;
    }
    
    /**
     * Adds a Keyword to the investigation,
     * also adds the investigation to the Keyword.
     */
    public void addKeyword(Keyword keyword){
        keyword.setInvestigation(this);
        
        Collection<Keyword> keywords = this.getKeywordCollection();
        if(keywords == null) keywords = new ArrayList<Keyword>();
        keywords.add(keyword);
        
        this.setKeywordCollection(keywords);
    }
    
    public String getFacility() {
        return facility;
    }
    
    public void setFacility(String facility) {
        this.facility = facility;
    }
    
    /**
     * Gets the studyInvestigationCollection of this Investigation.
     * @return the studyInvestigationCollection
     */
    @XmlTransient
    public Collection<StudyInvestigation> getStudyInvestigationCollection() {
        return this.studyInvestigationCollection;
    }
    
    /**
     * Sets the studyInvestigationCollection of this Investigation to the specified value.
     * @param studyInvestigationCollection the new studyInvestigationCollection
     */
    public void setStudyInvestigationCollection(Collection<StudyInvestigation> studyInvestigationCollection) {
        this.studyInvestigationCollection = studyInvestigationCollection;
    }
    
    /**
     * Gets the investigatorCollection of this Investigation.
     * @return the investigatorCollection
     */
    @XmlTransient
    public Collection<Investigator> getInvestigatorCollection() {
        return this.investigatorCollection;
    }
    
    /*public Facility getFacility() {
        return facility;
    }
     
    public void setFacility(Facility facility) {
        this.facility = facility;
    }*/
    
    /**
     * This method is used by JAXWS to map to investigatorCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="investigatorCollection")
    private Collection<Investigator> getInvestigatorCollection_() {
        if(investigationInclude.isInvestigators()){
            return this.investigatorCollection;
            //return null;
        }  else return null;
    }
    
    private void setInvestigatorCollection_(Collection<Investigator> investigatorCollection) {
        this.investigatorCollection = investigatorCollection;
    }
    
    /**
     * Adds a Keyword to the investigation,
     * also adds the investigation to the Keyword.
     */
    public void addInvestigator(Investigator investigator){
        investigator.setInvestigation(this);
        
        Collection<Investigator> investigators = this.getInvestigatorCollection();
        if(investigators == null) investigators = new ArrayList<Investigator>();
        investigators.add(investigator);
        
        this.setInvestigatorCollection(investigators);
    }
    
    
    /**
     * Sets the investigatorCollection of this Investigation to the specified value.
     * @param investigatorCollection the new investigatorCollection
     */
    public void setInvestigatorCollection(Collection<Investigator> investigatorCollection) {
        this.investigatorCollection = investigatorCollection;
    }
    
    /**
     * Gets the topicListCollection of this Investigation.
     * @return the topicListCollection
     */
    @XmlTransient
    public Collection<TopicList> getTopicListCollection() {
        return this.topicListCollection;
    }
    
    /**
     * Sets the topicListCollection of this Investigation to the specified value.
     * @param topicListCollection the new topicListCollection
     */
    public void setTopicListCollection(Collection<TopicList> topicListCollection) {
        this.topicListCollection = topicListCollection;
    }
    
    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param value value of the cascade type
     */
    public void setCascade(Cascade type, Object value) throws InsufficientPrivilegesException{
        setCascade(type, value, null, null);
    }
    
    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param value value of the cascade type
     */
    public void setCascade(Cascade type, Object value, EntityManager manager) throws InsufficientPrivilegesException{
        setCascade(type, value, manager, null);
    }
    
    
    /**
     * Sets type (see Cascade) flag on all items owned by this dataset
     *
     * @param type Cascade type, DELETE, MOD_ID, MOD_AND_CREATE_IDS and REMOVE_DELETED_ITEMS
     * @param cascadeValue value of the cascade type
     * @param manager entity manager to  connect to DB
     * @param managerValue value of the EntityManager value
     */
    public void setCascade(Cascade type, Object cascadeValue, EntityManager manager, Object managerValue) throws InsufficientPrivilegesException{
        log.trace("Cascading: "+toString()+" from type: "+type+" to :"+cascadeValue+" EntityManager: "+(manager == null ? "null" : "manager")+", managerValue: "+ managerValue);
        
        String deleted = "Y";
        String facilityAcquired = "Y";
        if(type == Cascade.DELETE || type == Cascade.FACILITY_ACQUIRED){
            deleted = (((Boolean)cascadeValue).booleanValue()) ? "Y" : "N";
            facilityAcquired = deleted;
            if(managerValue == null && type == Cascade.DELETE) throw new RuntimeException("Manager Value needs to be set aswell if Cascade.DELETE");
        }
        
        if(type == Cascade.REMOVE_DELETED_ITEMS){
            log.trace("Remove from: ");
            log.trace("Datasets? "+investigationInclude.isDatasets());
            log.trace("Datasets and datafiles? "+investigationInclude.isDatasetsAndDatafiles());
            log.trace("Investigators? "+investigationInclude.isInvestigators());
            log.trace("Keywords? "+investigationInclude.isKeywords());
            log.trace("Publications? "+investigationInclude.isPublications());
            log.trace("Roles? "+investigationInclude.isRoles());
            log.trace("Samples? "+investigationInclude.isSamples());
        }
        
        //datafiles
        if(type == Cascade.REMOVE_DELETED_ITEMS &&  investigationInclude.isDatasets() ){
            Collection<Dataset> datasets = new ArrayList<Dataset>();
            
            for(Dataset dataset : getDatasetCollection()){
                if(!dataset.isDeleted()) {
                    datasets.add(dataset);
                    if(((Boolean)cascadeValue).booleanValue()) dataset.setCascade(Cascade.REMOVE_DELETED_ITEMS, cascadeValue);
                }
            }
            
            //now set the new dataset collection
            log.trace("Setting new datasetCollection of size: "+datasets.size()+" because of deleted items from original size: "+getDatasetCollection().size());
            this.setDatasetCollection(datasets);
        } else if(type != Cascade.REMOVE_DELETED_ITEMS && getDatasetCollection() != null){
            
            for(Dataset dataset : getDatasetCollection()){
                dataset.setCascade(type, cascadeValue, manager, managerValue);
            }
        }
        
        //investigators
        //if REMOVE_DELETED_ITEMS, check if investigationInclude wants keywords
        if(type == Cascade.REMOVE_DELETED_ITEMS && investigationInclude.isInvestigators()){
            //create new collection if remove deleted items
            Collection<Investigator> investigators = new ArrayList<Investigator>();
            
            for(Investigator investigator : getInvestigatorCollection()){
                if(!investigator.isDeleted()) investigators.add(investigator);
            }
            
            //now set the new dataset collection
            log.trace("Setting new investigatorCollection of size: "+investigators.size()+" because of deleted items from original size: "+getInvestigatorCollection().size());
            this.setInvestigatorCollection(investigators);
            
        } else if(type != Cascade.REMOVE_DELETED_ITEMS && getInvestigatorCollection() != null){
            
            for(Investigator investigator : getInvestigatorCollection()){
                if(type == Cascade.DELETE) {
                    investigator.setMarkedDeleted(deleted);
                    investigator.setModId(managerValue.toString());
                } else if(type == Cascade.MOD_ID) investigator.setModId(cascadeValue.toString());
                else if(type == Cascade.FACILITY_ACQUIRED) investigator.setFacilityAcquired(facilityAcquired);
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    investigator.setModId(cascadeValue.toString());
                    investigator.setCreateId(cascadeValue.toString());
                }
            }
        }
        
        //TODO do delete
        //check if manager is null
        /*if(manager != null && type == Cascade.DELETE){
            Query query = manager.createNamedQuery("IcatAuthorisation.findByInvestigationId").
                    setParameter("elementType", null).
                    setParameter("elementId", null).
                    setParameter("investigationId", this.getId());
            Collection<IcatAuthorisation> icatAuthorisations = (Collection<IcatAuthorisation>)query.getResultList();
         
            //now mark them all as deleted
            for (IcatAuthorisation icatAuthorisation : icatAuthorisations) {
                log.trace("Marking: "+icatAuthorisation+" as "+cascadeValue);
                icatAuthorisation.setMarkedDeleted(deleted);
                icatAuthorisation.setModId(managerValue.toString());
            }
        }*/
        
        //sample
        //if REMOVE_DELETED_ITEMS, check if investigationInclude wants keywords
        if(type == Cascade.REMOVE_DELETED_ITEMS && investigationInclude.isSamples() ){
            //create new collection if remove deleted items
            Collection<Sample> samples = new ArrayList<Sample>();
            
            for(Sample sample : getSampleCollection()){
                if(!sample.isDeleted()) {
                    samples.add(sample);
                    //always cascade this
                    sample.setCascade(Cascade.REMOVE_DELETED_ITEMS, true);
                }
            }
            
            //now set the new dataset collection
            log.trace("Setting new sampleCollection of size: "+samples.size()+" because of deleted items from original size: "+getSampleCollection().size());
            this.setSampleCollection(samples);
        } else if(type != Cascade.REMOVE_DELETED_ITEMS && getSampleCollection() != null){
            //create new collection if remove deleted items
            Collection<Sample> samples = new ArrayList<Sample>();
            
            for(Sample sample : getSampleCollection()){
                sample.setCascade(type, cascadeValue, manager, managerValue);
            }
        }
        
        //study
        /*if(getStudyInvestigationCollection() != null){
            //create new collection if remove deleted items
            Collection<StudyInvestigation> studyInvestigations = new ArrayList<StudyInvestigation>();
         
            for(StudyInvestigation study : getStudyInvestigationCollection()){
                if(type == Cascade.DELETE) {
                    study.setMarkedDeleted(deleted);
                    study.setModId(managerValue.toString());
                } else if(type == Cascade.MOD_ID) study.setModId(cascadeValue.toString());
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    study.setModId(cascadeValue.toString());
                    study.setCreateId(cascadeValue.toString());
                } else if(type == Cascade.REMOVE_DELETED_ITEMS){
                    //remove all deleted items from the collection, ie only add ones that are not deleted
                    if(!study.isDeleted()) studyInvestigations.add(study);
                }
            }
            if(type == Cascade.REMOVE_DELETED_ITEMS){
                //now set the new dataset collection
                log.trace("Setting new studyInvestigationCollection of size: "+studyInvestigations.size()+" because of deleted items from original size: "+getStudyInvestigationCollection().size());
                this.setStudyInvestigationCollection(studyInvestigations);
            }
        }*/
        
        //if REMOVE_DELETED_ITEMS, check if investigationInclude wants keywords
        if(type == Cascade.REMOVE_DELETED_ITEMS && investigationInclude.isShifts() ){
            //create new collection if remove deleted items
            Collection<Shift> shifts = new ArrayList<Shift>();
            
            for(Shift shift : getShiftCollection()){
                if(!shift.isDeleted()) shifts.add(shift);
            }
            
            //now set the new dataset collection
            log.trace("Setting new shiftCollection of size: "+shifts.size()+" because of deleted items from original size: "+getShiftCollection().size());
            this.setShiftCollection(shifts);
            
        } else if(type != Cascade.REMOVE_DELETED_ITEMS && getShiftCollection() != null){
            
            for(Shift shift : getShiftCollection()){
                if(type == Cascade.DELETE) {
                    shift.setMarkedDeleted(deleted);
                    shift.setModId(managerValue.toString());
                } else if(type == Cascade.MOD_ID) shift.setModId(cascadeValue.toString());
                else if(type == Cascade.FACILITY_ACQUIRED) shift.setFacilityAcquired(facilityAcquired);
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    shift.setModId(cascadeValue.toString());
                    shift.setCreateId(cascadeValue.toString());
                }
            }
        }
        
        //publication
        //if REMOVE_DELETED_ITEMS, check if investigationInclude wants keywords
        if(type == Cascade.REMOVE_DELETED_ITEMS && investigationInclude.isPublications() ){
            //create new collection if remove deleted items
            Collection<Publication> publications = new ArrayList<Publication>();
            
            for(Publication publication : getPublicationCollection()){
                if(!publication.isDeleted()) publications.add(publication);
            }
            
            //now set the new dataset collection
            log.trace("Setting new publicationCollection of size: "+publications.size()+" because of deleted items from original size: "+getPublicationCollection().size());
            this.setPublicationCollection(publications);
            
        } else if(type != Cascade.REMOVE_DELETED_ITEMS && getPublicationCollection() != null){
            
            for(Publication publication : getPublicationCollection()){
                if(type == Cascade.DELETE) {
                    publication.setMarkedDeleted(deleted);
                    publication.setModId(managerValue.toString());
                } else if(type == Cascade.MOD_ID) publication.setModId(cascadeValue.toString());
                else if(type == Cascade.FACILITY_ACQUIRED) publication.setFacilityAcquired(facilityAcquired);
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    publication.setModId(cascadeValue.toString());
                    publication.setCreateId(cascadeValue.toString());
                }
            }
        }
        
        //keyword
        //if REMOVE_DELETED_ITEMS, check if investigationInclude wants keywords
        if(type == Cascade.REMOVE_DELETED_ITEMS &&  investigationInclude.isKeywords() ){
            //create new collection if remove deleted items
            Collection<Keyword> keywords = new ArrayList<Keyword>();
            
            for(Keyword keyword : getKeywordCollection()){
                if(!keyword.isDeleted()) keywords.add(keyword);
            }
            
            //now set the new dataset collection
            log.trace("Setting new keywordCollection of size: "+keywords.size()+" because of deleted items from original size: "+getKeywordCollection().size());
            this.setKeywordCollection(keywords);
            
        } else if(type != Cascade.REMOVE_DELETED_ITEMS && getKeywordCollection() != null){
            
            for(Keyword keyword : getKeywordCollection()){
                if(type == Cascade.DELETE) {
                    keyword.setMarkedDeleted(deleted);
                    keyword.setModId(managerValue.toString());
                } else if(type == Cascade.MOD_ID) keyword.setModId(cascadeValue.toString());
                else if(type == Cascade.FACILITY_ACQUIRED) keyword.setFacilityAcquired(facilityAcquired);
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    keyword.setModId(cascadeValue.toString());
                    keyword.setCreateId(cascadeValue.toString());
                }
            }
        }
        
        //topicList parameter
        /*if(getTopicListCollection() != null){
            //create new collection if remove deleted items
            Collection<TopicList> topicLists = new ArrayList<TopicList>();
         
            for(TopicList topicList : getTopicListCollection()){
                if(type == Cascade.DELETE)  {
                    topicList.setMarkedDeleted(deleted);
                    topicList.setModId(managerValue.toString());
                } else if(type == Cascade.MOD_ID) topicList.setModId(cascadeValue.toString());
                else if(type == Cascade.MOD_AND_CREATE_IDS) {
                    topicList.setModId(cascadeValue.toString());
                    topicList.setCreateId(cascadeValue.toString());
                } else if(type == Cascade.REMOVE_DELETED_ITEMS){
                    //remove all deleted items from the collection, ie only add ones that are not deleted
                    if(!topicList.isDeleted()) topicLists.add(topicList);
                }
            }
            if(type == Cascade.REMOVE_DELETED_ITEMS){
                //now set the new dataset collection
                log.trace("Setting new keywordCollection of size: "+topicLists.size()+" because of deleted items from original size: "+getTopicListCollection().size());
                this.setTopicListCollection(topicLists);
            }
        }*/
        
        if(type == Cascade.DELETE) {
            //need to check if use has permission to delete this
            //only check if the value of deleted is different the one wanting to be changed to
            if(this.isDeleted() != ((Boolean)cascadeValue).booleanValue()){
                GateKeeper.performAuthorisation(managerValue.toString(), this, AccessType.DELETE, manager);
                this.setMarkedDeleted(deleted);
                this.setModId(managerValue.toString());
            }
        } else if(type == Cascade.MOD_ID) this.setModId(cascadeValue.toString());
        else if(type == Cascade.FACILITY_ACQUIRED) this.setFacilityAcquired(facilityAcquired);
        else if(type == Cascade.MOD_AND_CREATE_IDS) {
            this.setModId(cascadeValue.toString());
            this.setCreateId(cascadeValue.toString());
        }
    }
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
    }
    
    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    /**
     * Determines whether another object is equal to this Investigation.  The result is
     * <code>true</code> if and only if the argument is not null and is a Investigation object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Investigation)) {
            return false;
        }
        Investigation other = (Investigation)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Investigation[id=" + id + "]";
    }
    
    /**
     * Method to be overriding if needed to check if the data held in the entity is valid.
     * This method should be used for search DB for foreign key constraints etc
     * Deep validation if all of its children need to be validated
     *
     * @return true if validation is correct,
     * @param manager if queries are needed
     * @param deepValidation if all of child entities need to be validated
     * @throws ValidationException if validation error.
     */
    public boolean isValid(EntityManager manager, boolean deepValidation)  throws ValidationException {
        if(manager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        
        if(deepValidation){
            
            if(this.instrument != null){
                //this.instrument.isValid(manager);
                //check instrument is correct.
                //check investigation type is correct.
                Instrument instrument = manager.find(Instrument.class, this.instrument);
                if(instrument == null)throw new ValidationException(this.instrument+" is not a valid instrument.");
            }
            
            if(this.invType != null){
                //this.invType.isValid(manager);
                
                //check investigation type is correct.
                InvestigationType investigationType = manager.find(InvestigationType.class, this.invType);
                if(investigationType == null)throw new ValidationException(this.invType+" is not a valid investigation type.");
            }
            
            //check all datasets now
            if(getDatasetCollection() != null){
                for(Dataset dataset : getDatasetCollection()){
                    dataset.isValid(manager);
                }
            }
        }
        //test is unique
        isUnique(manager);
        
        return isValid();
    }
    
    /**
     * Checks weather the investigation is unique in the database.
     */
    private boolean isUnique(EntityManager manager) throws ValidationException{
        log.trace("isUnique?");
        Query query =  manager.createNamedQuery("Investigation.findByUnique");
        query = query.setParameter("invNumber",invNumber);
        query = query.setParameter("visitId", visitId);
        query = query.setParameter("facilityCycle",facilityCycle);
        query = query.setParameter("instrument",instrument);
        
        try {
            log.trace("Looking for: invNumber: "+ invNumber);
            log.trace("Looking for: visitId: "+ visitId);
            log.trace("Looking for: facilityCycle: "+facilityCycle);
            log.trace("Looking for: instrument: "+instrument);
            
            Investigation investigationFound = (Investigation)query.getSingleResult();
            log.trace("Returned: "+investigationFound);
            if(investigationFound.getId() != null && investigationFound.getId().equals(this.getId())) {
                log.trace("investigation found is this dataset");
                return true;
            } else {
                log.trace("investigation found is not this investigation, so no unique");
                throw new ValidationException(this+" is not unique.  Same unique key as "+investigationFound);
            }
        } catch(NoResultException nre) {
            log.trace("No results so unique");
            //means it is unique
            return true;
        } catch(Throwable ex) {
            log.warn(ex);
            //means it is unique
            if(ex instanceof ValidationException) throw (ValidationException)ex;
            else throw new ValidationException(this+" is not unique.");
        }
    }
    
    /**
     * See getInvestigatorCollection_()
     */
    public void setInvestigationInclude(InvestigationInclude investigationInclude) {
        this.investigationInclude = investigationInclude;
    }

    public Date getInvStartDate() {
        return invStartDate;
    }

    public void setInvStartDate(Date invStartDate) {
        this.invStartDate = invStartDate;
    }

    public Date getInvEndDate() {
        return invEndDate;
    }

    public void setInvEndDate(Date invEndDate) {
        this.invEndDate = invEndDate;
    }

    public String getInvParamName() {
        return invParamName;
    }

    public void setInvParamName(String invParamName) {
        this.invParamName = invParamName;
    }

    public String getInvParamValue() {
        return invParamValue;
    }

    public void setInvParamValue(String invParamValue) {
        this.invParamValue = invParamValue;
    }
}
