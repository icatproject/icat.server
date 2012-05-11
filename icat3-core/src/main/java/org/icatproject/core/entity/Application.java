package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;


@Comment("Some piece of software")
@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "applicationGenerator", pkColumnValue = "Application")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "VERSION" }) })
public class Application extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Application.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "applicationGenerator")
	private Long id;

	@Comment("A short name for the software - e.g. mantid")
	@Column(name = "NAME")
	private String name;

	@Column(name = "VERSION")
	private String version;

	@OneToMany(mappedBy = "application")
	private List<Job> jobs = new ArrayList<Job>();

	public Application() {
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws 
			IcatException {
		super.preparePersist(modId, manager);
		id = null;
	}

	@Override
	public void canDelete(EntityManager manager) throws IcatException  {
		super.canDelete(manager);
		if (!this.jobs.isEmpty()) {
			throw new IcatException(IcatException.Type.VALIDATION, 
					"Applications may not be deleted while there are related jobs");
		}
	}

	public Long getId() {
		return this.id;
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

	@Override
	public Object getPK() {
		return this.id;
	}

	public String getVersion() {
		return this.version;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Application for " + includes);
		if (!this.includes.contains(Job.class)) {
			this.jobs = null;
		}
	}

}
