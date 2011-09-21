package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Embeddable
public class DatasetParameterPK extends EntityPrimaryKeyBaseBean implements Serializable {

	protected static Logger log = Logger.getLogger(DatasetParameterPK.class);

	@Column(name = "DATASET_ID", nullable = false)
	private Long datasetId;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "UNITS", nullable = false)
	private String units;

	public DatasetParameterPK() {
	}

	public Long getDatasetId() {
		return this.datasetId;
	}

	public void setDatasetId(Long datasetId) {
		this.datasetId = datasetId;
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
		hash += (this.datasetId != null ? this.datasetId.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof DatasetParameterPK)) {
			return false;
		}
		DatasetParameterPK other = (DatasetParameterPK) object;
		if (this.units != other.units && (this.units == null || !this.units.equals(other.units)))
			return false;
		if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
			return false;
		if (this.datasetId != other.datasetId && (this.datasetId == null || !this.datasetId.equals(other.datasetId)))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatasetParameterPK[units=" + units + ", name=" + name + ", datasetId=" + datasetId + "]";
	}

}
