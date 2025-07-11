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

@Comment("A type of investigation")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "FACILITY_ID" }) })
public class InvestigationType extends EntityBaseBean implements Serializable {

	@Comment("A short name identifying this type of investigation")
	@Column(nullable = false, name = "NAME")
	private String name;

	@Comment("The facility which has defined this investigation type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Comment("A description of this type of investigation")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<Investigation> investigations = new ArrayList<Investigation>();

	public List<Investigation> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(List<Investigation> investigations) {
		this.investigations = investigations;
	}

	public static Set<String> docFields = new HashSet<>(Arrays.asList("type.name", "type.id"));

	/* Needed for JPA */
	public InvestigationType() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void getDoc(EntityManager entityManager, JsonGenerator gen) throws IcatException {
		SearchApi.encodeString(gen, "type.name", name);
		SearchApi.encodeLong(gen, "type.id", id);
	}

}
