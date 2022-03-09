package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is required in order to map to and from JSON for Elasticsearch
 * client functions
 */
public class ElasticsearchDocument {

	private Long id;
	private String text;
	private Date date;
	private Date startDate;
	private Date endDate;
	private List<String> userName = new ArrayList<>();
	private List<String> userFullName = new ArrayList<>();
	private List<String> sampleName = new ArrayList<>();
	private List<String> sampleText = new ArrayList<>();
	private List<String> parameterName = new ArrayList<>();
	private List<String> parameterUnits = new ArrayList<>();
	private List<String> parameterStringValue = new ArrayList<>();
	private List<Date> parameterDateValue = new ArrayList<>();
	private List<Double> parameterNumericValue = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public List<Double> getParameterNumericValue() {
		return parameterNumericValue;
	}

	public void setParameterNumericValue(List<Double> parameterNumericValue) {
		this.parameterNumericValue = parameterNumericValue;
	}

	public List<Date> getParameterDateValue() {
		return parameterDateValue;
	}

	public void setParameterDateValue(List<Date> parameterDateValue) {
		this.parameterDateValue = parameterDateValue;
	}

	public List<String> getParameterStringValue() {
		return parameterStringValue;
	}

	public void setParameterStringValue(List<String> parameterStringValue) {
		this.parameterStringValue = parameterStringValue;
	}

	public List<String> getParameterUnits() {
		return parameterUnits;
	}

	public void setParameterUnits(List<String> parameterUnits) {
		this.parameterUnits = parameterUnits;
	}

	public List<String> getParameterName() {
		return parameterName;
	}

	public void setParameterName(List<String> parameterName) {
		this.parameterName = parameterName;
	}

	public List<String> getSampleText() {
		return sampleText;
	}

	public void setSampleText(List<String> sampleText) {
		this.sampleText = sampleText;
	}

	public List<String> getSampleName() {
		return sampleName;
	}

	public void setSampleName(List<String> sampleName) {
		this.sampleName = sampleName;
	}

	public List<String> getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(List<String> userFullName) {
		this.userFullName = userFullName;
	}

	public List<String> getUserName() {
		return userName;
	}

	public void setUserName(List<String> userName) {
		this.userName = userName;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
