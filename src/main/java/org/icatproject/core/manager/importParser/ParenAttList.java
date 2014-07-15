package org.icatproject.core.manager.importParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.importParser.Token.Type;

public class ParenAttList {

	// parenAttList = "(" attWithOptParenList {"," attWithOptParenList } ")"

	private List<AttWithOptParenList> parenAttList = new ArrayList<>();

	private List<Field> fields = new ArrayList<>();

	private String joins = "";

	private List<String> wheres = new ArrayList<>();

	private List<Attribute> attributes = new ArrayList<>();;

	public ParenAttList(Input input, Class<EntityBaseBean> entityClass,
			Map<String, Field> fieldsByName, Map<Field, Method> setters, String parentName)
			throws ParserException, IcatException {

		input.consume(Token.Type.OPENPAREN);
		int i = 0;
		while (true) {
			AttWithOptParenList attWithOptParenList = new AttWithOptParenList(input, entityClass,
					fieldsByName, setters, parentName, "n" + i++);
			parenAttList.add(attWithOptParenList);
			joins += attWithOptParenList.getJoins();
			wheres.addAll(attWithOptParenList.getWheres());
			attributes.addAll(attWithOptParenList.getAttributes());
			Type next = input.peek(0).getType();
			if (next == Token.Type.COMMA) {
				input.consume();
			} else if (next == Token.Type.CLOSEPAREN) {
				input.consume();
				break;
			} else {
				input.consume(Token.Type.COMMA, Token.Type.CLOSEPAREN);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (AttWithOptParenList attWithOptParenList : parenAttList) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(attWithOptParenList);
		}
		return "(" + sb.toString() + ")";
	}

	public List<Field> getFields() {
		return fields;
	}

	public String getJoins() {
		return joins;
	}

	public List<String> getWheres() {
		return wheres;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

}
