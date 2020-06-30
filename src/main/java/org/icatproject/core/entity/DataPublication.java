package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

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

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "publication")
	private List<DataPublicationUser> users = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "publication")
	private List<DataPublicationDate> dates = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "publication")
	private List<RelatedIdentifier> relatedIdentifiers = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "publication")
	private List<FundingReference> fundingReferences = new ArrayList<>();

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

	public List<DataPublicationUser> getUsers() {
		return users;
	}

	public List<DataPublicationDate> getDates() {
		return dates;
	}

	public List<RelatedIdentifier> getRelatedIdentifiers() {
		return relatedIdentifiers;
	}

	public List<FundingReference> getFundingReferences() {
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

	public void setUsers(List<DataPublicationUser> users) {
		this.users = users;
	}

	public void setDates(List<DataPublicationDate> dates) {
		this.dates = dates;
	}

	public void setRelatedIdentifiers(List<RelatedIdentifier> relatedIdentifiers) {
		this.relatedIdentifiers = relatedIdentifiers;
	}

	public void setFundingReferences(List<FundingReference> fundingReferences) {
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
