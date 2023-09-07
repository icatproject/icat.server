package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("A date relevant for the publication")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "DATAPUBLICATION_ID", "DATETYPE" }) })
public class DataPublicationDate extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "DATAPUBLICATION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublication publication;

	@Comment("Type of the date, see DataCite property dateType for suggested values")
	@Column(name = "DATETYPE", nullable = false)
	private String dateType;

	@Comment("Use ISO 8601 format, may also be a range")
	@Column(name = "DATE_", nullable = false)  // DATE is a reserved word in Oracle
	private String date;

	/* Needed for JPA */
	public DataPublicationDate() {
	}

	public DataPublication getPublication() {
		return publication;
	}

	public String getDateType() {
		return dateType;
	}

	public String getDate() {
		return date;
	}

	public void setPublication(DataPublication publication) {
		this.publication = publication;
	}

	public void setDateType(String dateType) {
		this.dateType = dateType;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
