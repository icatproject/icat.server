package uk.icat3.entity;

import java.io.Serializable;
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
import javax.persistence.TableGenerator;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.ValidationException;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "sampleGenerator", pkColumnValue = "Sample")
public class Sample extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(Sample.class);

	private String chemicalFormula;

	@OneToMany(mappedBy = "sample")
	private List<Dataset> datasets;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "sampleGenerator")
	private Long id;

	private String instance;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@Column(nullable = false)
	private String name;

	private Integer proposalSampleId;

	@Column(nullable = false)
	private String safetyInformation;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<SampleParameter> sampleParameters;

	/* Needed for JPA */
	public Sample() {
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Sample for " + this.includes);
		if (!this.includes.contains(Investigation.class)) {
			this.investigation = null;
		}
		if (!this.includes.contains(SampleParameter.class)) {
			this.sampleParameters = null;
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

	public String getChemicalFormula() {
		return this.chemicalFormula;
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

	public Integer getProposalSampleId() {
		return this.proposalSampleId;
	}

	public String getSafetyInformation() {
		return this.safetyInformation;//
	}

	public List<SampleParameter> getSampleParameters() {
		return this.sampleParameters;
	}

	@Override
	public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException,
			IcatInternalException {
		super.isValid(manager, deepValidation);
		if (deepValidation) {
			if (this.sampleParameters != null) {
				for (final SampleParameter sampleParameter : this.sampleParameters) {
					sampleParameter.isValid(manager);
				}
			}
		}
	}

	public void setChemicalFormula(String chemicalFormula) {
		this.chemicalFormula = chemicalFormula;
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

	public void setProposalSampleId(Integer proposalSampleId) {
		this.proposalSampleId = proposalSampleId;
	}

	public void setSafetyInformation(String safetyInformation) {
		this.safetyInformation = safetyInformation;
	}

	public void setSampleParameters(List<SampleParameter> sampleParameters) {
		this.sampleParameters = sampleParameters;
	}

	@Override
	public String toString() {
		return "Sample[id=" + this.id + "]";
	}
}
