package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@Entity
@TableGenerator(name = "investigatorGenerator", pkColumnValue = "Investigator")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "UNITS" }) })
public class ParameterType extends EntityBaseBean implements Serializable {

	public enum ParameterValueType {
		NUMERIC, STRING, DATE_AND_TIME
	};

	private final static Logger logger = Logger.getLogger(ParameterType.class);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "parameterGenerator")
	private Long id;

	@Column(name = "NAME", nullable = false)
	private String name;

	@Column(name = "UNITS", nullable = false)
	private String units;

	private String unitsFullName;

	public ParameterValueType getValueType() {
		return valueType;
	}

	public void setValueType(ParameterValueType valueType) {
		this.valueType = valueType;
	}

	@Column(nullable = false)
	private ParameterValueType valueType;

	private boolean applicableToSample;
	private boolean applicableToDataset;
	private boolean applicableToDatafile;
	private boolean applicableToInvestigation;

	private String description;

	public List<SampleParameter> getSampleParameters() {
		return sampleParameters;
	}

	public void setSampleParameters(List<SampleParameter> sampleParameters) {
		this.sampleParameters = sampleParameters;
	}

	public List<DatafileParameter> getDatafileParameters() {
		return datafileParameters;
	}

	public void setDatafileParameters(List<DatafileParameter> datafileParameters) {
		this.datafileParameters = datafileParameters;
	}

	public List<InvestigationParameter> getInvestigationParameters() {
		return investigationParameters;
	}

	public void setInvestigationParameters(List<InvestigationParameter> investigationParameters) {
		this.investigationParameters = investigationParameters;
	}

	public void setDatasetParameters(List<DatasetParameter> datasetParameters) {
		this.datasetParameters = datasetParameters;
	}

	private boolean verified;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parameterType")
	private List<DatasetParameter> datasetParameters;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parameterType")
	private List<SampleParameter> sampleParameters;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parameterType")
	private List<DatafileParameter> datafileParameters;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parameterType")
	private List<InvestigationParameter> investigationParameters;

	/* Needed for JPA */
	public ParameterType() {
	}

	public String getDescription() {
		return this.description;
	}

	public String getUnitsFullName() {
		return unitsFullName;
	}

	public void setUnitsFullName(String unitsFullName) {
		this.unitsFullName = unitsFullName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "ParameterType[id=" + id + "]";
	}

	@Override
	public Object getPK() {
		return id;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Investigator for " + includes);
		if (!this.includes.contains(DatasetParameter.class)) {
			this.datasetParameters = null;
		}
		if (!this.includes.contains(DatafileParameter.class)) {
			this.datafileParameters = null;
		}
		if (!this.includes.contains(SampleParameter.class)) {
			this.sampleParameters = null;
		}
		if (!this.includes.contains(InvestigationParameter.class)) {
			this.investigationParameters = null;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public boolean isApplicableToSample() {
		return applicableToSample;
	}

	public void setApplicableToSample(boolean applicableToSample) {
		this.applicableToSample = applicableToSample;
	}

	public boolean isApplicableToDataset() {
		return applicableToDataset;
	}

	public void setApplicableToDataset(boolean applicableToDataset) {
		this.applicableToDataset = applicableToDataset;
	}

	public boolean isApplicableToDatafile() {
		return applicableToDatafile;
	}

	public void setApplicableToDatafile(boolean applicableToDatafile) {
		this.applicableToDatafile = applicableToDatafile;
	}

	public boolean isApplicableToInvestigation() {
		return applicableToInvestigation;
	}

	public void setApplicableToInvestigation(boolean applicableToInvestigation) {
		this.applicableToInvestigation = applicableToInvestigation;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public List<DatasetParameter> getDatasetParameters() {
		return datasetParameters;
	}

}
