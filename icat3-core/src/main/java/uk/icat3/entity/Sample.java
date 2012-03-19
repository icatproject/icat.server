package uk.icat3.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
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

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.NoSuchObjectFoundException;
import uk.icat3.exceptions.ValidationException;

@Comment("**** Maybe we need a sample type as well. " + "An individual sample to be used in an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "INSTANCE", "INVESTIGATION_ID" }) })
@TableGenerator(name = "sampleGenerator", pkColumnValue = "Sample")
public class Sample extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Sample.class);

	@Comment("The formula written as a string -e.g. C2H6O2 for ethylene glycol")
	private String molecularFormula;

	@OneToMany(mappedBy = "sample")
	private List<Dataset> datasets;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "sampleGenerator")
	private Long id;

	@Comment("To distinguish between different instances of the same material")
	@Column(name = "INSTANCE")
	private String instance;

	@JoinColumn(nullable = false, name = "INVESTIGATION_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@Column(nullable = false, name = "NAME")
	private String name;

	@Comment("Any safety information related to this sample")
	@Column(length = 4000)
	private String safetyInformation;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<SampleParameter> parameters = new ArrayList<SampleParameter>();

	public List<SampleParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<SampleParameter> parameters) {
		this.parameters = parameters;
	}

	/* Needed for JPA */
	public Sample() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Sample for " + this.includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
		if (!this.includes.contains(SampleParameter.class)) {
			this.parameters = null;
		}
		if (!this.includes.contains(Dataset.class)) {
			this.datasets = null;
		}
	}

	@Override
	public void canDelete(EntityManager manager) {
		// TODO add code to ensure that sample is not part of a dataset nor an
		// investigation
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public Long getId() {
		return this.id;
	}

	public String getInstance() {
		return this.instance;
	}

	public Investigation getInvestigation() {
		return this.investigation;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public Object getPK() {
		return this.id;
	}

	public String getSafetyInformation() {
		return this.safetyInformation;//
	}



	@Override
	public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException,
			IcatInternalException {
		super.isValid(manager, deepValidation);
		if (deepValidation) {
			if (this.parameters != null) {
				for (final SampleParameter sampleParameter : this.parameters) {
					sampleParameter.isValid(manager);
				}
			}
		}
	}

	public void preparePersist(String modId, EntityManager manager) throws NoSuchObjectFoundException,
			BadParameterException, IcatInternalException {
		super.preparePersist(modId, manager);
		id = null;
		for (SampleParameter sampleParameter : parameters) {
			sampleParameter.preparePersist(modId, manager);
			sampleParameter.setSample(this); // Set back ref
		}
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSafetyInformation(String safetyInformation) {
		this.safetyInformation = safetyInformation;
	}

	@Override
	public String toString() {
		return "Sample[id=" + this.id + "]";
	}
}
