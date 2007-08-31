/*
 * ShiftPK.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Primary Key class ShiftPK for entity class Shift
 * 
 * @author gjd37
 */
@Embeddable
public class ShiftPK extends EntityPrimaryKeyBaseBean implements Serializable {

    @Column(name = "INVESTIGATION_ID", nullable = false)
    private Long investigationId;

    @Column(name = "START_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "END_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    
    /** Creates a new instance of ShiftPK */
    public ShiftPK() {
    }

    /**
     * Creates a new instance of ShiftPK with the specified values.
     * @param endDate the endDate of the ShiftPK
     * @param startDate the startDate of the ShiftPK
     * @param investigationId the investigationId of the ShiftPK
     */
    public ShiftPK(Date endDate, Date startDate, Long investigationId) {
        this.endDate = endDate;
        this.startDate = startDate;
        this.investigationId = investigationId;
    }

    /**
     * Gets the investigationId of this ShiftPK.
     * @return the investigationId
     */
    public Long getInvestigationId() {
        return this.investigationId;
    }

    /**
     * Sets the investigationId of this ShiftPK to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    /**
     * Gets the startDate of this ShiftPK.
     * @return the startDate
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Sets the startDate of this ShiftPK to the specified value.
     * @param startDate the new startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the endDate of this ShiftPK.
     * @return the endDate
     */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * Sets the endDate of this ShiftPK to the specified value.
     * @param endDate the new endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.endDate != null ? this.endDate.hashCode() : 0);
        hash += (this.startDate != null ? this.startDate.hashCode() : 0);
        hash += (this.investigationId != null ? this.investigationId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this ShiftPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a ShiftPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ShiftPK)) {
            return false;
        }
        ShiftPK other = (ShiftPK)object;
        if (this.endDate != other.endDate && (this.endDate == null || !this.endDate.equals(other.endDate))) return false;
        if (this.startDate != other.startDate && (this.startDate == null || !this.startDate.equals(other.startDate))) return false;
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
        return "ShiftPK[endDate=" + endDate + ", startDate=" + startDate + ", investigationId=" + investigationId + "]";
    }
    
}
