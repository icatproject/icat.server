package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Many to many relationship between investigation and group which might be used within authorization rules. Please see UserInvestigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "GROUP_ID", "INVESTIGATION_ID" }) })
public class InvestigationGroup extends EntityBaseBean implements Serializable {

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

	@Override
	public String toString() {
		return "InvestigationGroup[id=" + id + "]";
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

}
