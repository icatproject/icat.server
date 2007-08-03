/*
 * DatafileFormatPK.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Primary Key class DatafileFormatPK for entity class DatafileFormat
 * 
 * @author gjd37
 */
@Embeddable
public class DatafileFormatPK extends EntityPrimaryKeyBaseBean implements Serializable {

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "VERSION", nullable = false)
    private String version;
    
    /** Creates a new instance of DatafileFormatPK */
    public DatafileFormatPK() {
    }

    /**
     * Creates a new instance of DatafileFormatPK with the specified values.
     * @param version the version of the DatafileFormatPK
     * @param name the name of the DatafileFormatPK
     */
    public DatafileFormatPK(String version, String name) {
        this.version = version;
        this.name = name;
    }

    /**
     * Gets the name of this DatafileFormatPK.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this DatafileFormatPK to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the version of this DatafileFormatPK.
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version of this DatafileFormatPK to the specified value.
     * @param version the new version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.version != null ? this.version.hashCode() : 0);
        hash += (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this DatafileFormatPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a DatafileFormatPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatafileFormatPK)) {
            return false;
        }
        DatafileFormatPK other = (DatafileFormatPK)object;
        if (this.version != other.version && (this.version == null || !this.version.equals(other.version))) return false;
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
        return "DatafileFormatPK[version=" + version + ", name=" + name + "]";
    }
    
}
