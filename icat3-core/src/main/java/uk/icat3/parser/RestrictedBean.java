package uk.icat3.parser;

import java.util.Set;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;

public class RestrictedBean {

	// RestrictedBean ::= name Restriction

	private String tableName;
	private Restriction restriction;

	public RestrictedBean(Input input) throws ParserException {
		Token t = input.consume(Token.Type.NAME);
		tableName = t.getValue();
		restriction = new Restriction(input);
	}

	public String getQuery() throws BadParameterException, IcatInternalException {
		return restriction.getQuery(tableName);
	}

	@Override
	public String toString() {
		return tableName + " " + restriction;
	}

	public String getBean() {
		return tableName;
	}

	public String getSearchWhere() throws BadParameterException {
		return restriction.getSearchWhere(tableName);
	}

	public Set<Class<? extends EntityBaseBean>> getRelatedEntities() throws BadParameterException {
		return restriction.getRelatedEntities();
	}

	public boolean isRestricted() {
		return restriction.isRestricted();
	}

}
