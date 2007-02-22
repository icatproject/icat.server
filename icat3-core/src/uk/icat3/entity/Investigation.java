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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import javax.management.Query;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import uk.icat3.util.Queries;

/**
 * Entity class Investigation
 *
 * @author gjd37
 */
@Entity
@Table(name = "INVESTIGATION")
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
    
    //Added searches for ICAT3 API
    // @NamedQuery(name = Queries.INVESTIGATIONS_BY_KEYWORD, query ="SELECT  FROM  (SELECT Investigation i FROM i WHERE i.investigatorCollection.investigatorPK.facilityUserId = :userId) ")
    @NamedQuery(name = Queries.ADVANCED_SEARCH, query = Queries.ADVANCED_SEARCH_JPQL),
    @NamedQuery(name = Queries.INVESTIGATIONS_BY_USER, query = Queries.INVESTIGATIONS_BY_USER_JPQL)
    
    
})

@NamedNativeQueries({
    //Added searches for ICAT3 API
    @NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_USERID,  query= Queries.INVESTIGATION_NATIVE_LIST_BY_USERID_SQL,resultSetMapping="investigationMapping"),
    @NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_SURNAME, query= Queries.INVESTIGATIONS_BY_USER_SURNAME_SQL, resultSetMapping="investigationMapping"),
    @NamedNativeQuery(name = Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD, query= Queries.INVESTIGATION_NATIVE_LIST_BY_KEYWORD_SQL, resultSetMapping="investigationMapping")
    
})
@SqlResultSetMappings({
    @SqlResultSetMapping(name="investigationMapping",entities={@EntityResult(entityClass=Investigation.class)}),
    @SqlResultSetMapping(name="investigationIdMappingLong",entities={@EntityResult(entityClass=Long.class)})
    
})

public class Investigation extends EntityBaseBean implements Serializable {
    
    @Id
    @Column(name = "ID", nullable = false)
    private BigDecimal id;
    
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
    private BigInteger grantId;
    
    @Column(name = "RELEASE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date releaseDate;
    
    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;
    
    @Column(name = "MOD_ID", nullable = false)
    private String modId;
    
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
    
    @JoinColumn(name = "INV_TYPE", referencedColumnName = "NAME")
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
    
    /** Creates a new instance of Investigation */
    public Investigation() {
    }
    
    /**
     * Creates a new instance of Investigation with the specified values.
     * @param id the id of the Investigation
     */
    public Investigation(BigDecimal id) {
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
    public Investigation(BigDecimal id, String invNumber, String title, Date modTime, String modId) {
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
    public BigDecimal getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this Investigation to the specified value.
     * @param id the new id
     */
    public void setId(BigDecimal id) {
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
    public BigInteger getGrantId() {
        return this.grantId;
    }
    
    /**
     * Sets the grantId of this Investigation to the specified value.
     * @param grantId the new grantId
     */
    public void setGrantId(BigInteger grantId) {
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
     * Gets the modTime of this Investigation.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }
    
    /**
     * Sets the modTime of this Investigation to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }
    
    /**
     * Gets the modId of this Investigation.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }
    
    /**
     * Sets the modId of this Investigation to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }
    
    /**
     * Gets the publicationCollection of this Investigation.
     * @return the publicationCollection
     */
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
    public Collection<Sample> getSampleCollection() {
        return this.sampleCollection;
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
    public Collection<Dataset> getDatasetCollection() {
        return this.datasetCollection;
    }
    
    /**
     * Sets the datasetCollection of this Investigation to the specified value.
     * @param datasetCollection the new datasetCollection
     */
    public void setDatasetCollection(Collection<Dataset> datasetCollection) {
        this.datasetCollection = datasetCollection;
    }
    
    /**
     * Gets the shiftCollection of this Investigation.
     * @return the shiftCollection
     */
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
    public Collection<Keyword> getKeywordCollection() {
        return this.keywordCollection;
    }
    
    /**
     * Sets the keywordCollection of this Investigation to the specified value.
     * @param keywordCollection the new keywordCollection
     */
    public void setKeywordCollection(Collection<Keyword> keywordCollection) {
        this.keywordCollection = keywordCollection;
    }
    
    /**
     * Gets the studyInvestigationCollection of this Investigation.
     * @return the studyInvestigationCollection
     */
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
    public Collection<Investigator> getInvestigatorCollection() {
        return this.investigatorCollection;
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
    
}
