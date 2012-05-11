package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("A sample to be used in an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME",
		"FACILITY_ID" }) })
@TableGenerator(name = "sampleTypeGenerator", pkColumnValue = "SampleType")
public class SampleType extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(SampleType.class);

	@Comment("The facility which has defined this sample type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "sampleTypeGenerator")
	private Long id;

	@Comment("The formula written as a string -e.g. C2H6O2 for ethylene glycol")
	private String molecularFormula;

	@Column(nullable = false, name = "NAME")
	private String name;

	@Comment("Any safety information related to this sample")
	@Column(length = 4000)
	private String safetyInformation;

	@OneToMany(mappedBy = "type")
	private List<Sample> samples;

	/* Needed for JPA */
	public SampleType() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Sample for " + this.includes);
		if (!this.includes.contains(Sample.class)) {
			this.samples = null;
		}
		if (!this.includes.contains(Facility.class)) {
			this.facility = null;
		}
	}

	public Facility getFacility() {
		return facility;
	}

	public Long getId() {
		return this.id;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getPK() {
		return this.id;
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

	public void setId(Long id) {
		this.id = id;
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
	public String toString() {
		return "SampleType[id=" + this.id + "]";
	}
}
