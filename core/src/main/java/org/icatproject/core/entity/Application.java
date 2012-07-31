package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;

@Comment("Some piece of software")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "VERSION" }) })
public class Application extends EntityBaseBean implements Serializable {

	@Comment("A short name for the software - e.g. mantid")
	@Column(name = "NAME")
	private String name;

	@Column(name = "VERSION")
	private String version;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "application")
	private List<Job> jobs = new ArrayList<Job>();

	public Application() {
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		id = null;
	}

	@Override
	public void canDelete(EntityManager manager) throws IcatException {
		super.canDelete(manager);
		if (!this.jobs.isEmpty()) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
					"Applications may not be deleted while there are related jobs");
		}
	}

	public List<Job> getJobs() {
		return this.jobs;
	}

	public String getName() {
		return this.name;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	public String getVersion() {
		return this.version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
