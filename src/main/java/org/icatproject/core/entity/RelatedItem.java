package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("A reference to an external resource or item that is related to a data publication, "
		+ "such as a scientific article that is based on the data or the instrument "
		+ "that has been used to collect the data")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATION_ID", "IDENTIFIER" }) })
public class RelatedItem extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATAPUBLICATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublication publication;

	@Comment("The identifier of the related resource")
	@Column(name = "IDENTIFIER", nullable = false)
	private String identifier;

	@Comment("Description of the relationship with the related resource, see DataCite property relationType for suggested values")
	@Column(nullable = false)
	private String relationType;

	@Comment("The full reference for the related resource as it should be displayed on the landing page")
	@Column(length = 1023)
	private String fullReference;

	@Comment("The type of the related resource, see DataCite property resourceTypeGeneral for suggested values")
	private String relatedItemType;

	@Comment("Title or name of the related resource")
	private String title;

	/* Needed for JPA */
	public RelatedItem() {
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

	public String getRelatedItemType() {
		return relatedItemType;
	}

	public String getTitle() {
		return title;
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

	public void setRelatedItemType(String relatedItemType) {
		this.relatedItemType = relatedItemType;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
