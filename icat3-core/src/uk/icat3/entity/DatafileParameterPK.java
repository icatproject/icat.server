/*
 * DatafileParameterPK.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.ValidationException;

/**
 * Primary Key class DatafileParameterPK for entity class DatafileParameter
 *
 * @author gjd37
 */
@Embeddable
public class DatafileParameterPK extends EntityPrimaryKeyBaseBean implements Serializable {
    
    protected static Logger log = Logger.getLogger(DatafileParameterPK.class);
    
    
    @Column(name = "DATAFILE_ID", nullable = false)
    private Long datafileId;
    
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "UNITS", nullable = false)
    private String units;
    
    /** Creates a new instance of DatafileParameterPK */
    public DatafileParameterPK() {
    }
    
    /**
     * Creates a new instance of DatafileParameterPK with the specified values.
     * @param units the units of the DatafileParameterPK
     * @param name the name of the DatafileParameterPK
     * @param datafileId the datafileId of the DatafileParameterPK
     */
    public DatafileParameterPK(String units, String name, Long datafileId) {
        this.units = units;
        this.name = name;
        this.datafileId = datafileId;
    }
    
    /**
     * Gets the datafileId of this DatafileParameterPK.
     * @return the datafileId
     */
    public Long getDatafileId() {
        return this.datafileId;
    }
    
    /**
     * Sets the datafileId of this DatafileParameterPK to the specified value.
     * @param datafileId the new datafileId
     */
    public void setDatafileId(Long datafileId) {
        this.datafileId = datafileId;
    }
    
    /**
     * Gets the name of this DatafileParameterPK.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this DatafileParameterPK to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the units of this DatafileParameterPK.
     * @return the units
     */
    public String getUnits() {
        return this.units;
    }
    
    /**
     * Sets the units of this DatafileParameterPK to the specified value.
     * @param units the new units
     */
    public void setUnits(String units) {
        this.units = units;
    }
    
    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.units != null ? this.units.hashCode() : 0);
        hash += (this.name != null ? this.name.hashCode() : 0);
        hash += (this.datafileId != null ? this.datafileId.hashCode() : 0);
        return hash;
    }
    
    /**
     * Determines whether another object is equal to this DatafileParameterPK.  The result is
     * <code>true</code> if and only if the argument is not null and is a DatafileParameterPK object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatafileParameterPK)) {
            return false;
        }
        DatafileParameterPK other = (DatafileParameterPK)object;
        if (this.units != other.units && (this.units == null || !this.units.equals(other.units))) return false;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) return false;
        if (this.datafileId != other.datafileId && (this.datafileId == null || !this.datafileId.equals(other.datafileId))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "DatafileParameterPK[units=" + units + ", name=" + name + ", datafileId=" + datafileId + "]";
    }
           
}
