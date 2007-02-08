/*
 * InvestigationLevelPermission.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class InvestigationLevelPermission
 * 
 * @author gjd37
 */
@Entity
@Table(name = "INVESTIGATION_LEVEL_PERMISSION")
@NamedQueries( {
        @NamedQuery(name = "InvestigationLevelPermission.findById", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.id = :id"),
        @NamedQuery(name = "InvestigationLevelPermission.findByPrmAdmin", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.prmAdmin = :prmAdmin"),
        @NamedQuery(name = "InvestigationLevelPermission.findByPrmFineGrainedAccess", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.prmFineGrainedAccess = :prmFineGrainedAccess"),
        @NamedQuery(name = "InvestigationLevelPermission.findByPrmCreate", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.prmCreate = :prmCreate"),
        @NamedQuery(name = "InvestigationLevelPermission.findByPrmRead", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.prmRead = :prmRead"),
        @NamedQuery(name = "InvestigationLevelPermission.findByPrmUpdate", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.prmUpdate = :prmUpdate"),
        @NamedQuery(name = "InvestigationLevelPermission.findByPrmDelete", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.prmDelete = :prmDelete"),
        @NamedQuery(name = "InvestigationLevelPermission.findByModTime", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.modTime = :modTime"),
        @NamedQuery(name = "InvestigationLevelPermission.findByModId", query = "SELECT i FROM InvestigationLevelPermission i WHERE i.modId = :modId")
    })
public class InvestigationLevelPermission extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private BigDecimal id;

    @Column(name = "PRM_ADMIN", nullable = false)
    private short prmAdmin;

    @Column(name = "PRM_FINE_GRAINED_ACCESS", nullable = false)
    private short prmFineGrainedAccess;

    @Column(name = "PRM_CREATE", nullable = false)
    private short prmCreate;

    @Column(name = "PRM_READ", nullable = false)
    private short prmRead;

    @Column(name = "PRM_UPDATE", nullable = false)
    private short prmUpdate;

    @Column(name = "PRM_DELETE", nullable = false)
    private short prmDelete;

    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;

    @Column(name = "MOD_ID", nullable = false)
    private String modId;

    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID")
    @ManyToOne
    private Investigation investigationId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "investigationLevelPermission")
    private Collection<AccessGroupIlp> accessGroupIlpCollection;
    
    /** Creates a new instance of InvestigationLevelPermission */
    public InvestigationLevelPermission() {
    }

    /**
     * Creates a new instance of InvestigationLevelPermission with the specified values.
     * @param id the id of the InvestigationLevelPermission
     */
    public InvestigationLevelPermission(BigDecimal id) {
        this.id = id;
    }

    /**
     * Creates a new instance of InvestigationLevelPermission with the specified values.
     * @param id the id of the InvestigationLevelPermission
     * @param prmAdmin the prmAdmin of the InvestigationLevelPermission
     * @param prmFineGrainedAccess the prmFineGrainedAccess of the InvestigationLevelPermission
     * @param prmCreate the prmCreate of the InvestigationLevelPermission
     * @param prmRead the prmRead of the InvestigationLevelPermission
     * @param prmUpdate the prmUpdate of the InvestigationLevelPermission
     * @param prmDelete the prmDelete of the InvestigationLevelPermission
     * @param modTime the modTime of the InvestigationLevelPermission
     * @param modId the modId of the InvestigationLevelPermission
     */
    public InvestigationLevelPermission(BigDecimal id, short prmAdmin, short prmFineGrainedAccess, short prmCreate, short prmRead, short prmUpdate, short prmDelete, Date modTime, String modId) {
        this.id = id;
        this.prmAdmin = prmAdmin;
        this.prmFineGrainedAccess = prmFineGrainedAccess;
        this.prmCreate = prmCreate;
        this.prmRead = prmRead;
        this.prmUpdate = prmUpdate;
        this.prmDelete = prmDelete;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Gets the id of this InvestigationLevelPermission.
     * @return the id
     */
    public BigDecimal getId() {
        return this.id;
    }

    /**
     * Sets the id of this InvestigationLevelPermission to the specified value.
     * @param id the new id
     */
    public void setId(BigDecimal id) {
        this.id = id;
    }

    /**
     * Gets the prmAdmin of this InvestigationLevelPermission.
     * @return the prmAdmin
     */
    public short getPrmAdmin() {
        return this.prmAdmin;
    }

    /**
     * Sets the prmAdmin of this InvestigationLevelPermission to the specified value.
     * @param prmAdmin the new prmAdmin
     */
    public void setPrmAdmin(short prmAdmin) {
        this.prmAdmin = prmAdmin;
    }

    /**
     * Gets the prmFineGrainedAccess of this InvestigationLevelPermission.
     * @return the prmFineGrainedAccess
     */
    public short getPrmFineGrainedAccess() {
        return this.prmFineGrainedAccess;
    }

    /**
     * Sets the prmFineGrainedAccess of this InvestigationLevelPermission to the specified value.
     * @param prmFineGrainedAccess the new prmFineGrainedAccess
     */
    public void setPrmFineGrainedAccess(short prmFineGrainedAccess) {
        this.prmFineGrainedAccess = prmFineGrainedAccess;
    }

    /**
     * Gets the prmCreate of this InvestigationLevelPermission.
     * @return the prmCreate
     */
    public short getPrmCreate() {
        return this.prmCreate;
    }

    /**
     * Sets the prmCreate of this InvestigationLevelPermission to the specified value.
     * @param prmCreate the new prmCreate
     */
    public void setPrmCreate(short prmCreate) {
        this.prmCreate = prmCreate;
    }

    /**
     * Gets the prmRead of this InvestigationLevelPermission.
     * @return the prmRead
     */
    public short getPrmRead() {
        return this.prmRead;
    }

    /**
     * Sets the prmRead of this InvestigationLevelPermission to the specified value.
     * @param prmRead the new prmRead
     */
    public void setPrmRead(short prmRead) {
        this.prmRead = prmRead;
    }

    /**
     * Gets the prmUpdate of this InvestigationLevelPermission.
     * @return the prmUpdate
     */
    public short getPrmUpdate() {
        return this.prmUpdate;
    }

    /**
     * Sets the prmUpdate of this InvestigationLevelPermission to the specified value.
     * @param prmUpdate the new prmUpdate
     */
    public void setPrmUpdate(short prmUpdate) {
        this.prmUpdate = prmUpdate;
    }

    /**
     * Gets the prmDelete of this InvestigationLevelPermission.
     * @return the prmDelete
     */
    public short getPrmDelete() {
        return this.prmDelete;
    }

    /**
     * Sets the prmDelete of this InvestigationLevelPermission to the specified value.
     * @param prmDelete the new prmDelete
     */
    public void setPrmDelete(short prmDelete) {
        this.prmDelete = prmDelete;
    }

    /**
     * Gets the modTime of this InvestigationLevelPermission.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }

    /**
     * Sets the modTime of this InvestigationLevelPermission to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    /**
     * Gets the modId of this InvestigationLevelPermission.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }

    /**
     * Sets the modId of this InvestigationLevelPermission to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }

    /**
     * Gets the investigationId of this InvestigationLevelPermission.
     * @return the investigationId
     */
    public Investigation getInvestigationId() {
        return this.investigationId;
    }

    /**
     * Sets the investigationId of this InvestigationLevelPermission to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Investigation investigationId) {
        this.investigationId = investigationId;
    }

    /**
     * Gets the accessGroupIlpCollection of this InvestigationLevelPermission.
     * @return the accessGroupIlpCollection
     */
    public Collection<AccessGroupIlp> getAccessGroupIlpCollection() {
        return this.accessGroupIlpCollection;
    }

    /**
     * Sets the accessGroupIlpCollection of this InvestigationLevelPermission to the specified value.
     * @param accessGroupIlpCollection the new accessGroupIlpCollection
     */
    public void setAccessGroupIlpCollection(Collection<AccessGroupIlp> accessGroupIlpCollection) {
        this.accessGroupIlpCollection = accessGroupIlpCollection;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this InvestigationLevelPermission.  The result is 
     * <code>true</code> if and only if the argument is not null and is a InvestigationLevelPermission object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InvestigationLevelPermission)) {
            return false;
        }
        InvestigationLevelPermission other = (InvestigationLevelPermission)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "uk.icat3.entity.InvestigationLevelPermission[id=" + id + "]";
    }
    
}
