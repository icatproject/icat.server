package uk.icat3.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
import javax.xml.bind.annotation.XmlElement;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.ValidationException;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "applicationGenerator", pkColumnValue = "Application")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "version" }) })
public class Application extends EntityBaseBean implements Serializable {

	private final static Logger logger = Logger.getLogger(Application.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "applicationGenerator")
	private Long id;

	@Column
	private String name;

	private String version;

	@OneToMany(mappedBy = "application")
	@XmlElement
	private Set<Job> jobs = new HashSet<Job>();

	public Application() {
	}

	@Override
	public void canDelete(EntityManager manager) throws ValidationException {
		super.canDelete(manager);
		if (!this.jobs.isEmpty()) {
			throw new ValidationException("Applications may not be deleted while there are related jobs");
		}
	}

	public Long getId() {
		return this.id;
	}

	public Set<Job> getJobs() {
		return this.jobs;
	}

	public String getName() {
		return this.name;
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
