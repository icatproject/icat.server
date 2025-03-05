package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.search.SearchApi;

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

	public static Set<String> docFields = new HashSet<>(
			Arrays.asList("sample.name", "sample.id", "sample.investigation.id"));

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
	public void getDoc(EntityManager manager, JsonGenerator gen) throws IcatException {
		SearchApi.encodeString(gen, "sample.name", name);
		SearchApi.encodeLong(gen, "sample.id", id);
		if (investigation != null) {
			// Investigation is not nullable, but it is possible to pass Samples without their Investigation
			// relationship populated when creating Datasets, where this field is not needed anyway - so guard against
			// null pointers
			SearchApi.encodeLong(gen, "sample.investigation.id", investigation.id);
		}
		if (type != null) {
			if (type.getName() == null) {
				type = manager.find(type.getClass(), type.id);
			}
			type.getDoc(manager, gen);
		}
	}

}
