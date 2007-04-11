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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
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
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.search.InvestigationSearch;
import uk.icat3.util.Cascade;
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
    // @NamedQuery(name = Queries.INVESTIGATIONS_BY_KEYWORD, query ="SELECT  FROM  (SELECT Investigation i FROM i WHERE i.investigatorCollection.investigatorPK.facilityUserId = :userId) ")
    // @NamedQuery(name = Queries.ADVANCED_SEARCH, query = Queries.ADVANCED_SEARCH_JPQL),
    @NamedQuery(name = Queries.INVESTIGATIONS_BY_USER, query = Queries.INVESTIGATIONS_BY_USER_JPQL),
    @NamedQuery(name = Queries.INVESTIGATION_LIST_BY_SURNAME, query= Queries.INVESTIGATIONS_LIST_BY_SURNAME_JPQL),
    @NamedQuery(name = Queries.INVESTIGATION_LIST_BY_USERID, query= Queries.INVESTIGATION_LIST_BY_USERID_JPQL),
    @NamedQuery(name = Queries.INVESTIGATIONS_FOR_USER, query = Queries.INVESTIGATIONS_FOR_USER_JPQL),
    @NamedQuery(name = Queries.INVESTIGATIONS_FOR_USER_RTN_ID, query = Queries.INVESTIGATIONS_FOR_USER_RTN_ID_JPQL)
    
})

@NamedNativeQueries({
    //Added searches for ICAT3 API
    @NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_USERID,  query= Queries.INVESTIGATION_NATIVE_LIST_BY_USERID_SQL,resultSetMapping="investigationMapping"),
    @NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_SURNAME, query= Queries.INVESTIGATIONS_LIST_BY_USER_SURNAME_SQL, resultSetMapping="investigationMapping"),
    @NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID, query= Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD_RTN_ID_SQL, resultSetMapping="investigationIdMapping"),
    //@NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD+"test", query= "SELECT DISTINCT ID, FROM INVESTIGATION where ID = 11915480", resultSetMapping="investigationMapping"),
    @NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD, query= Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD_SQL, resultSetMapping="investigationMapping")
    
})
@SqlResultSetMappings({
    @SqlResultSetMapping(name="investigationMapping",entities={@EntityResult(entityClass=Investigation.class)}),
    @SqlResultSetMapping(name="investigationIdMapping",columns={@ColumnResult(name="ID")})
})
@XmlRootElement
@SequenceGenerator(name="INVESTIGATION_SEQ",sequenceName="INVESTIGATION_ID_SEQ",allocationSize=1)
public class Investigation extends EntityBaseBean implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="INVESTIGATION_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "INV_NUMBER", nullable = false)
    private String invNumber;
    
    @Column(name = "VISIT_ID")
    private String visitId;
    
    @Column(name = "TITLE", nullable = false)
    private String title;
    
    @Column(name = "INV_ABSTRACT")
    private String invAbstract;
    
    @Column(name = "PREV_INV_NUMBER")
    private String prevInvNumber;
    
    @Column(name = "BCAT_INV_STR")
    private String bcatInvStr;
    
    @Column(name = "GRANT_ID")
    private Long grantId;
    
    @Column(name = "RELEASE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date releaseDate;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigationId")
    private Collection<Publication> publicationCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigationId")
    private Collection<Sample> sampleCollection;
    
    @JoinColumn(name = "FACILITY_CYCLE", referencedColumnName = "NAME")
    @ManyToOne
    private FacilityCycle facilityCycle;
    
    @JoinColumn(name = "INSTRUMENT", referencedColumnName = "NAME")
    @ManyToOne
    private Instrument instrument;
    
    @JoinColumn(name = "INV_TYPE", referencedColumnName = "NAME", nullable= false)
    @ManyToOne
    private InvestigationType invType;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigationId")
    private Collection<Dataset> datasetCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<Shift> shiftCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<Keyword> keywordCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<StudyInvestigation> studyInvestigationCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigationId")
    private Collection<InvestigationLevelPermission> investigationLevelPermissionCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<Investigator> investigatorCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigation")
    private Collection<TopicList> topicListCollection;
    
    @XmlTransient
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
    @XmlTransient
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
     * This method is used by JAXWS to map to datasetCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="sampleCollection")
    private Collection<Sample> getSampleCollection_() {
        if(investigationInclude.toString().equals(investigationInclude.SAMPLES_ONLY.toString())){
            return this.sampleCollection;
        } else if(investigationInclude.toString().equals(investigationInclude.ALL.toString())){
            return this.sampleCollection;
        }  else return null;
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
    public Instrument getInstrument() {
        return this.instrument;
    }
    
    /**
     * Sets the instrument of this Investigation to the specified value.
     * @param instrument the new instrument
     */
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
    
    /**
     * Gets the invType of this Investigation.
     * @return the invType
     */
    public InvestigationType getInvType() {
        return this.invType;
    }
    
    /**
     * Sets the invType of this Investigation to the specified value.
     * @param invType the new invType
     */
    public void setInvType(InvestigationType invType) {
        this.invType = invType;
    }
    
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
        if(investigationInclude.toString().equals(investigationInclude.DATASETS_ONLY.toString())){
            return this.datasetCollection;
        } else if(investigationInclude.toString().equals(investigationInclude.ALL.toString())){
            return this.datasetCollection;
        }  else return null;
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
        dataSet.setInvestigationId(this);
        
        Collection<Dataset> datasets = this.getDatasetCollection();
        datasets.add(dataSet);
        
        this.setDatasetCollection(datasets);
        
    }
    
    /**
     * Gets the shiftCollection of this Investigation.
     * @return the shiftCollection
     */
    @XmlTransient
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
        if(investigationInclude.toString().equals(investigationInclude.KEYWORDS_ONLY.toString())){
            return this.keywordCollection;
        } else if(investigationInclude.toString().equals(investigationInclude.ALL.toString())){
            return this.keywordCollection;
        } if(investigationInclude.toString().equals(investigationInclude.INVESTIGATORS_AND_KEYWORDS.toString())){
            return this.keywordCollection;
        }  else return null;
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
        keywords.add(keyword);
        
        this.setKeywordCollection(keywords);
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
     * Gets the investigationLevelPermissionCollection of this Investigation.
     * @return the investigationLevelPermissionCollection
     */
    @XmlTransient
    public Collection<InvestigationLevelPermission> getInvestigationLevelPermissionCollection() {
        return this.investigationLevelPermissionCollection;
    }
    
    /**
     * Sets the investigationLevelPermissionCollection of this Investigation to the specified value.
     * @param investigationLevelPermissionCollection the new investigationLevelPermissionCollection
     */
    public void setInvestigationLevelPermissionCollection(Collection<InvestigationLevelPermission> investigationLevelPermissionCollection) {
        this.investigationLevelPermissionCollection = investigationLevelPermissionCollection;
    }
    
    /**
     * Gets the investigatorCollection of this Investigation.
     * @return the investigatorCollection
     */
    @XmlTransient
    public Collection<Investigator> getInvestigatorCollection() {
        return this.investigatorCollection;
    }
    
    /**
     * This method is used by JAXWS to map to investigatorCollection.  Depending on what the include is
     * set to depends on what is returned to JAXWS and serialised into XML.  This is because without
     * XmlTransient all the collections in the domain model are serialised into XML (meaning alot of
     * DB hits and serialisation).
     */
    @XmlElement(name="investigatorCollection")
    private Collection<Investigator> getInvestigatorCollection_() {
        if(investigationInclude.toString().equals(investigationInclude.INVESTIGATORS_ONLY.toString())){
            return this.investigatorCollection;
            //return null;
        } else if(investigationInclude.toString().equals(investigationInclude.ALL.toString())){
            return this.investigatorCollection;
            //return null;
        } if(investigationInclude.toString().equals(investigationInclude.INVESTIGATORS_AND_KEYWORDS.toString())){
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
     * Sets deleted flag on all items owned by this datasets
     *
     * @param isDeleted
     */
    public void setCascadeDeleted(boolean isDeleted){
        log.trace("Setting: "+toString()+" to deleted? "+isDeleted);
        String deleted = (isDeleted) ? "Y" : "N";
        
        //datafiles
        for(Dataset dataset : getDatasetCollection()){
            dataset.setCascade(Cascade.DELETE, isDeleted);
        }
        
        //investigators
        for(Investigator investigator : getInvestigatorCollection()){
            investigator.setDeleted(deleted);
        }
        
        //access groups
        for(InvestigationLevelPermission investigationLevelPermission : getInvestigationLevelPermissionCollection()){
            investigationLevelPermission.setDeleted(deleted);
            for(AccessGroupIlp agilp : investigationLevelPermission.getAccessGroupIlpCollection()){
                agilp.setDeleted(deleted);
            }
        }
        
        //sample
        for(Sample sample : getSampleCollection()){
            for(SampleParameter sp : sample.getSampleParameterCollection()){
                sp.setDeleted(deleted);
            }
            sample.setDeleted(deleted);
        }
        
        //study
        for(StudyInvestigation study : getStudyInvestigationCollection()){
            study.setDeleted(deleted);
        }
        
        //shift
        for(Shift shift : getShiftCollection()){
            shift.setDeleted(deleted);
        }
        
        //publication
        for(Publication publication : getPublicationCollection()){
            publication.setDeleted(deleted);
        }
        
        //keyword
        for(Keyword keyword : getKeywordCollection()){
            keyword.setDeleted(deleted);
        }
        
        //topicList parameter
        for(TopicList topicList : getTopicListCollection()){
            topicList.setDeleted(deleted);
        }
        
        this.setDeleted(deleted);
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
        return "uk.icat3.entity.Investigation[id=" + id + "]";
    }
    
    /**
     * Method to be overridden if needed to check if the data held in the entity is valid.
     * This method checks whether all the fields which are marked as not null are not null
     *
     * @throws ValidationException if validation error.
     * @return true if validation is correct,
     */
    @Override
    public boolean isValid() throws ValidationException {
        
        //get public the fields in class
        Field[] allFields = this.getClass().getDeclaredFields();
        //all subclasses should use this line below
        //Field[] allFields = getClass().getDeclaredFields();
        outer:
            for (int i = 0; i < allFields.length; i++) {
            //get name of field
            String fieldName = allFields[i].getName();
            
            //check if field is labeled id and generateValue (primary key, then it can be null)
            boolean id = false;
            boolean generateValue = false;
            
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                if(a.annotationType().getName().equals(javax.persistence.Id.class.getName())){
                    id = true;     }
                if(a.annotationType().getName().equals(javax.persistence.GeneratedValue.class.getName())){
                    generateValue = true;
                }
                if(generateValue && id) {
                    log.trace(getClass().getSimpleName()+": "+fieldName+" is auto generated id value, no need to check.");
                    continue outer;
                }
            }
            
            //now check all annoatations
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                //if this means its a none null column field
                if(a.annotationType().getName().contains("Column") && a.toString().contains("nullable=false") ){
                    
                    //now check if it is null, if so throw error
                    try {
                        //get value
                        if(allFields[i].get(this) == null){
                            throw new ValidationException(getClass().getSimpleName()+": "+fieldName+" cannot be null.");
                        } else {
                            log.trace(getClass().getSimpleName()+": "+fieldName+" is valid");
                        }
                    } catch (IllegalAccessException ex) {
                        log.warn(getClass().getSimpleName()+": "+fieldName+" cannot be accessed.");
                    }
                }
            }
            }
        
        //ok here
        return super.isValid();
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
            boolean valid = false;
            if(instrument != null){
                //check instrument is correct.
                Collection<String> instruments = InvestigationSearch.listAllInstruments("null", manager);
                
                for(String instrument : instruments){
                    //log.trace(instrument);
                    if(instrument.equals(getInstrument().getName())) valid = true;
                }
            } else valid = true;
            if(!valid) throw new ValidationException("Investigation: "+getInstrument().getName()+" is not a valid instrument.");
            
            //check all datasets now
            if(getDatasetCollection() != null){
                for(Dataset dataset : getDatasetCollection()){
                    dataset.isValid(manager);
                }
            }
        }
        //check if unique
        if(!isUnique(manager)) throw new ValidationException(this+" is not unique.");
        
        return isValid();
    }
    
    private boolean isUnique(EntityManager manager){
        
        Query query =  manager.createNamedQuery("Investigation.findByUnique");
        query = query.setParameter("invNumber",invNumber);
        query = query.setParameter("visitId", visitId);
        query = query.setParameter("facilityCycle",facilityCycle);
        query = query.setParameter("instrument",instrument);
        
        try {
            Investigation investigation = (Investigation)query.getSingleResult();
            //if found id is this id then it is unique
            if(investigation != null && investigation.getId().equals(id)) return true;
            else return false;
        } catch(NoResultException nre) {
            //means it is unique
            return true;
        }
    }
    
    /**
     * See getInvestigatorCollection_()
     */
    public void setInvestigationInclude(InvestigationInclude investigationInclude) {
        this.investigationInclude = investigationInclude;
    }
}
