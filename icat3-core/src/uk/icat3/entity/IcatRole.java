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
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;
import uk.icat3.util.IcatRoles;
import uk.icat3.util.Queries;
import static uk.icat3.util.Util.*;
/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "ICAT_ROLE")
@NamedQueries({
    @NamedQuery(name = "IcatRole.findByRole", query = "SELECT i FROM IcatRole i WHERE i.role = :role"),
     @NamedQuery(name = Queries.ALL_ROLES, query = Queries.ALL_ROLES_JPQL)
})
        public class IcatRole extends EntityBaseBean implements Serializable {
    
    @Id
    @Column(name = "ROLE", nullable = false)
    private String role;
    
    @Column(name = "ROLE_WEIGHT", nullable = false)
    private Long roleWeight;
    
    @Column(name = "ACTION_INSERT", nullable = false)
    private String actionCanInsert;
    
    @Column(name = "ACTION_INSERT_WEIGHT", nullable = false)
    private Long actionCanInsertWeight;
    
    @Column(name = "ACTION_SELECT", nullable = false)
    private String actionCanSelect;
    
    @Column(name = "ACTION_SELECT_WEIGHT", nullable = false)
    private Long actionCanSelectWeight;
    
    @Column(name = "ACTION_DOWNLOAD", nullable = false)
    private String actionCanDownload;
    
    @Column(name = "ACTION_DOWNLOAD_WEIGHT", nullable = false)
    private Long actionCanDownloadWeight;
    
    @Column(name = "ACTION_UPDATE", nullable = false)
    private String actionCanUpdate;
    
    @Column(name = "ACTION_UPDATE_WEIGHT", nullable = false)
    private Long actionCanUpdateWeight;
    
    @Column(name = "ACTION_DELETE", nullable = false)
    private String actionCanDelete;
    
    @Column(name = "ACTION_DELETE_WEIGHT", nullable = false)
    private Long actionCanDeleteWeight;
    
    @Column(name = "ACTION_REMOVE", nullable = false)
    private String actionCanRemove;
    
    @Column(name = "ACTION_REMOVE_WEIGHT", nullable = false)
    private Long actionCanRemoveWeight;
    
    @Column(name = "ACTION_ROOT_INSERT", nullable = false)
    private String actionCanRootInsert;
    
    @Column(name = "ACTION_ROOT_INSERT_WEIGHT", nullable = false)
    private Long actionCanRootInsertWeight;
    
    @Column(name = "ACTION_ROOT_REMOVE", nullable = false)
    private String actionCanRootRemove;
    
    @Column(name = "ACTION_ROOT_REMOVE_WEIGHT", nullable = false)
    private Long actionCanRootRemoveWeight;
    
    @Column(name = "ACTION_SET_FA", nullable = false)
    private String actionCanSetFa;
    
    @Column(name = "ACTION_SET_FA_WEIGHT", nullable = false)
    private Long actionCanSetFaWeight;
    
    @Column(name = "ACTION_MANAGE_USERS", nullable = false)
    private String actionCanManageUsers;
    
    @Column(name = "ACTION_MANAGE_USERS_WEIGHT", nullable = false)
    private Long actionCanManageUsersWeight;
    
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
    
    @XmlTransient
    public Long getRoleWeight() {
        return roleWeight;
    }
    
    public void setRoleWeight(Long roleWeight) {
        this.roleWeight = roleWeight;
    }
    
    @XmlTransient
    public String getActionCanInsert() {
        return actionCanInsert;
    }
    
    public void setActionCanInsert(String actionInsert) {
        this.actionCanInsert = actionInsert;
    }
    
    @XmlTransient
    public Long getActionCanInsertWeight() {
        return actionCanInsertWeight;
    }
    
    public void setActionCanInsertWeight(Long actionInsertWeight) {
        this.actionCanInsertWeight = actionInsertWeight;
    }
    
    @XmlTransient
    public String getActionCanSelect() {
        return actionCanSelect;
    }
    
    public void setActionCanSelect(String actionCanSelect) {
        this.actionCanSelect = actionCanSelect;
    }
    
    @XmlTransient
    public Long getActionCanSelectWeight() {
        return actionCanSelectWeight;
    }
    
    public void setActionCanSelectWeight(Long actionSelectWeight) {
        this.actionCanSelectWeight = actionSelectWeight;
    }
    
    @XmlTransient
    public String getActionCanDownload() {
        return actionCanDownload;
    }
    
    public void setActionCanDownload(String actionDownload) {
        this.actionCanDownload = actionDownload;
    }
    
    @XmlTransient
    public Long getActionCanDownloadWeight() {
        return actionCanDownloadWeight;
    }
    
    public void setActionCanDownloadWeight(Long actionDownloadWeight) {
        this.actionCanDownloadWeight = actionDownloadWeight;
    }
    
    @XmlTransient
    public String getActionCanUpdate() {
        return actionCanUpdate;
    }
    
    public void setActionCanUpdate(String actionUpdate) {
        this.actionCanUpdate = actionUpdate;
    }
    
    @XmlTransient
    public Long getActionCanUpdateWeight() {
        return actionCanUpdateWeight;
    }
    
    public void setActionCanUpdateWeight(Long actionUpdateWeight) {
        this.actionCanUpdateWeight = actionUpdateWeight;
    }
    
    @XmlTransient
    public String getActionCanDelete() {
        return actionCanDelete;
    }
    
    public void setActionCanDelete(String actionDelete) {
        this.actionCanDelete = actionDelete;
    }
    
    @XmlTransient
    public Long getActionCanDeleteWeight() {
        return actionCanDeleteWeight;
    }
    
    public void setActionCanDeleteWeight(Long actionDeleteWeight) {
        this.actionCanDeleteWeight = actionDeleteWeight;
    }
    
    @XmlTransient
    public String getActionCanRemove() {
        return actionCanRemove;
    }
    
    public void setActionCanRemove(String actionRemove) {
        this.actionCanRemove = actionRemove;
    }
    
    @XmlTransient
    public Long getActionCanRemoveWeight() {
        return actionCanRemoveWeight;
    }
    
    public void setActionCanRemoveWeight(Long actionRemoveWeight) {
        this.actionCanRemoveWeight = actionRemoveWeight;
    }
    
    @XmlTransient
    public String getActionCanRootInsert() {
        return actionCanRootInsert;
    }
    
    public void setActionCanRootInsert(String actionRootInsert) {
        this.actionCanRootInsert = actionRootInsert;
    }
    
    @XmlTransient
    public Long getActionCanRootInsertWeight() {
        return actionCanRootInsertWeight;
    }
    
    public void setActionCanRootInsertWeight(Long actionRootInsertWeight) {
        this.actionCanRootInsertWeight = actionRootInsertWeight;
    }
    
    @XmlTransient
    public String getActionCanRootRemove() {
        return actionCanRootRemove;
    }
    
    public void setActionCanRootRemove(String actionRootRemove) {
        this.actionCanRootRemove = actionRootRemove;
    }
    
    @XmlTransient
    public Long getActionCanRootRemoveWeight() {
        return actionCanRootRemoveWeight;
    }
    
    public void setActionCanRootRemoveWeight(Long actionRootRemoveWeight) {
        this.actionCanRootRemoveWeight = actionRootRemoveWeight;
    }
    
    @XmlTransient
    public String getActionCanSetFa() {
        return actionCanSetFa;
    }
    
    public void setActionCanSetFa(String actionSetFa) {
        this.actionCanSetFa = actionSetFa;
    }
    
    @XmlTransient
    public Long getActionCanSetFaWeight() {
        return actionCanSetFaWeight;
    }
    
    public void setActionCanSetFaWeight(Long actionSetFaWeight) {
        this.actionCanSetFaWeight = actionSetFaWeight;
    }
    
    @XmlTransient
    public String getActionCanManageUsers() {
        return actionCanManageUsers;
    }
    
    public void setActionCanManageUsers(String actionManageUsers) {
        this.actionCanManageUsers = actionManageUsers;
    }
    
    @XmlTransient
    public Long getActionCanManageUsersWeight() {
        return actionCanManageUsersWeight;
    }
    
    public void setActionCanManageUsersWeight(Long actionManageUsersWeight) {
        this.actionCanManageUsersWeight = actionManageUsersWeight;
    }
    
    @XmlTransient
    public Collection<IcatAuthorisation> getIcatAuthorisationCollection() {
        return icatAuthorisationCollection;
    }
    
    public void setIcatAuthorisationCollection(Collection<IcatAuthorisation> icatAuthorisationCollection) {
        this.icatAuthorisationCollection = icatAuthorisationCollection;
    }
    
    ///boolean getter methods for web services
    public boolean isActionSelect(){
        return parseBoolean(actionCanSelect);
    }
    
    public boolean isActionInsert(){
        return parseBoolean(actionCanInsert);
    }
    
    public boolean isActionDownload(){
        return parseBoolean(actionCanDownload);
    }
    
    public boolean isActionUpdate(){
        return parseBoolean(actionCanUpdate);
    }
    
    public boolean isActionDelete(){
        return parseBoolean(actionCanDelete);
    }
    
    public boolean isActionRemove(){
        return parseBoolean(actionCanRemove);
    }
    
    public boolean isActionManageUsers(){
        return parseBoolean(actionCanManageUsers);
    }
    
    public boolean isActionRootRemove(){
        return parseBoolean(actionCanRootRemove);
    }
    
    public boolean isActionRootInsert(){
        return parseBoolean(actionCanRootInsert);
    }
    
    public boolean isActionFacilityAcquired(){
        return parseBoolean(actionCanSetFa);
    }
    
    //setters
    public void setActionSelect(boolean ignore){}    
    public void setActionInsert(boolean ignore){}  
    public void setActionDownload(boolean ignore){}  
    public void setActionUpdate(boolean ignore){}
    public void setActionDelete(boolean ignore){} 
    public void setActionRemove(boolean ignore){}  
    public void setActionManageUsers(boolean ignore){}    
    public void setActionRootRemove(boolean ignore){}  
    public void setActionRootInsert(boolean ignore){}    
    public void setActionFacilityAcquired(boolean ignore){}
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
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
    
    /**
     * To check weather passed in role is greater than this role.  This needs to be false
     * as the added role to a Root element cannot be greater than the users role been added.
     * @param role role to be matched
     * @return true is passed in role is less
     */
    public boolean isGreaterEqualTo(IcatRole role){
        if(this.getRoleWeight() >= role.getRoleWeight()) return true;
        else return false;
    }
    
    @Override
    public String toString() {
        return "IcatRole[role=" + role + "]";
    }
    
    public boolean isIcatAdminRole(){
        if(getRole() != null && getRole().equals(IcatRoles.ICAT_ADMIN.toString())) return true;
        else return false;
    }
    
}
