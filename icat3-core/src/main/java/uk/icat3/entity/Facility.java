package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "THIS_ICAT")
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
   
    @Column(name = "DAYS_UNTIL_PUBLIC_RELEASE", nullable = false)
    private Long daysUntilRelease;
    
    public Facility() {
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
        return "Facility[facilityShortName=" + facilityShortName + "]";
    }

    public Long getDaysUntilRelease() {
        return daysUntilRelease;
    }

    public void setDaysUntilRelease(Long daysUntilRelease) {
        this.daysUntilRelease = daysUntilRelease;
    }

	@Override
	public Object getPK() {
		return facilityShortName;
	}

}
