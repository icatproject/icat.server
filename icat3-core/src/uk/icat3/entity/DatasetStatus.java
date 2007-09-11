/*
 * DatasetStatus.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 * Entity class DatasetStatus
 * 
 * @author gjd37
 */
@Entity
@Table(name = "DATASET_STATUS")
@NamedQueries( {
        @NamedQuery(name = "DatasetStatus.findByName", query = "SELECT d FROM DatasetStatus d WHERE d.name = :name"),
        @NamedQuery(name = "DatasetStatus.findByDescription", query = "SELECT d FROM DatasetStatus d WHERE d.description = :description"),
        @NamedQuery(name = "DatasetStatus.findByModTime", query = "SELECT d FROM DatasetStatus d WHERE d.modTime = :modTime"),
        @NamedQuery(name = Queries.ALL_DATASET_STATUS, query = Queries.ALL_DATASET_STATUS_JPQL),
        @NamedQuery(name = "DatasetStatus.findByModId", query = "SELECT d FROM DatasetStatus d WHERE d.modId = :modId")
    })
public class DatasetStatus extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;
   
    /*@OneToMany(mappedBy = "datasetStatus")
    @XmlTransient
    private Collection<Dataset> datasetCollection;*/
    
    /** Creates a new instance of DatasetStatus */
    public DatasetStatus() {
    }

    /**
     * Creates a new instance of DatasetStatus with the specified values.
     * @param name the name of the DatasetStatus
     */
    public DatasetStatus(String name) {
        this.name = name;
    }
   
    /**
     * Gets the name of this DatasetStatus.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this DatasetStatus to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of this DatasetStatus.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this DatasetStatus to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
   
    /**
     * Gets the datasetCollection of this DatasetStatus.
     * @return the datasetCollection
     */
    /* @XmlTransient
    public Collection<Dataset> getDatasetCollection() {
        return this.datasetCollection;
    }*/

    /**
     * Sets the datasetCollection of this DatasetStatus to the specified value.
     * @param datasetCollection the new datasetCollection
     */
    /*public void setDatasetCollection(Collection<Dataset> datasetCollection) {
        this.datasetCollection = datasetCollection;
    }*/

    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.DATASET;
    }
    
    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this DatasetStatus.  The result is 
     * <code>true</code> if and only if the argument is not null and is a DatasetStatus object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatasetStatus)) {
            return false;
        }
        DatasetStatus other = (DatasetStatus)object;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "DatasetStatus[name=" + name + "]";
    }
    
}
