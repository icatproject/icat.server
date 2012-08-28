package org.icatproject.core.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.icatproject.core.IcatException;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.LexerException;
import org.icatproject.core.parser.ParserException;
import org.icatproject.core.parser.RestrictedBean;
import org.icatproject.core.parser.Token;
import org.icatproject.core.parser.Tokenizer;

@Comment("An authorization rule")
@SuppressWarnings("serial")
@Entity
@NamedQueries({
		@NamedQuery(name = "Rule.CreateQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.group g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.c = TRUE"),
		@NamedQuery(name = "Rule.ReadQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.group g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.r = TRUE"),
		@NamedQuery(name = "Rule.UpdateQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.group g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.u = TRUE"),
		@NamedQuery(name = "Rule.DeleteQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.group g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.d = TRUE"),
		@NamedQuery(name = "Rule.SearchQuery", query = "SELECT DISTINCT r          FROM Rule r LEFT JOIN r.group g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.r = TRUE") })
public class Rule extends EntityBaseBean implements Serializable {

	public static final String CREATE_QUERY = "Rule.CreateQuery";
	public static final String READ_QUERY = "Rule.ReadQuery";
	public static final String UPDATE_QUERY = "Rule.UpdateQuery";
	public static final String DELETE_QUERY = "Rule.DeleteQuery";
	public static final String SEARCH_QUERY = "Rule.SearchQuery";

	private final static Logger logger = Logger.getLogger(Rule.class);

	@XmlTransient
	private boolean c;

	@XmlTransient
	private boolean r;

	@XmlTransient
	private boolean u;

	@XmlTransient
	private boolean d;

	@ManyToOne(fetch = FetchType.LAZY)
	private Group group;

	@XmlTransient
	@Column(length = 1024)
	private String crudJPQL;

	@Comment("To what the rules applies")
	private String what;

	@XmlTransient
	@Column(length = 1024)
	private String searchJPQL;

	@XmlTransient
	private String beans;

	@Comment("Contains letters from the set \"CRUD\"")
	@Column(nullable = false, length = 4)
	private String crudFlags;

	@XmlTransient
	private boolean restricted;

	@XmlTransient
	private String bean;

	@XmlTransient
	public String getBean() {
		return bean;
	}

	public void setBean(String bean) {
		this.bean = bean;
	}

	@XmlTransient
	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	// Needed for JPA
	public Rule() {
	}

	private void fixup() throws IcatException {
		this.crudFlags = this.crudFlags.toUpperCase().trim();
		for (int i = 0; i < this.crudFlags.length(); i++) {
			final char c = this.crudFlags.charAt(i);
			if (c == 'C') {
				this.c = true;
			} else if (c == 'R') {
				this.r = true;
			} else if (c == 'U') {
				this.u = true;
			} else if (c == 'D') {
				this.d = true;
			} else {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER,
						"CRUD value " + this.crudFlags + " contains " + c);
			}
		}

		List<Token> tokens = null;
		try {
			tokens = Tokenizer.getTokens(this.what);
		} catch (final LexerException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}
		final Input input = new Input(tokens);
		RestrictedBean r;
		try {
			r = new RestrictedBean(input);
		} catch (final ParserException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}
		if (r.getSearchWhere().isEmpty()) {
			this.crudJPQL = null;
			this.searchJPQL = null;
		} else {
			this.crudJPQL = r.getQuery();
			this.searchJPQL = r.getSearchWhere();
		}
		this.bean = r.getBean();

		final StringBuilder sb = new StringBuilder();
		for (final Class<? extends EntityBaseBean> bean : r.getRelatedEntities()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(bean.getSimpleName());
		}

		this.beans = sb.toString();
		this.restricted = r.isRestricted();

	}

	@XmlTransient
	public String getBeans() {
		return this.beans;
	}

	public String getCrudFlags() {
		return this.crudFlags;
	}

	@XmlTransient
	public String getCrudJPQL() {
		return this.crudJPQL;
	}

	public Group getGroup() {
		return this.group;
	}

	@XmlTransient
	public String getSearchJPQL() {
		return this.searchJPQL;
	}

	public String getWhat() {
		return this.what;
	}

	@XmlTransient
	public boolean isC() {
		return this.c;
	}

	public boolean isD() {
		return this.d;
	}

	@XmlTransient
	public boolean isR() {
		return this.r;
	}

	@XmlTransient
	public boolean isU() {
		return this.u;
	}

	@Override
	public void postMergeFixup(EntityManager manager) throws IcatException {
		super.postMergeFixup(manager);
		this.c = false;
		this.r = false;
		this.u = false;
		this.d = false;
		this.fixup();
		logger.debug("postMergeFixup of Rule for " + this.crudFlags + " of " + this.what);
	}

	@Override
	public void preparePersist(String modId, EntityManager manager) throws IcatException {
		super.preparePersist(modId, manager);
		this.fixup();
		logger.debug("PreparePersist of Rule for " + this.crudFlags + " of " + this.what);
	}

	public void setBeans(String beans) {
		this.beans = beans;
	}

	public void setC(boolean c) {
		this.c = c;
	}

	public void setCrudFlags(String crudFlags) {
		this.crudFlags = crudFlags;
	}

	public void setCrudJPQL(String crudJPQL) {
		this.crudJPQL = crudJPQL;
	}

	public void setD(boolean d) {
		this.d = d;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public void setR(boolean r) {
		this.r = r;
	}

	public void setSearchJPQL(String searchJPQL) {
		this.searchJPQL = searchJPQL;
	}

	public void setU(boolean u) {
		this.u = u;
	}

	public void setWhat(String what) {
		this.what = what;
	}

}
