package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.manager.LuceneApi;

@Comment("A sample to be used in one or more investigations")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "PID" }) })
public class Sample extends EntityBaseBean implements Serializable {

	@Comment("A persistent identifier attributed to this sample")
	@Column(nullable = false, name = "PID")
	private String pid;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<Dataset> datasets = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sample")
	private List<InvestigationSample> investigationSamples = new ArrayList<>();

	@Column(nullable = false)
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

	public List<InvestigationSample> getInvestigationSamples() {
		return this.investigationSamples;
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

	public void setInvestigationSamples(List<InvestigationSample> investigationSamples) {
		this.investigationSamples = investigationSamples;
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
		StringBuilder sb = new StringBuilder(pid + " " + name);
		if (type != null) {
			sb.append(" " + type.getName());
		}
		LuceneApi.encodeTextfield(gen, "text", sb.toString());
	}
}
