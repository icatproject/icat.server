/*
 * RelatedDatafilesPK.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Primary Key class RelatedDatafilesPK for entity class RelatedDatafiles
 * 
 * @author gjd37
 */
@Embeddable
public class RelatedDatafilesPK  extends EntityPrimaryKeyBaseBean implements Serializable {

    @Column(name = "SOURCE_DATAFILE_ID", nullable = false)
    private Long sourceDatafileId;

    @Column(name = "DEST_DATAFILE_ID", nullable = false)
    private Long destDatafileId;
    
    /** Creates a new instance of RelatedDatafilesPK */
    public RelatedDatafilesPK() {
    }

    /**
     * Creates a new instance of RelatedDatafilesPK with the specified values.
     * @param destDatafileId the destDatafileId of the RelatedDatafilesPK
     * @param sourceDatafileId the sourceDatafileId of the RelatedDatafilesPK
     */
    public RelatedDatafilesPK(Long destDatafileId, Long sourceDatafileId) {
        this.destDatafileId = destDatafileId;
        this.sourceDatafileId = sourceDatafileId;
    }

    /**
     * Gets the sourceDatafileId of this RelatedDatafilesPK.
     * @return the sourceDatafileId
     */
    public Long getSourceDatafileId() {
        return this.sourceDatafileId;
    }

    /**
     * Sets the sourceDatafileId of this RelatedDatafilesPK to the specified value.
     * @param sourceDatafileId the new sourceDatafileId
     */
    public void setSourceDatafileId(Long sourceDatafileId) {
        this.sourceDatafileId = sourceDatafileId;
    }

    /**
     * Gets the destDatafileId of this RelatedDatafilesPK.
     * @return the destDatafileId
     */
    public Long getDestDatafileId() {
        return this.destDatafileId;
    }

    /**
     * Sets the destDatafileId of this RelatedDatafilesPK to the specified value.
     * @param destDatafileId the new destDatafileId
     */
    public void setDestDatafileId(Long destDatafileId) {
        this.destDatafileId = destDatafileId;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.destDatafileId != null ? this.destDatafileId.hashCode() : 0);
        hash += (this.sourceDatafileId != null ? this.sourceDatafileId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this RelatedDatafilesPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a RelatedDatafilesPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RelatedDatafilesPK)) {
            return false;
        }
        RelatedDatafilesPK other = (RelatedDatafilesPK)object;
        if (this.destDatafileId != other.destDatafileId && (this.destDatafileId == null || !this.destDatafileId.equals(other.destDatafileId))) return false;
        if (this.sourceDatafileId != other.sourceDatafileId && (this.sourceDatafileId == null || !this.sourceDatafileId.equals(other.sourceDatafileId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "RelatedDatafilesPK[destDatafileId=" + destDatafileId + ", sourceDatafileId=" + sourceDatafileId + "]";
    }
    
}
