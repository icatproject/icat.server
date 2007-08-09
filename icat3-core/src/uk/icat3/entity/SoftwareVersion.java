/*
 * SoftwareVersion.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class SoftwareVersion
 * 
 * @author gjd37
 */
@Entity
@Table(name = "SOFTWARE_VERSION")
@NamedQueries( {
        @NamedQuery(name = "SoftwareVersion.findById", query = "SELECT s FROM SoftwareVersion s WHERE s.id = :id"),
        @NamedQuery(name = "SoftwareVersion.findByName", query = "SELECT s FROM SoftwareVersion s WHERE s.name = :name"),
        @NamedQuery(name = "SoftwareVersion.findBySwVersion", query = "SELECT s FROM SoftwareVersion s WHERE s.swVersion = :swVersion"),
        @NamedQuery(name = "SoftwareVersion.findByFeatures", query = "SELECT s FROM SoftwareVersion s WHERE s.features = :features"),
        @NamedQuery(name = "SoftwareVersion.findByDescription", query = "SELECT s FROM SoftwareVersion s WHERE s.description = :description"),
        @NamedQuery(name = "SoftwareVersion.findByAuthors", query = "SELECT s FROM SoftwareVersion s WHERE s.authors = :authors"),
        @NamedQuery(name = "SoftwareVersion.findByModTime", query = "SELECT s FROM SoftwareVersion s WHERE s.modTime = :modTime"),
        @NamedQuery(name = "SoftwareVersion.findByModId", query = "SELECT s FROM SoftwareVersion s WHERE s.modId = :modId")
    })
public class SoftwareVersion implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SW_VERSION")
    private String swVersion;

    @Column(name = "FEATURES")
    private String features;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "AUTHORS")
    private String authors;

    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;

    @Column(name = "MOD_ID", nullable = false)
    private String modId;
    
    /** Creates a new instance of SoftwareVersion */
    public SoftwareVersion() {
    }

    /**
     * Creates a new instance of SoftwareVersion with the specified values.
     * @param id the id of the SoftwareVersion
     */
    public SoftwareVersion(Long id) {
        this.id = id;
    }

    /**
     * Creates a new instance of SoftwareVersion with the specified values.
     * @param id the id of the SoftwareVersion
     * @param modTime the modTime of the SoftwareVersion
     * @param modId the modId of the SoftwareVersion
     */
    public SoftwareVersion(Long id, Date modTime, String modId) {
        this.id = id;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Gets the id of this SoftwareVersion.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id of this SoftwareVersion to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of this SoftwareVersion.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this SoftwareVersion to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the swVersion of this SoftwareVersion.
     * @return the swVersion
     */
    public String getSwVersion() {
        return this.swVersion;
    }

    /**
     * Sets the swVersion of this SoftwareVersion to the specified value.
     * @param swVersion the new swVersion
     */
    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    /**
     * Gets the features of this SoftwareVersion.
     * @return the features
     */
    public String getFeatures() {
        return this.features;
    }

    /**
     * Sets the features of this SoftwareVersion to the specified value.
     * @param features the new features
     */
    public void setFeatures(String features) {
        this.features = features;
    }

    /**
     * Gets the description of this SoftwareVersion.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this SoftwareVersion to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the authors of this SoftwareVersion.
     * @return the authors
     */
    public String getAuthors() {
        return this.authors;
    }

    /**
     * Sets the authors of this SoftwareVersion to the specified value.
     * @param authors the new authors
     */
    public void setAuthors(String authors) {
        this.authors = authors;
    }

    /**
     * Gets the modTime of this SoftwareVersion.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }

    /**
     * Sets the modTime of this SoftwareVersion to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    /**
     * Gets the modId of this SoftwareVersion.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }

    /**
     * Sets the modId of this SoftwareVersion to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
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
     * Determines whether another object is equal to this SoftwareVersion.  The result is 
     * <code>true</code> if and only if the argument is not null and is a SoftwareVersion object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SoftwareVersion)) {
            return false;
        }
        SoftwareVersion other = (SoftwareVersion)object;
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
        return "SoftwareVersion[id=" + id + "]";
    }
    
}
