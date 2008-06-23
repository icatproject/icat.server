/*
 * To change this template, choose Tools | Templates
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
@Embeddable
public class FacilityInstrumentScientistPK extends EntityPrimaryKeyBaseBean implements Serializable {
   
    @Column(name = "INSTRUMENT_NAME", nullable = false)
    private String instrumentName;
    
    @Column(name = "FEDERAL_ID", nullable = false)
    private String federalId;

    public FacilityInstrumentScientistPK() {
    }

    public FacilityInstrumentScientistPK(String instrumentName, String federalId) {
        this.instrumentName = instrumentName;
        this.federalId = federalId;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getFederalId() {
        return federalId;
    }

    public void setFederalId(String federalId) {
        this.federalId = federalId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (instrumentName != null ? instrumentName.hashCode() : 0);
        hash += (federalId != null ? federalId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FacilityInstrumentScientistPK)) {
            return false;
        }
        FacilityInstrumentScientistPK other = (FacilityInstrumentScientistPK) object;
        if ((this.instrumentName == null && other.instrumentName != null) || (this.instrumentName != null && !this.instrumentName.equals(other.instrumentName))) {
            return false;
        }
        if ((this.federalId == null && other.federalId != null) || (this.federalId != null && !this.federalId.equals(other.federalId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FacilityInstrumentScientistPK[instrumentName=" + instrumentName + ", federalId=" + federalId + "]";
    }

}
