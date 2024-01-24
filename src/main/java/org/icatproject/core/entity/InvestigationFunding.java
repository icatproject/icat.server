package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between an investigation and a funding reference")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "FUNDING_ID" }) })
public class InvestigationFunding extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@JoinColumn(name = "FUNDING_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private FundingReference funding;

	/* Needed for JPA */
	public InvestigationFunding() {
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public FundingReference getFunding() {
		return funding;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setFunding(FundingReference funding) {
		this.funding = funding;
	}
}
