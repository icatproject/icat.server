package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;

@Comment("A period of time related to an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "STARTDATE",
		"ENDDATE" }) })
public class Shift extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Shift.class);

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

	/* Needed for JPA */
	public Shift() {
	}

	public Investigation getInvestigation() {
		return this.investigation;
	}

	@Override
	public String toString() {
		return "Shift[id=" + id + "]";
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Shift for " + includes);

		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.id = null;
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

}
