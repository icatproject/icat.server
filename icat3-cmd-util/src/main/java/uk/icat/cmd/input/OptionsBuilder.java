// $Id: OptionsBuilder.java 951 2011-08-11 13:18:19Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.input;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import uk.icat.cmd.entity.Parameter;
import uk.icat3.client.EntityBaseBean;

public class OptionsBuilder {

	private static final String JAVA_PACKAGE = "java";

	public static Options getAllOptions(Method method, List<Parameter> parameters) {
		Options options = new Options();
		for (Parameter parameter : parameters) {
			addDefaultOptions(options);
			if (parameter.getType().isEnum()) {
				createEnumOption(options, parameter);
			} else if (parameter.getType().getName().startsWith(JAVA_PACKAGE)) {
				createSimpleOption(options, parameter, null);
			} else {
				createComplexOption(options, parameter);
			}
		}
		return options;
	}

	private static void createEnumOption(Options options, Parameter parameter) {
		Field[] fields = parameter.getType().getFields();
		StringBuffer buffer = new StringBuffer();
		buffer.append("Property " + parameter.getName() + " with available values: ");
		for (Field f : fields) {
			if (f.isEnumConstant()) {
				buffer.append(f.getName());
				buffer.append(" ");
			}
		}
		options.addOption(new Option(null, parameter.getName(), true, buffer.toString()));
	}

	private static void addDefaultOptions(Options options) {
		options.addOption(new Option("h", "help", false, "prints help"));
	}

	private static void createComplexOption(Options options, Parameter parameter) {
		Field[] fields = parameter.getType().getDeclaredFields();
		for (Field field : fields) {
			Class<?> type = field.getType();
			Parameter newParam = new Parameter(field.getName(), type);
			createSimpleOption(options, newParam, parameter.getType().getName());
		}
	}

	private static void createSimpleOption(Options options, Parameter parameter, String parentParameter) {
		String description = null;
		if (parentParameter != null) {
			description = "Property " + parameter.getName() + " of " + " parameter " + parentParameter;
		} else {
			description = "Property " + parameter.getName();
		}
		options.addOption(new Option(null, parameter.getName(), true, description));
	}

}
