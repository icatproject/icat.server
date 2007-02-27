/*
 * Dataset.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class Dataset
 *
 * @author gjd37
 */
@Entity
@Table(name = "DATASET")
@NamedQueries( {
    @NamedQuery(name = "Dataset.findById", query = "SELECT d FROM Dataset d WHERE d.id = :id"),
    @NamedQuery(name = "Dataset.findBySampleId", query = "SELECT d FROM Dataset d WHERE d.sampleId = :sampleId"),
    @NamedQuery(name = "Dataset.findByName", query = "SELECT d FROM Dataset d WHERE d.name = :name"),
    @NamedQuery(name = "Dataset.findByDescription", query = "SELECT d FROM Dataset d WHERE d.description = :description"),
    @NamedQuery(name = "Dataset.findByModTime", query = "SELECT d FROM Dataset d WHERE d.modTime = :modTime"),
    @NamedQuery(name = "Dataset.findByModId", query = "SELECT d FROM Dataset d WHERE d.modId = :modId")
})
public class Dataset extends EntityBaseBean implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="DATASET_SEQUENCE")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "SAMPLE_ID")
    private Long sampleId;
    
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;
    
    @Column(name = "MOD_ID", nullable = false)
    private String modId;
    
    @JoinColumn(name = "DATASET_STATUS", referencedColumnName = "NAME")
    @ManyToOne
    private DatasetStatus datasetStatus;
    
    @JoinColumn(name = "DATASET_TYPE", referencedColumnName = "NAME")
    @ManyToOne
    private DatasetType datasetType;
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID")
    @ManyToOne
    private Investigation investigationId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    private Collection<DatasetParameter> datasetParameterCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetId")
    private Collection<Datafile> datafileCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetId")
    private Collection<DatasetLevelPermission> datasetLevelPermissionCollection;
    
    /** Creates a new instance of Dataset */
    public Dataset() {
    }
    
    /**
     * Creates a new instance of Dataset with the specified values.
     * @param id the id of the Dataset
     */
    public Dataset(Long id) {
        this.id = id;
    }
    
    /**
     * Creates a new instance of Dataset with the specified values.
     * @param id the id of the Dataset
     * @param name the name of the Dataset
     * @param modTime the modTime of the Dataset
     * @param modId the modId of the Dataset
     */
    public Dataset(Long id, String name, Date modTime, String modId) {
        this.id = id;
        this.name = name;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the id of this Dataset.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this Dataset to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the sampleId of this Dataset.
     * @return the sampleId
     */
    public Long getSampleId() {
        return this.sampleId;
    }
    
    /**
     * Sets the sampleId of this Dataset to the specified value.
     * @param sampleId the new sampleId
     */
    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }
    
    /**
     * Gets the name of this Dataset.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this Dataset to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of this Dataset.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this Dataset to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the modTime of this Dataset.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }
    
    /**
     * Sets the modTime of this Dataset to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }
    
    /**
     * Gets the modId of this Dataset.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }
    
    /**
     * Sets the modId of this Dataset to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }
    
    /**
     * Gets the datasetStatus of this Dataset.
     * @return the datasetStatus
     */
    public DatasetStatus getDatasetStatus() {
        return this.datasetStatus;
    }
    
    /**
     * Sets the datasetStatus of this Dataset to the specified value.
     * @param datasetStatus the new datasetStatus
     */
    public void setDatasetStatus(DatasetStatus datasetStatus) {
        this.datasetStatus = datasetStatus;
    }
    
    /**
     * Gets the datasetType of this Dataset.
     * @return the datasetType
     */
    public DatasetType getDatasetType() {
        return this.datasetType;
    }
    
    /**
     * Sets the datasetType of this Dataset to the specified value.
     * @param datasetType the new datasetType
     */
    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }
    
    /**
     * Gets the investigationId of this Dataset.
     * @return the investigationId
     */
    public Investigation getInvestigationId() {
        return this.investigationId;
    }
    
    /**
     * Sets the investigationId of this Dataset to the specified value.
     * @param investigationId the new investigationId
     */
    public void setInvestigationId(Investigation investigationId) {
        this.investigationId = investigationId;
    }
    
    /**
     * Gets the datasetParameterCollection of this Dataset.
     * @return the datasetParameterCollection
     */
    public Collection<DatasetParameter> getDatasetParameterCollection() {
        return this.datasetParameterCollection;
    }
    
    /**
     * Sets the datasetParameterCollection of this Dataset to the specified value.
     * @param datasetParameterCollection the new datasetParameterCollection
     */
    public void setDatasetParameterCollection(Collection<DatasetParameter> datasetParameterCollection) {
        this.datasetParameterCollection = datasetParameterCollection;
    }
    
    /**
     * Gets the datafileCollection of this Dataset.
     * @return the datafileCollection
     */
    public Collection<Datafile> getDatafileCollection() {
        return this.datafileCollection;
    }
    
    /**
     * Sets the datafileCollection of this Dataset to the specified value.
     * @param datafileCollection the new datafileCollection
     */
    public void setDatafileCollection(Collection<Datafile> datafileCollection) {
        this.datafileCollection = datafileCollection;
    }
    
    /**
     * Gets the datasetLevelPermissionCollection of this Dataset.
     * @return the datasetLevelPermissionCollection
     */
    public Collection<DatasetLevelPermission> getDatasetLevelPermissionCollection() {
        return this.datasetLevelPermissionCollection;
    }
    
    /**
     * Sets the datasetLevelPermissionCollection of this Dataset to the specified value.
     * @param datasetLevelPermissionCollection the new datasetLevelPermissionCollection
     */
    public void setDatasetLevelPermissionCollection(Collection<DatasetLevelPermission> datasetLevelPermissionCollection) {
        this.datasetLevelPermissionCollection = datasetLevelPermissionCollection;
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
     * Determines whether another object is equal to this Dataset.  The result is
     * <code>true</code> if and only if the argument is not null and is a Dataset object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dataset)) {
            return false;
        }
        Dataset other = (Dataset)object;
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
        return "uk.icat3.entity.Dataset[id=" + id + "]";
    }
    
}
