/*
 * AccessGroupDlpPK.java
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
 * Primary Key class AccessGroupDlpPK for entity class AccessGroupDlp
 * 
 * @author gjd37
 */
@Embeddable
public class AccessGroupDlpPK implements Serializable {

    @Column(name = "ACCESS_GROUP_ID", nullable = false)
    private Long accessGroupId;

    @Column(name = "DLP_ID", nullable = false)
    private Long dlpId;
    
    /** Creates a new instance of AccessGroupDlpPK */
    public AccessGroupDlpPK() {
    }

    /**
     * Creates a new instance of AccessGroupDlpPK with the specified values.
     * @param dlpId the dlpId of the AccessGroupDlpPK
     * @param accessGroupId the accessGroupId of the AccessGroupDlpPK
     */
    public AccessGroupDlpPK(Long dlpId, Long accessGroupId) {
        this.dlpId = dlpId;
        this.accessGroupId = accessGroupId;
    }

    /**
     * Gets the accessGroupId of this AccessGroupDlpPK.
     * @return the accessGroupId
     */
    public Long getAccessGroupId() {
        return this.accessGroupId;
    }

    /**
     * Sets the accessGroupId of this AccessGroupDlpPK to the specified value.
     * @param accessGroupId the new accessGroupId
     */
    public void setAccessGroupId(Long accessGroupId) {
        this.accessGroupId = accessGroupId;
    }

    /**
     * Gets the dlpId of this AccessGroupDlpPK.
     * @return the dlpId
     */
    public Long getDlpId() {
        return this.dlpId;
    }

    /**
     * Sets the dlpId of this AccessGroupDlpPK to the specified value.
     * @param dlpId the new dlpId
     */
    public void setDlpId(Long dlpId) {
        this.dlpId = dlpId;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.dlpId != null ? this.dlpId.hashCode() : 0);
        hash += (this.accessGroupId != null ? this.accessGroupId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this AccessGroupDlpPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a AccessGroupDlpPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccessGroupDlpPK)) {
            return false;
        }
        AccessGroupDlpPK other = (AccessGroupDlpPK)object;
        if (this.dlpId != other.dlpId && (this.dlpId == null || !this.dlpId.equals(other.dlpId))) return false;
        if (this.accessGroupId != other.accessGroupId && (this.accessGroupId == null || !this.accessGroupId.equals(other.accessGroupId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.AccessGroupDlpPK[dlpId=" + dlpId + ", accessGroupId=" + accessGroupId + "]";
    }
    
}
