package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.search.SearchApi;

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

	@Override
	public void getDoc(EntityManager manager, JsonGenerator gen) throws IcatException {
		if (instrument.getName() == null) {
			instrument = manager.find(instrument.getClass(), instrument.id);
		}
		instrument.getDoc(manager, gen);
		SearchApi.encodeLong(gen, "investigation.id", investigation.id);
		SearchApi.encodeLong(gen, "id", id);
	}

}
