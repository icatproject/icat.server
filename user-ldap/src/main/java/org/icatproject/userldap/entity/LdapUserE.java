package org.icatproject.userldap.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "USER_TABLE")
@NamedQuery(name = "LdapUserE.findByUserId", query = "SELECT u FROM LdapUserE u WHERE u.userId = :userId")
public class LdapUserE implements Serializable {

	@Id
	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "PASSWORD", nullable = false)
	private String password;

	// Needed by JPA
	public LdapUserE(){}

	public String toString() {
		return userId;
	}

	public String getPassword() {
		return password;
	}



}
