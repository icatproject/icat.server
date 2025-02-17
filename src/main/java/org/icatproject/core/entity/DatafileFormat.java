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

@Comment("A data file format")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME", "VERSION" }) })
public class DatafileFormat extends EntityBaseBean implements Serializable {

	@Comment("The facility which has defined this format")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Comment("Files with this format")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datafileFormat")
	private List<Datafile> datafiles = new ArrayList<Datafile>();

	@Comment("An informal description of the format")
	private String description;

	@Comment("Holds the underlying format - such as binary or text")
	private String type;

	@Comment("A short name identifying the format -e.g. \"mp3\" within the facility")
	@Column(name = "NAME", nullable = false)
	private String name;

	@Comment("The version if needed.  The version code may be part of the basic name")
	@Column(name = "VERSION", nullable = false)
	private String version;

	public static Set<String> docFields = new HashSet<>(Arrays.asList("datafileFormat.name", "datafileFormat.id"));

	/* Needed for JPA */
	public DatafileFormat() {
	}

	public List<Datafile> getDatafiles() {
		return this.datafiles;
	}

	public String getDescription() {
		return this.description;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setDatafiles(List<Datafile> datafiles) {
		this.datafiles = datafiles;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public void getDoc(EntityManager manager, JsonGenerator gen) throws IcatException {
		SearchApi.encodeString(gen, "datafileFormat.name", name);
		SearchApi.encodeLong(gen, "datafileFormat.id", id);
	}

}
