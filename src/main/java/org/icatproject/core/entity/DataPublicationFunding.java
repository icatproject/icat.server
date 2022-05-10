package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Represents a many-to-many relationship between a data publication and a funding reference")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATION_ID", "FUNDING_ID" }) })
public class DataPublicationFunding extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATAPUBLICATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublication dataPublication;

	@JoinColumn(name = "FUNDING_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private FundingReference funding;

	/* Needed for JPA */
	public DataPublicationFunding() {
	}

	public DataPublication getDataPublication() {
		return dataPublication;
	}

	public FundingReference getFunding() {
		return funding;
	}

	public void setDataPublication(DataPublication dataPublication) {
		this.dataPublication = dataPublication;
	}

	public void setFunding(FundingReference funding) {
		this.funding = funding;
	}
}
