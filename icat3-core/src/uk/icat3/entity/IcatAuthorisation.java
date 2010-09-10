/*
 * IcatAuthorisation.java
 *
 * Created on 24-Jul-2007, 10:44:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.ElementType;
import uk.icat3.util.Queries;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "ICAT_AUTHORISATION")
@NamedQueries({
    @NamedQuery(name = "IcatAuthorisation.findById", query = "SELECT i FROM IcatAuthorisation i WHERE i.id = :id"),
    @NamedQuery(name = "IcatAuthorisation.findByUserId", query = "SELECT i FROM IcatAuthorisation i WHERE i.userId = :userId"),
    @NamedQuery(name = "IcatAuthorisation.findByElementType", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = :elementType"),
    @NamedQuery(name = "IcatAuthorisation.findByElementId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findByParentElementType", query = "SELECT i FROM IcatAuthorisation i WHERE i.parentElementType = :parentElementType"),
    @NamedQuery(name = "IcatAuthorisation.findByParentElementId", query = "SELECT i FROM IcatAuthorisation i WHERE i.parentElementId = :parentElementId"),
    @NamedQuery(name = "IcatAuthorisation.findByModTime", query = "SELECT i FROM IcatAuthorisation i WHERE i.modTime = :modTime"),
    @NamedQuery(name = "IcatAuthorisation.findByModId", query = "SELECT i FROM IcatAuthorisation i WHERE i.modId = :modId"),
    @NamedQuery(name = "IcatAuthorisation.findByCreateTime", query = "SELECT i FROM IcatAuthorisation i WHERE i.createTime = :createTime"),
    @NamedQuery(name = "IcatAuthorisation.findByCreateId", query = "SELECT i FROM IcatAuthorisation i WHERE i.createId = :createId"),
    @NamedQuery(name = "IcatAuthorisation.findByFacilityAcquired", query = "SELECT i FROM IcatAuthorisation i WHERE i.facilityAcquired = :facilityAcquired"),
    @NamedQuery(name = "IcatAuthorisation.findByDeleted", query = "SELECT i FROM IcatAuthorisation i WHERE i.markedDeleted = :deleted"),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY, query = Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_CREATE, query = Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_CREATE_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDALL_FOR_ELEMENTTYPE, query = Queries.ICAT_AUTHORISATION_FINDALL_FOR_ELEMENTTYPE_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_ELEMENTID, query = Queries.ICAT_AUTHORISATION_FINDBY_ELEMENTID_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_CREATE_DATAFILE_DATASET, query = Queries.ICAT_AUTHORISATION_FINDBY_CREATE_DATAFILE_DATASET_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_CREATE_INVESTIGATION, query = Queries.ICAT_AUTHORISATION_FINDBY_CREATE_INVESTIGATION_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_INVESTIGATION, query = Queries.ICAT_AUTHORISATION_FINDBY_INVESTIGATION_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_DATAFILE_DATASET, query = Queries.ICAT_AUTHORISATION_FINDBY_DATAFILE_DATASET_JPQL),
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_ELEMENTID_ELEMENTTYPE_USERID, query=Queries.ICAT_AUTHORISATION_FINDBY_ELEMENTID_ELEMENTTYPE_USERID_JPQL)
})
        @SequenceGenerator(name="ICAT_AUTHORISATION_SEQ",sequenceName="ICAT_AUTHORISATION_ID_SEQ",allocationSize=1)
        public class IcatAuthorisation extends EntityBaseBean implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ICAT_AUTHORISATION_SEQ")
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "USER_ID", nullable = false)
    private String userId;
    
    @Column(name = "ELEMENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ElementType elementType;
    
    @Column(name = "ELEMENT_ID")
    private Long elementId;
    
    @Column(name = "PARENT_ELEMENT_TYPE")
    @Enumerated(EnumType.STRING)
    private ElementType parentElementType;
    
    @Column(name = "PARENT_ELEMENT_ID")
    private Long parentElementId;
    
    @Column(name = "USER_CHILD_RECORD")
    private Long userChildRecord;
    
    @JoinColumn(name = "ROLE", referencedColumnName = "ROLE")
    @ManyToOne
    private IcatRole role;
    
    
    public IcatAuthorisation() {
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public IcatRole getRole() {
        return role;
    }
    
    public void setRole(IcatRole role) {
        this.role = role;
    }
    
    public ElementType getElementType() {
        return elementType;
    }
    
    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }
    
    public Long getElementId() {
        return elementId;
    }
    
    public void setElementId(Long elementId) {
        this.elementId = elementId;
    }
    
    @XmlTransient
    public ElementType getParentElementType() {
        return parentElementType;
    }
    
    public void setParentElementType(ElementType parentElementType) {
        this.parentElementType = parentElementType;
    }
    
    @XmlTransient
    public Long getParentElementId() {
        return parentElementId;
    }
    
    public void setParentElementId(Long parentElementId) {
        this.parentElementId = parentElementId;
    }
    
    public Long getUserChildRecord() {
        return userChildRecord;
    }
    
    public void setUserChildRecord(Long userChildRecord) {
        this.userChildRecord = userChildRecord;
    }
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
    }
    
    /**
     * Checks weather the sample is unique in the database.
     */
    private boolean isUnique(EntityManager manager) throws ValidationException {
        log.trace("Checking if "+this+" is unique");
        try {
            Query query = null;
            if(elementId == null){
                //need to search looking for IS null NOT = to null
                query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY_CREATE);
            } else {
                query = manager.createNamedQuery(Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE_KEY);
                query.setParameter("elementId", elementId);
            }
            query.setParameter("userId", userId).
                    setParameter("elementType", elementType).
                    setParameter("parentElementId", parentElementId).
                    setParameter("parentElementType", parentElementType);
            
            IcatAuthorisation icatAuthorisation = (IcatAuthorisation) query.getSingleResult();
            log.trace("Found: "+icatAuthorisation);
            if(id != null && icatAuthorisation.getId().equals(id)) return true;
            throw new ValidationException(this+" is not unique.  Same unique key as "+icatAuthorisation);
        } catch(NoResultException nre) {
            log.trace("NoResultException: so is unique");
            return true;
        }
    }
    
    /**
     * Overrides the isValid function,
     *
     * @throws ValidationException
     * @return
     */
    @Override
    public boolean isValid(EntityManager manager) throws ValidationException {
        if(manager == null) throw new IllegalArgumentException("EntityManager cannot be null");
        
        //check if unique
        isUnique(manager);
        
        //once here then its valid
        return isValid();
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IcatAuthorisation)) {
            return false;
        }
        IcatAuthorisation other = (IcatAuthorisation) object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "IcatAuthorisation[id="+id+"]";
    }
    
}
