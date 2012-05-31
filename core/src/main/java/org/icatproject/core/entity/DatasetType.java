package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("A type of data set")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME" }) })
public class DatasetType extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(DatasetType.class);

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<Dataset> datasets = new ArrayList<Dataset>();

	@Comment("A description of this data set type")
	private String description;

	@Comment("The facility which has defined this data set type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@Comment("A short name identifying this data set type within the facility")
	@Column(name = "NAME", nullable = false)
	private String name;

	/* Needed for JPA */
	public DatasetType() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling DatasetType for " + this.includes);
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
		if (!this.includes.contains(Facility.class)) {
			this.facility = null;
		}
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "DatasetType[name=" + this.name + "]";
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

}
