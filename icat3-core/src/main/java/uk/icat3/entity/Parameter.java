/*
 * Parameter.java
 *
 * Created on 08 February 2007, 10:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.ObjectAlreadyExistsException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.ParameterValueType;
import uk.icat3.util.Queries;

/**
 * Entity class Parameter
 * 
 * @author gjd37
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "PARAMETER")
@NamedQueries({
		@NamedQuery(name = "Parameter.findByName", query = "SELECT p FROM Parameter p WHERE p.parameterPK.name = :name"),
		@NamedQuery(name = "Parameter.findByUnits", query = "SELECT p FROM Parameter p WHERE p.parameterPK.units = :units"),
		@NamedQuery(name = "Parameter.findByUnitsLongVersion", query = "SELECT p FROM Parameter p WHERE p.unitsLongVersion = :unitsLongVersion"),
		@NamedQuery(name = "Parameter.findByValueType", query = "SELECT p FROM Parameter p WHERE p.valueType = :valueType"),
		@NamedQuery(name = "Parameter.findByNonNumericValueFormat", query = "SELECT p FROM Parameter p WHERE p.nonNumericValueFormat = :nonNumericValueFormat"),
		@NamedQuery(name = "Parameter.findByIsSampleParameter", query = "SELECT p FROM Parameter p WHERE p.sampleParameter = :isSampleParameter"),
		@NamedQuery(name = "Parameter.findByIsDatasetParameter", query = "SELECT p FROM Parameter p WHERE p.datasetParameter = :isDatasetParameter"),
		@NamedQuery(name = "Parameter.findByIsDatafileParameter", query = "SELECT p FROM Parameter p WHERE p.datafileParameter = :isDatafileParameter"),
		@NamedQuery(name = "Parameter.findByDescription", query = "SELECT p FROM Parameter p WHERE p.description = :description"),
		@NamedQuery(name = "Parameter.findByModId", query = "SELECT p FROM Parameter p WHERE p.modId = :modId"),
		@NamedQuery(name = "Parameter.findByModTime", query = "SELECT p FROM Parameter p WHERE p.modTime = :modTime"),
		@NamedQuery(name = "Parameter.findByNameAndUnits", query = "SELECT p FROM Parameter p WHERE p.parameterPK.name = :name and p.parameterPK.units = :units"),

		@NamedQuery(name = Queries.ALL_PARAMETERS, query = Queries.ALL_PARAMETERS_JPQL),
		@NamedQuery(name = Queries.PARAMETER_SEARCH_BY_NAME_UNITS, query = Queries.PARAMETER_SEARCH_BY_NAME_UNITS_JPQL),
		@NamedQuery(name = Queries.PARAMETER_SEARCH_BY_NAME, query = Queries.PARAMETER_SEARCH_BY_NAME_JPQL),
		@NamedQuery(name = Queries.PARAMETER_SEARCH_BY_UNITS, query = Queries.PARAMETER_SEARCH_BY_UNITS_JPQL),
		@NamedQuery(name = Queries.PARAMETER_SEARCH_BY_NAME_UNITS_SENSITIVE, query = Queries.PARAMETER_SEARCH_BY_NAME_UNITS_JPQL_SENSITIVE),
		@NamedQuery(name = Queries.PARAMETER_SEARCH_BY_NAME_SENSITIVE, query = Queries.PARAMETER_SEARCH_BY_NAME_JPQL_SENSITIVE),
		@NamedQuery(name = Queries.PARAMETER_SEARCH_BY_UNITS_SENSITIVE, query = Queries.PARAMETER_SEARCH_BY_UNITS_JPQL_SENSITIVE) })
public class Parameter extends EntityBaseBean implements Serializable {

	/**
	 * EmbeddedId primary key field
	 */
	@EmbeddedId
	protected ParameterPK parameterPK;

	@Column(name = "UNITS_LONG_VERSION")
	private String unitsLongVersion;

	@Column(name = "NUMERIC_VALUE", nullable = false)
	private String valueType;

	@Column(name = "NON_NUMERIC_VALUE_FORMAT")
	private String nonNumericValueFormat;

	@Column(name = "IS_SAMPLE_PARAMETER")
	private String sampleParameter = "N";

	@Column(name = "IS_DATASET_PARAMETER")
	private String datasetParameter = "N";

	@Column(name = "IS_DATAFILE_PARAMETER")
	private String datafileParameter = "N";

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "VERIFIED")
	private String verified = "N";;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parameter")
	@XmlTransient
	private Collection<DatasetParameter> datasetParameterCollection;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parameter")
	@XmlTransient
	private Collection<SampleParameter> sampleParameterCollection;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parameter")
	@XmlTransient
	private Collection<DatafileParameter> datafileParameterCollection;

	/** Creates a new instance of Parameter */
	public Parameter() {
	}

	/**
	 * Gets the parameterPK of this Parameter.
	 * 
	 * @return the parameterPK
	 */
	public ParameterPK getParameterPK() {
		return this.parameterPK;
	}

	/**
	 * Sets the parameterPK of this Parameter to the specified value.
	 * 
	 * @param parameterPK
	 *            the new parameterPK
	 */
	public void setParameterPK(ParameterPK parameterPK) {
		this.parameterPK = parameterPK;
	}

	/**
	 * Gets the unitsLongVersion of this Parameter.
	 * 
	 * @return the unitsLongVersion
	 */
	public String getUnitsLongVersion() {
		return this.unitsLongVersion;
	}

	/**
	 * Sets the unitsLongVersion of this Parameter to the specified value.
	 * 
	 * @param unitsLongVersion
	 *            the new unitsLongVersion
	 */
	public void setUnitsLongVersion(String unitsLongVersion) {
		this.unitsLongVersion = unitsLongVersion;
	}

	/**
	 * Gets the nonNumericValueFormat of this Parameter.
	 * 
	 * @return the nonNumericValueFormat
	 */
	public String getNonNumericValueFormat() {
		return this.nonNumericValueFormat;
	}

	/**
	 * Sets the nonNumericValueFormat of this Parameter to the specified value.
	 * 
	 * @param nonNumericValueFormat
	 *            the new nonNumericValueFormat
	 */
	public void setNonNumericValueFormat(String nonNumericValueFormat) {
		this.nonNumericValueFormat = nonNumericValueFormat;
	}

	/**
	 * Gets the description of this Parameter.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description of this Parameter to the specified value.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the datasetParameterCollection of this Parameter.
	 * 
	 * @return the datasetParameterCollection
	 */
	@XmlTransient
	public Collection<DatasetParameter> getDatasetParameterCollection() {
		return this.datasetParameterCollection;
	}

	/**
	 * Sets the datasetParameterCollection of this Parameter to the specified value.
	 * 
	 * @param datasetParameterCollection
	 *            the new datasetParameterCollection
	 */
	public void setDatasetParameterCollection(Collection<DatasetParameter> datasetParameterCollection) {
		this.datasetParameterCollection = datasetParameterCollection;
	}

	/**
	 * Gets the sampleParameterCollection of this Parameter.
	 * 
	 * @return the sampleParameterCollection
	 */
	@XmlTransient
	public Collection<SampleParameter> getSampleParameterCollection() {
		return this.sampleParameterCollection;
	}

	/**
	 * Sets the sampleParameterCollection of this Parameter to the specified value.
	 * 
	 * @param sampleParameterCollection
	 *            the new sampleParameterCollection
	 */
	public void setSampleParameterCollection(Collection<SampleParameter> sampleParameterCollection) {
		this.sampleParameterCollection = sampleParameterCollection;
	}

	/**
	 * Gets the datafileParameterCollection of this Parameter.
	 * 
	 * @return the datafileParameterCollection
	 */
	@XmlTransient
	public Collection<DatafileParameter> getDatafileParameterCollection() {
		return this.datafileParameterCollection;
	}

	/**
	 * Sets the datafileParameterCollection of this Parameter to the specified value.
	 * 
	 * @param datafileParameterCollection
	 *            the new datafileParameterCollection
	 */
	public void setDatafileParameterCollection(Collection<DatafileParameter> datafileParameterCollection) {
		this.datafileParameterCollection = datafileParameterCollection;
	}

	/**
	 * Returns a hash code value for the object. This implementation computes a hash code value
	 * based on the id fields in this object.
	 * 
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (this.parameterPK != null ? this.parameterPK.hashCode() : 0);
		return hash;
	}

	/**
	 * Determines whether another object is equal to this Parameter. The result is <code>true</code>
	 * if and only if the argument is not null and is a Parameter object that has the same id field
	 * values as this object.
	 * 
	 * @param object
	 *            the reference object with which to compare
	 * @return <code>true</code> if this object is the same as the argument; <code>false</code>
	 *         otherwise.
	 */
	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof Parameter)) {
			return false;
		}
		Parameter other = (Parameter) object;
		if (this.parameterPK != other.parameterPK
				&& (this.parameterPK == null || !this.parameterPK.equals(other.parameterPK)))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Parameter[parameterPK=" + parameterPK + "]";
	}


	public boolean isDatasetParameter() {
		if (datasetParameter != null && datasetParameter.equalsIgnoreCase("Y"))
			return true;
		else
			return false;
	}

	public boolean isDatafileParameter() {
			return datafileParameter != null && datafileParameter.equalsIgnoreCase("Y");
	}

	public boolean isSampleParameter() {
		return sampleParameter != null && sampleParameter.equalsIgnoreCase("Y");
	}

	public boolean isNumeric() {
		return getValueType() != null && getValueType() == ParameterValueType.NUMERIC;
	}

	public boolean isString() {
		return getValueType() != null && getValueType() == ParameterValueType.STRING;
	}

	public boolean isDateTime() {
		return getValueType() != null && getValueType() == ParameterValueType.DATE_AND_TIME;
	}

	public boolean isVerified() {
		return verified != null && verified.equalsIgnoreCase("Y");
	}

	public void setVerified(boolean verified) {
		this.verified = verified ? "Y" : "N";
	}

	public void setValueType(ParameterValueType type) {
		valueType = type.getValue();
	}

	public ParameterValueType getValueType() {
		return ParameterValueType.toParameterValueType(valueType);
	}

	public void setSampleParameter(boolean sampleParameter) {
		this.sampleParameter = sampleParameter ? "Y" : "N";
	}

	public void setDatafileParameter(boolean datafileParameter) {
		this.datafileParameter = datafileParameter ? "Y" : "N";
	}

	public void setDatasetParameter(boolean datasetParameter) {
		this.datasetParameter = datasetParameter ? "Y" : "N";
	}

	@Override
	public Object getPK() {
		return this.parameterPK;
	}

}
