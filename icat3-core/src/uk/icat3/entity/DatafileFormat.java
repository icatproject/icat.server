/*
 * DatafileFormat.java
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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 * Entity class DatafileFormat
 *
 * @author gjd37
 */
@Entity
@Table(name = "DATAFILE_FORMAT")
@NamedQueries( {
    @NamedQuery(name = "DatafileFormat.findByName", query = "SELECT d FROM DatafileFormat d WHERE d.datafileFormatPK.name = :name"),
    @NamedQuery(name = "DatafileFormat.findByVersion", query = "SELECT d FROM DatafileFormat d WHERE d.datafileFormatPK.version = :version"),
    @NamedQuery(name = "DatafileFormat.findByFormatType", query = "SELECT d FROM DatafileFormat d WHERE d.formatType = :formatType"),
    @NamedQuery(name = "DatafileFormat.findByDescription", query = "SELECT d FROM DatafileFormat d WHERE d.description = :description"),
    @NamedQuery(name = Queries.ALL_DATAFILE_FORMAT, query = Queries.ALL_DATAFILE_FORMAT_JPQL),
    @NamedQuery(name = "DatafileFormat.findByModId", query = "SELECT d FROM DatafileFormat d WHERE d.modId = :modId")
})
        public class DatafileFormat extends EntityBaseBean implements Serializable {
    
    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected DatafileFormatPK datafileFormatPK;
    
    @Column(name = "FORMAT_TYPE")
    private String formatType;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @OneToMany(mappedBy = "datafileFormat")
    @XmlTransient
    private Collection<Datafile> datafileCollection;
    
    /** Creates a new instance of DatafileFormat */
    public DatafileFormat() {
    }
    
    /**
     * Creates a new instance of DatafileFormat with the specified values.
     * @param datafileFormatPK the datafileFormatPK of the DatafileFormat
     */
    public DatafileFormat(DatafileFormatPK datafileFormatPK) {
        this.datafileFormatPK = datafileFormatPK;
    }
    
    /**
     * Creates a new instance of DatafileFormatPK with the specified values.
     * @param version the version of the DatafileFormatPK
     * @param name the name of the DatafileFormatPK
     */
    public DatafileFormat(String version, String name) {
        this.datafileFormatPK = new DatafileFormatPK(version, name);
    }
    
    /**
     * Gets the datafileFormatPK of this DatafileFormat.
     * @return the datafileFormatPK
     */
    public DatafileFormatPK getDatafileFormatPK() {
        return this.datafileFormatPK;
    }
    
    /**
     * Sets the datafileFormatPK of this DatafileFormat to the specified value.
     * @param datafileFormatPK the new datafileFormatPK
     */
    public void setDatafileFormatPK(DatafileFormatPK datafileFormatPK) {
        this.datafileFormatPK = datafileFormatPK;
    }
    
    /**
     * Gets the formatType of this DatafileFormat.
     * @return the formatType
     */
    public String getFormatType() {
        return this.formatType;
    }
    
    /**
     * Sets the formatType of this DatafileFormat to the specified value.
     * @param formatType the new formatType
     */
    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }
    
    /**
     * Gets the description of this DatafileFormat.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this DatafileFormat to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the datafileCollection of this DatafileFormat.
     * @return the datafileCollection
     */
    @XmlTransient
    public Collection<Datafile> getDatafileCollection() {
        return this.datafileCollection;
    }
    
    /**
     * Sets the datafileCollection of this DatafileFormat to the specified value.
     * @param datafileCollection the new datafileCollection
     */
    public void setDatafileCollection(Collection<Datafile> datafileCollection) {
        this.datafileCollection = datafileCollection;
    }
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.DATAFILE;
    }
    
    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.datafileFormatPK != null ? this.datafileFormatPK.hashCode() : 0);
        return hash;
    }
    
    /**
     * Determines whether another object is equal to this DatafileFormat.  The result is
     * <code>true</code> if and only if the argument is not null and is a DatafileFormat object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatafileFormat)) {
            return false;
        }
        DatafileFormat other = (DatafileFormat)object;
        if (this.datafileFormatPK != other.datafileFormatPK && (this.datafileFormatPK == null || !this.datafileFormatPK.equals(other.datafileFormatPK))) return false;
        return true;
    }
    
    /**
     * Overrides the isValid function,
     * @throws ValidationException
     * @return
     */
    @Override
    public boolean isValid(EntityManager manager) throws ValidationException {
        if(manager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        
        if(datafileFormatPK == null) throw new ValidationException(this +" primary key cannot be null");
        
        datafileFormatPK.isValid();
        
        return true;
        
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "DatafileFormat[datafileFormatPK=" + datafileFormatPK + "]";
    }
    
}
