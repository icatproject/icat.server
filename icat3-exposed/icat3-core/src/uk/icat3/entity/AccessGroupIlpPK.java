/*
 * AccessGroupIlpPK.java
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
 * Primary Key class AccessGroupIlpPK for entity class AccessGroupIlp
 * 
 * @author gjd37
 */
@Embeddable
public class AccessGroupIlpPK implements Serializable {

    @Column(name = "ACCESS_GROUP_ID", nullable = false)
    private BigInteger accessGroupId;

    @Column(name = "ILP_ID", nullable = false)
    private BigInteger ilpId;
    
    /** Creates a new instance of AccessGroupIlpPK */
    public AccessGroupIlpPK() {
    }

    /**
     * Creates a new instance of AccessGroupIlpPK with the specified values.
     * @param ilpId the ilpId of the AccessGroupIlpPK
     * @param accessGroupId the accessGroupId of the AccessGroupIlpPK
     */
    public AccessGroupIlpPK(BigInteger ilpId, BigInteger accessGroupId) {
        this.ilpId = ilpId;
        this.accessGroupId = accessGroupId;
    }

    /**
     * Gets the accessGroupId of this AccessGroupIlpPK.
     * @return the accessGroupId
     */
    public BigInteger getAccessGroupId() {
        return this.accessGroupId;
    }

    /**
     * Sets the accessGroupId of this AccessGroupIlpPK to the specified value.
     * @param accessGroupId the new accessGroupId
     */
    public void setAccessGroupId(BigInteger accessGroupId) {
        this.accessGroupId = accessGroupId;
    }

    /**
     * Gets the ilpId of this AccessGroupIlpPK.
     * @return the ilpId
     */
    public BigInteger getIlpId() {
        return this.ilpId;
    }

    /**
     * Sets the ilpId of this AccessGroupIlpPK to the specified value.
     * @param ilpId the new ilpId
     */
    public void setIlpId(BigInteger ilpId) {
        this.ilpId = ilpId;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.ilpId != null ? this.ilpId.hashCode() : 0);
        hash += (this.accessGroupId != null ? this.accessGroupId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this AccessGroupIlpPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a AccessGroupIlpPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccessGroupIlpPK)) {
            return false;
        }
        AccessGroupIlpPK other = (AccessGroupIlpPK)object;
        if (this.ilpId != other.ilpId && (this.ilpId == null || !this.ilpId.equals(other.ilpId))) return false;
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
        return "uk.icat3.entity.AccessGroupIlpPK[ilpId=" + ilpId + ", accessGroupId=" + accessGroupId + "]";
    }
    
}
