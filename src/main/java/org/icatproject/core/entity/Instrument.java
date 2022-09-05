package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.json.stream.JsonGenerator;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.icatproject.core.manager.search.SearchApi;

@Comment("Used by a user within an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME" }) })
public class Instrument extends EntityBaseBean implements Serializable {

	@Comment("A persistent identifier attributed to this instrument")
	private String pid;

	@Comment("A description of this instrument")
	@Column(length = 4000)
	private String description;

	@Comment("The facility which has this instrument")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@Comment("The formal name of this instrument")
	private String fullName;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument")
	private List<InstrumentScientist> instrumentScientists = new ArrayList<InstrumentScientist>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument")
	private List<InvestigationInstrument> investigationInstruments = new ArrayList<InvestigationInstrument>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument")
	private List<DatasetInstrument> datasetInstruments = new ArrayList<DatasetInstrument>();

	@Comment("A short name identifying this instrument within the facility")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("A URL associated with this instrument")
	private String url;

	@Comment("Shifts associated with this instrument")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument")
	private List<Shift> shifts = new ArrayList<Shift>();

	private String type;

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	// Needed for JPA
	public Instrument() {
	}

	public String getDescription() {
		return this.description;
	}

	public Facility getFacility() {
		return facility;
	}

	public String getFullName() {
		return this.fullName;
	}

	public List<InstrumentScientist> getInstrumentScientists() {
		return instrumentScientists;
	}

	public List<InvestigationInstrument> getInvestigationInstruments() {
		return investigationInstruments;
	}

	public List<DatasetInstrument> getDatasetInstruments() {
		return datasetInstruments;
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	public List<Shift> getShifts() {
		return this.shifts;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setInstrumentScientists(List<InstrumentScientist> instrumentScientists) {
		this.instrumentScientists = instrumentScientists;
	}

	public void setInvestigationInstruments(List<InvestigationInstrument> investigationInstruments) {
		this.investigationInstruments = investigationInstruments;
	}

	public void setDatasetInstruments(List<DatasetInstrument> datasetInstruments) {
		this.datasetInstruments = datasetInstruments;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setShifts(List<Shift> shifts) {
		this.shifts = shifts;
	}

	@Override
	public void getDoc(JsonGenerator gen) {
		if (fullName != null) {
			SearchApi.encodeText(gen, "instrument.fullName", fullName);
		}
		SearchApi.encodeString(gen, "instrument.name", name);
		SearchApi.encodeString(gen, "instrument.id", id);
	}

}
