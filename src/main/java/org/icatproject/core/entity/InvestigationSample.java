package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between an investigation and a sample that has been used in that investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "SAMPLE_ID" }) })
public class InvestigationSample extends EntityBaseBean implements Serializable {

	@JoinColumn(nullable = false, name = "INVESTIGATION_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@JoinColumn(nullable = false, name = "SAMPLE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Sample sample;

	public Investigation getInvestigation() {
		return this.investigation;
	}

	public Sample getSample() {
		return this.sample;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}
}