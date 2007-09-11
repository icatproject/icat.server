/*
 * Instrument.java
 *
 * Created on 08 February 2007, 09:49
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
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;
/**
 * Entity class Instrument
 *
 * @author gjd37
 */
@Entity
@Table(name = "INSTRUMENT")
@NamedQueries( {
    @NamedQuery(name = "Instrument.findByName", query = "SELECT i FROM Instrument i WHERE i.name = :name"),
    @NamedQuery(name = "Instrument.findByType", query = "SELECT i FROM Instrument i WHERE i.type = :type"),
    @NamedQuery(name = "Instrument.findByDescription", query = "SELECT i FROM Instrument i WHERE i.description = :description"),
    @NamedQuery(name = "Instrument.findByModTime", query = "SELECT i FROM Instrument i WHERE i.modTime = :modTime"),
    @NamedQuery(name = "Instrument.findByModId", query = "SELECT i FROM Instrument i WHERE i.modId = :modId"),
    
    //Added searches for ICAT3 API
    @NamedQuery(name = Queries.ALL_INSTRUMENTS, query = Queries.ALL_INSTRUMENTS_JPQL)
})
public class Instrument extends EntityBaseBean implements Serializable {
    
    @Id
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "TYPE")
    private String type;
    
    @Column(name = "DESCRIPTION")
    private String description;
               
    /*@OneToMany(mappedBy = "instrument")
    @XmlTransient
    private Collection<Investigation> investigationCollection;*/
    
    
    /** Creates a new instance of Instrument */
    public Instrument() {
    }
    
    /**
     * Creates a new instance of Instrument with the specified values.
     * @param name the name of the Instrument
     */
    public Instrument(String name) {
        this.name = name;
    }
    
    /**
     * Creates a new instance of Instrument with the specified values.
     * @param name the name of the Instrument
     * @param modTime the modTime of the Instrument
     * @param modId the modId of the Instrument
     */
    public Instrument(String name, Date modTime, String modId) {
        this.name = name;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the name of this Instrument.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this Instrument to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the type of this Instrument.
     * @return the type
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * Sets the type of this Instrument to the specified value.
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Gets the description of this Instrument.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this Instrument to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }    
     
    /**
     * Gets the investigationCollection of this Instrument.
     * @return the investigationCollection
     */
    /* @XmlTransient
    public Collection<Investigation> getInvestigationCollection() {
        return this.investigationCollection;
    }*/
    
    /**
     * Sets the investigationCollection of this Instrument to the specified value.
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
     * Determines whether another object is equal to this Instrument.  The result is
     * <code>true</code> if and only if the argument is not null and is a Instrument object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Instrument)) {
            return false;
        }
        Instrument other = (Instrument)object;
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
        return "Instrument[name=" + name + "]";
    }
    
}
