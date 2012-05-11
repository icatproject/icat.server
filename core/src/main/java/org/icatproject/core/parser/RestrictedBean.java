package org.icatproject.core.parser;

import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;


public class RestrictedBean {

	// RestrictedBean ::= name Restriction

	private String tableName;
	private Restriction restriction;

	public RestrictedBean(Input input) throws ParserException {
		Token t = input.consume(Token.Type.NAME);
		tableName = t.getValue();
		restriction = new Restriction(input);
	}

	public String getQuery() throws IcatException {
		return restriction.getQuery(tableName);
	}

	@Override
	public String toString() {
		return tableName + " " + restriction;
	}

	public String getBean() {
		return tableName;
	}

	public String getSearchWhere() throws IcatException {
		return restriction.getSearchWhere(tableName);
	}

	public Set<Class<? extends EntityBaseBean>> getRelatedEntities() throws IcatException {
		return restriction.getRelatedEntities();
	}

	public boolean isRestricted() {
		return restriction.isRestricted();
	}

}
