package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Identifier of a related resource to a data publication")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATION_ID", "IDENTIFIER" }) })
public class RelatedIdentifier extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATAPUBLICATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublication publication;

	@Comment("The identifier of the related resource")
	@Column(name = "IDENTIFIER", nullable = false)
	private String identifier;

	@Comment("See DataCite property relationType")
	@Column(nullable = false)
	private String relationType;

	@Column(length = 1023)
	private String fullReference;

	/* Needed for JPA */
	public RelatedIdentifier() {
	}

	public DataPublication getPublication() {
		return publication;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getRelationType() {
		return relationType;
	}

	public String getFullReference() {
		return fullReference;
	}

	public void setPublication(DataPublication publication) {
		this.publication = publication;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public void setFullReference(String fullReference) {
		this.fullReference = fullReference;
	}
}
