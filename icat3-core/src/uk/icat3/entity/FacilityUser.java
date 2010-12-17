/*
 * FacilityUser.java
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
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;

/**
 * Entity class FacilityUser
 *
 * @author gjd37
 */
@Entity
@Table(name = "FACILITY_USER")
@NamedQueries( {
    @NamedQuery(name = "FacilityUser.findByFacilityUserId", query = "SELECT f FROM FacilityUser f WHERE f.facilityUserId = :facilityUserId"),
    @NamedQuery(name = "FacilityUser.findByFederalId", query = "SELECT f FROM FacilityUser f WHERE f.federalId = :fedId"),
    @NamedQuery(name = "FacilityUser.findByTitle", query = "SELECT f FROM FacilityUser f WHERE f.title = :title"),
    @NamedQuery(name = "FacilityUser.findByInitials", query = "SELECT f FROM FacilityUser f WHERE f.initials = :initials"),
    @NamedQuery(name = "FacilityUser.findByFirstName", query = "SELECT f FROM FacilityUser f WHERE f.firstName = :firstName"),
    @NamedQuery(name = "FacilityUser.findByMiddleName", query = "SELECT f FROM FacilityUser f WHERE f.middleName = :middleName"),
    @NamedQuery(name = "FacilityUser.findByLastName", query = "SELECT f FROM FacilityUser f WHERE f.lastName = :lastName"),
    @NamedQuery(name = "FacilityUser.findByModTime", query = "SELECT f FROM FacilityUser f WHERE f.modTime = :modTime"),
    @NamedQuery(name = "FacilityUser.findByModId", query = "SELECT f FROM FacilityUser f WHERE f.modId = :modId")
})
        @XmlRootElement
        public class FacilityUser extends EntityBaseBean implements Serializable {
    
    @Id
    @XmlTransient
    @Column(name = "FACILITY_USER_ID", nullable = false)
    private String facilityUserId;
    
    @Column(name = "FEDERAL_ID")
    private String federalId;
    
    @Column(name = "TITLE")
    private String title;
    
    @Column(name = "INITIALS")
    private String initials;
    
    @Column(name = "FIRST_NAME")
    private String firstName;
    
    @Column(name = "MIDDLE_NAME")
    private String middleName;
    
    @Column(name = "LAST_NAME")
    private String lastName;
    
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "facilityUser")
    private Collection<Investigator> investigatorCollection;
    
    /** Creates a new instance of FacilityUser */
    public FacilityUser() {
    }
    
    /**
     * Creates a new instance of FacilityUser with the specified values.
     * @param facilityUserId the facilityUserId of the FacilityUser
     */
    public FacilityUser(String facilityUserId) {
        this.facilityUserId = facilityUserId;
    }
    
    /**
     * Creates a new instance of FacilityUser with the specified values.
     * @param facilityUserId the facilityUserId of the FacilityUser
     * @param modTime the modTime of the FacilityUser
     * @param modId the modId of the FacilityUser
     */
    public FacilityUser(String facilityUserId, Date modTime, String modId) {
        this.facilityUserId = facilityUserId;
        this.modTime = modTime;
        this.modId = modId;
    }
    
    /**
     * Gets the facilityUserId of this FacilityUser.
     * @return the facilityUserId
     */
    public String getFacilityUserId() {
        return this.facilityUserId;
    }
    
    /**
     * Sets the facilityUserId of this FacilityUser to the specified value.
     * @param facilityUserId the new facilityUserId
     */
    public void setFacilityUserId(String facilityUserId) {
        this.facilityUserId = facilityUserId;
    }
    
    /**
     * Gets the federalId of this FacilityUser.
     * @return the federalId
     */
    public String getFederalId() {
        return this.federalId;
    }
    
    /**
     * Sets the federalId of this FacilityUser to the specified value.
     * @param federalId the new federalId
     */
    public void setFederalId(String federalId) {
        this.federalId = federalId;
    }
    
    /**
     * Gets the title of this FacilityUser.
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Sets the title of this FacilityUser to the specified value.
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the initials of this FacilityUser.
     * @return the initials
     */
    public String getInitials() {
        return this.initials;
    }
    
    /**
     * Sets the initials of this FacilityUser to the specified value.
     * @param initials the new initials
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }
    
    /**
     * Gets the firstName of this FacilityUser.
     * @return the firstName
     */
    public String getFirstName() {
        return this.firstName;
    }
    
    /**
     * Sets the firstName of this FacilityUser to the specified value.
     * @param firstName the new firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * Gets the middleName of this FacilityUser.
     * @return the middleName
     */
    public String getMiddleName() {
        return this.middleName;
    }
    
    /**
     * Sets the middleName of this FacilityUser to the specified value.
     * @param middleName the new middleName
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    /**
     * Gets the lastName of this FacilityUser.
     * @return the lastName
     */
    public String getLastName() {
        return this.lastName;
    }
    
    /**
     * Sets the lastName of this FacilityUser to the specified value.
     * @param lastName the new lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Gets the investigatorCollection of this FacilityUser.
     * @return the investigatorCollection
     */
    @XmlTransient
    public Collection<Investigator> getInvestigatorCollection() {
        return this.investigatorCollection;
    }
    
    /**
     * Sets the investigatorCollection of this FacilityUser to the specified value.
     * @param investigatorCollection the new investigatorCollection
     */
    public void setInvestigatorCollection(Collection<Investigator> investigatorCollection) {
        this.investigatorCollection = investigatorCollection;
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
        hash += (this.facilityUserId != null ? this.facilityUserId.hashCode() : 0);
        return hash;
    }
    
    /**
     * Determines whether another object is equal to this FacilityUser.  The result is
     * <code>true</code> if and only if the argument is not null and is a FacilityUser object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FacilityUser)) {
            return false;
        }
        FacilityUser other = (FacilityUser)object;
        if (this.facilityUserId != other.facilityUserId && (this.facilityUserId == null || !this.facilityUserId.equals(other.facilityUserId))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "FacilityUser[facilityUserId=" + facilityUserId + "]";
    }
    
}
