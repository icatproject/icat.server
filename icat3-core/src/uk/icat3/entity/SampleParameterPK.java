/*
 * SampleParameterPK.java
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
 * Primary Key class SampleParameterPK for entity class SampleParameter
 * 
 * @author gjd37
 */
@Embeddable
public class SampleParameterPK implements Serializable {

    protected static Logger log = Logger.getLogger(SampleParameterPK.class);
    
    @Column(name = "SAMPLE_ID", nullable = false)
    private Long sampleId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "UNITS", nullable = false)
    private String units;
    
    /** Creates a new instance of SampleParameterPK */
    public SampleParameterPK() {
    }

    /**
     * Creates a new instance of SampleParameterPK with the specified values.
     * @param units the units of the SampleParameterPK
     * @param name the name of the SampleParameterPK
     * @param sampleId the sampleId of the SampleParameterPK
     */
    public SampleParameterPK(String units, String name, Long sampleId) {
        this.units = units;
        this.name = name;
        this.sampleId = sampleId;
    }

    /**
     * Gets the sampleId of this SampleParameterPK.
     * @return the sampleId
     */
    public Long getSampleId() {
        return this.sampleId;
    }

    /**
     * Sets the sampleId of this SampleParameterPK to the specified value.
     * @param sampleId the new sampleId
     */
    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    /**
     * Gets the name of this SampleParameterPK.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this SampleParameterPK to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the units of this SampleParameterPK.
     * @return the units
     */
    public String getUnits() {
        return this.units;
    }

    /**
     * Sets the units of this SampleParameterPK to the specified value.
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
        hash += (this.sampleId != null ? this.sampleId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this SampleParameterPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a SampleParameterPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SampleParameterPK)) {
            return false;
        }
        SampleParameterPK other = (SampleParameterPK)object;
        if (this.units != other.units && (this.units == null || !this.units.equals(other.units))) return false;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) return false;
        if (this.sampleId != other.sampleId && (this.sampleId == null || !this.sampleId.equals(other.sampleId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.SampleParameterPK[units=" + units + ", name=" + name + ", sampleId=" + sampleId + "]";
    }
    
     /**
     * Method to be overridden if needed to check if the data held in the entity is valid.
     * This method checks whether all the fields which are marked as not null are not null
     *
     * @throws ValidationException if validation error.
     * @return true if validation is correct,
     */
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
                if(a.annotationType().getName().equals(
                        javax.persistence.Column.class.getName()) && a.toString().contains("nullable=false") ){
                    
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
          return true;
    }
    
    
}
