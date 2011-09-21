package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table(name = "USERGROUP")
@NamedQueries({
		@NamedQuery(name = "UserGroup.All", query = "SELECT g FROM UserGroup g ORDER BY g.name, g.member"),
		@NamedQuery(name = "UserGroup.PK", query = "SELECT g FROM UserGroup g WHERE g.name = :name and g.member = :member") })
@XmlRootElement
public class UserGroup implements Serializable {

	public static final String PK = "UserGroup.PK";
	public static final String ALL = "UserGroup.All";

	// Needed for JPA
	public UserGroup() {
	};

	public UserGroup(String name, String member) {
		this.name = name;
		this.member = member;
		modTime = new Date();
	}

	public String getName() {
		return this.name;
	}

	public String getMember() {
		return this.member;
	}

	@Id
	@Column(name = "NAME", nullable = false)
	private String name;

	@Id
	@Column(name = "MEMBER", nullable = false)
	private String member;
	
	@SuppressWarnings("unused")
	@Column(name = "MOD_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@XmlElement
	private Date modTime;


}
