package org.icatproject.core.manager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;

/**
 * This class is required in order to map to and from JSON for Elasticsearch
 * client functions
 */
@JsonInclude(Include.NON_EMPTY)
public class ElasticsearchDocument {

	private Long id;
	private String investigation;
	private String dataset;
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

	public ElasticsearchDocument() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ElasticsearchDocument(JsonArray jsonArray) throws IcatException {
		try {
			for (JsonValue fieldValue : jsonArray) {
				JsonObject fieldObject = (JsonObject) fieldValue;
				for (Entry<String, JsonValue> fieldEntry : fieldObject.entrySet()) {
					// TODO this is hideous, replace with something more dynamic? or at least a
					// switch?
					if (fieldEntry.getKey().equals("id")) {
						if (fieldEntry.getValue().getValueType().equals(ValueType.STRING)) {
							id = Long.valueOf(fieldObject.getString("id"));
						} else if (fieldEntry.getValue().getValueType().equals(ValueType.NUMBER)) {
							id = fieldObject.getJsonNumber("id").longValue();
						}
					} else if (fieldEntry.getKey().equals("investigation")) {
						if (fieldEntry.getValue().getValueType().equals(ValueType.STRING)) {
							investigation = fieldObject.getString("investigation");
						} else if (fieldEntry.getValue().getValueType().equals(ValueType.NUMBER)) {
							investigation = String.valueOf(fieldObject.getInt("investigation"));
						}
					} else if (fieldEntry.getKey().equals("dataset")) {
						if (fieldEntry.getValue().getValueType().equals(ValueType.STRING)) {
							dataset = fieldObject.getString("dataset");
						} else if (fieldEntry.getValue().getValueType().equals(ValueType.NUMBER)) {
							dataset = String.valueOf(fieldObject.getInt("dataset"));
						}
					} else if (fieldEntry.getKey().equals("text")) {
						text = fieldObject.getString("text");
					} else if (fieldEntry.getKey().equals("date")) {
						date = SearchApi.decodeDate(fieldObject.getString("date"));
					} else if (fieldEntry.getKey().equals("startDate")) {
						startDate = SearchApi.decodeDate(fieldObject.getString("startDate"));
					} else if (fieldEntry.getKey().equals("endDate")) {
						endDate = SearchApi.decodeDate(fieldObject.getString("endDate"));
					} else if (fieldEntry.getKey().equals("user.name")) {
						userName.add(fieldObject.getString("user.name"));
					} else if (fieldEntry.getKey().equals("user.fullName")) {
						userFullName.add(fieldObject.getString("user.fullName"));
					} else if (fieldEntry.getKey().equals("sample.name")) {
						sampleName.add(fieldObject.getString("sample.name"));
					} else if (fieldEntry.getKey().equals("sample.text")) {
						sampleText.add(fieldObject.getString("sample.text"));
					} else if (fieldEntry.getKey().equals("parameter.name")) {
						parameterName.add(fieldObject.getString("parameter.name"));
					} else if (fieldEntry.getKey().equals("parameter.units")) {
						parameterUnits.add(fieldObject.getString("parameter.units"));
					} else if (fieldEntry.getKey().equals("parameter.stringValue")) {
						parameterStringValue.add(fieldObject.getString("parameter.stringValue"));
					} else if (fieldEntry.getKey().equals("parameter.dateValue")) {
						parameterDateValue.add(SearchApi.decodeDate(fieldObject.getString("parameter.dateValue")));
					} else if (fieldEntry.getKey().equals("parameter.numericValue")) {
						parameterNumericValue
								.add(fieldObject.getJsonNumber("parameter.numericValue").doubleValue());
					}
				}
			}
		} catch (ParseException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getClass() + " " + e.getMessage());
		}
	}

	public ElasticsearchDocument(JsonArray jsonArray, String index, String parentIndex) throws IcatException {
		try {
			for (JsonValue fieldValue : jsonArray) {
				JsonObject fieldObject = (JsonObject) fieldValue;
				for (Entry<String, JsonValue> fieldEntry : fieldObject.entrySet()) {
					if (fieldEntry.getKey().equals(parentIndex)) {
						if (fieldEntry.getValue().getValueType().equals(ValueType.STRING)) {
							id = Long.valueOf(fieldObject.getString(parentIndex));
						} else if (fieldEntry.getValue().getValueType().equals(ValueType.NUMBER)) {
							id = fieldObject.getJsonNumber(parentIndex).longValue();
						}
					} else if (fieldEntry.getKey().equals("userName")) {
						userName.add(fieldObject.getString("userName"));
					} else if (fieldEntry.getKey().equals("userFullName")) {
						userFullName.add(fieldObject.getString("userFullName"));
					} else if (fieldEntry.getKey().equals("sampleName")) {
						sampleName.add(fieldObject.getString("sampleName"));
					} else if (fieldEntry.getKey().equals("sampleText")) {
						sampleText.add(fieldObject.getString("sampleText"));
					} else if (fieldEntry.getKey().equals("parameterName")) {
						parameterName.add(fieldObject.getString("parameterName"));
					} else if (fieldEntry.getKey().equals("parameterUnits")) {
						parameterUnits.add(fieldObject.getString("parameterUnits"));
					} else if (fieldEntry.getKey().equals("parameterStringValue")) {
						parameterStringValue.add(fieldObject.getString("parameterStringValue"));
					} else if (fieldEntry.getKey().equals("parameterDateValue")) {
						parameterDateValue.add(SearchApi.decodeDate(fieldObject.getString("parameterDateValue")));
					} else if (fieldEntry.getKey().equals("parameterNumericValue")) {
						parameterNumericValue
								.add(fieldObject.getJsonNumber("parameterNumericValue").doubleValue());
					}
				}
			}
		} catch (ParseException e) {
			throw new IcatException(IcatExceptionType.BAD_PARAMETER, e.getClass() + " " + e.getMessage());
		}
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getInvestigation() {
		return investigation;
	}

	public void setInvestigation(String investigation) {
		this.investigation = investigation;
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

}
