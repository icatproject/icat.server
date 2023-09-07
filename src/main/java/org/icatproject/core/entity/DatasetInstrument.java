package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between an instrument and a dataset with data collected at that instrument")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATASET_ID", "INSTRUMENT_ID" }) })
public class DatasetInstrument extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATASET_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Dataset dataset;

	@JoinColumn(name = "INSTRUMENT_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Instrument instrument;

	public Dataset getDataset() {
		return dataset;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}
}
