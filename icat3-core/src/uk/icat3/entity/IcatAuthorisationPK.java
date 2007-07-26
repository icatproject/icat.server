/*
 * IcatAuthorisationPK.java
 * 
 * Created on 24-Jul-2007, 10:44:52
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author gjd37
 */
//@Embeddable
/*public class IcatAuthorisationPK extends EntityPrimaryKeyBaseBean implements Serializable {

    @Column(name = "INVESTIGATION_ID", nullable = false)
    private Long investigationId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    public IcatAuthorisationPK() {
    }

    public IcatAuthorisationPK(Long investigationId, String userId) {
        this.investigationId = investigationId;
        this.userId = userId;
    }

    public Long getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (investigationId != null ? investigationId.hashCode() : 0);
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IcatAuthorisationPK)) {
            return false;
        }
        IcatAuthorisationPK other = (IcatAuthorisationPK) object;
        if (this.investigationId != other.investigationId && (this.investigationId == null || !this.investigationId.equals(other.investigationId))) {
            return false;
        }
        if (this.userId != other.userId && (this.userId == null || !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IcatAuthorisationPK[investigationId=" + investigationId + ", userId=" + userId + "]";
    }

}*/
