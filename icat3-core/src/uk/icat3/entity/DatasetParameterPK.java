/*
 * DatasetParameterPK.java
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
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.ValidationException;

/**
 * Primary Key class DatasetParameterPK for entity class DatasetParameter
 * 
 * @author gjd37
 */
@Embeddable
public class DatasetParameterPK extends EntityPrimaryKeyBaseBean implements Serializable {

    protected static Logger log = Logger.getLogger(DatasetParameterPK.class);
    
    @Column(name = "DATASET_ID", nullable = false)
    private Long datasetId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "UNITS", nullable = false)
    private String units;
    
    /** Creates a new instance of DatasetParameterPK */
    public DatasetParameterPK() {
    }

    /**
     * Creates a new instance of DatasetParameterPK with the specified values.
     * @param units the units of the DatasetParameterPK
     * @param name the name of the DatasetParameterPK
     * @param datasetId the datasetId of the DatasetParameterPK
     */
    public DatasetParameterPK(String units, String name, Long datasetId) {
        this.units = units;
        this.name = name;
        this.datasetId = datasetId;
    }

    /**
     * Gets the datasetId of this DatasetParameterPK.
     * @return the datasetId
     */
    public Long getDatasetId() {
        return this.datasetId;
    }

    /**
     * Sets the datasetId of this DatasetParameterPK to the specified value.
     * @param datasetId the new datasetId
     */
    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Gets the name of this DatasetParameterPK.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this DatasetParameterPK to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the units of this DatasetParameterPK.
     * @return the units
     */
    public String getUnits() {
        return this.units;
    }

    /**
     * Sets the units of this DatasetParameterPK to the specified value.
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
        hash += (this.datasetId != null ? this.datasetId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this DatasetParameterPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a DatasetParameterPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatasetParameterPK)) {
            return false;
        }
        DatasetParameterPK other = (DatasetParameterPK)object;
        if (this.units != other.units && (this.units == null || !this.units.equals(other.units))) return false;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) return false;
        if (this.datasetId != other.datasetId && (this.datasetId == null || !this.datasetId.equals(other.datasetId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "DatasetParameterPK[units=" + units + ", name=" + name + ", datasetId=" + datasetId + "]";
    }
    
}
