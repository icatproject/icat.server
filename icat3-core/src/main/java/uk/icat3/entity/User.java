package uk.icat3.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@Comment("A user of the facility")
@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(name = "\"USER\"")
public class User extends EntityBaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(User.class);

	@Comment("The name of the user to match that provided by the authentication mechanism")
	@Id
	private String name;

	@Comment("May include title")
	private String fullName;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<UserGroup> userGroups;

	public List<UserGroup> getUserGroups() {
		return userGroups;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<InvestigationUser> investigationUsers;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private List<InstrumentScientist> instrumentScientists;

	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

	public User() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User[name=" + name + "]";
	}

	@Override
	public Object getPK() {
		return name;
	}

	public void beforeMarshal(Marshaller source) {
		logger.trace("Marshalling User for " + includes);
		if (!this.includes.contains(InvestigationUser.class)) {
			this.investigationUsers = null;
		}
		if (!this.includes.contains(UserGroup.class)) {
			this.userGroups = null;
		}
		if (!this.includes.contains(InstrumentScientist.class)) {
			this.instrumentScientists = null;
		}
	}

	public List<InvestigationUser> getInvestigationUsers() {
		return investigationUsers;
	}

	public void setInvestigationUsers(List<InvestigationUser> investigationUsers) {
		this.investigationUsers = investigationUsers;
	}

	public List<InstrumentScientist> getInstrumentScientists() {
		return instrumentScientists;
	}

	public void setInstrumentScientists(List<InstrumentScientist> instrumentScientists) {
		this.instrumentScientists = instrumentScientists;
	}

}
