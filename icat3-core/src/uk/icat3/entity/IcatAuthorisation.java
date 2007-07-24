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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "ICAT_AUTHORISATION")
@NamedQueries({
    @NamedQuery(name = "IcatAuthorisation.findByInvestigationIdPK", query = "SELECT i FROM IcatAuthorisation i WHERE i.icatAuthorisationPK.investigationId = :investigationId"), 
    @NamedQuery(name = "IcatAuthorisation.findByUserId", query = "SELECT i FROM IcatAuthorisation i WHERE i.icatAuthorisationPK.userId = :userId"),
    @NamedQuery(name = "IcatAuthorisation.findByElementType", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = :elementType"),
    @NamedQuery(name = "IcatAuthorisation.findByElementId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findByInvestigationId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = 'INVESTIGATION' AND i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findByDatasetId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = 'DATASET' AND i.elementId = :elementId"),
    @NamedQuery(name = "IcatAuthorisation.findByDatafileId", query = "SELECT i FROM IcatAuthorisation i WHERE i.elementType = 'DATAFILE' AND i.elementId = :elementId")
})
public class IcatAuthorisation extends EntityBaseBean implements Serializable {

    @EmbeddedId
    protected IcatAuthorisationPK icatAuthorisationPK;

    @Column(name = "ELEMENT_TYPE")
    private String elementType;

    @Column(name = "ELEMENT_ID")
    private Long elementId;
  
    @JoinColumn(name = "ROLE", referencedColumnName = "ROLE")
    @ManyToOne
    private IcatRole role;

    @JoinColumn(name = "INVESTIGATION_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @ManyToOne
    private Investigation investigation;

    public IcatAuthorisation() {
    }

    public IcatAuthorisation(IcatAuthorisationPK icatAuthorisationPK) {
        this.icatAuthorisationPK = icatAuthorisationPK;
    }

    public IcatAuthorisation(IcatAuthorisationPK icatAuthorisationPK, Date modTime, String modId, Date createTime, String createId, String facilityAcquired, String deleted) {
        this.icatAuthorisationPK = icatAuthorisationPK;
        this.modTime = modTime;
        this.modId = modId;
        this.createTime = createTime;
        this.createId = createId;
        this.facilityAcquired = facilityAcquired;
        this.markedDeleted = deleted;
    }

    public IcatAuthorisation(Long investigationId, String userId) {
        this.icatAuthorisationPK = new IcatAuthorisationPK(investigationId, userId);
    }

    public IcatAuthorisationPK getIcatAuthorisationPK() {
        return icatAuthorisationPK;
    }

    public void setIcatAuthorisationPK(IcatAuthorisationPK icatAuthorisationPK) {
        this.icatAuthorisationPK = icatAuthorisationPK;
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
    
    public IcatRole getRole() {
        return role;
    }

    public void setRole(IcatRole role) {
        this.role = role;
    }

    @XmlTransient
    public Investigation getInvestigation() {
        return investigation;
    }

    public void setInvestigation(Investigation investigation) {
        this.investigation = investigation;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (icatAuthorisationPK != null ? icatAuthorisationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IcatAuthorisation)) {
            return false;
        }
        IcatAuthorisation other = (IcatAuthorisation) object;
        if (this.icatAuthorisationPK != other.icatAuthorisationPK && (this.icatAuthorisationPK == null || !this.icatAuthorisationPK.equals(other.icatAuthorisationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IcatAuthorisation[icatAuthorisationPK=" + icatAuthorisationPK + "]";
    }

}
