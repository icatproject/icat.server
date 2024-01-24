package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.EntityBeanManager.PersistMode;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.LuceneApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@MappedSuperclass
@NamedQuery(name = "Parameter.psv", query = "SELECT DISTINCT p.value FROM PermissibleStringValue p LEFT JOIN p.type t WHERE t.id = :tid")
public abstract class Parameter extends EntityBaseBean implements Serializable {

	private static Logger logger = LoggerFactory.getLogger(Parameter.class);

	@Comment("The type of the parameter")
	@JoinColumn(name = "PARAMETER_TYPE_ID", nullable = false)
	@ManyToOne
	ParameterType type;

	@Comment("The value if the parameter is a string")
	@Column(name = "STRING_VALUE", length = 4000)
	private String stringValue;

	@Comment("The value if the parameter is numeric")
	@Column(name = "NUMERIC_VALUE", precision = 38, scale = 19)
	private Double numericValue;

	@Comment("The value if the parameter is a date")
	@Column(name = "DATETIME_VALUE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateTimeValue;

	@Comment("The maximum value of the numeric parameter that was observed during the measurement period")
	@Column(precision = 38, scale = 19)
	private Double rangeTop;

	@Comment("The minimum value of the numeric parameter that was observed during the measurement period")
	@Column(precision = 38, scale = 19)
	private Double rangeBottom;

	@Comment("The error of the numeric parameter")
	@Column(precision = 38, scale = 19)
	private Double error;

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Double getNumericValue() {
		return numericValue;
	}

	public void setNumericValue(Double numericValue) {
		this.numericValue = numericValue;
	}

	public Date getDateTimeValue() {
		return dateTimeValue;
	}

	public void setDateTimeValue(Date dateTimeValue) {
		this.dateTimeValue = dateTimeValue;
	}

	public Double getRangeTop() {
		return rangeTop;
	}

	public void setRangeTop(Double rangeTop) {
		this.rangeTop = rangeTop;
	}

	public Double getRangeBottom() {
		return rangeBottom;
	}

	public void setRangeBottom(Double rangeBottom) {
		this.rangeBottom = rangeBottom;
	}

	public Double getError() {
		return error;
	}

	public void setError(Double error) {
		this.error = error;
	}

	@Override
	public void preparePersist(String modId, EntityManager manager, GateKeeper gateKeeper, PersistMode persistMode)
			throws IcatException {
		super.preparePersist(modId, manager, gateKeeper, persistMode);
		check(manager);
	}

	private void check(EntityManager manager) throws IcatException {
		if (type == null) {
			throw new IcatException(IcatException.IcatExceptionType.VALIDATION, "Type of parameter is not set");
		}
		if (!type.isEnforced()) {
			return;
		}
		ParameterValueType pvt = type.getValueType();
		if (pvt == ParameterValueType.NUMERIC) {
			logger.debug(
					"Parameter of type " + type.getName() + " has numeric value " + numericValue + " to be checked");
			Double min = type.getMinimumNumericValue();
			Double max = type.getMaximumNumericValue();
			if (min != null && numericValue < min) {
				throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
						"Parameter of type " + type.getName() + " has value " + numericValue + " < " + min);
			}
			if (max != null && numericValue > max) {
				throw new IcatException(IcatException.IcatExceptionType.VALIDATION,
						"Parameter of type " + type.getName() + " has value " + numericValue + " > " + max);
			}
		} else if (pvt == ParameterValueType.STRING) {
			logger.debug("Parameter of type " + type.getName() + " has string value " + stringValue + " to be checked");
			// The query is used because the ParameterType passed in may not
			// include its PermissibleStringValues
			List<String> values = manager.createNamedQuery("Parameter.psv", String.class)
					.setParameter("tid", type.getId()).getResultList();
			if (!values.isEmpty() && values.indexOf(stringValue) < 0) {
				throw new IcatException(IcatException.IcatExceptionType.VALIDATION, "Parameter of type "
						+ type.getName() + " has value " + stringValue + " not in allowed set " + values);
			}
		}
	}

	@Override
	public void postMergeFixup(EntityManager manager, GateKeeper gateKeeper) throws IcatException {
		super.postMergeFixup(manager, gateKeeper);
		check(manager);
	}

	@Override
	public void getDoc(JsonGenerator gen) {
		LuceneApi.encodeStringField(gen, "name", type.getName());
		LuceneApi.encodeStringField(gen, "units", type.getUnits());
		if (stringValue != null) {
			LuceneApi.encodeStringField(gen, "stringValue", stringValue);
		} else if (numericValue != null) {
			LuceneApi.encodeDoubleField(gen, "numericValue", numericValue);
		} else if (dateTimeValue != null) {
			LuceneApi.encodeStringField(gen, "dateTimeValue", dateTimeValue);
		}
	}

}
