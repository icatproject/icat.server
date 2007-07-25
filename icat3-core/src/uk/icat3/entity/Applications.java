/*
 * Applications.java
 * 
 * Created on 24-Jul-2007, 10:44:51
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "APPLICATIONS")
@NamedQueries({
    @NamedQuery(name = "Applications.findByAppCode", query = "SELECT a FROM Applications a WHERE a.appCode = :appCode"), 
    @NamedQuery(name = "Applications.findByAppName", query = "SELECT a FROM Applications a WHERE a.appName = :appName"), 
    @NamedQuery(name = "Applications.findByAppDescription", query = "SELECT a FROM Applications a WHERE a.appDescription = :appDescription"),
    @NamedQuery(name = "Applications.findByModTime", query = "SELECT a FROM Applications a WHERE a.modTime = :modTime"), 
    @NamedQuery(name = "Applications.findByModId", query = "SELECT a FROM Applications a WHERE a.modId = :modId"),
    @NamedQuery(name = "Applications.findByCreateTime", query = "SELECT a FROM Applications a WHERE a.createTime = :createTime"), 
    @NamedQuery(name = "Applications.findByCreateId", query = "SELECT a FROM Applications a WHERE a.createId = :createId"), 
    @NamedQuery(name = "Applications.findByFacilityAquired", query = "SELECT a FROM Applications a WHERE a.facilityAcquired = :facilityAquired"), 
    @NamedQuery(name = "Applications.findByDeleted", query = "SELECT a FROM Applications a WHERE a.markedDeleted = :deleted")
})
public class Applications extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "APP_CODE", nullable = false)
    private String appCode;

    @Column(name = "APP_NAME", nullable = false)
    private String appName;

    @Column(name = "APP_DESCRIPTION")
    private String appDescription;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "applications")
    private Collection<UserRoles> userRolesCollection;

    public Applications() {
    }

    public Applications(String appCode) {
        this.appCode = appCode;
    }

    public Applications(String appCode, String appName, Date modTime, String modId, Date createTime, String createId, String facilityAcquired, String deleted) {
        this.appCode = appCode;
        this.appName = appName;
        this.modTime = modTime;
        this.modId = modId;
        this.createTime = createTime;
        this.createId = createId;
        this.facilityAcquired = facilityAcquired;
        this.markedDeleted = deleted;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }
  
    @XmlTransient
    public Collection<UserRoles> getUserRolesCollection() {
        return userRolesCollection;
    }

    public void setUserRolesCollection(Collection<UserRoles> userRolesCollection) {
        this.userRolesCollection = userRolesCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (appCode != null ? appCode.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Applications)) {
            return false;
        }
        Applications other = (Applications) object;
        if (this.appCode != other.appCode && (this.appCode == null || !this.appCode.equals(other.appCode))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Applications[appCode=" + appCode + "]";
    }

}
