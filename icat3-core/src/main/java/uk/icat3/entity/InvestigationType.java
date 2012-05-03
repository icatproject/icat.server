package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("A type of investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "FACILITY_ID" }) })
@TableGenerator(name = "investigationTypeGenerator", pkColumnValue = "InvestigationType")
public class InvestigationType extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(InvestigationType.class);

	@Comment("A short name identifying this type of investigation")
	@Column(nullable = false, name = "NAME")
	private String name;

	@Comment("The facility which has defined this investigation type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "sampleTypeGenerator")
	private Long id;

	@Comment("A description of this type of investigation")
	private String description;

	@OneToMany(mappedBy = "type")
	private List<Investigation> investigations;

	public List<Investigation> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	/* Needed for JPA */
	public InvestigationType() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "InvestigationType[id=" + id + "]";
	}

	@Override
	public Object getPK() {
		return id;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling InvestigationType for " + includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigations = null;
		}
		if (!this.includes.contains(Facility.class)) {
			this.facility = null;
		}
	}

}
