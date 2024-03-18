package org.icatproject.core.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;
import org.icatproject.core.parser.Token.Type;

public class RuleWhat {

	// RuleWhat ::= name from_clause [where_clause]

	private String idVar;

	private WhereClause whereClause;

	private FromClause fromClause;

	private String attribute;

	private Pattern idPattern = Pattern.compile("^[a-zA-Z_$]([\\w_$])*$");

	public RuleWhat(String query) throws ParserException, IcatException {
		List<Token> tokens;
		try {
			tokens = Tokenizer.getTokens(query);
		} catch (final LexerException e) {
			throw new IcatException(IcatException.IcatExceptionType.BAD_PARAMETER, e.getMessage());
		}
		final Input input = new Input(tokens);

		input.consume(Token.Type.SELECT);
		idVar = input.consume(Token.Type.NAME).getValue();

		int offset = idVar.indexOf('.');
		if (offset > 0) {
			attribute = idVar.substring(offset + 1);
			idVar = idVar.substring(0, offset);
		}

		if (!idPattern.matcher(idVar).matches()) {
			throw new ParserException(
					"Rule must have an entity reference possibly followed by an attribute in the SELECT clause rather than "
							+ idVar);
		}

		Set<String> idPaths = new HashSet<>(Arrays.asList(idVar));
		fromClause = new FromClause(input, idPaths);

		if (offset > 0) {
			Class<? extends EntityBaseBean> bean = fromClause.getAuthzMap().get(idVar + ".id");
			if (EntityInfoHandler.getFieldsByName(bean).get(attribute) == null) {
				throw new ParserException(bean + " does not have attribute " + attribute);
			}
		}

		Token t = input.peek(0);
		if (t != null && t.getType() == Token.Type.WHERE) {
			whereClause = new WhereClause(input);
			t = input.peek(0);
		}
		if (t != null) {
			throw new ParserException(input, new Type[0]);
		}

	}

	public Class<? extends EntityBaseBean> getBean() {
		return fromClause.getAuthzMap().get(idVar + ".id");
	}

	public String getWhere() {
		return whereClause == null ? "" : whereClause.toString();
	}

	public String getFrom() {
		return fromClause == null ? "" : fromClause.toString();
	}

	public String getIdPath() {
		return idVar + ".id";
	}

	public String getAttribute() {
		return attribute;
	}

}
