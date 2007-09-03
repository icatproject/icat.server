/*
 * ParameterPK.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Primary Key class ParameterPK for entity class Parameter
 * 
 * @author gjd37
 */
@Embeddable
public class ParameterPK  extends EntityPrimaryKeyBaseBean implements Serializable {

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "UNITS", nullable = false)
    private String units;
    
    /** Creates a new instance of ParameterPK */
    public ParameterPK() {
    }

    /**
     * Creates a new instance of ParameterPK with the specified values.
     * @param units the units of the ParameterPK
     * @param name the name of the ParameterPK
     */
    public ParameterPK(String units, String name) {
        this.units = units;
        this.name = name;
    }

    /**
     * Gets the name of this ParameterPK.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this ParameterPK to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the units of this ParameterPK.
     * @return the units
     */
    public String getUnits() {
        return this.units;
    }

    /**
     * Sets the units of this ParameterPK to the specified value.
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
        return hash;
    }

    /**
     * Determines whether another object is equal to this ParameterPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a ParameterPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ParameterPK)) {
            return false;
        }
        ParameterPK other = (ParameterPK)object;
        if (this.units != other.units && (this.units == null || !this.units.equals(other.units))) return false;
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
        return "ParameterPK[units=" + units + ", name=" + name + "]";
    }
    
}
