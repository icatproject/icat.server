package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

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

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facilityCycle")
	private List<InvestigationFacilityCycle> investigationFacilityCycles = new ArrayList<>();

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

	public List<InvestigationFacilityCycle> getInvestigationFacilityCycles() {
		return investigationFacilityCycles;
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

	public void setInvestigationFacilityCycles(List<InvestigationFacilityCycle> investigationFacilityCycles) {
		this.investigationFacilityCycles = investigationFacilityCycles;
	}

}
