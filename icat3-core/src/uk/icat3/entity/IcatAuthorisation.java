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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "ICAT_AUTHORISATION")
@NamedQueries({
    @NamedQuery(name = "IcatAuthorisation.findByInvestigationIdPK", query = "SELECT i FROM IcatAuthorisation i WHERE i.investigationId = :investigationId"),
    @NamedQuery(name = "IcatAuthorisation.findByUserId", query = "SELECT i FROM IcatAuthorisation i WHERE i.userId = :userId"),
    @NamedQuery(name = "IcatAuthorisation.findByElementType", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = :elementType"),
    @NamedQuery(name = "IcatAuthorisation.findByElementId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findByInvestigationId", query = "SELECT i FROM IcatAuthorisation i WHERE i.investigation.id = :investigationId AND i.elementType = 'INVESTIGATION' AND i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findByDatasetId", query = "SELECT i FROM IcatAuthorisation i WHERE i.investigation.id = :investigationId  AND i.elementType = 'DATASET' AND i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findByDatafileId", query = "SELECT i FROM IcatAuthorisation i WHERE i.investigation.id = :investigationId AND i.elementType = 'DATAFILE' AND i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findById", query = "SELECT i FROM IcatAuthorisation i WHERE i.investigation.id = :investigationId AND i.elementType = :elementType AND i.elementId = :elementId AND i.userId = :userId AND i.markedDeleted = 'N'"),
    @NamedQuery(name = "IcatAuthorisation.findByIdNullInvestigationId", query = "SELECT i FROM IcatAuthorisation i WHERE i.investigationId IS null AND i.elementType = :elementType AND i.elementId IS null AND i.userId = :userId AND i.markedDeleted = 'N'"),
    @NamedQuery(name = "IcatAuthorisation.findAllByInvestigationId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = 'INVESTIGATION' AND i.elementId = :id AND i.markedDeleted = 'N'"),
    @NamedQuery(name = "IcatAuthorisation.findAllByDatasetId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = 'DATASET' AND i.elementId = :id AND i.markedDeleted = 'N'"),
    @NamedQuery(name = "IcatAuthorisation.findAllByDatafileId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = 'DATAFILE' AND i.elementId = :id AND i.markedDeleted = 'N'")
})
        public class IcatAuthorisation extends EntityBaseBean implements Serializable {
    
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "USER_ID", nullable = false)
    private String userId;
        
        @Column(name = "ELEMENT_TYPE", nullable = false)
    private String elementType;
    
    @Column(name = "ELEMENT_ID")
    private Long elementId;
    
    @Column(name = "PARENT_ELEMENT_TYPE")
    private String parentElementType;
    
    @Column(name = "PARENT_ELEMENT_ID")
    private Long parentElementId;
    
    @JoinColumn(name = "ROLE", referencedColumnName = "ROLE")
    @ManyToOne
    private IcatRole role;
    
    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private Investigation investigation;
    
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
    
    public String getElementType() {
        return elementType;
    }
    
    public void setElementType(String elementType) {
        this.elementType = elementType;
    }
    
    public Long getElementId() {
        return elementId;
    }
    
    public void setElementId(Long elementId) {
        this.elementId = elementId;
    }
    
    public String getParentElementType() {
        return parentElementType;
    }
    
    public void setParentElementType(String parentElementType) {
        this.parentElementType = parentElementType;
    }
    
    public Long getParentElementId() {
        return parentElementId;
    }
    
    public void setParentElementId(Long parentElementId) {
        this.parentElementId = parentElementId;
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
