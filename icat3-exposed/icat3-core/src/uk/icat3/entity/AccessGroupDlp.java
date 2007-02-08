/*
 * AccessGroupDlp.java
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
 * Entity class AccessGroupDlp
 * 
 * @author gjd37
 */
@Entity
@Table(name = "ACCESS_GROUP_DLP")
@NamedQueries( {
        @NamedQuery(name = "AccessGroupDlp.findByAccessGroupId", query = "SELECT a FROM AccessGroupDlp a WHERE a.accessGroupDlpPK.accessGroupId = :accessGroupId"),
        @NamedQuery(name = "AccessGroupDlp.findByDlpId", query = "SELECT a FROM AccessGroupDlp a WHERE a.accessGroupDlpPK.dlpId = :dlpId"),
        @NamedQuery(name = "AccessGroupDlp.findByModTime", query = "SELECT a FROM AccessGroupDlp a WHERE a.modTime = :modTime"),
        @NamedQuery(name = "AccessGroupDlp.findByModId", query = "SELECT a FROM AccessGroupDlp a WHERE a.modId = :modId")
    })
public class AccessGroupDlp extends EntityBaseBean implements Serializable {

    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected AccessGroupDlpPK accessGroupDlpPK;

    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;

    @Column(name = "MOD_ID", nullable = false)
    private String modId;

    @JoinColumn(name = "ACCESS_GROUP_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private AccessGroup accessGroup;

    @JoinColumn(name = "DLP_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private DatasetLevelPermission datasetLevelPermission;
    
    /** Creates a new instance of AccessGroupDlp */
    public AccessGroupDlp() {
    }

    /**
     * Creates a new instance of AccessGroupDlp with the specified values.
     * @param accessGroupDlpPK the accessGroupDlpPK of the AccessGroupDlp
     */
    public AccessGroupDlp(AccessGroupDlpPK accessGroupDlpPK) {
        this.accessGroupDlpPK = accessGroupDlpPK;
    }

    /**
     * Creates a new instance of AccessGroupDlp with the specified values.
     * @param accessGroupDlpPK the accessGroupDlpPK of the AccessGroupDlp
     * @param modTime the modTime of the AccessGroupDlp
     * @param modId the modId of the AccessGroupDlp
     */
    public AccessGroupDlp(AccessGroupDlpPK accessGroupDlpPK, Date modTime, String modId) {
        this.accessGroupDlpPK = accessGroupDlpPK;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Creates a new instance of AccessGroupDlpPK with the specified values.
     * @param dlpId the dlpId of the AccessGroupDlpPK
     * @param accessGroupId the accessGroupId of the AccessGroupDlpPK
     */
    public AccessGroupDlp(BigInteger dlpId, BigInteger accessGroupId) {
        this.accessGroupDlpPK = new AccessGroupDlpPK(dlpId, accessGroupId);
    }

    /**
     * Gets the accessGroupDlpPK of this AccessGroupDlp.
     * @return the accessGroupDlpPK
     */
    public AccessGroupDlpPK getAccessGroupDlpPK() {
        return this.accessGroupDlpPK;
    }

    /**
     * Sets the accessGroupDlpPK of this AccessGroupDlp to the specified value.
     * @param accessGroupDlpPK the new accessGroupDlpPK
     */
    public void setAccessGroupDlpPK(AccessGroupDlpPK accessGroupDlpPK) {
        this.accessGroupDlpPK = accessGroupDlpPK;
    }

    /**
     * Gets the modTime of this AccessGroupDlp.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }

    /**
     * Sets the modTime of this AccessGroupDlp to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    /**
     * Gets the modId of this AccessGroupDlp.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }

    /**
     * Sets the modId of this AccessGroupDlp to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }

    /**
     * Gets the accessGroup of this AccessGroupDlp.
     * @return the accessGroup
     */
    public AccessGroup getAccessGroup() {
        return this.accessGroup;
    }

    /**
     * Sets the accessGroup of this AccessGroupDlp to the specified value.
     * @param accessGroup the new accessGroup
     */
    public void setAccessGroup(AccessGroup accessGroup) {
        this.accessGroup = accessGroup;
    }

    /**
     * Gets the datasetLevelPermission of this AccessGroupDlp.
     * @return the datasetLevelPermission
     */
    public DatasetLevelPermission getDatasetLevelPermission() {
        return this.datasetLevelPermission;
    }

    /**
     * Sets the datasetLevelPermission of this AccessGroupDlp to the specified value.
     * @param datasetLevelPermission the new datasetLevelPermission
     */
    public void setDatasetLevelPermission(DatasetLevelPermission datasetLevelPermission) {
        this.datasetLevelPermission = datasetLevelPermission;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.accessGroupDlpPK != null ? this.accessGroupDlpPK.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this AccessGroupDlp.  The result is 
     * <code>true</code> if and only if the argument is not null and is a AccessGroupDlp object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccessGroupDlp)) {
            return false;
        }
        AccessGroupDlp other = (AccessGroupDlp)object;
        if (this.accessGroupDlpPK != other.accessGroupDlpPK && (this.accessGroupDlpPK == null || !this.accessGroupDlpPK.equals(other.accessGroupDlpPK))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.AccessGroupDlp[accessGroupDlpPK=" + accessGroupDlpPK + "]";
    }
    
}
