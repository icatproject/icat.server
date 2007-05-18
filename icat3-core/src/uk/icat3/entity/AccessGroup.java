/*
 * AccessGroup.java
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Entity class AccessGroup
 *
 * @author gjd37
 */
@Entity
@Table(name = "ACCESS_GROUP")
@NamedQueries( {
    @NamedQuery(name = "AccessGroup.findById", query = "SELECT a FROM AccessGroup a WHERE a.id = :id"),
    @NamedQuery(name = "AccessGroup.findByName", query = "SELECT a FROM AccessGroup a WHERE a.name = :name"),
    @NamedQuery(name = "AccessGroup.findByDescription", query = "SELECT a FROM AccessGroup a WHERE a.description = :description"),
    @NamedQuery(name = "AccessGroup.findByModTime", query = "SELECT a FROM AccessGroup a WHERE a.modTime = :modTime"),
    @NamedQuery(name = "AccessGroup.findByModId", query = "SELECT a FROM AccessGroup a WHERE a.modId = :modId")
})
@SequenceGenerator(name="ACCESS_GROUP_SEQ",sequenceName="ACCESS_GROUP_ID_SEQ",allocationSize=1)
public class AccessGroup extends EntityBaseBean implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ACCESS_GROUP_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "NAME", nullable = false)
    private String name;
    
    @Column(name = "DESCRIPTION")
    private String description;
           
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accessGroup")
    private Collection<AccessGroupDlp> accessGroupDlpCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accessGroup")
    private Collection<AccessGroupIlp> accessGroupIlpCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accessGroup")
    private Collection<UserAccessGroup> userAccessGroupCollection;
    
    /** Creates a new instance of AccessGroup */
    public AccessGroup() {
    }
    
    /**
     * Creates a new instance of AccessGroup with the specified values.
     * @param id the id of the AccessGroup
     */
    public AccessGroup(Long id) {
        this.id = id;
    }
    
    /**
     * Creates a new instance of AccessGroup with the specified values.
     * @param id the id of the AccessGroup
     * @param name the name of the AccessGroup
     * @param modTime the modTime of the AccessGroup
     * @param modId the modId of the AccessGroup
     */
    public AccessGroup(Long id, String name, Date modTime, String modId) {
        this.id = id;
        this.name = name;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the id of this AccessGroup.
     * @return the id
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id of this AccessGroup to the specified value.
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the name of this AccessGroup.
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name of this AccessGroup to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of this AccessGroup.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Sets the description of this AccessGroup to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
        
    /**
     * Gets the accessGroupDlpCollection of this AccessGroup.
     * @return the accessGroupDlpCollection
     */
    public Collection<AccessGroupDlp> getAccessGroupDlpCollection() {
        return this.accessGroupDlpCollection;
    }
    
    /**
     * Sets the accessGroupDlpCollection of this AccessGroup to the specified value.
     * @param accessGroupDlpCollection the new accessGroupDlpCollection
     */
    public void setAccessGroupDlpCollection(Collection<AccessGroupDlp> accessGroupDlpCollection) {
        this.accessGroupDlpCollection = accessGroupDlpCollection;
    }
    
    /**
     * Gets the accessGroupIlpCollection of this AccessGroup.
     * @return the accessGroupIlpCollection
     */
    public Collection<AccessGroupIlp> getAccessGroupIlpCollection() {
        return this.accessGroupIlpCollection;
    }
    
    /**
     * Sets the accessGroupIlpCollection of this AccessGroup to the specified value.
     * @param accessGroupIlpCollection the new accessGroupIlpCollection
     */
    public void setAccessGroupIlpCollection(Collection<AccessGroupIlp> accessGroupIlpCollection) {
        this.accessGroupIlpCollection = accessGroupIlpCollection;
    }
    
    /**
     * Gets the userAccessGroupCollection of this AccessGroup.
     * @return the userAccessGroupCollection
     */
    public Collection<UserAccessGroup> getUserAccessGroupCollection() {
        return this.userAccessGroupCollection;
    }
    
    /**
     * Sets the userAccessGroupCollection of this AccessGroup to the specified value.
     * @param userAccessGroupCollection the new userAccessGroupCollection
     */
    public void setUserAccessGroupCollection(Collection<UserAccessGroup> userAccessGroupCollection) {
        this.userAccessGroupCollection = userAccessGroupCollection;
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
     * Determines whether another object is equal to this AccessGroup.  The result is
     * <code>true</code> if and only if the argument is not null and is a AccessGroup object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccessGroup)) {
            return false;
        }
        AccessGroup other = (AccessGroup)object;
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
        return "uk.icat3.entity.AccessGroup[id=" + id + "]";
    }
    
}
