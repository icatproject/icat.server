/*
 * IcatRole.java
 * 
 * Created on 24-Jul-2007, 10:44:50
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
@Table(name = "ICAT_ROLE")
@NamedQueries({
    @NamedQuery(name = "IcatRole.findByRole", query = "SELECT i FROM IcatRole i WHERE i.role = :role"), 
    @NamedQuery(name = "IcatRole.findByActionInsert", query = "SELECT i FROM IcatRole i WHERE i.actionInsert = :actionInsert"), 
    @NamedQuery(name = "IcatRole.findByActionSelect", query = "SELECT i FROM IcatRole i WHERE i.actionSelect = :actionSelect"), 
    @NamedQuery(name = "IcatRole.findByActionDownload", query = "SELECT i FROM IcatRole i WHERE i.actionDownload = :actionDownload"), 
    @NamedQuery(name = "IcatRole.findByActionUpdate", query = "SELECT i FROM IcatRole i WHERE i.actionUpdate = :actionUpdate"),
    @NamedQuery(name = "IcatRole.findByActionDelete", query = "SELECT i FROM IcatRole i WHERE i.actionDelete = :actionDelete"), 
    @NamedQuery(name = "IcatRole.findByActionRemove", query = "SELECT i FROM IcatRole i WHERE i.actionRemove = :actionRemove"), 
    @NamedQuery(name = "IcatRole.findByActionRootInsert", query = "SELECT i FROM IcatRole i WHERE i.actionRootInsert = :actionRootInsert"), 
    @NamedQuery(name = "IcatRole.findByActionRootRemove", query = "SELECT i FROM IcatRole i WHERE i.actionRootRemove = :actionRootRemove"),
    @NamedQuery(name = "IcatRole.findByActionSetFa", query = "SELECT i FROM IcatRole i WHERE i.actionSetFa = :actionSetFa")
})
public class IcatRole extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "ROLE", nullable = false)
    private String role;

    @Column(name = "ACTION_INSERT", nullable = false)
    private String actionInsert;

    @Column(name = "ACTION_SELECT", nullable = false)
    private String actionSelect;

    @Column(name = "ACTION_DOWNLOAD", nullable = false)
    private String actionDownload;

    @Column(name = "ACTION_UPDATE", nullable = false)
    private String actionUpdate;

    @Column(name = "ACTION_DELETE", nullable = false)
    private String actionDelete;

    @Column(name = "ACTION_REMOVE", nullable = false)
    private String actionRemove;

    @Column(name = "ACTION_ROOT_INSERT", nullable = false)
    private String actionRootInsert;

    @Column(name = "ACTION_ROOT_REMOVE", nullable = false)
    private String actionRootRemove;

    @Column(name = "ACTION_SET_FA", nullable = false)
    private String actionSetFa;
   
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "role")
    private Collection<IcatAuthorisation> icatAuthorisationCollection;

    public IcatRole() {
    }

    public IcatRole(String role) {
        this.role = role;
    }

    public IcatRole(String role, String actionInsert, String actionSelect, String actionDownload, String actionUpdate, String actionDelete, String actionRemove, String actionRootInsert, String actionRootRemove, String actionSetFa, Date modTime, String modId, Date createTime, String createId, String facilityAcquired, String deleted) {
        this.role = role;
        this.actionInsert = actionInsert;
        this.actionSelect = actionSelect;
        this.actionDownload = actionDownload;
        this.actionUpdate = actionUpdate;
        this.actionDelete = actionDelete;
        this.actionRemove = actionRemove;
        this.actionRootInsert = actionRootInsert;
        this.actionRootRemove = actionRootRemove;
        this.actionSetFa = actionSetFa;
        this.modTime = modTime;
        this.modId = modId;
        this.createTime = createTime;
        this.createId = createId;
        this.facilityAcquired = facilityAcquired;
        this.markedDeleted = deleted;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getActionInsert() {
        return actionInsert;
    }

    public void setActionInsert(String actionInsert) {
        this.actionInsert = actionInsert;
    }

    public String getActionSelect() {
        return actionSelect;
    }

    public void setActionSelect(String actionSelect) {
        this.actionSelect = actionSelect;
    }

    public String getActionDownload() {
        return actionDownload;
    }

    public void setActionDownload(String actionDownload) {
        this.actionDownload = actionDownload;
    }

    public String getActionUpdate() {
        return actionUpdate;
    }

    public void setActionUpdate(String actionUpdate) {
        this.actionUpdate = actionUpdate;
    }

    public String getActionDelete() {
        return actionDelete;
    }

    public void setActionDelete(String actionDelete) {
        this.actionDelete = actionDelete;
    }

    public String getActionRemove() {
        return actionRemove;
    }

    public void setActionRemove(String actionRemove) {
        this.actionRemove = actionRemove;
    }

    public String getActionRootInsert() {
        return actionRootInsert;
    }

    public void setActionRootInsert(String actionRootInsert) {
        this.actionRootInsert = actionRootInsert;
    }

    public String getActionRootRemove() {
        return actionRootRemove;
    }

    public void setActionRootRemove(String actionRootRemove) {
        this.actionRootRemove = actionRootRemove;
    }

    public String getActionSetFa() {
        return actionSetFa;
    }

    public void setActionSetFa(String actionSetFa) {
        this.actionSetFa = actionSetFa;
    }
  
    @XmlTransient    
    public Collection<IcatAuthorisation> getIcatAuthorisationCollection() {
        return icatAuthorisationCollection;
    }

    public void setIcatAuthorisationCollection(Collection<IcatAuthorisation> icatAuthorisationCollection) {
        this.icatAuthorisationCollection = icatAuthorisationCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (role != null ? role.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IcatRole)) {
            return false;
        }
        IcatRole other = (IcatRole) object;
        if (this.role != other.role && (this.role == null || !this.role.equals(other.role))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IcatRole[role=" + role + "]";
    }

}
