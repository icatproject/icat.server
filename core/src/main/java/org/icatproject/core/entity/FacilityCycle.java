package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Comment("An operating cycle within a facility")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME" }) })
public class FacilityCycle extends EntityBaseBean implements Serializable {

	@Comment("A description of this facility cycle")
	private String description;

	@Comment("End of cycle")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Comment("The facility which has this cycle")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@Comment("A short name identifying this facility cycle within the facility")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("Start of cycle")
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	/* Needed for JPA */
	public FacilityCycle() {
	}

	public String getDescription() {
		return description;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Facility getFacility() {
		return facility;
	}

	public String getName() {
		return name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public String toString() {
		return "FacilityCycle[name=" + name + "]";
	}

}
