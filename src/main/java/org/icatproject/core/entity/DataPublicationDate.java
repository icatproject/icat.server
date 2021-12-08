package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
	@Column(nullable = false)
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
