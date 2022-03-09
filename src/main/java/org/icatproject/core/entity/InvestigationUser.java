package org.icatproject.core.entity;

import java.io.Serializable;

import javax.json.stream.JsonGenerator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.icatproject.core.manager.SearchApi;

@Comment("Many to many relationship between investigation and user. It is expected that this will show the association of "
		+ "individual users with an investigation which might be derived from the proposal. It may also be used as the "
		+ "basis of authorization rules. See InvestigationGroup if you wish to separate authorization rules from who is "
		+ "on the proposal.")
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_ID", "INVESTIGATION_ID", "ROLE" }) })
public class InvestigationUser extends EntityBaseBean implements Serializable {

	@Comment("A role such as PI showing the position of the user with respect to the investigation")
	@Column(name = "ROLE", nullable = false)
	private String role;

	@JoinColumn(name = "USER_ID", nullable = false)
	@ManyToOne
	private User user;

	@JoinColumn(name = "INVESTIGATION_ID", nullable = false)
	@ManyToOne
	private Investigation investigation;

	/* Needed for JPA */
	public InvestigationUser() {
	}

	@Override
	public void getDoc(JsonGenerator gen, SearchApi searchApi) {
		if (user.getFullName() != null) {
			searchApi.encodeTextField(gen, "text", user.getFullName());
		}
		searchApi.encodeStringField(gen, "name", user.getName());
		searchApi.encodeSortedDocValuesField(gen, "investigation", investigation.id);
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Investigation getInvestigation() {
		return investigation;
	}

	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

}
