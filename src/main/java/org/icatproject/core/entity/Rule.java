package org.icatproject.core.entity;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.GateKeeper;
import org.icatproject.core.manager.SingletonFinder;
import org.icatproject.core.oldparser.OldInput;
import org.icatproject.core.oldparser.OldLexerException;
import org.icatproject.core.oldparser.OldParserException;
import org.icatproject.core.oldparser.OldSearchQuery;
import org.icatproject.core.oldparser.OldTokenizer;
import org.icatproject.core.parser.ParserException;
import org.icatproject.core.parser.RuleWhat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Comment("An authorization rule")
@SuppressWarnings("serial")
@Entity
@Table(name = "RULE_")
@NamedQueries({
		@NamedQuery(name = "Rule.CreateQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.grouping g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.c = TRUE"),
		@NamedQuery(name = "Rule.ReadQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.grouping g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.r = TRUE"),
		@NamedQuery(name = "Rule.IncludeQuery", query = "SELECT DISTINCT r.includeJPQL FROM Rule r LEFT JOIN r.grouping g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.r = TRUE"),
		@NamedQuery(name = "Rule.UpdateQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.grouping g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.u = TRUE"),
		@NamedQuery(name = "Rule.DeleteQuery", query = "SELECT DISTINCT r.crudJPQL FROM Rule r LEFT JOIN r.grouping g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.d = TRUE"),
		@NamedQuery(name = "Rule.SearchQuery", query = "SELECT DISTINCT r          FROM Rule r LEFT JOIN r.grouping g LEFT JOIN g.userGroups ug LEFT JOIN ug.user u WHERE (u.name = :member OR g IS NULL) AND r.bean = :bean AND r.r = TRUE"),
		@NamedQuery(name = "Rule.PublicQuery", query = "SELECT DISTINCT r.bean     FROM Rule r LEFT JOIN r.grouping g WHERE r.restricted = FALSE AND g IS NULL") })
public class Rule extends EntityBaseBean implements Serializable {

	@EJB
	@XmlTransient
	@Transient
	private GateKeeper gatekeeper;

	private final static Logger logger = LoggerFactory.getLogger(Rule.class);

	public static final String CREATE_QUERY = "Rule.CreateQuery";
	public static final String DELETE_QUERY = "Rule.DeleteQuery";
	public static final String READ_QUERY = "Rule.ReadQuery";
	public static final String INCLUDE_QUERY = "Rule.IncludeQuery";
	public static final String SEARCH_QUERY = "Rule.SearchQuery";
	public static final String UPDATE_QUERY = "Rule.UpdateQuery";
	public static final String PUBLIC_QUERY = "Rule.PublicQuery";

	@XmlTransient
	private String bean;

	@XmlTransient
	private boolean c;

	@Comment("Contains letters from the set \"CRUD\"")
	@Column(nullable = false, length = 4)
	private String crudFlags;

	@XmlTransient
	@Column(length = 1024)
	private String crudJPQL;

	@XmlTransient
	private boolean d;

	@ManyToOne(fetch = FetchType.LAZY)
	private Grouping grouping;

	@XmlTransient
	private boolean r;

	@XmlTransient
	private boolean restricted;

	@XmlTransient
	private boolean u;

	@Comment("To what the rules applies")
	@Column(nullable = false, length = 1024)
	private String what;

	@XmlTransient
	@Column(length = 1024)
	private String searchJPQL;

	@XmlTransient
	@Column(length = 1024)
	private String includeJPQL;

	// Needed for JPA
	public Rule() {
	}

	private void fixup(EntityManager manager, GateKeeper gateKeeper) throws IcatException {
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

		if (what == null) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, "'what' must not be null");
		}

		String query = what;
		if (!query.toUpperCase().trim().startsWith("SELECT")) {

			/* Parse the old style rule */
			try {
				OldSearchQuery oldSearchQuery = new OldSearchQuery(new OldInput(OldTokenizer.getTokens(query)));
				query = oldSearchQuery.getNewQuery();
			} catch (OldLexerException e) {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
			} catch (OldParserException e) {
				throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
			}
			logger.debug("New style rule: " + query);
		} else {
			/* This should be pure JPQL so can check it */
			gateKeeper.checkJPQL(query);
		}

		RuleWhat r;
		try {
			r = new RuleWhat(query);
		} catch (final ParserException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}

		crudJPQL = "SELECT COUNT(" + r.getIdPath() + ") FROM " + r.getFrom() + " WHERE " + r.getIdPath() + " = :pkid"
				+ (r.getWhere().isEmpty() ? "" : " AND (" + r.getWhere() + ")");
		includeJPQL = "SELECT " + r.getIdPath() + " FROM " + r.getFrom() + " WHERE " + r.getIdPath() + " IN (:pkids)"
				+ (r.getWhere().isEmpty() ? "" : " AND (" + r.getWhere() + ")");
		searchJPQL = "SELECT " + r.getIdPath() + " FROM " + r.getFrom()
				+ (r.getWhere().isEmpty() ? "" : " WHERE " + r.getWhere());

		bean = r.getBean().getSimpleName();

		restricted = !r.getWhere().isEmpty();
	}

	@XmlTransient
	public String getBean() {
		return bean;
	}

	public String getCrudFlags() {
		return crudFlags;
	}

	public Grouping getGrouping() {
		return grouping;
	}

	public String getWhat() {
		return what;
	}

	@XmlTransient
	public boolean isC() {
		return c;
	}

	@XmlTransient
	public boolean isD() {
		return d;
	}

	@XmlTransient
	public boolean isR() {
		return r;
	}

	@XmlTransient
	public boolean isRestricted() {
		return restricted;
	}

	@XmlTransient
	public boolean isU() {
		return this.u;
	}

	@Override
	public void postMergeFixup(EntityManager manager, GateKeeper gateKeeper) throws IcatException {
		super.postMergeFixup(manager, gateKeeper);
		this.c = false;
		this.r = false;
		this.u = false;
		this.d = false;
		this.fixup(manager, gateKeeper);
		logger.debug("postMergeFixup of Rule for " + this.crudFlags + " of " + this.what);
	}

	@Override
	public void preparePersist(String modId, EntityManager manager, GateKeeper gateKeeper, boolean rootUser, boolean clearId)
			throws IcatException {
		super.preparePersist(modId, manager, gateKeeper, rootUser, clearId);
		this.fixup(manager, gateKeeper);
		logger.debug("PreparePersist of Rule for " + this.crudFlags + " of " + this.what);
	}

	public void setBean(String bean) {
		this.bean = bean;
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

	public void setSearchJPQL(String searchJPQL) {
		this.searchJPQL = searchJPQL;
	}

	public void setGrouping(Grouping grouping) {
		this.grouping = grouping;
	}

	public void setR(boolean r) {
		this.r = r;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public void setU(boolean u) {
		this.u = u;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	@PostRemove()
	void postRemove() {
		try {
			SingletonFinder.getGateKeeper().requestUpdatePublicTables();
		} catch (Throwable e) {
			logger.error(e.getClass() + " " + e.getMessage());
		}
	}

	@PostPersist()
	void postPersist() {
		try {
			SingletonFinder.getGateKeeper().requestUpdatePublicTables();
		} catch (Throwable e) {
			logger.error(e.getClass() + " " + e.getMessage());
		}
	}

	public String getSearchJPQL() {
		return searchJPQL;
	}

}
