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
@TableGenerator(name = "sampleParameterGenerator", pkColumnValue = "SampleParameter")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SAMPLE_ID", "PARAMETER_TYPE_ID" }) })
@XmlRootElement
public class SampleParameter extends Parameter implements Serializable {

	private static Logger logger = Logger.getLogger(SampleParameter.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "sampleParameterGenerator")
	private Long id;

	@JoinColumn(name = "SAMPLE_ID", nullable = false)
	@ManyToOne
	private Sample sample;

	/* Needed for JPA */
	public SampleParameter() {
	}

	@Override
	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling SampleParameter for " + this.includes);
		if (!this.includes.contains(Sample.class)) {
			this.sample = null;
		}
	}

	public Long getId() {
		return this.id;
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	public Sample getSample() {
		return this.sample;
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

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	@Override
	public String toString() {
		return "SampleParameter[id=" + this.id + "]";
	}
}