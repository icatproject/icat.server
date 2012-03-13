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

	@Comment("**** I suggest we change this to be of type Double")
	private String rangeTop;

	@Comment("**** I suggest we change this to be of type Double")
	private String rangeBottom;

	@Comment("**** I suggest we change this to be of type Double")
	private String error;
	
	@Comment("**** Do we need this - surely this is part of the parameter type - not needed by ISIS")
	@Column(name = "DESCRIPTION")
	private String description;

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

	public String getRangeTop() {
		return rangeTop;
	}

	public void setRangeTop(String rangeTop) {
		this.rangeTop = rangeTop;
	}

	public String getRangeBottom() {
		return rangeBottom;
	}

	public void setRangeBottom(String rangeBottom) {
		this.rangeBottom = rangeBottom;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
