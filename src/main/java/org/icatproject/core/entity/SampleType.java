package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public static Set<String> docFields = new HashSet<>(Arrays.asList("sample.type.name", "type.id"));

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

	@Override
	public void getDoc(JsonGenerator gen) {
		SearchApi.encodeString(gen, "type.name", name);
		SearchApi.encodeString(gen, "type.id", id);
	}


	/**
	 * Alternative method for encoding that applies a prefix to potentially
	 * ambiguous fields: "type.id". In the case of a single
	 * Dataset Sample, this fields will already be used by the Dataset and so
	 * cannot be overwritten by the Sample.
	 * 
	 * @param gen    JsonGenerator
	 * @param prefix String to precede all ambiguous field names.
	 */
	public void getDoc(JsonGenerator gen, String prefix) {
		SearchApi.encodeString(gen, prefix + "type.name", name);
		SearchApi.encodeString(gen, prefix + "type.id", id);
	}

}
