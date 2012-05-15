package org.icatproject.useransto.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "USER_TABLE_ANSTO")
@NamedQuery(name = "AnstoUser.findByUserId", query = "SELECT u FROM UserE u WHERE u.userId = :userId")
public class UserE implements Serializable {

	@Id
	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "PASSWORD", nullable = false)
	private String password;
	
	// Needed by JPA
	public UserE(){}

	public String toString() {
		return userId;
	}

	public String getPassword() {
		return password;
	}



}
