package uk.icat3.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "investigationParameterGenerator", pkColumnValue = "InvestigationParameter")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class InvestigationParameter extends Parameter implements Serializable {

	private static Logger logger = Logger.getLogger(InvestigationParameter.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "investigationParameterGenerator")
	private Long id;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	/* Needed for JPA */
	public InvestigationParameter() {
	}

	@Override
	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling InvestigationParameter for " + this.includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
	}

	public Long getId() {
		return this.id;
	}

	public Investigation getInvestigation() {
		return this.investigation;
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		this.id = null;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	@Override
	public String toString() {
		return "InvestigationParameter[id=" + this.id + "]";
	}
}