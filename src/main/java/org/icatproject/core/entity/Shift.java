package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Comment("A period of time related to an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "STARTDATE",
		"ENDDATE" }) })
public class Shift extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	@Column(name = "STARTDATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Column(name = "ENDDATE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Column(name = "\"COMMENT\"")
	private String comment;

	@Comment("The instrument, if this shift is associated to one")
	@ManyToOne(fetch = FetchType.LAZY)
	private Instrument instrument;

	/* Needed for JPA */
	public Shift() {
	}

	public Investigation getInvestigation() {
		return this.investigation;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

}
