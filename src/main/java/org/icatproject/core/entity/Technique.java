package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.stream.JsonGenerator;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.icatproject.core.manager.search.SearchApi;

@Comment("Represents an experimental technique")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME" }) })
public class Technique extends EntityBaseBean implements Serializable {

	@Comment("A short name for the technique")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("A persistent identifier attributed to the technique, ideally referring to a vocabulary term")
	private String pid;

	@Comment("An informal description for the technique")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "technique")
	private List<DatasetTechnique> datasetTechniques = new ArrayList<DatasetTechnique>();

	public static Set<String> docFields = new HashSet<>(
			Arrays.asList("technique.id", "technique.name", "technique.description", "technique.pid"));

	public String getName() {
		return name;
	}

	public String getPid() {
		return pid;
	}

	public String getDescription() {
		return description;
	}

	public List<DatasetTechnique> getDatasetTechniques() {
		return datasetTechniques;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDatasetTechniques(List<DatasetTechnique> datasetTechniques) {
		this.datasetTechniques = datasetTechniques;
	}

	@Override
	public void getDoc(JsonGenerator gen) {
		SearchApi.encodeString(gen, "technique.id", id);
		SearchApi.encodeString(gen, "technique.name", name);
		SearchApi.encodeString(gen, "technique.description", description);
		SearchApi.encodeString(gen, "technique.pid", pid);
	}
}
