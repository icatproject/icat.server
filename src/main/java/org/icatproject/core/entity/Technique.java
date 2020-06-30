package org.icatproject.core.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("Represents an experimental technique.")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME" }) })
public class Technique extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@Comment("A short name for the technique")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("A persistent identifier attributed to the technique, ideally referring to a vocabulary term")
	private String pid;

	@Comment("An informal description")
	private String description;

	public Facility getFacility() {
		return facility;
	}

	public String getName() {
		return name;
	}

	public String getPid() {
		return pid;
	}

	public String getDescription() {
		return description;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
