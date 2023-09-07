package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Many to many relationship between investigation and group which might be used within authorization rules. "
		+ "Please see InvestigationUser")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "GROUP_ID", "INVESTIGATION_ID",
		"ROLE" }) })
public class InvestigationGroup extends EntityBaseBean implements Serializable {

	@Comment("A role showing the position of the members of the group with respect to the investigation")
	@Column(name = "ROLE", nullable = false)
	private String role;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@JoinColumn(name = "GROUP_ID", nullable = false)
	@ManyToOne
	private Grouping grouping;

	public Grouping getGrouping() {
		return grouping;
	}

	public void setGrouping(Grouping grouping) {
		this.grouping = grouping;
	}

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	/* Needed for JPA */
	public InvestigationGroup() {
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

}
