/*
 * IcatRole.java
 * 
 * Created on 31-Jul-2007, 13:56:23
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigInteger;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "ICAT_ROLE")
@NamedQueries({
    @NamedQuery(name = "IcatRole.findByRole", query = "SELECT i FROM IcatRole i WHERE i.role = :role"), 
    @NamedQuery(name = "IcatRole.findByActionInsert", query = "SELECT i FROM IcatRole i WHERE i.actionInsert = :actionInsert"), 
    @NamedQuery(name = "IcatRole.findByActionInsertWeight", query = "SELECT i FROM IcatRole i WHERE i.actionInsertWeight = :actionInsertWeight"), 
    @NamedQuery(name = "IcatRole.findByActionSelect", query = "SELECT i FROM IcatRole i WHERE i.actionSelect = :actionSelect"), 
    @NamedQuery(name = "IcatRole.findByActionSelectWeight", query = "SELECT i FROM IcatRole i WHERE i.actionSelectWeight = :actionSelectWeight"),
    @NamedQuery(name = "IcatRole.findByActionDownload", query = "SELECT i FROM IcatRole i WHERE i.actionDownload = :actionDownload"), 
    @NamedQuery(name = "IcatRole.findByActionDownloadWeight", query = "SELECT i FROM IcatRole i WHERE i.actionDownloadWeight = :actionDownloadWeight"), 
    @NamedQuery(name = "IcatRole.findByActionUpdate", query = "SELECT i FROM IcatRole i WHERE i.actionUpdate = :actionUpdate"), 
    @NamedQuery(name = "IcatRole.findByActionUpdateWeight", query = "SELECT i FROM IcatRole i WHERE i.actionUpdateWeight = :actionUpdateWeight"),
    @NamedQuery(name = "IcatRole.findByActionDelete", query = "SELECT i FROM IcatRole i WHERE i.actionDelete = :actionDelete"), 
    @NamedQuery(name = "IcatRole.findByActionDeleteWeight", query = "SELECT i FROM IcatRole i WHERE i.actionDeleteWeight = :actionDeleteWeight"), 
    @NamedQuery(name = "IcatRole.findByActionRemove", query = "SELECT i FROM IcatRole i WHERE i.actionRemove = :actionRemove"),
    @NamedQuery(name = "IcatRole.findByActionRemoveWeight", query = "SELECT i FROM IcatRole i WHERE i.actionRemoveWeight = :actionRemoveWeight"),
    @NamedQuery(name = "IcatRole.findByActionRootInsert", query = "SELECT i FROM IcatRole i WHERE i.actionRootInsert = :actionRootInsert"),
    @NamedQuery(name = "IcatRole.findByActionRootInsertWeight", query = "SELECT i FROM IcatRole i WHERE i.actionRootInsertWeight = :actionRootInsertWeight"),
    @NamedQuery(name = "IcatRole.findByActionRootRemove", query = "SELECT i FROM IcatRole i WHERE i.actionRootRemove = :actionRootRemove"),
    @NamedQuery(name = "IcatRole.findByActionRootRemoveWeight", query = "SELECT i FROM IcatRole i WHERE i.actionRootRemoveWeight = :actionRootRemoveWeight"), 
    @NamedQuery(name = "IcatRole.findByActionSetFa", query = "SELECT i FROM IcatRole i WHERE i.actionSetFa = :actionSetFa"),
    @NamedQuery(name = "IcatRole.findByActionSetFaWeight", query = "SELECT i FROM IcatRole i WHERE i.actionSetFaWeight = :actionSetFaWeight"), 
    @NamedQuery(name = "IcatRole.findByActionManageUsers", query = "SELECT i FROM IcatRole i WHERE i.actionManageUsers = :actionManageUsers"),
    @NamedQuery(name = "IcatRole.findByActionManageUsersWeight", query = "SELECT i FROM IcatRole i WHERE i.actionManageUsersWeight = :actionManageUsersWeight"), 
    @NamedQuery(name = "IcatRole.findByModTime", query = "SELECT i FROM IcatRole i WHERE i.modTime = :modTime"), 
    @NamedQuery(name = "IcatRole.findByModId", query = "SELECT i FROM IcatRole i WHERE i.modId = :modId"), 
    @NamedQuery(name = "IcatRole.findByCreateTime", query = "SELECT i FROM IcatRole i WHERE i.createTime = :createTime"), 
    @NamedQuery(name = "IcatRole.findByCreateId", query = "SELECT i FROM IcatRole i WHERE i.createId = :createId"),
    @NamedQuery(name = "IcatRole.findByFacilityAcquired", query = "SELECT i FROM IcatRole i WHERE i.facilityAcquired = :facilityAcquired"), 
    @NamedQuery(name = "IcatRole.findByDeleted", query = "SELECT i FROM IcatRole i WHERE i.deleted = :deleted"),
     @NamedQuery(name = Queries.ALL_ROLES, query = Queries.ALL_ROLES_JPQL)
})
public class IcatRole implements Serializable {

    @Id
    @Column(name = "ROLE", nullable = false)
    private String role;

    @Column(name = "ROLE_WEIGHT", nullable = false)
    private Long roleWeight;
    
    @Column(name = "ACTION_INSERT", nullable = false)
    private String actionInsert;

    @Column(name = "ACTION_INSERT_WEIGHT", nullable = false)
    private BigInteger actionInsertWeight;

    @Column(name = "ACTION_SELECT", nullable = false)
    private String actionSelect;

    @Column(name = "ACTION_SELECT_WEIGHT", nullable = false)
    private BigInteger actionSelectWeight;

    @Column(name = "ACTION_DOWNLOAD", nullable = false)
    private String actionDownload;

    @Column(name = "ACTION_DOWNLOAD_WEIGHT", nullable = false)
    private BigInteger actionDownloadWeight;

    @Column(name = "ACTION_UPDATE", nullable = false)
    private String actionUpdate;

    @Column(name = "ACTION_UPDATE_WEIGHT", nullable = false)
    private BigInteger actionUpdateWeight;

    @Column(name = "ACTION_DELETE", nullable = false)
    private String actionDelete;

    @Column(name = "ACTION_DELETE_WEIGHT", nullable = false)
    private BigInteger actionDeleteWeight;

    @Column(name = "ACTION_REMOVE", nullable = false)
    private String actionRemove;

    @Column(name = "ACTION_REMOVE_WEIGHT", nullable = false)
    private BigInteger actionRemoveWeight;

    @Column(name = "ACTION_ROOT_INSERT", nullable = false)
    private String actionRootInsert;

    @Column(name = "ACTION_ROOT_INSERT_WEIGHT", nullable = false)
    private BigInteger actionRootInsertWeight;

    @Column(name = "ACTION_ROOT_REMOVE", nullable = false)
    private String actionRootRemove;

    @Column(name = "ACTION_ROOT_REMOVE_WEIGHT", nullable = false)
    private BigInteger actionRootRemoveWeight;

    @Column(name = "ACTION_SET_FA", nullable = false)
    private String actionSetFa;

    @Column(name = "ACTION_SET_FA_WEIGHT", nullable = false)
    private BigInteger actionSetFaWeight;

    @Column(name = "ACTION_MANAGE_USERS", nullable = false)
    private String actionManageUsers;

    @Column(name = "ACTION_MANAGE_USERS_WEIGHT", nullable = false)
    private BigInteger actionManageUsersWeight;

    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;

    @Column(name = "MOD_ID", nullable = false)
    private String modId;

    @Column(name = "CREATE_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "CREATE_ID", nullable = false)
    private String createId;

    @Column(name = "FACILITY_ACQUIRED", nullable = false)
    private String facilityAcquired;

    @Column(name = "DELETED", nullable = false)
    private String deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "role")
    private Collection<IcatAuthorisation> icatAuthorisationCollection;

    public IcatRole() {
    }

    public IcatRole(String role) {
        this.role = role;
    }
   
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public Long getRoleWeight() {
        return roleWeight;
    }
    
    public void setRoleWeight(Long roleWeight) {
        this.roleWeight = roleWeight;
    }

    public String getActionInsert() {
        return actionInsert;
    }

    public void setActionInsert(String actionInsert) {
        this.actionInsert = actionInsert;
    }

    public BigInteger getActionInsertWeight() {
        return actionInsertWeight;
    }

    public void setActionInsertWeight(BigInteger actionInsertWeight) {
        this.actionInsertWeight = actionInsertWeight;
    }

    public String getActionSelect() {
        return actionSelect;
    }

    public void setActionSelect(String actionSelect) {
        this.actionSelect = actionSelect;
    }

    public BigInteger getActionSelectWeight() {
        return actionSelectWeight;
    }

    public void setActionSelectWeight(BigInteger actionSelectWeight) {
        this.actionSelectWeight = actionSelectWeight;
    }

    public String getActionDownload() {
        return actionDownload;
    }

    public void setActionDownload(String actionDownload) {
        this.actionDownload = actionDownload;
    }

    public BigInteger getActionDownloadWeight() {
        return actionDownloadWeight;
    }

    public void setActionDownloadWeight(BigInteger actionDownloadWeight) {
        this.actionDownloadWeight = actionDownloadWeight;
    }

    public String getActionUpdate() {
        return actionUpdate;
    }

    public void setActionUpdate(String actionUpdate) {
        this.actionUpdate = actionUpdate;
    }

    public BigInteger getActionUpdateWeight() {
        return actionUpdateWeight;
    }

    public void setActionUpdateWeight(BigInteger actionUpdateWeight) {
        this.actionUpdateWeight = actionUpdateWeight;
    }

    public String getActionDelete() {
        return actionDelete;
    }

    public void setActionDelete(String actionDelete) {
        this.actionDelete = actionDelete;
    }

    public BigInteger getActionDeleteWeight() {
        return actionDeleteWeight;
    }

    public void setActionDeleteWeight(BigInteger actionDeleteWeight) {
        this.actionDeleteWeight = actionDeleteWeight;
    }

    public String getActionRemove() {
        return actionRemove;
    }

    public void setActionRemove(String actionRemove) {
        this.actionRemove = actionRemove;
    }

    public BigInteger getActionRemoveWeight() {
        return actionRemoveWeight;
    }

    public void setActionRemoveWeight(BigInteger actionRemoveWeight) {
        this.actionRemoveWeight = actionRemoveWeight;
    }

    public String getActionRootInsert() {
        return actionRootInsert;
    }

    public void setActionRootInsert(String actionRootInsert) {
        this.actionRootInsert = actionRootInsert;
    }

    public BigInteger getActionRootInsertWeight() {
        return actionRootInsertWeight;
    }

    public void setActionRootInsertWeight(BigInteger actionRootInsertWeight) {
        this.actionRootInsertWeight = actionRootInsertWeight;
    }

    public String getActionRootRemove() {
        return actionRootRemove;
    }

    public void setActionRootRemove(String actionRootRemove) {
        this.actionRootRemove = actionRootRemove;
    }

    public BigInteger getActionRootRemoveWeight() {
        return actionRootRemoveWeight;
    }

    public void setActionRootRemoveWeight(BigInteger actionRootRemoveWeight) {
        this.actionRootRemoveWeight = actionRootRemoveWeight;
    }

    public String getActionSetFa() {
        return actionSetFa;
    }

    public void setActionSetFa(String actionSetFa) {
        this.actionSetFa = actionSetFa;
    }

    public BigInteger getActionSetFaWeight() {
        return actionSetFaWeight;
    }

    public void setActionSetFaWeight(BigInteger actionSetFaWeight) {
        this.actionSetFaWeight = actionSetFaWeight;
    }

    public String getActionManageUsers() {
        return actionManageUsers;
    }

    public void setActionManageUsers(String actionManageUsers) {
        this.actionManageUsers = actionManageUsers;
    }

    public BigInteger getActionManageUsersWeight() {
        return actionManageUsersWeight;
    }

    public void setActionManageUsersWeight(BigInteger actionManageUsersWeight) {
        this.actionManageUsersWeight = actionManageUsersWeight;
    }

    public Date getModTime() {
        return modTime;
    }

    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    public String getModId() {
        return modId;
    }

    public void setModId(String modId) {
        this.modId = modId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    public String getFacilityAcquired() {
        return facilityAcquired;
    }

    public void setFacilityAcquired(String facilityAcquired) {
        this.facilityAcquired = facilityAcquired;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

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
        return "rr.IcatRole[role=" + role + "]";
    }

}
