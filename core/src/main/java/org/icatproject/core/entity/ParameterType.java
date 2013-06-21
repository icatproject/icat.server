package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("A parameter type with unique name and units")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME", "UNITS" }) })
public class ParameterType extends EntityBaseBean implements Serializable {

	public enum ParameterValueType {
		DATE_AND_TIME, NUMERIC, STRING
	}

	@Comment("If a parameter of this type may be applied to a data file")
	private boolean applicableToDatafile;

	@Comment("If a parameter of this type may be applied to a data set")
	private boolean applicableToDataset;

	@Comment("If a parameter of this type may be applied to an investigation")
	private boolean applicableToInvestigation;

	@Comment("If a parameter of this type may be applied to a sample")
	private boolean applicableToSample;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<DatafileParameter> datafileParameters = new ArrayList<DatafileParameter>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<DatasetParameter> datasetParameters = new ArrayList<DatasetParameter>();

	@Comment("Description of the parameter type")
	private String description;

	@Comment("True if constraints are enforced")
	private boolean enforced;

	@Comment("The facility which has defined this data set type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<InvestigationParameter> investigationParameters = new ArrayList<InvestigationParameter>();

	@Column(precision = 38, scale = 127)
	private Double maximumNumericValue;

	@Column(precision = 38, scale = 127)
	private Double minimumNumericValue;

	@Comment("The name of the parameter type")
	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<PermissibleStringValue> permissibleStringValues = new ArrayList<PermissibleStringValue>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<SampleParameter> sampleParameters = new ArrayList<SampleParameter>();

	@Comment("The name of the parameter type units")
	@Column(name = "UNITS")
	private String units;

	@Comment("The formal name of the parameter type units")
	private String unitsFullName;

	@Comment("enum with possible values: NUMERIC, STRING, DATE_AND_TIME")
	@Column(nullable = false)
	private ParameterValueType valueType;

	@Comment("If ordinary users are allowed to create their own parameter types this indicates that this one has been approved")
	private boolean verified;

	/* Needed for JPA */
	public ParameterType() {
	}

	public List<DatafileParameter> getDatafileParameters() {
		return datafileParameters;
	}

	public List<DatasetParameter> getDatasetParameters() {
		return datasetParameters;
	}

	public String getDescription() {
		return this.description;
	}

	public Facility getFacility() {
		return facility;
	}

	public List<InvestigationParameter> getInvestigationParameters() {
		return investigationParameters;
	}

	public Double getMaximumNumericValue() {
		return maximumNumericValue;
	}

	public Double getMinimumNumericValue() {
		return minimumNumericValue;
	}

	public String getName() {
		return name;
	}

	public List<PermissibleStringValue> getPermissibleStringValues() {
		return permissibleStringValues;
	}

	public List<SampleParameter> getSampleParameters() {
		return sampleParameters;
	}

	public String getUnits() {
		return units;
	}

	public String getUnitsFullName() {
		return unitsFullName;
	}

	public ParameterValueType getValueType() {
		return valueType;
	}

	public boolean isApplicableToDatafile() {
		return applicableToDatafile;
	}

	public boolean isApplicableToDataset() {
		return applicableToDataset;
	}

	public boolean isApplicableToInvestigation() {
		return applicableToInvestigation;
	}

	public boolean isApplicableToSample() {
		return applicableToSample;
	}

	public boolean isEnforced() {
		return enforced;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setApplicableToDatafile(boolean applicableToDatafile) {
		this.applicableToDatafile = applicableToDatafile;
	}

	public void setApplicableToDataset(boolean applicableToDataset) {
		this.applicableToDataset = applicableToDataset;
	}

	public void setApplicableToInvestigation(boolean applicableToInvestigation) {
		this.applicableToInvestigation = applicableToInvestigation;
	}

	public void setApplicableToSample(boolean applicableToSample) {
		this.applicableToSample = applicableToSample;
	}

	public void setDatafileParameters(List<DatafileParameter> datafileParameters) {
		this.datafileParameters = datafileParameters;
	}

	public void setDatasetParameters(List<DatasetParameter> datasetParameters) {
		this.datasetParameters = datasetParameters;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnforced(boolean enforced) {
		this.enforced = enforced;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setInvestigationParameters(List<InvestigationParameter> investigationParameters) {
		this.investigationParameters = investigationParameters;
	}

	public void setMaximumNumericValue(Double maximumNumericValue) {
		this.maximumNumericValue = maximumNumericValue;
	}

	public void setMinimumNumericValue(Double minimumNumericValue) {
		this.minimumNumericValue = minimumNumericValue;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPermissibleStringValues(List<PermissibleStringValue> permissibleStringValues) {
		this.permissibleStringValues = permissibleStringValues;
	}

	public void setSampleParameters(List<SampleParameter> sampleParameters) {
		this.sampleParameters = sampleParameters;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public void setUnitsFullName(String unitsFullName) {
		this.unitsFullName = unitsFullName;
	}

	public void setValueType(ParameterValueType valueType) {
		this.valueType = valueType;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	@Override
	public String toString() {
		return "ParameterType[id=" + id + "]";
	}

}
