/*
 * KeywordPK.java
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
 * Primary Key class KeywordPK for entity class Keyword
 * 
 * @author gjd37
 */
@Embeddable
public class KeywordPK extends EntityPrimaryKeyBaseBean implements Serializable {

     protected static Logger log = Logger.getLogger(KeywordPK.class);
  
     
    @Column(name = "INVESTIGATION_ID", nullable = false)
    private Long investigationId;

    @Column(name = "NAME", nullable = false)
    private String name;
    
    /** Creates a new instance of KeywordPK */
    public KeywordPK() {
    }

    /**
     * Creates a new instance of KeywordPK with the specified values.
     * @param name the name of the KeywordPK
     * @param investigationId the investigationId of the KeywordPK
     */
    public KeywordPK(String name, Long investigationId) {
        this.name = name;
        this.investigationId = investigationId;
    }

    /**
     * Gets the investigationId of this KeywordPK.
     * @return the investigationId
     */
    public Long getInvestigationId() {
        return this.investigationId;
    }

    /**
     * Sets the investigationId of this KeywordPK to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    /**
     * Gets the name of this KeywordPK.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this KeywordPK to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
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
        hash += (this.investigationId != null ? this.investigationId.hashCode() : 0);
        return hash;
    }

   
    
    /**
     * Determines whether another object is equal to this KeywordPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a KeywordPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof KeywordPK)) {
            return false;
        }
        KeywordPK other = (KeywordPK)object;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) return false;
        if (this.investigationId != other.investigationId && (this.investigationId == null || !this.investigationId.equals(other.investigationId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "KeywordPK[name=" + name + ", investigationId=" + investigationId + "]";
    }
    
}
