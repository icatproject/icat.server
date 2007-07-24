/*
 * UserRoles.java
 * 
 * Created on 24-Jul-2007, 10:44:52
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "USER_ROLES")
@NamedQueries({
    @NamedQuery(name = "UserRoles.findByAppCode", query = "SELECT u FROM UserRoles u WHERE u.userRolesPK.appCode = :appCode"),
    @NamedQuery(name = "UserRoles.findByUsername", query = "SELECT u FROM UserRoles u WHERE u.userRolesPK.username = :username"), 
    @NamedQuery(name = "UserRoles.findByRole", query = "SELECT u FROM UserRoles u WHERE u.role = :role")
})
public class UserRoles extends EntityBaseBean implements Serializable {

    @EmbeddedId
    protected UserRolesPK userRolesPK;

    @Column(name = "ROLE", nullable = false)
    private String role;
   
    @JoinColumn(name = "APP_CODE", referencedColumnName = "APP_CODE", insertable = false, updatable = false)
    @ManyToOne
    private Applications applications;

    public UserRoles() {
    }

    public UserRoles(UserRolesPK userRolesPK) {
        this.userRolesPK = userRolesPK;
    }

    public UserRoles(UserRolesPK userRolesPK, String role, Date modTime, String modId, Date createTime, String createId, String facilityAcquired, String deleted) {
        this.userRolesPK = userRolesPK;
        this.role = role;
        this.modTime = modTime;
        this.modId = modId;
        this.createTime = createTime;
        this.createId = createId;
        this.facilityAcquired = facilityAcquired;
        this.markedDeleted = deleted;
    }

    public UserRoles(String appCode, String username) {
        this.userRolesPK = new UserRolesPK(appCode, username);
    }

    public UserRolesPK getUserRolesPK() {
        return userRolesPK;
    }

    public void setUserRolesPK(UserRolesPK userRolesPK) {
        this.userRolesPK = userRolesPK;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
  
    public Applications getApplications() {
        return applications;
    }

    public void setApplications(Applications applications) {
        this.applications = applications;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userRolesPK != null ? userRolesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserRoles)) {
            return false;
        }
        UserRoles other = (UserRoles) object;
        if (this.userRolesPK != other.userRolesPK && (this.userRolesPK == null || !this.userRolesPK.equals(other.userRolesPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserRoles[userRolesPK=" + userRolesPK + "]";
    }

}
