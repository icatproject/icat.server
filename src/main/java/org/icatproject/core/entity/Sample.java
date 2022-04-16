package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.json.stream.JsonGenerator;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.icatproject.core.manager.SearchApi;

@Comment("A sample to be used in an investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "INVESTIGATION_ID", "NAME" }) })
public class Sample extends EntityBaseBean implements Serializable {

	@Comment("A persistent identifier attributed to this sample")
	private String pid;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<Dataset> datasets = new ArrayList<>();

	@JoinColumn(nullable = false, name = "INVESTIGATION_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private Investigation investigation;

	@Column(nullable = false, name = "NAME")
	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<SampleParameter> parameters = new ArrayList<>();

	@JoinColumn(name = "SAMPLETYPE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private SampleType type;

	/* Needed for JPA */
	public Sample() {
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public List<Dataset> getDatasets() {
		return this.datasets;
	}

	public Investigation getInvestigation() {
		return this.investigation;
	}

	public String getName() {
		return this.name;
	}

	public List<SampleParameter> getParameters() {
		return parameters;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(List<SampleParameter> parameters) {
		this.parameters = parameters;
	}

	public SampleType getType() {
		return type;
	}

	public void setType(SampleType type) {
		this.type = type;
	}

	@Override
	public void getDoc(JsonGenerator gen) {
		SearchApi.encodeString(gen, "name", name);
		SearchApi.encodeString(gen, "id", id);
		SearchApi.encodeString(gen, "investigation.id", investigation.id);
		if (type != null) {
			type.getDoc(gen);
		}
	}

	/**
	 * Alternative method for encoding that applies a prefix to potentially
	 * ambiguous fields: "id" and "investigation.id". In the case of a single
	 * Dataset Sample, these fields will already be used by the Dataset and so
	 * cannot be overwritten by the Sample.
	 * 
	 * @param gen    JsonGenerator
	 * @param prefix String to precede all ambiguous field names.
	 */
	public void getDoc(JsonGenerator gen, String prefix) {
		SearchApi.encodeString(gen, prefix + "name", name);
		SearchApi.encodeString(gen, prefix + "id", id);
		SearchApi.encodeString(gen, prefix + "investigation.id", investigation.id);
		if (type != null) {
			type.getDoc(gen, prefix);
		}
	}

}
