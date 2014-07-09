package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between an investigation and the instruments assigned")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "INSTRUMENT_ID" }) })
public class InvestigationInstrument extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "INSTRUMENT_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Instrument instrument;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	public Instrument getInstrument() {
		return instrument;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

}
