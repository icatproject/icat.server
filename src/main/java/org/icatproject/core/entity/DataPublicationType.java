package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Comment("A type of data publication, for example, whole investigation, user-selected datasets/files")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "NAME" }) })
public class DataPublicationType extends EntityBaseBean implements Serializable {

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private List<DataPublication> dataPublications = new ArrayList<DataPublication>();

	@Comment("A description of this data publicaion type")
	private String description;

	@Comment("The facility which has defined this data publication type")
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@Comment("A short name identifying this data publication type within the facility")
	@Column(name = "NAME", nullable = false)
	private String name;

	/* Needed for JPA */
	public DataPublicationType() {
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public List<DataPublication> getDataPublications() {
		return this.dataPublications;
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	public void setDataPublications(List<DataPublication> dataPublications) {
		this.dataPublications = dataPublications;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

}
