package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Relation between a data publication and a funding reference")
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
