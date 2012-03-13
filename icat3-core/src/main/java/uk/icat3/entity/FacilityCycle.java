package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@Comment("An operating cycle within a facility")
@SuppressWarnings("serial")
@Entity
public class FacilityCycle extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(FacilityCycle.class);

	@Comment("A short name identifying this facility cycle")
	@Id
	private String name;

	@Comment("Start of cycle")
	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Comment("End of cycle")
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Comment("A description of this facility cycle")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "facilityCycle")
	private List<Investigation> investigations;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Investigation> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	/* Needed for JPA */
	public FacilityCycle() {
	}

	@Override
	public String toString() {
		return "FacilityCycle[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling FacilityCycle for " + includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigations = null;
		}
	}

}
