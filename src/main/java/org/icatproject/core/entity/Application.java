package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Comment("Some piece of software")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME", "VERSION" }) })
public class Application extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "application")
	private List<Job> jobs = new ArrayList<Job>();

	@Comment("A short name for the software - e.g. mantid")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "VERSION", nullable = false)
	private String version;

	public Application() {
	}

	public Facility getFacility() {
		return facility;
	}

	public List<Job> getJobs() {
		return this.jobs;
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
