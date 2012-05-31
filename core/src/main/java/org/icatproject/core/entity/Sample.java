package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("A sample to be used in an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "SAMPLETYPE_ID",
		"INVESTIGATION_ID" }) })
public class Sample extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Sample.class);

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<Dataset> datasets;

	@JoinColumn(nullable = false, name = "INVESTIGATION_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@Column(nullable = false, name = "NAME")
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<SampleParameter> parameters = new ArrayList<SampleParameter>();

	@JoinColumn(name = "SAMPLETYPE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private SampleType type;

	/* Needed for JPA */
	public Sample() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Sample for " + this.includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
		if (!this.includes.contains(SampleParameter.class)) {
			this.parameters = null;
		}
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
		if (!this.includes.contains(SampleType.class)) {
			this.type = null;
		}
	}

	@Override
	public void canDelete(EntityManager manager) {
		// TODO add code to ensure that sample is not part of a dataset nor an
		// investigation
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public Investigation getInvestigation() {
		return this.investigation;
	}

	public String getName() {
		return this.name;
	}

	public List<SampleParameter> getParameters() {
		return parameters;
	}

	@Override
	public void isValid(EntityManager manager, boolean deepValidation) throws IcatException {
		super.isValid(manager, deepValidation);
		if (deepValidation) {
			if (this.parameters != null) {
				for (final SampleParameter sampleParameter : this.parameters) {
					sampleParameter.isValid(manager);
				}
			}
		}
	}

	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		id = null;
		for (SampleParameter sampleParameter : parameters) {
			sampleParameter.preparePersist(modId, manager);
			sampleParameter.setSample(this); // Set back ref
		}
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(List<SampleParameter> parameters) {
		this.parameters = parameters;
	}

	public SampleType getType() {
		return type;
	}

	public void setType(SampleType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Sample[id=" + this.id + "]";
	}
}
