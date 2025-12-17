package org.icatproject.core.entity;

import java.io.Serializable;

import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.search.SearchApi;

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
	public void getDoc(EntityManager entityManager, JsonGenerator gen) throws IcatException {
		if (user.getName() == null) {
			user = entityManager.find(user.getClass(), user.id);
		}
		user.getDoc(entityManager, gen);
		SearchApi.encodeLong(gen, "investigation.id", investigation.id);
		SearchApi.encodeLong(gen, "id", id);
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
