/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.apache.log4j.Logger;
import uk.icat3.util.ElementType;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "FACILITY_INSTRUMENT_SCIENTIST")
@NamedQueries({
    @NamedQuery(name = "FacilityInstrumentScientist.findByUserAndInstrument", query = "SELECT f FROM FacilityInstrumentScientist f WHERE f.facilityInstrumentScientistPK.instrumentName = :instrumentName AND f.facilityInstrumentScientistPK.federalId = :federalId"),    
    @NamedQuery(name = "FacilityInstrumentScientist.findByInstrumentName", query = "SELECT f FROM FacilityInstrumentScientist f WHERE f.facilityInstrumentScientistPK.instrumentName = :instrumentName"), 
    @NamedQuery(name = "FacilityInstrumentScientist.findByFederalId", query = "SELECT f FROM FacilityInstrumentScientist f WHERE f.facilityInstrumentScientistPK.federalId = :federalId") 
 })
public class FacilityInstrumentScientist extends EntityBaseBean implements Serializable {
    
    /**
     * Override logger
     */
    protected static Logger log = Logger.getLogger(FacilityInstrumentScientist.class);
    
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected FacilityInstrumentScientistPK facilityInstrumentScientistPK;
       
    public FacilityInstrumentScientist() {
    }

    public FacilityInstrumentScientist(FacilityInstrumentScientistPK facilityInstrumentScientistPK) {
        this.facilityInstrumentScientistPK = facilityInstrumentScientistPK;
    }
   
    public FacilityInstrumentScientist(String instrumentName, String federalId) {
        this.facilityInstrumentScientistPK = new FacilityInstrumentScientistPK(instrumentName, federalId);
    }

    public FacilityInstrumentScientistPK getFacilityInstrumentScientistPK() {
        return facilityInstrumentScientistPK;
    }

    public void setFacilityInstrumentScientistPK(FacilityInstrumentScientistPK facilityInstrumentScientistPK) {
        this.facilityInstrumentScientistPK = facilityInstrumentScientistPK;
    }
  
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (facilityInstrumentScientistPK != null ? facilityInstrumentScientistPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FacilityInstrumentScientist)) {
            return false;
        }
        FacilityInstrumentScientist other = (FacilityInstrumentScientist) object;
        if ((this.facilityInstrumentScientistPK == null && other.facilityInstrumentScientistPK != null) || (this.facilityInstrumentScientistPK != null && !this.facilityInstrumentScientistPK.equals(other.facilityInstrumentScientistPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FacilityInstrumentScientist[facilityInstrumentScientistPK=" + facilityInstrumentScientistPK + "]";
    }
   
    /**
     * Gets the element type of the bean
     */
     @Override
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
    }

}
