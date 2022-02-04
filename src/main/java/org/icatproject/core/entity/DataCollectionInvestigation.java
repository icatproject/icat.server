package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between a DataCollection and its Investigations.")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATACOLLECTION_ID", "INVESTIGATION_ID" }) })
public class DataCollectionInvestigation extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATACOLLECTION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataCollection dataCollection;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	public DataCollection getDataCollection() {
		return dataCollection;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setDataCollection(DataCollection dataCollection) {
		this.dataCollection = dataCollection;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

}
