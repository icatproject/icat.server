/*
 * Study.java
 *
 * Created on 08 February 2007, 10:04
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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import uk.icat3.util.ElementType;

/**
 * Entity class Study
 * 
 * @author gjd37
 */
@Entity
@Table(name = "STUDY")
@NamedQueries( {
        @NamedQuery(name = "Study.findById", query = "SELECT s FROM Study s WHERE s.id = :id"),
        @NamedQuery(name = "Study.findByName", query = "SELECT s FROM Study s WHERE s.name = :name"),
        @NamedQuery(name = "Study.findByPurpose", query = "SELECT s FROM Study s WHERE s.purpose = :purpose"),
        @NamedQuery(name = "Study.findByRelatedMaterial", query = "SELECT s FROM Study s WHERE s.relatedMaterial = :relatedMaterial"),
        @NamedQuery(name = "Study.findByStudyCreationDate", query = "SELECT s FROM Study s WHERE s.studyCreationDate = :studyCreationDate"),
        @NamedQuery(name = "Study.findByStudyManager", query = "SELECT s FROM Study s WHERE s.studyManager = :studyManager"),
        @NamedQuery(name = "Study.findByModTime", query = "SELECT s FROM Study s WHERE s.modTime = :modTime"),
        @NamedQuery(name = "Study.findByModId", query = "SELECT s FROM Study s WHERE s.modId = :modId")
    })
public class Study extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "PURPOSE")
    private String purpose;

    @Column(name = "RELATED_MATERIAL")
    private String relatedMaterial;

    @Column(name = "STUDY_CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date studyCreationDate;

    @Column(name = "STUDY_MANAGER")
    private BigInteger studyManager;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "study")
    private Collection<StudyInvestigation> studyInvestigationCollection;

    @JoinColumn(name = "STATUS", referencedColumnName = "NAME")
    @ManyToOne
    private StudyStatus status;
    
    /** Creates a new instance of Study */
    public Study() {
    }

    /**
     * Creates a new instance of Study with the specified values.
     * @param id the id of the Study
     */
    public Study(Long id) {
        this.id = id;
    }

    /**
     * Creates a new instance of Study with the specified values.
     * @param id the id of the Study
     * @param name the name of the Study
     * @param modTime the modTime of the Study
     * @param modId the modId of the Study
     */
    public Study(Long id, String name, Date modTime, String modId) {
        this.id = id;
        this.name = name;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Gets the id of this Study.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id of this Study to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of this Study.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this Study to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the purpose of this Study.
     * @return the purpose
     */
    public String getPurpose() {
        return this.purpose;
    }

    /**
     * Sets the purpose of this Study to the specified value.
     * @param purpose the new purpose
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * Gets the relatedMaterial of this Study.
     * @return the relatedMaterial
     */
    public String getRelatedMaterial() {
        return this.relatedMaterial;
    }

    /**
     * Sets the relatedMaterial of this Study to the specified value.
     * @param relatedMaterial the new relatedMaterial
     */
    public void setRelatedMaterial(String relatedMaterial) {
        this.relatedMaterial = relatedMaterial;
    }

    /**
     * Gets the studyCreationDate of this Study.
     * @return the studyCreationDate
     */
    public Date getStudyCreationDate() {
        return this.studyCreationDate;
    }

    /**
     * Sets the studyCreationDate of this Study to the specified value.
     * @param studyCreationDate the new studyCreationDate
     */
    public void setStudyCreationDate(Date studyCreationDate) {
        this.studyCreationDate = studyCreationDate;
    }

    /**
     * Gets the studyManager of this Study.
     * @return the studyManager
     */
    public BigInteger getStudyManager() {
        return this.studyManager;
    }

    /**
     * Sets the studyManager of this Study to the specified value.
     * @param studyManager the new studyManager
     */
    public void setStudyManager(BigInteger studyManager) {
        this.studyManager = studyManager;
    }

        /**
     * Sets the modId of this Study to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }

    /**
     * Gets the studyInvestigationCollection of this Study.
     * @return the studyInvestigationCollection
     */
    public Collection<StudyInvestigation> getStudyInvestigationCollection() {
        return this.studyInvestigationCollection;
    }

    /**
     * Sets the studyInvestigationCollection of this Study to the specified value.
     * @param studyInvestigationCollection the new studyInvestigationCollection
     */
    public void setStudyInvestigationCollection(Collection<StudyInvestigation> studyInvestigationCollection) {
        this.studyInvestigationCollection = studyInvestigationCollection;
    }

    /**
     * Gets the status of this Study.
     * @return the status
     */
    public StudyStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the status of this Study to the specified value.
     * @param status the new status
     */
    public void setStatus(StudyStatus status) {
        this.status = status;
    }

    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.STUDY;
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
     * Determines whether another object is equal to this Study.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Study object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Study)) {
            return false;
        }
        Study other = (Study)object;
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
        return "Study[id=" + id + "]";
    }
    
}
