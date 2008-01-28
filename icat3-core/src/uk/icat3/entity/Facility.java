/*
 * ThisIcat.java
 * 
 * Created on 31-Jul-2007, 13:56:24
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import uk.icat3.util.ElementType;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "THIS_ICAT")
@NamedQueries({
    @NamedQuery(name = "Facility.findByFacilityShortName", query = "SELECT t FROM Facility t WHERE t.facilityShortName = :facilityShortName"), 
    @NamedQuery(name = "Facility.findByFacilityLongName", query = "SELECT t FROM Facility t WHERE t.facilityLongName = :facilityLongName"),
    @NamedQuery(name = "Facility.findByFacilityUrl", query = "SELECT t FROM Facility t WHERE t.facilityUrl = :facilityUrl"), 
    @NamedQuery(name = "Facility.findByFacilityDescription", query = "SELECT t FROM Facility t WHERE t.facilityDescription = :facilityDescription"), 
    @NamedQuery(name = "Facility.findByModId", query = "SELECT t FROM Facility t WHERE t.modId = :modId"), 
    @NamedQuery(name = "Facility.findByModTime", query = "SELECT t FROM Facility t WHERE t.modTime = :modTime"),
    @NamedQuery(name = "Facility.findByCreateId", query = "SELECT t FROM Facility t WHERE t.createId = :createId"), 
    @NamedQuery(name = "Facility.findByCreateTime", query = "SELECT t FROM Facility t WHERE t.createTime = :createTime"), 
    @NamedQuery(name = "Facility.findByDeleted", query = "SELECT t FROM Facility t WHERE t.markedDeleted = :deleted"),
    @NamedQuery(name = "Facility.findByFacilityAcquired", query = "SELECT t FROM Facility t WHERE t.facilityAcquired = :facilityAcquired")
})
public class Facility extends EntityBaseBean implements Serializable {

    @Id
    @Column(name = "FACILITY_SHORT_NAME", nullable = false)
    private String facilityShortName;

    @Column(name = "FACILITY_LONG_NAME")
    private String facilityLongName;

    @Column(name = "FACILITY_URL")
    private String facilityUrl;

    @Column(name = "FACILITY_DESCRIPTION")
    private String facilityDescription;
   
    @Column(name = "DAYS_UNTIL_PUBLIC_RELEASE")
    private Long daysUntilRelease;
    
    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "facility")
    private Collection<Investigation> investigationCollection;*/

    public Facility() {
    }

    public Facility(String facilityShortName) {
        this.facilityShortName = facilityShortName;
    }
   
    public String getFacilityShortName() {
        return facilityShortName;
    }

    public void setFacilityShortName(String facilityShortName) {
        this.facilityShortName = facilityShortName;
    }

    public String getFacilityLongName() {
        return facilityLongName;
    }

    public void setFacilityLongName(String facilityLongName) {
        this.facilityLongName = facilityLongName;
    }

    public String getFacilityUrl() {
        return facilityUrl;
    }

    public void setFacilityUrl(String facilityUrl) {
        this.facilityUrl = facilityUrl;
    }

    public String getFacilityDescription() {
        return facilityDescription;
    }

    public void setFacilityDescription(String facilityDescription) {
        this.facilityDescription = facilityDescription;
    }
  
    /*@XmlTransient
    public Collection<Investigation> getInvestigationCollection() {
        return investigationCollection;
    }*/

    /*public void setInvestigationCollection(Collection<Investigation> investigationCollection) {
        this.investigationCollection = investigationCollection;
    }*/

    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (facilityShortName != null ? facilityShortName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Facility)) {
            return false;
        }
        Facility other = (Facility) object;
        if (this.facilityShortName != other.facilityShortName && (this.facilityShortName == null || !this.facilityShortName.equals(other.facilityShortName))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ThisIcat[facilityShortName=" + facilityShortName + "]";
    }

    public Long getDaysUntilRelease() {
        return daysUntilRelease;
    }

    public void setDaysUntilRelease(Long daysUntilRelease) {
        this.daysUntilRelease = daysUntilRelease;
    }

}
