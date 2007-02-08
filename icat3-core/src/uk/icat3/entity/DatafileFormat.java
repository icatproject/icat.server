/*
 * DatafileFormat.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class DatafileFormat
 * 
 * @author gjd37
 */
@Entity
@Table(name = "DATAFILE_FORMAT")
@NamedQueries( {
        @NamedQuery(name = "DatafileFormat.findByName", query = "SELECT d FROM DatafileFormat d WHERE d.datafileFormatPK.name = :name"),
        @NamedQuery(name = "DatafileFormat.findByVersion", query = "SELECT d FROM DatafileFormat d WHERE d.datafileFormatPK.version = :version"),
        @NamedQuery(name = "DatafileFormat.findByFormatType", query = "SELECT d FROM DatafileFormat d WHERE d.formatType = :formatType"),
        @NamedQuery(name = "DatafileFormat.findByDescription", query = "SELECT d FROM DatafileFormat d WHERE d.description = :description"),
        @NamedQuery(name = "DatafileFormat.findByModTime", query = "SELECT d FROM DatafileFormat d WHERE d.modTime = :modTime"),
        @NamedQuery(name = "DatafileFormat.findByModId", query = "SELECT d FROM DatafileFormat d WHERE d.modId = :modId")
    })
public class DatafileFormat extends EntityBaseBean implements Serializable {

    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected DatafileFormatPK datafileFormatPK;

    @Column(name = "FORMAT_TYPE")
    private String formatType;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "MOD_ID", nullable = false)
    private String modId;

    @OneToMany(mappedBy = "datafileFormat")
    private Collection<Datafile> datafileCollection;
    
    /** Creates a new instance of DatafileFormat */
    public DatafileFormat() {
    }

    /**
     * Creates a new instance of DatafileFormat with the specified values.
     * @param datafileFormatPK the datafileFormatPK of the DatafileFormat
     */
    public DatafileFormat(DatafileFormatPK datafileFormatPK) {
        this.datafileFormatPK = datafileFormatPK;
    }

    /**
     * Creates a new instance of DatafileFormat with the specified values.
     * @param datafileFormatPK the datafileFormatPK of the DatafileFormat
     * @param modTime the modTime of the DatafileFormat
     * @param modId the modId of the DatafileFormat
     */
    public DatafileFormat(DatafileFormatPK datafileFormatPK, Date modTime, String modId) {
        this.datafileFormatPK = datafileFormatPK;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Creates a new instance of DatafileFormatPK with the specified values.
     * @param version the version of the DatafileFormatPK
     * @param name the name of the DatafileFormatPK
     */
    public DatafileFormat(String version, String name) {
        this.datafileFormatPK = new DatafileFormatPK(version, name);
    }

    /**
     * Gets the datafileFormatPK of this DatafileFormat.
     * @return the datafileFormatPK
     */
    public DatafileFormatPK getDatafileFormatPK() {
        return this.datafileFormatPK;
    }

    /**
     * Sets the datafileFormatPK of this DatafileFormat to the specified value.
     * @param datafileFormatPK the new datafileFormatPK
     */
    public void setDatafileFormatPK(DatafileFormatPK datafileFormatPK) {
        this.datafileFormatPK = datafileFormatPK;
    }

    /**
     * Gets the formatType of this DatafileFormat.
     * @return the formatType
     */
    public String getFormatType() {
        return this.formatType;
    }

    /**
     * Sets the formatType of this DatafileFormat to the specified value.
     * @param formatType the new formatType
     */
    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    /**
     * Gets the description of this DatafileFormat.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this DatafileFormat to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    
    /**
     * Gets the modId of this DatafileFormat.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }

    /**
     * Sets the modId of this DatafileFormat to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }

    /**
     * Gets the datafileCollection of this DatafileFormat.
     * @return the datafileCollection
     */
    public Collection<Datafile> getDatafileCollection() {
        return this.datafileCollection;
    }

    /**
     * Sets the datafileCollection of this DatafileFormat to the specified value.
     * @param datafileCollection the new datafileCollection
     */
    public void setDatafileCollection(Collection<Datafile> datafileCollection) {
        this.datafileCollection = datafileCollection;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.datafileFormatPK != null ? this.datafileFormatPK.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this DatafileFormat.  The result is 
     * <code>true</code> if and only if the argument is not null and is a DatafileFormat object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatafileFormat)) {
            return false;
        }
        DatafileFormat other = (DatafileFormat)object;
        if (this.datafileFormatPK != other.datafileFormatPK && (this.datafileFormatPK == null || !this.datafileFormatPK.equals(other.datafileFormatPK))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.DatafileFormat[datafileFormatPK=" + datafileFormatPK + "]";
    }
    
}
