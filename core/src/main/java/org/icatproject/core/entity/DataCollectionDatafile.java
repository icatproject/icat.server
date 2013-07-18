package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between a DataCollection and its Datafiles.")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATACOLLECTION_ID", "DATAFILE_ID" }) })
public class DataCollectionDatafile extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATACOLLECTION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataCollection dataCollection;

	@JoinColumn(name = "DATAFILE_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Datafile datafile;

	public DataCollection getDataCollection() {
		return dataCollection;
	}

	public Datafile getDatafile() {
		return datafile;
	}

	public void setDataCollection(DataCollection dataCollection) {
		this.dataCollection = dataCollection;
	}

	public void setDatafile(Datafile datafile) {
		this.datafile = datafile;
	}

}
