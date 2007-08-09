/*
 * InvestigatorPK.java
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
import org.apache.log4j.Logger;

/**
 * Primary Key class InvestigatorPK for entity class Investigator
 *
 * @author gjd37
 */
@Embeddable
public class InvestigatorPK extends EntityPrimaryKeyBaseBean implements Serializable {
    
    protected static Logger log = Logger.getLogger(InvestigatorPK.class);
    
    @Column(name = "INVESTIGATION_ID", nullable = false)
    private Long investigationId;
    
    @Column(name = "FACILITY_USER_ID", nullable = false)
    private String facilityUserId;
    
    /** Creates a new instance of InvestigatorPK */
    public InvestigatorPK() {
    }
    
    /**
     * Creates a new instance of InvestigatorPK with the specified values.
     * @param facilityUserId the facilityUserId of the InvestigatorPK
     * @param investigationId the investigationId of the InvestigatorPK
     */
    public InvestigatorPK(String facilityUserId, Long investigationId) {
        this.facilityUserId = facilityUserId;
        this.investigationId = investigationId;
    }
    
    /**
     * Gets the investigationId of this InvestigatorPK.
     * @return the investigationId
     */
    public Long getInvestigationId() {
        return this.investigationId;
    }
    
    /**
     * Sets the investigationId of this InvestigatorPK to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }
    
    /**
     * Gets the facilityUserId of this InvestigatorPK.
     * @return the facilityUserId
     */
    public String getFacilityUserId() {
        return this.facilityUserId;
    }
    
    /**
     * Sets the facilityUserId of this InvestigatorPK to the specified value.
     * @param facilityUserId the new facilityUserId
     */
    public void setFacilityUserId(String facilityUserId) {
        this.facilityUserId = facilityUserId;
    }
    
    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.facilityUserId != null ? this.facilityUserId.hashCode() : 0);
        hash += (this.investigationId != null ? this.investigationId.hashCode() : 0);
        return hash;
    }
           
    /**
     * Determines whether another object is equal to this InvestigatorPK.  The result is
     * <code>true</code> if and only if the argument is not null and is a InvestigatorPK object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InvestigatorPK)) {
            return false;
        }
        InvestigatorPK other = (InvestigatorPK)object;
        if (this.facilityUserId != other.facilityUserId && (this.facilityUserId == null || !this.facilityUserId.equals(other.facilityUserId))) return false;
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
        return "InvestigatorPK[facilityUserId=" + facilityUserId + ", investigationId=" + investigationId + "]";
    }
    
}
