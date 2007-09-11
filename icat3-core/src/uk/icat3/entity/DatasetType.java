/*
 * DatasetType.java
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
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 * Entity class DatasetType
 *
 * @author gjd37
 */
@Entity
@Table(name = "DATASET_TYPE")
@NamedQueries( {
    @NamedQuery(name = "DatasetType.findByName", query = "SELECT d FROM DatasetType d WHERE d.name = :name"),
    @NamedQuery(name = "DatasetType.findByDescription", query = "SELECT d FROM DatasetType d WHERE d.description = :description"),
    @NamedQuery(name = "DatasetType.findByModTime", query = "SELECT d FROM DatasetType d WHERE d.modTime = :modTime"),
    @NamedQuery(name = Queries.ALL_DATASET_TYPE, query = Queries.ALL_DATASET_TYPE_JPQL),
    @NamedQuery(name = "DatasetType.findByModId", query = "SELECT d FROM DatasetType d WHERE d.modId = :modId")
})
public class DatasetType extends EntityBaseBean implements Serializable {
    
    @Id
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetType")
    @XmlTransient
    private Collection<Dataset> datasetCollection;*/
    
    /** Creates a new instance of DatasetType */
    public DatasetType() {
    }
    
    /**
     * Creates a new instance of DatasetType with the specified values.
     * @param name the name of the DatasetType
     */
    public DatasetType(String name) {
        this.name = name;
    }
          
    /**
     * Gets the name of this DatasetType.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this DatasetType to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of this DatasetType.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this DatasetType to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the datasetCollection of this DatasetType.
     * @return the datasetCollection
     */
    /*@XmlTransient
    public Collection<Dataset> getDatasetCollection() {
        return this.datasetCollection;
    }*/
    
    /**
     * Sets the datasetCollection of this DatasetType to the specified value.
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
     * Determines whether another object is equal to this DatasetType.  The result is
     * <code>true</code> if and only if the argument is not null and is a DatasetType object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatasetType)) {
            return false;
        }
        DatasetType other = (DatasetType)object;
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
        return "DatasetType[name=" + name + "]";
    }
    
}
