/*
 * DatasetLevelPermission.java
 *
 * Created on 08 February 2007, 10:04
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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Entity class DatasetLevelPermission
 *
 * @author gjd37
 */
@Entity
@Table(name = "DATASET_LEVEL_PERMISSION")
@NamedQueries( {
    @NamedQuery(name = "DatasetLevelPermission.findById", query = "SELECT d FROM DatasetLevelPermission d WHERE d.id = :id"),
    @NamedQuery(name = "DatasetLevelPermission.findByPrmAdmin", query = "SELECT d FROM DatasetLevelPermission d WHERE d.prmAdmin = :prmAdmin"),
    @NamedQuery(name = "DatasetLevelPermission.findByPrmCreate", query = "SELECT d FROM DatasetLevelPermission d WHERE d.prmCreate = :prmCreate"),
    @NamedQuery(name = "DatasetLevelPermission.findByPrmRead", query = "SELECT d FROM DatasetLevelPermission d WHERE d.prmRead = :prmRead"),
    @NamedQuery(name = "DatasetLevelPermission.findByPrmUpdate", query = "SELECT d FROM DatasetLevelPermission d WHERE d.prmUpdate = :prmUpdate"),
    @NamedQuery(name = "DatasetLevelPermission.findByPrmDelete", query = "SELECT d FROM DatasetLevelPermission d WHERE d.prmDelete = :prmDelete"),
    @NamedQuery(name = "DatasetLevelPermission.findByModTime", query = "SELECT d FROM DatasetLevelPermission d WHERE d.modTime = :modTime"),
    @NamedQuery(name = "DatasetLevelPermission.findByModId", query = "SELECT d FROM DatasetLevelPermission d WHERE d.modId = :modId")
})
@SequenceGenerator(name="DATASET_LEVEL_PERMISSION_SEQ",sequenceName="DATASET_LEVEL_PERMISSION_ID_SEQ",allocationSize=1)
public class DatasetLevelPermission extends EntityBaseBean implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="DATASET_LEVEL_PERMISSION_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "PRM_ADMIN", nullable = false)
    private short prmAdmin;
    
    @Column(name = "PRM_CREATE", nullable = false)
    private short prmCreate;
    
    @Column(name = "PRM_READ", nullable = false)
    private short prmRead;
    
    @Column(name = "PRM_UPDATE", nullable = false)
    private short prmUpdate;
    
    @Column(name = "PRM_DELETE", nullable = false)
    private short prmDelete;
    
    @Column(name = "MOD_ID", nullable = false)
    private String modId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetLevelPermission")
    private Collection<AccessGroupDlp> accessGroupDlpCollection;
    
    @JoinColumn(name = "DATASET_ID", referencedColumnName = "ID")
    @ManyToOne
    private Dataset datasetId;
    
    /** Creates a new instance of DatasetLevelPermission */
    public DatasetLevelPermission() {
    }
    
    /**
     * Creates a new instance of DatasetLevelPermission with the specified values.
     * @param id the id of the DatasetLevelPermission
     */
    public DatasetLevelPermission(Long id) {
        this.id = id;
    }
    
    /**
     * Creates a new instance of DatasetLevelPermission with the specified values.
     * @param id the id of the DatasetLevelPermission
     * @param prmAdmin the prmAdmin of the DatasetLevelPermission
     * @param prmCreate the prmCreate of the DatasetLevelPermission
     * @param prmRead the prmRead of the DatasetLevelPermission
     * @param prmUpdate the prmUpdate of the DatasetLevelPermission
     * @param prmDelete the prmDelete of the DatasetLevelPermission
     * @param modTime the modTime of the DatasetLevelPermission
     * @param modId the modId of the DatasetLevelPermission
     */
    public DatasetLevelPermission(Long id, short prmAdmin, short prmCreate, short prmRead, short prmUpdate, short prmDelete, Date modTime, String modId) {
        this.id = id;
        this.prmAdmin = prmAdmin;
        this.prmCreate = prmCreate;
        this.prmRead = prmRead;
        this.prmUpdate = prmUpdate;
        this.prmDelete = prmDelete;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the id of this DatasetLevelPermission.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this DatasetLevelPermission to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the prmAdmin of this DatasetLevelPermission.
     * @return the prmAdmin
     */
    public short getPrmAdmin() {
        return this.prmAdmin;
    }
    
    /**
     * Sets the prmAdmin of this DatasetLevelPermission to the specified value.
     * @param prmAdmin the new prmAdmin
     */
    public void setPrmAdmin(short prmAdmin) {
        this.prmAdmin = prmAdmin;
    }
    
    /**
     * Gets the prmCreate of this DatasetLevelPermission.
     * @return the prmCreate
     */
    public short getPrmCreate() {
        return this.prmCreate;
    }
    
    /**
     * Sets the prmCreate of this DatasetLevelPermission to the specified value.
     * @param prmCreate the new prmCreate
     */
    public void setPrmCreate(short prmCreate) {
        this.prmCreate = prmCreate;
    }
    
    /**
     * Gets the prmRead of this DatasetLevelPermission.
     * @return the prmRead
     */
    public short getPrmRead() {
        return this.prmRead;
    }
    
    /**
     * Sets the prmRead of this DatasetLevelPermission to the specified value.
     * @param prmRead the new prmRead
     */
    public void setPrmRead(short prmRead) {
        this.prmRead = prmRead;
    }
    
    /**
     * Gets the prmUpdate of this DatasetLevelPermission.
     * @return the prmUpdate
     */
    public short getPrmUpdate() {
        return this.prmUpdate;
    }
    
    /**
     * Sets the prmUpdate of this DatasetLevelPermission to the specified value.
     * @param prmUpdate the new prmUpdate
     */
    public void setPrmUpdate(short prmUpdate) {
        this.prmUpdate = prmUpdate;
    }
    
    /**
     * Gets the prmDelete of this DatasetLevelPermission.
     * @return the prmDelete
     */
    public short getPrmDelete() {
        return this.prmDelete;
    }
    
    /**
     * Sets the prmDelete of this DatasetLevelPermission to the specified value.
     * @param prmDelete the new prmDelete
     */
    public void setPrmDelete(short prmDelete) {
        this.prmDelete = prmDelete;
    }
    
    /**
     * Gets the modId of this DatasetLevelPermission.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }
    
    /**
     * Sets the modId of this DatasetLevelPermission to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }
    
    /**
     * Gets the accessGroupDlpCollection of this DatasetLevelPermission.
     * @return the accessGroupDlpCollection
     */
    public Collection<AccessGroupDlp> getAccessGroupDlpCollection() {
        return this.accessGroupDlpCollection;
    }
    
    /**
     * Sets the accessGroupDlpCollection of this DatasetLevelPermission to the specified value.
     * @param accessGroupDlpCollection the new accessGroupDlpCollection
     */
    public void setAccessGroupDlpCollection(Collection<AccessGroupDlp> accessGroupDlpCollection) {
        this.accessGroupDlpCollection = accessGroupDlpCollection;
    }
    
    /**
     * Gets the datasetId of this DatasetLevelPermission.
     * @return the datasetId
     */
    public Dataset getDatasetId() {
        return this.datasetId;
    }
    
    /**
     * Sets the datasetId of this DatasetLevelPermission to the specified value.
     * @param datasetId the new datasetId
     */
    public void setDatasetId(Dataset datasetId) {
        this.datasetId = datasetId;
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
     * Determines whether another object is equal to this DatasetLevelPermission.  The result is
     * <code>true</code> if and only if the argument is not null and is a DatasetLevelPermission object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatasetLevelPermission)) {
            return false;
        }
        DatasetLevelPermission other = (DatasetLevelPermission)object;
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
        return "uk.icat3.entity.DatasetLevelPermission[id=" + id + "]";
    }
    
}
