package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
public class Instrument extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Instrument.class);

	@Id
	private String name;

	private String type;

	private String description;

	private String fullName;

	@OneToMany(mappedBy = "instrument")
	private List<Investigation> investigations;

	@OneToMany(mappedBy = "instrument")
	private List<FacilityInstrumentScientist> facilityInstrumentScientists;

	// Needed for JPA
	public Instrument() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public List<Investigation> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	public List<FacilityInstrumentScientist> getFacilityInstrumentScientists() {
		return facilityInstrumentScientists;
	}

	public void setFacilityInstrumentScientists(List<FacilityInstrumentScientist> facilityInstrumentScientists) {
		this.facilityInstrumentScientists = facilityInstrumentScientists;
	}

	@Override
	public String toString() {
		return "Instrument[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Instrument for " + includes);
		if (!this.includes.contains(FacilityInstrumentScientist.class)) {
			this.facilityInstrumentScientists = null;
		}
		if (!this.includes.contains(Investigation.class)) {
			this.investigations = null;
		}
	}

}
