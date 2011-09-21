/*
 * DatasetStatus.java
 *
 * Created on 08 February 2007, 09:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.security.parser.Input;
import uk.icat3.security.parser.LexerException;
import uk.icat3.security.parser.ParserException;
import uk.icat3.security.parser.Restriction;
import uk.icat3.security.parser.Token;
import uk.icat3.security.parser.Tokenizer;

@SuppressWarnings("serial")
@Entity
@Table(name = "RULE")
@NamedQueries({
		@NamedQuery(name = "Rule.CreateQuery", query = "SELECT DISTINCT r.crudJPQL FROM UserGroup g, Rule r where ((g.member = :member and r.groupName = g.name) or "
				+ "r.groupName is null) and r.what = :what and r.cAllowed = 'Y'"),
		@NamedQuery(name = "Rule.ReadQuery", query = "SELECT DISTINCT r.crudJPQL FROM UserGroup g, Rule r where ((g.member = :member and r.groupName = g.name) or "
				+ "r.groupName is null) and r.what = :what and r.rAllowed = 'Y'"),
		@NamedQuery(name = "Rule.UpdateQuery", query = "SELECT DISTINCT r.crudJPQL FROM UserGroup g, Rule r where ((g.member = :member and r.groupName = g.name) or "
				+ "r.groupName is null) and r.what = :what and r.uAllowed = 'Y'"),
		@NamedQuery(name = "Rule.DeleteQuery", query = "SELECT DISTINCT r.crudJPQL FROM UserGroup g, Rule r where ((g.member = :member and r.groupName = g.name) or "
				+ "r.groupName is null) and r.what = :what and r.dAllowed = 'Y'"),
		@NamedQuery(name = "Rule.SearchQuery", query = "SELECT DISTINCT r FROM UserGroup g, Rule r where ((g.member = :member and r.groupName = g.name) or "
				+ "r.groupName is null) and r.what = :what and r.rAllowed = 'Y'"),
		@NamedQuery(name = "Rule.All", query = "SELECT r FROM Rule r") })
@XmlRootElement
@SequenceGenerator(name = "RULE_SEQ", sequenceName = "RULE_ID_SEQ", allocationSize = 1)
public class Rule implements Serializable {

	public static final String CREATE_QUERY = "Rule.CreateQuery";
	public static final String READ_QUERY = "Rule.ReadQuery";
	public static final String UPDATE_QUERY = "Rule.UpdateQuery";
	public static final String DELETE_QUERY = "Rule.DeleteQuery";
	public static final String SEARCH_QUERY = "Rule.SearchQuery";
	public static final String ALL = "Rule.All";

	@Column(name = "C", nullable = false)
	private String cAllowed = "N";

	@Column(name = "D", nullable = false)
	private String dAllowed = "N";

	@Column(name = "GROUP_NAME", nullable = false)
	private String groupName;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RULE_SEQ")
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "CRUD_JPQL")
	private String crudJPQL;

	@Column(name = "R", nullable = false)
	private String rAllowed = "N";

	@Column(name = "RESTRICTION")
	private String restriction;

	@Column(name = "U", nullable = false)
	private String uAllowed = "N";;

	@Column(name = "WHAT")
	private String what;

	@Column(name = "SEARCH_JPQL")
	private String searchJPQL;

	@Column(name = "BEANS")
	private String beans;
	
	@SuppressWarnings("unused")
	@Column(name = "MOD_TIME", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@XmlElement
	private Date modTime;


	// Needed for JPA
	public Rule() {
	}

	public Rule(String groupName, String what, String crud, String restriction)
			throws BadParameterException, IcatInternalException {
		this.groupName = groupName;
		this.what = what;
		crud = crud.toUpperCase();
		for (int i = 0; i < crud.length(); i++) {
			char c = crud.charAt(i);
			if (c == 'C') {
				this.cAllowed = "Y";
			} else if (c == 'R') {
				this.rAllowed = "Y";
			} else if (c == 'U') {
				this.uAllowed = "Y";
			} else if (c == 'D') {
				this.dAllowed = "Y";
			} else {
				throw new BadParameterException("CRUD value " + crud + " contains " + c);
			}
		}
		if (restriction != null) {
			List<Token> tokens = null;
			try {
				tokens = Tokenizer.getTokens(restriction);
			} catch (LexerException e) {
				throw new BadParameterException(e.getMessage());
			}
			Input input = new Input(tokens);
			Restriction r;
			try {
				r = new Restriction(input);
			} catch (ParserException e) {
				throw new BadParameterException(e.getMessage());
			}
			crudJPQL = r.getQuery(what);
			searchJPQL = r.getSearchWhere(what);

			StringBuilder sb = new StringBuilder();
			for (Class<? extends EntityBaseBean> bean : r.getRelatedEntities()) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(bean.getSimpleName());
			}

			beans = sb.toString();
		}
		this.restriction = restriction;
		this.modTime = new Date();
	}

	public boolean getdAllowed() {
		return this.dAllowed == "Y";
	}

	public String getGroupName() {
		return this.groupName;
	}

	public Long getId() {
		return this.id;
	}

	public String getCrudJPQL() {
		return this.crudJPQL;
	}

	public String getRestriction() {
		return this.restriction;
	}

	public String getWhat() {
		return this.what;
	}

	public boolean isCAllowed() {
		return this.cAllowed == "Y";
	}

	public boolean isRAllowed() {
		return this.rAllowed == "Y";
	}

	public boolean isUAllowed() {
		return this.uAllowed == "Y";
	}

	public String getBeans() {
		return beans;
	}

	public String getSearchJPQL() {
		return searchJPQL;
	}

}
