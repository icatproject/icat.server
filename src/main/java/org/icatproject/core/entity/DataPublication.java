package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Comment("A curated data publication")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "FACILITY_ID", "PID" }) })
public class DataPublication extends EntityBaseBean implements Serializable {

	@JoinColumn(name = "FACILITY_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Facility facility;

	@JoinColumn(name = "DATACOLLECTION_ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private DataCollection content;

	@JoinColumn(name = "DATAPUBLICATIONTYPE_ID")
	@ManyToOne(fetch = FetchType.LAZY)
	private DataPublicationType type;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "publication")
	private List<DataPublicationUser> users = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "publication")
	private List<DataPublicationDate> dates = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "publication")
	private List<RelatedItem> relatedItems = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dataPublication")
	private List<DataPublicationFunding> fundingReferences = new ArrayList<>();

	@Comment("Persistent Identifier of the publication, such as a DOI")
	@Column(name = "PID", nullable = false)
	private String pid;

	@Comment("Title of the publication")
	@Column(nullable = false)
	private String title;

	@Comment("Date when the data was made publicly available")
	@Temporal(TemporalType.TIMESTAMP)
	private Date publicationDate;

	@Comment("List of keywords")
	@Column(length = 1023)
	private String subject;

	@Comment("Abstract")
	@Column(length = 4000)
	private String description;

	/* Needed for JPA */
	public DataPublication() {
	}

	public Facility getFacility() {
		return facility;
	}

	public DataCollection getContent() {
		return content;
	}

	public DataPublicationType getType() {
		return type;
	}

	public List<DataPublicationUser> getUsers() {
		return users;
	}

	public List<DataPublicationDate> getDates() {
		return dates;
	}

	public List<RelatedItem> getRelatedItems() {
		return relatedItems;
	}

	public List<DataPublicationFunding> getFundingReferences() {
		return fundingReferences;
	}

	public String getPid() {
		return pid;
	}

	public String getTitle() {
		return title;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public String getSubject() {
		return subject;
	}

	public String getDescription() {
		return description;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public void setContent(DataCollection content) {
		this.content = content;
	}

	public void setType(DataPublicationType type) {
		this.type = type;
	}

	public void setUsers(List<DataPublicationUser> users) {
		this.users = users;
	}

	public void setDates(List<DataPublicationDate> dates) {
		this.dates = dates;
	}

	public void setRelatedItems(List<RelatedItem> relatedItems) {
		this.relatedItems = relatedItems;
	}

	public void setFundingReferences(List<DataPublicationFunding> fundingReferences) {
		this.fundingReferences = fundingReferences;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
