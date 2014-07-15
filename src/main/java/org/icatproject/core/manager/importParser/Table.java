package org.icatproject.core.manager.importParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.manager.EntityInfoHandler;

public class Table {

	// table = String "(" [ tableField " {"," , tableField} ] ")"

	private String name;
	private List<TableField> tableFields = new ArrayList<>();
	private Class<EntityBaseBean> tableClass;
	private final static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	public Table(Input input) throws ParserException, IcatException {
		name = input.consume(Token.Type.NAME).getValue();
		tableClass = EntityInfoHandler.getClass(name);
		Map<String, Field> fieldsByName = eiHandler.getFieldsByName(tableClass);
		Map<Field, Method> setters = eiHandler.getSetters(tableClass);

		input.consume(Token.Type.OPENPAREN);
		while (true) {

			Token next = input.peek(0);
			if (next != null && next.getType() == Token.Type.CLOSEPAREN) {
				input.consume(Token.Type.CLOSEPAREN);
				break;
			}

			TableField tableField = new TableField(input, tableClass, fieldsByName, setters);
			tableFields.add(tableField);

			next = input.peek(0);
			if (next != null && next.getType() == Token.Type.COMMA) {
				input.consume();
			}
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TableField tableField : tableFields) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append(tableField);
		}
		return name + "(" + sb.toString() + ")";

	}

	public List<TableField> getTableFields() {
		return tableFields;
	}

	public EntityBaseBean createEntity() throws IcatException {
		try {
			return tableClass.newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			throw new IcatException(IcatExceptionType.INTERNAL, e.getClass() + " " + e.getMessage());
		}
	}
}
