package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("Used by a user within an investigation")
@SuppressWarnings("serial")
@Entity
public class Instrument extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Instrument.class);

	@Comment("A description of this instrument")
	private String description;

	@Comment("The formal name of this instrument")
	private String fullName;

	@OneToMany(mappedBy = "instrument")
	private List<InstrumentScientist> instrumentScientists;

	@OneToMany(mappedBy = "instrument")
	private List<Investigation> investigations;

	@Comment("A short name identifying this instrument")
	@Id
	private String name;

	private String type;

	// Needed for JPA
	public Instrument() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Instrument for " + this.includes);
		if (!this.includes.contains(InstrumentScientist.class)) {
			this.instrumentScientists = null;
		}
		if (!this.includes.contains(Investigation.class)) {
			this.investigations = null;
		}
	}

	public String getDescription() {
		return this.description;
	}

	public String getFullName() {
		return this.fullName;
	}

	public List<Investigation> getInvestigations() {
		return this.investigations;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getPK() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public List<InstrumentScientist> getInstrumentScientists() {
		return instrumentScientists;
	}

	public void setInstrumentScientists(List<InstrumentScientist> instrumentScientists) {
		this.instrumentScientists = instrumentScientists;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Instrument[name=" + this.name + "]";
	}

}
