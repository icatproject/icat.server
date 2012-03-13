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

@Comment("A parameter type with unique name and units")
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

	@Comment("The name of the parameter type")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("The name of the parameter type units")
	@Column(name = "UNITS")
	private String units;

	@Comment("The formal name of the parameter type units")
	private String unitsFullName;

	public ParameterValueType getValueType() {
		return valueType;
	}

	public void setValueType(ParameterValueType valueType) {
		this.valueType = valueType;
	}

	@Comment("enum with possible values: NUMERIC, STRING, DATE_AND_TIME")
	@Column(nullable = false)
	private ParameterValueType valueType;

	@Comment("If a parameter of this type may be applied to a sample")
	private boolean applicableToSample;

	@Comment("If a parameter of this type may be applied to a data set")
	private boolean applicableToDataset;

	@Comment("If a parameter of this type may be applied to a data file")
	private boolean applicableToDatafile;

	@Comment("If a parameter of this type may be applied to an investigation")
	private boolean applicableToInvestigation;

	@Comment("Description of the parameter type")
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

	@Comment("If ordinary users are allowed to create their own parameter types this indicates that this one has been approved")
	private boolean verified;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<DatasetParameter> datasetParameters;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<SampleParameter> sampleParameters;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<DatafileParameter> datafileParameters;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
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
