// $Id: Parameter.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.entity;

public class Parameter {

	String name;
	Class<?> type;

	public Parameter(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

}
