/*
 * DatasetParameter.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.manager.ManagerUtil;
import uk.icat3.util.ParameterValueType;

/**
 * Entity class DatasetParameter
 * 
 * @author gjd37
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "DATASET_PARAMETER")
@NamedQueries({
		@NamedQuery(name = "DatasetParameter.findByDatasetId", query = "SELECT d FROM DatasetParameter d WHERE d.datasetParameterPK.datasetId = :datasetId"),
		@NamedQuery(name = "DatasetParameter.findByName", query = "SELECT d FROM DatasetParameter d WHERE d.datasetParameterPK.name = :name"),
		@NamedQuery(name = "DatasetParameter.findByUnits", query = "SELECT d FROM DatasetParameter d WHERE d.datasetParameterPK.units = :units"),
		@NamedQuery(name = "DatasetParameter.findByStringValue", query = "SELECT d FROM DatasetParameter d WHERE d.stringValue = :stringValue"),
		@NamedQuery(name = "DatasetParameter.findByNumericValue", query = "SELECT d FROM DatasetParameter d WHERE d.numericValue = :numericValue"),
		@NamedQuery(name = "DatesetParameter.findByDateTimeValue", query = "SELECT d FROM DatasetParameter d WHERE d.dateTimeValue = :dateTimeValue"),
		@NamedQuery(name = "DatasetParameter.findByRangeTop", query = "SELECT d FROM DatasetParameter d WHERE d.rangeTop = :rangeTop"),
		@NamedQuery(name = "DatasetParameter.findByRangeBottom", query = "SELECT d FROM DatasetParameter d WHERE d.rangeBottom = :rangeBottom"),
		@NamedQuery(name = "DatasetParameter.findByError", query = "SELECT d FROM DatasetParameter d WHERE d.error = :error"),
		@NamedQuery(name = "DatasetParameter.findByDescription", query = "SELECT d FROM DatasetParameter d WHERE d.description = :description"),
		@NamedQuery(name = "DatasetParameter.findByModTime", query = "SELECT d FROM DatasetParameter d WHERE d.modTime = :modTime"),
		@NamedQuery(name = "DatasetParameter.findByModId", query = "SELECT d FROM DatasetParameter d WHERE d.modId = :modId") })
public class DatasetParameter extends EntityBaseBean implements Serializable {
	
	private static Logger logger = Logger.getLogger(DatasetParameter.class);

	/**
	 * EmbeddedId primary key field
	 */
	@EmbeddedId
	protected DatasetParameterPK datasetParameterPK;

	@Column(name = "STRING_VALUE")
	private String stringValue;

	@Column(name = "NUMERIC_VALUE")
	private Double numericValue;

	@Column(name = "DATETIME_VALUE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateTimeValue;

	@Column(name = "RANGE_TOP")
	private String rangeTop;

	@Column(name = "RANGE_BOTTOM")
	private String rangeBottom;

	@Column(name = "ERROR")
	private String error;

	@Column(name = "DESCRIPTION")
	private String description;

	@JoinColumn(name = "DATASET_ID", referencedColumnName = "ID", insertable = false, updatable = false)
	@ManyToOne
	@XmlTransient
	private Dataset dataset;

	@JoinColumns(value = {
			@JoinColumn(name = "NAME", referencedColumnName = "NAME", insertable = false, updatable = false),
			@JoinColumn(name = "UNITS", referencedColumnName = "UNITS", insertable = false, updatable = false) })
	@ManyToOne
	@XmlTransient
	private Parameter parameter;

	@Transient
	protected transient ParameterValueType valueType;

	/** Creates a new instance of DatasetParameter */
	public DatasetParameter() {
	}

	/**
	 * Gets the datasetParameterPK of this DatasetParameter.
	 * 
	 * @return the datasetParameterPK
	 */
	public DatasetParameterPK getDatasetParameterPK() {
		return this.datasetParameterPK;
	}

	/**
	 * Sets the datasetParameterPK of this DatasetParameter to the specified value.
	 * 
	 * @param datasetParameterPK
	 *            the new datasetParameterPK
	 */
	public void setDatasetParameterPK(DatasetParameterPK datasetParameterPK) {
		this.datasetParameterPK = datasetParameterPK;
	}

	/**
	 * Gets the stringValue of this DatasetParameter.
	 * 
	 * @return the stringValue
	 */
	public String getStringValue() {
		return this.stringValue;
	}

	/**
	 * Sets the stringValue of this DatasetParameter to the specified value.
	 * 
	 * @param stringValue
	 *            the new stringValue
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	/**
	 * Gets the numericValue of this DatasetParameter.
	 * 
	 * @return the numericValue
	 */
	public Double getNumericValue() {
		return this.numericValue;
	}

	/**
	 * Sets the numericValue of this DatasetParameter to the specified value.
	 * 
	 * @param numericValue
	 *            the new numericValue
	 */
	public void setNumericValue(Double numericValue) {
		this.numericValue = numericValue;
	}

	/**
	 * Gets the data time of the DatasetParameter
	 * 
	 * @return Date in milliseconds.
	 */
	public Date getDateTimeValue() {
		return dateTimeValue;
	}

	/**
	 * Sets the dataTimeValue of this DatasetParameter to the specified value. the time can be set
	 * in milliseconds.
	 * 
	 * @param dateTimeValue
	 *            the new date time value
	 */
	public void setDateTimeValue(Date dateTimeValue) {
		this.dateTimeValue = dateTimeValue;
	}

	/**
	 * Gets the rangeTop of this DatasetParameter.
	 * 
	 * @return the rangeTop
	 */
	public String getRangeTop() {
		return this.rangeTop;
	}

	/**
	 * Sets the rangeTop of this DatasetParameter to the specified value.
	 * 
	 * @param rangeTop
	 *            the new rangeTop
	 */
	public void setRangeTop(String rangeTop) {
		this.rangeTop = rangeTop;
	}

	/**
	 * Gets the rangeBottom of this DatasetParameter.
	 * 
	 * @return the rangeBottom
	 */
	public String getRangeBottom() {
		return this.rangeBottom;
	}

	/**
	 * Sets the rangeBottom of this DatasetParameter to the specified value.
	 * 
	 * @param rangeBottom
	 *            the new rangeBottom
	 */
	public void setRangeBottom(String rangeBottom) {
		this.rangeBottom = rangeBottom;
	}

	/**
	 * Gets the error of this DatasetParameter.
	 * 
	 * @return the error
	 */
	public String getError() {
		return this.error;
	}

	/**
	 * Sets the error of this DatasetParameter to the specified value.
	 * 
	 * @param error
	 *            the new error
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * Gets the description of this DatasetParameter.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description of this DatasetParameter to the specified value.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the dataset of this DatasetParameter.
	 * 
	 * @return the dataset
	 */
	@XmlTransient
	public Dataset getDataset() {
		return this.dataset;
	}

	/**
	 * Sets the dataset of this DatasetParameter to the specified value.
	 * 
	 * @param dataset
	 *            the new dataset
	 */
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * Gets the parameter of this DatasetParameter.
	 * 
	 * @return the parameter
	 */
	public Parameter getParameter() {
		return this.parameter;
	}

	/**
	 * Sets the parameter of this DatasetParameter to the specified value.
	 * 
	 * @param parameter
	 *            the new parameter
	 */
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public ParameterValueType getValueType() {
		return valueType;
	}

	public void setValueType(ParameterValueType valueType) {
		this.valueType = valueType;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (this.datasetParameterPK != null ? this.datasetParameterPK.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof DatasetParameter)) {
			return false;
		}
		DatasetParameter other = (DatasetParameter) object;
		if (this.datasetParameterPK != other.datasetParameterPK
				&& (this.datasetParameterPK == null || !this.datasetParameterPK.equals(other.datasetParameterPK)))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DatasetParameter[datasetParameterPK=" + datasetParameterPK + "]";
	}

	@Override
	public void isValid(EntityManager manager, boolean deepValidation) throws ValidationException,
			IcatInternalException {
		super.isValid(manager, deepValidation);
		if (datasetParameterPK == null)
			throw new ValidationException(this + " primary key cannot be null");

		// check embedded primary key
		datasetParameterPK.isValid();

		// check valid
		String paramName = this.getDatasetParameterPK().getName();
		String paramUnits = this.getDatasetParameterPK().getUnits();

		// check if this name is parameter table
		ParameterPK paramPK = new ParameterPK();
		paramPK.setName(paramName);
		paramPK.setUnits(paramUnits);

		Parameter parameterDB = manager.find(Parameter.class, paramPK);

		// check paramPK is in the parameter table
		if (parameterDB == null) {
			logger.info(datasetParameterPK
					+ " is not in the parameter table as a data set parameter so been marked as unverified and inserting new row in Parameter table");
			// add new parameter into database
			parameterDB = ManagerUtil.addParameter(this.createId, manager, paramName, paramUnits, getValueType());
			if (parameterDB == null)
				throw new ValidationException("Parameter: " + paramName + " with units: " + paramUnits
						+ " cannot be inserted into the Parameter table.");
		}

		// check that it is a dataset parameter
		if (!parameterDB.isDatasetParameter())
			throw new ValidationException("DatasetParameter: " + paramName + " with units: " + paramUnits
					+ " is not a data set parameter.");

		// check is numeric
		if (parameterDB.isNumeric()) {
			if (this.getStringValue() != null)
				throw new ValidationException("DatasetParameter: " + paramName + " with units: " + paramUnits
						+ " must be a numeric value only.");
		}

		// check if string
		if (!parameterDB.isNumeric()) {
			if (this.getNumericValue() != null)
				throw new ValidationException("DatasetParameter: " + paramName + " with units: " + paramUnits
						+ " must be a string value only.");
		}

		// check that the parameter dataset id is the same as actual dataset id
		if (getDataset() != null) {
			datasetParameterPK.isValid();

			if (!datasetParameterPK.getDatasetId().equals(getDataset().getId())) {
				throw new ValidationException("DatasetParameter: " + paramName + " with units: " + paramUnits
						+ " has dataset id: " + datasetParameterPK.getDatasetId()
						+ " that does not corresponds to its parent dataset id: " + getDataset().getId());
			}
		}
	}

	@Override
	public Object getPK() {
		return datasetParameterPK;
	}

}
