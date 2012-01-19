package uk.icat3.security.parser;

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

}
