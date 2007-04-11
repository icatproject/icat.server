/*
 * AccessGroupIlp.java
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
 * Entity class AccessGroupIlp
 * 
 * @author gjd37
 */
@Entity
@Table(name = "ACCESS_GROUP_ILP")
@NamedQueries( {
        @NamedQuery(name = "AccessGroupIlp.findByAccessGroupId", query = "SELECT a FROM AccessGroupIlp a WHERE a.accessGroupIlpPK.accessGroupId = :accessGroupId"),
        @NamedQuery(name = "AccessGroupIlp.findByIlpId", query = "SELECT a FROM AccessGroupIlp a WHERE a.accessGroupIlpPK.ilpId = :ilpId"),
        @NamedQuery(name = "AccessGroupIlp.findByModTime", query = "SELECT a FROM AccessGroupIlp a WHERE a.modTime = :modTime"),
        @NamedQuery(name = "AccessGroupIlp.findByModId", query = "SELECT a FROM AccessGroupIlp a WHERE a.modId = :modId")
    })
public class AccessGroupIlp extends EntityBaseBean implements Serializable {

    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected AccessGroupIlpPK accessGroupIlpPK;

    @JoinColumn(name = "ACCESS_GROUP_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private AccessGroup accessGroup;

    @JoinColumn(name = "ILP_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private InvestigationLevelPermission investigationLevelPermission;
    
    /** Creates a new instance of AccessGroupIlp */
    public AccessGroupIlp() {
    }

    /**
     * Creates a new instance of AccessGroupIlp with the specified values.
     * @param accessGroupIlpPK the accessGroupIlpPK of the AccessGroupIlp
     */
    public AccessGroupIlp(AccessGroupIlpPK accessGroupIlpPK) {
        this.accessGroupIlpPK = accessGroupIlpPK;
    }

    /**
     * Creates a new instance of AccessGroupIlp with the specified values.
     * @param accessGroupIlpPK the accessGroupIlpPK of the AccessGroupIlp
     * @param modTime the modTime of the AccessGroupIlp
     * @param modId the modId of the AccessGroupIlp
     */
    public AccessGroupIlp(AccessGroupIlpPK accessGroupIlpPK, Date modTime, String modId) {
        this.accessGroupIlpPK = accessGroupIlpPK;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Creates a new instance of AccessGroupIlpPK with the specified values.
     * @param ilpId the ilpId of the AccessGroupIlpPK
     * @param accessGroupId the accessGroupId of the AccessGroupIlpPK
     */
    public AccessGroupIlp(Long ilpId, Long accessGroupId) {
        this.accessGroupIlpPK = new AccessGroupIlpPK(ilpId, accessGroupId);
    }

    /**
     * Gets the accessGroupIlpPK of this AccessGroupIlp.
     * @return the accessGroupIlpPK
     */
    public AccessGroupIlpPK getAccessGroupIlpPK() {
        return this.accessGroupIlpPK;
    }

    /**
     * Sets the accessGroupIlpPK of this AccessGroupIlp to the specified value.
     * @param accessGroupIlpPK the new accessGroupIlpPK
     */
    public void setAccessGroupIlpPK(AccessGroupIlpPK accessGroupIlpPK) {
        this.accessGroupIlpPK = accessGroupIlpPK;
    }
    
    /**
     * Gets the accessGroup of this AccessGroupIlp.
     * @return the accessGroup
     */
    public AccessGroup getAccessGroup() {
        return this.accessGroup;
    }

    /**
     * Sets the accessGroup of this AccessGroupIlp to the specified value.
     * @param accessGroup the new accessGroup
     */
    public void setAccessGroup(AccessGroup accessGroup) {
        this.accessGroup = accessGroup;
    }

    /**
     * Gets the investigationLevelPermission of this AccessGroupIlp.
     * @return the investigationLevelPermission
     */
    public InvestigationLevelPermission getInvestigationLevelPermission() {
        return this.investigationLevelPermission;
    }

    /**
     * Sets the investigationLevelPermission of this AccessGroupIlp to the specified value.
     * @param investigationLevelPermission the new investigationLevelPermission
     */
    public void setInvestigationLevelPermission(InvestigationLevelPermission investigationLevelPermission) {
        this.investigationLevelPermission = investigationLevelPermission;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.accessGroupIlpPK != null ? this.accessGroupIlpPK.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this AccessGroupIlp.  The result is 
     * <code>true</code> if and only if the argument is not null and is a AccessGroupIlp object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccessGroupIlp)) {
            return false;
        }
        AccessGroupIlp other = (AccessGroupIlp)object;
        if (this.accessGroupIlpPK != other.accessGroupIlpPK && (this.accessGroupIlpPK == null || !this.accessGroupIlpPK.equals(other.accessGroupIlpPK))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.AccessGroupIlp[accessGroupIlpPK=" + accessGroupIlpPK + "]";
    }
    
}
