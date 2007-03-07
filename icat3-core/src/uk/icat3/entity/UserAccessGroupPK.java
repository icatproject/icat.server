/*
 * UserAccessGroupPK.java
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
 * Primary Key class UserAccessGroupPK for entity class UserAccessGroup
 * 
 * @author gjd37
 */
@Embeddable
public class UserAccessGroupPK implements Serializable {

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "ACCESS_GROUP_ID", nullable = false)
    private Long accessGroupId;
    
    /** Creates a new instance of UserAccessGroupPK */
    public UserAccessGroupPK() {
    }

    /**
     * Creates a new instance of UserAccessGroupPK with the specified values.
     * @param accessGroupId the accessGroupId of the UserAccessGroupPK
     * @param userId the userId of the UserAccessGroupPK
     */
    public UserAccessGroupPK(Long accessGroupId, String userId) {
        this.accessGroupId = accessGroupId;
        this.userId = userId;
    }

    /**
     * Gets the userId of this UserAccessGroupPK.
     * @return the userId
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Sets the userId of this UserAccessGroupPK to the specified value.
     * @param userId the new userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the accessGroupId of this UserAccessGroupPK.
     * @return the accessGroupId
     */
    public Long getAccessGroupId() {
        return this.accessGroupId;
    }

    /**
     * Sets the accessGroupId of this UserAccessGroupPK to the specified value.
     * @param accessGroupId the new accessGroupId
     */
    public void setAccessGroupId(Long accessGroupId) {
        this.accessGroupId = accessGroupId;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.accessGroupId != null ? this.accessGroupId.hashCode() : 0);
        hash += (this.userId != null ? this.userId.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this UserAccessGroupPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a UserAccessGroupPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserAccessGroupPK)) {
            return false;
        }
        UserAccessGroupPK other = (UserAccessGroupPK)object;
        if (this.accessGroupId != other.accessGroupId && (this.accessGroupId == null || !this.accessGroupId.equals(other.accessGroupId))) return false;
        if (this.userId != other.userId && (this.userId == null || !this.userId.equals(other.userId))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.UserAccessGroupPK[accessGroupId=" + accessGroupId + ", userId=" + userId + "]";
    }
    
}
