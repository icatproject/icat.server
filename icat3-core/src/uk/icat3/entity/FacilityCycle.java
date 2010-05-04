/*
 * FacilityCycle.java
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
 * Entity class FacilityCycle
 * 
 * @author gjd37
 */
@Entity
@Table(name = "FACILITY_CYCLE")
@NamedQueries( {
        @NamedQuery(name = "FacilityCycle.findByName", query = "SELECT f FROM FacilityCycle f WHERE f.name = :name"),
        @NamedQuery(name = "FacilityCycle.findByStartDate", query = "SELECT f FROM FacilityCycle f WHERE f.startDate = :startDate"),
        @NamedQuery(name = "FacilityCycle.findByFinishDate", query = "SELECT f FROM FacilityCycle f WHERE f.finishDate = :finishDate"),
        @NamedQuery(name = "FacilityCycle.findByDescription", query = "SELECT f FROM FacilityCycle f WHERE f.description = :description"),
        @NamedQuery(name = "FacilityCycle.findByModTime", query = "SELECT f FROM FacilityCycle f WHERE f.modTime = :modTime"),
        @NamedQuery(name = "FacilityCycle.findByModId", query = "SELECT f FROM FacilityCycle f WHERE f.modId = :modId"),

        //Added searches for ICAT3 API
        @NamedQuery(name = Queries.ALL_FACILITYCYCLES, query = Queries.ALL_FACILITYCYCLES_JPQL)

    })
public class FacilityCycle extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "START_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "FINISH_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishDate;

    @Column(name = "DESCRIPTION")
    private String description;

    @XmlTransient
    @OneToMany(mappedBy = "facilityCycle")
    private Collection<Investigation> investigationCollection;
    
    /** Creates a new instance of FacilityCycle */
    public FacilityCycle() {
    }

    /**
     * Creates a new instance of FacilityCycle with the specified values.
     * @param name the name of the FacilityCycle
     */
    public FacilityCycle(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of FacilityCycle with the specified values.
     * @param name the name of the FacilityCycle
     * @param modTime the modTime of the FacilityCycle
     * @param modId the modId of the FacilityCycle
     */
    public FacilityCycle(String name, Date modTime, String modId) {
        this.name = name;
        this.modTime = modTime;
        this.modId = modId;
    }

    /**
     * Gets the name of this FacilityCycle.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this FacilityCycle to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the startDate of this FacilityCycle.
     * @return the startDate
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Sets the startDate of this FacilityCycle to the specified value.
     * @param startDate the new startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the finishDate of this FacilityCycle.
     * @return the finishDate
     */
    public Date getFinishDate() {
        return this.finishDate;
    }

    /**
     * Sets the finishDate of this FacilityCycle to the specified value.
     * @param finishDate the new finishDate
     */
    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    /**
     * Gets the description of this FacilityCycle.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this FacilityCycle to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
   
    /**
     * Gets the investigationCollection of this FacilityCycle.
     * @return the investigationCollection
     */
    @XmlTransient
    public Collection<Investigation> getInvestigationCollection() {
        return this.investigationCollection;
    }

    /**
     * Sets the investigationCollection of this FacilityCycle to the specified value.
     * @param investigationCollection the new investigationCollection
     */
    public void setInvestigationCollection(Collection<Investigation> investigationCollection) {
        this.investigationCollection = investigationCollection;
    }

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
     * Determines whether another object is equal to this FacilityCycle.  The result is 
     * <code>true</code> if and only if the argument is not null and is a FacilityCycle object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FacilityCycle)) {
            return false;
        }
        FacilityCycle other = (FacilityCycle)object;
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
        return "FacilityCycle[name=" + name + "]";
    }
    
}
