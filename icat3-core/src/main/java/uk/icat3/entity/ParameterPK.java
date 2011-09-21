package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class ParameterPK extends EntityPrimaryKeyBaseBean implements Serializable {

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "UNITS", nullable = false)
	private String units;

	public ParameterPK() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnits() {
		return this.units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (this.units != null ? this.units.hashCode() : 0);
		hash += (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof ParameterPK)) {
			return false;
		}
		ParameterPK other = (ParameterPK) object;
		if (this.units != other.units && (this.units == null || !this.units.equals(other.units)))
			return false;
		if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterPK[units=" + units + ", name=" + name + "]";
	}

}
