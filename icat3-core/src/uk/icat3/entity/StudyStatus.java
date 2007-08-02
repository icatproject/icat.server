/*
 * StudyStatus.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;

/**
 * Entity class StudyStatus
 * 
 * @author gjd37
 */
@Entity
@Table(name = "STUDY_STATUS")
@NamedQueries( {
        @NamedQuery(name = "StudyStatus.findByName", query = "SELECT s FROM StudyStatus s WHERE s.name = :name"),
        @NamedQuery(name = "StudyStatus.findByDescription", query = "SELECT s FROM StudyStatus s WHERE s.description = :description"),
        @NamedQuery(name = "StudyStatus.findByModTime", query = "SELECT s FROM StudyStatus s WHERE s.modTime = :modTime"),
        @NamedQuery(name = "StudyStatus.findByModId", query = "SELECT s FROM StudyStatus s WHERE s.modId = :modId")
    })
public class StudyStatus implements Serializable {

    @Id
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;

    @Column(name = "MOD_ID", nullable = false)
    private String modId;

    @OneToMany(mappedBy = "status")
    @XmlTransient
    private Collection<Study> studyCollection;
    
    /** Creates a new instance of StudyStatus */
    public StudyStatus() {
    }

    /**
     * Creates a new instance of StudyStatus with the specified values.
     * @param name the name of the StudyStatus
     */
    public StudyStatus(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of StudyStatus with the specified values.
     * @param name the name of the StudyStatus
     * @param description the description of the StudyStatus
     * @param modTime the modTime of the StudyStatus
     * @param modId the modId of the StudyStatus
     */
    public StudyStatus(String name, String description, Date modTime, String modId) {
        this.name = name;
        this.description = description;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Gets the name of this StudyStatus.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this StudyStatus to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of this StudyStatus.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this StudyStatus to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the modTime of this StudyStatus.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }

    /**
     * Sets the modTime of this StudyStatus to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    /**
     * Gets the modId of this StudyStatus.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }

    /**
     * Sets the modId of this StudyStatus to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }

    /**
     * Gets the studyCollection of this StudyStatus.
     * @return the studyCollection
     */
    @XmlTransient
    public Collection<Study> getStudyCollection() {
        return this.studyCollection;
    }

    /**
     * Sets the studyCollection of this StudyStatus to the specified value.
     * @param studyCollection the new studyCollection
     */
    public void setStudyCollection(Collection<Study> studyCollection) {
        this.studyCollection = studyCollection;
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
        hash += (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this StudyStatus.  The result is 
     * <code>true</code> if and only if the argument is not null and is a StudyStatus object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StudyStatus)) {
            return false;
        }
        StudyStatus other = (StudyStatus)object;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "StudyStatus[name=" + name + "]";
    }
    
}
