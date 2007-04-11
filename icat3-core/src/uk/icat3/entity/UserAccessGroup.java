/*
 * UserAccessGroup.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class UserAccessGroup
 * 
 * @author gjd37
 */
@Entity
@Table(name = "USER_ACCESS_GROUP")
@NamedQueries( {
        @NamedQuery(name = "UserAccessGroup.findByUserId", query = "SELECT u FROM UserAccessGroup u WHERE u.userAccessGroupPK.userId = :userId"),
        @NamedQuery(name = "UserAccessGroup.findByAccessGroupId", query = "SELECT u FROM UserAccessGroup u WHERE u.userAccessGroupPK.accessGroupId = :accessGroupId"),
        @NamedQuery(name = "UserAccessGroup.findByModTime", query = "SELECT u FROM UserAccessGroup u WHERE u.modTime = :modTime"),
        @NamedQuery(name = "UserAccessGroup.findByModId", query = "SELECT u FROM UserAccessGroup u WHERE u.modId = :modId")
    })
public class UserAccessGroup extends EntityBaseBean implements Serializable {

    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected UserAccessGroupPK userAccessGroupPK;

    @JoinColumn(name = "ACCESS_GROUP_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private AccessGroup accessGroup;
    
    /** Creates a new instance of UserAccessGroup */
    public UserAccessGroup() {
    }

    /**
     * Creates a new instance of UserAccessGroup with the specified values.
     * @param userAccessGroupPK the userAccessGroupPK of the UserAccessGroup
     */
    public UserAccessGroup(UserAccessGroupPK userAccessGroupPK) {
        this.userAccessGroupPK = userAccessGroupPK;
    }
 
    /**
     * Creates a new instance of UserAccessGroupPK with the specified values.
     * @param accessGroupId the accessGroupId of the UserAccessGroupPK
     * @param userId the userId of the UserAccessGroupPK
     */
    public UserAccessGroup(Long accessGroupId, String userId) {
        this.userAccessGroupPK = new UserAccessGroupPK(accessGroupId, userId);
    }

    /**
     * Gets the userAccessGroupPK of this UserAccessGroup.
     * @return the userAccessGroupPK
     */
    public UserAccessGroupPK getUserAccessGroupPK() {
        return this.userAccessGroupPK;
    }

    /**
     * Sets the userAccessGroupPK of this UserAccessGroup to the specified value.
     * @param userAccessGroupPK the new userAccessGroupPK
     */
    public void setUserAccessGroupPK(UserAccessGroupPK userAccessGroupPK) {
        this.userAccessGroupPK = userAccessGroupPK;
    }
    
    /**
     * Gets the accessGroup of this UserAccessGroup.
     * @return the accessGroup
     */
    public AccessGroup getAccessGroup() {
        return this.accessGroup;
    }

    /**
     * Sets the accessGroup of this UserAccessGroup to the specified value.
     * @param accessGroup the new accessGroup
     */
    public void setAccessGroup(AccessGroup accessGroup) {
        this.accessGroup = accessGroup;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.userAccessGroupPK != null ? this.userAccessGroupPK.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this UserAccessGroup.  The result is 
     * <code>true</code> if and only if the argument is not null and is a UserAccessGroup object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserAccessGroup)) {
            return false;
        }
        UserAccessGroup other = (UserAccessGroup)object;
        if (this.userAccessGroupPK != other.userAccessGroupPK && (this.userAccessGroupPK == null || !this.userAccessGroupPK.equals(other.userAccessGroupPK))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.UserAccessGroup[userAccessGroupPK=" + userAccessGroupPK + "]";
    }
    
}
