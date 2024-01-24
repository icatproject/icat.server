package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("A sample to be used in an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME",
		"MOLECULARFORMULA" }) })
public class SampleType extends EntityBaseBean implements Serializable {

	@Comment("The facility which has defined this sample type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@Comment("The formula written as a string -e.g. C2H6O2 for ethylene glycol")
	@Column(nullable = false, name = "MOLECULARFORMULA")
	private String molecularFormula;

	@Column(nullable = false, name = "NAME")
	private String name;

	@Comment("Any safety information related to this sample")
	@Column(length = 4000)
	private String safetyInformation;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<Sample> samples = new ArrayList<>();

	/* Needed for JPA */
	public SampleType() {
	}

	public Facility getFacility() {
		return facility;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public String getName() {
		return this.name;
	}

	public String getSafetyInformation() {
		return this.safetyInformation;//
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSafetyInformation(String safetyInformation) {
		this.safetyInformation = safetyInformation;
	}

	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}

}
