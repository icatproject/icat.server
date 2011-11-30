/*
 * Investigator.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.ValidationException;

/**
 * Entity class Investigator
 *
 * @author gjd37
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "INVESTIGATOR")
@NamedQueries( {
    @NamedQuery(name = "Investigator.findByInvestigationId", query = "SELECT i FROM Investigator i WHERE i.investigatorPK.investigationId = :investigationId"),
    @NamedQuery(name = "Investigator.findByFacilityUserId", query = "SELECT i FROM Investigator i WHERE i.investigatorPK.facilityUserId = :facilityUserId"),
    @NamedQuery(name = "Investigator.findByModTime", query = "SELECT i FROM Investigator i WHERE i.modTime = :modTime"),
    @NamedQuery(name = "Investigator.findByModId", query = "SELECT i FROM Investigator i WHERE i.modId = :modId")
})
@XmlRootElement
public class Investigator extends EntityBaseBean implements Serializable {
    
    /**
     * EmbeddedId primary key field
     */
    @EmbeddedId
    protected InvestigatorPK investigatorPK;
    
    @Column(name = "ROLE")
    private String role;
    
    @JoinColumn(name = "FACILITY_USER_ID", referencedColumnName = "FACILITY_USER_ID", insertable = false, updatable = false)
    @ManyToOne
    @XmlTransient
    private FacilityUser facilityUser;
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID",  insertable = false, updatable = false)
    @ManyToOne
    @XmlTransient
    private Investigation investigation;
    
    /* Needed for JPA */
    public Investigator() {
    }
     
    /**
     * Gets the investigatorPK of this Investigator.
     * @return the investigatorPK
     */
    public InvestigatorPK getInvestigatorPK() {
        return this.investigatorPK;
    }
    
    /**
     * Sets the investigatorPK of this Investigator to the specified value.
     * @param investigatorPK the new investigatorPK
     */
    
    public void setInvestigatorPK(InvestigatorPK investigatorPK) {
        this.investigatorPK = investigatorPK;
    }
    
    /**
     * Gets the facilityUser of this Investigator.
     * @return the facilityUser
     */ 
    public FacilityUser getFacilityUser() {
        return this.facilityUser;
    }
    
    /**
     * Gets the role of this FacilityUser.
     * @return the role
     */
    public String getRole() {
        return this.role;
     }
    
    /**
     * Sets the role of this FacilityUser to the specified value.
     * @param role the new role
     */
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Gets the investigation of this Investigator.
     * @return the investigation
     */
    public Investigation getInvestigation() {
        return this.investigation;
    }
    
    /**
     * Returns a hash code value for the object.  This implementation computes
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.investigatorPK != null ? this.investigatorPK.hashCode() : 0);
        return hash;
    }
    
    /**
     * Overrides the isValid function, checks each of the datafiles and datafile parameters are valid
     *
     * @throws ValidationException
     * @return
     * @throws IcatInternalException 
     */
    @Override
    public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException, IcatInternalException {
    	super.isValid(manager,deepValidation);
        //check that the parameter dataset id is the same as actual dataset id
        if(getInvestigation() != null){
            //check embedded primary key
            investigatorPK.isValid();
            
            if(!investigatorPK.getInvestigationId().equals(getInvestigation().getId())){
                throw new ValidationException("Investigator: "+investigatorPK.getFacilityUserId()+" with investigation id: "+investigatorPK.getInvestigationId()+ " that does not corresponds to its parent investigation id: "+getInvestigation().getId());
            }
        } //else //throw new ValidationException("DatasetParameter: "+paramName+" with units: "+paramUnits+" has not dataset id");
        
        //check if facility user exists
        FacilityUser user = manager.find(FacilityUser.class, investigatorPK.getFacilityUserId());
        if(user == null) throw new ValidationException("FacilityUser: "+investigatorPK.getFacilityUserId()+" does not exist");
        
        investigatorPK.isValid();
      
    }
    
    /**
     * Determines whether another object is equal to this Investigator.  The result is
     * <code>true</code> if and only if the argument is not null and is a Investigator object that
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Investigator)) {
            return false;
        }
        Investigator other = (Investigator)object;
        if (this.investigatorPK != other.investigatorPK && (this.investigatorPK == null || !this.investigatorPK.equals(other.investigatorPK))) return false;
        return true;
    }
    
    /**
     * Returns a string representation of the object.  This implementation constructs
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Investigator[investigatorPK=" + investigatorPK + "]";
    }

	@Override
	public Object getPK() {
		return investigatorPK;
	}
    
}