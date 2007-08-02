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
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
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
    @NamedQuery(name = Queries.ICAT_AUTHORISATION_FINDBY_NULL, query = Queries.ICAT_AUTHORISATION_FINDBY_NULL_JPQL),
    @NamedQuery(name = "IcatAuthorisation.findAllById", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = :elementType AND i.elementId = :id AND i.markedDeleted = 'N'"),
     @NamedQuery(name =Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE, query = Queries.ICAT_AUTHORISATION_FINDBY_UNIQUE_JPQL)
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
