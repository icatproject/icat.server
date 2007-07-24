/*
 * UserRolesPK.java
 * 
 * Created on 24-Jul-2007, 10:44:52
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author gjd37
 */
@Embeddable
public class UserRolesPK extends EntityPrimaryKeyBaseBean implements Serializable {

    @Column(name = "APP_CODE", nullable = false)
    private String appCode;

    @Column(name = "USERNAME", nullable = false)
    private String username;

    public UserRolesPK() {
    }

    public UserRolesPK(String appCode, String username) {
        this.appCode = appCode;
        this.username = username;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (appCode != null ? appCode.hashCode() : 0);
        hash += (username != null ? username.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserRolesPK)) {
            return false;
        }
        UserRolesPK other = (UserRolesPK) object;
        if (this.appCode != other.appCode && (this.appCode == null || !this.appCode.equals(other.appCode))) {
            return false;
        }
        if (this.username != other.username && (this.username == null || !this.username.equals(other.username))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserRolesPK[appCode=" + appCode + ", username=" + username + "]";
    }

}
