/*
 * InvestigationType.java
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
 * Entity class InvestigationType
 *
 * @author gjd37
 */
@Entity
@Table(name = "INVESTIGATION_TYPE")
@NamedQueries( {
    @NamedQuery(name = "InvestigationType.findByName", query = "SELECT i FROM InvestigationType i WHERE i.name = :name"),
    @NamedQuery(name = "InvestigationType.findByDescription", query = "SELECT i FROM InvestigationType i WHERE i.description = :description"),
    @NamedQuery(name = "InvestigationType.findByModTime", query = "SELECT i FROM InvestigationType i WHERE i.modTime = :modTime"),
    @NamedQuery(name = "InvestigationType.findByModId", query = "SELECT i FROM InvestigationType i WHERE i.modId = :modId"),
    
    @NamedQuery(name = Queries.ALL_INVESTIGATION_TYPES, query = Queries.ALL_INVESTIGATION_TYPES_JPQL)
    
})
        public class InvestigationType extends EntityBaseBean implements Serializable {
    
    @Id
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "invType")
    @XmlTransient
    private Collection<Investigation> investigationCollection;*/
    
    /** Creates a new instance of InvestigationType */
    public InvestigationType() {
    }
    
    /**
     * Creates a new instance of InvestigationType with the specified values.
     * @param name the name of the InvestigationType
     */
    public InvestigationType(String name) {
        this.name = name;
    }
    
    /**
     * Creates a new instance of InvestigationType with the specified values.
     * @param name the name of the InvestigationType
     * @param modTime the modTime of the InvestigationType
     * @param modId the modId of the InvestigationType
     */
    public InvestigationType(String name, Date modTime, String modId) {
        this.name = name;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the name of this InvestigationType.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this InvestigationType to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of this InvestigationType.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this InvestigationType to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the investigationCollection of this InvestigationType.
     * @return the investigationCollection
     */
    /*@XmlTransient
    public Collection<Investigation> getInvestigationCollection() {
        return this.investigationCollection;
    }*/
    
    /**
     * Sets the investigationCollection of this InvestigationType to the specified value.
     * @param investigationCollection the new investigationCollection
     */
    /*public void setInvestigationCollection(Collection<Investigation> investigationCollection) {
        this.investigationCollection = investigationCollection;
    }*/
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
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
     * Determines whether another object is equal to this InvestigationType.  The result is
     * <code>true</code> if and only if the argument is not null and is a InvestigationType object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InvestigationType)) {
            return false;
        }
        InvestigationType other = (InvestigationType)object;
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
        return "InvestigationType[name=" + name + "]";
    }
    
}
