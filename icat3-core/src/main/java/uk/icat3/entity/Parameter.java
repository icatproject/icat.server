package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class Parameter extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(DatasetParameter.class);

	@Comment("The type of the parameter")
	@JoinColumn(name = "PARAMETER_TYPE_ID", nullable = false)
	@ManyToOne
	ParameterType type;

	@Comment("The value if the parameter is a string")
	@Column(name = "STRING_VALUE")
	private String stringValue;

	@Comment("The value if the parameter is numeric")
	@Column(name = "NUMERIC_VALUE")
	private Double numericValue;

	@Comment("The value if the parameter is a date")
	@Column(name = "DATETIME_VALUE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateTimeValue;

	@Comment("The maximum value of the numeric parameter that was observed during the measurement period")
	private Double rangeTop;

	@Comment("The minimum value of the numeric parameter that was observed during the measurement period")
	private Double rangeBottom;

	@Comment("The error of the numeric parameter")
	private Double error;

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling Parameter for " + includes);
		if (!this.includes.contains(ParameterType.class)) {
			this.type = null;
		}
	}

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

}
