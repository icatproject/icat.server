// $Id: ParameterUtil.java 970 2011-08-17 10:47:34Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jws.WebParam;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.cli.Option;
import org.pojava.datetime.DateTime;

import uk.icat.cmd.entity.Parameter;
import uk.icat.cmd.exception.ParameterCreationException;
import uk.icat.cmd.exception.UnknownParameterException;

public class ParameterUtil {

	public static List<Parameter> extractParameters(Method method) {
		List<Parameter> parameters = new ArrayList<Parameter>();

		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();

		int i = 0;
		for (Annotation[] annotations : parameterAnnotations) {
			Class<?> parameterType = parameterTypes[i++];
			for (Annotation annotation : annotations) {
				if (annotation instanceof WebParam) {
					WebParam myAnnotation = (WebParam) annotation;
					parameters.add(new Parameter(myAnnotation.name(), parameterType));
				}
			}
		}
		return parameters;
	}

	public static Object getParameterInstance(Parameter p) throws ParameterCreationException {
		Class<?> clazz = p.getType();
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new ParameterCreationException(e);
		}
	}

	public static Object createParameterInstance(Parameter parameter, Option[] options, int index) throws Exception {

		String value = getParameterValue(options, parameter);

		if (value != null) {
			return createSimpleTypeParameter(parameter, value);
		}

		// isPrimitive but value isn't given
		if (isPrimitive(parameter)) {
			return null;
		}

		Object instance = getParameterInstance(parameter);
		setParameterProperties(options, instance);

		return instance;
	}

	private static Object createSimpleTypeParameter(Parameter parameter, String value) throws DatatypeConfigurationException, UnknownParameterException {
		try {
			return createInstance(parameter.getType(), value);
		} catch (IllegalArgumentException e) {
			System.err.println("Unable to convert value: \"" + value + "\"");
			throw new UnknownParameterException();
		}
	}

	private static boolean isPrimitive(Parameter parameter) {
		return isAssignable(parameter, Long.class) || isAssignable(parameter, String.class) || isAssignable(parameter, Double.class) || isAssignable(parameter, XMLGregorianCalendar.class)
				|| parameter.getType().isEnum();
	}

	private static boolean isAssignable(Parameter parameter, Class<?> clazz) {
		return parameter.getType().isAssignableFrom(clazz);
	}

	private static void setParameterProperties(Option[] options, Object instance) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, DatatypeConfigurationException {
		for (Option o : options) {
			Class<?> propertyType = PropertyUtils.getPropertyType(instance, o.getLongOpt());
			Object val = createInstance(propertyType, o.getValue());
			PropertyUtils.setProperty(instance, o.getLongOpt(), val);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object createInstance(Class<?> clazz, String value) throws DatatypeConfigurationException {
		if (clazz.isAssignableFrom(XMLGregorianCalendar.class)) {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(DateTime.parse(value).toDate());
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} else if (clazz.isAssignableFrom(Long.class)) {
			return Long.valueOf(value);
		} else if (clazz.isAssignableFrom(String.class)) {
			return value;
		} else if (clazz.isAssignableFrom(Double.class)) {
			return Long.valueOf(value);
		} else if (clazz.isEnum()) {
			return Enum.valueOf((Class<? extends Enum>) clazz, value);
		}
		return null;
	}

	private static String getParameterValue(Option[] options, Parameter parameter) {
		for (Option o : options) {
			if (parameter.getName().equals(o.getLongOpt())) {
				return o.getValue();
			}
		}
		return null;
	}

	public static List<Parameter> extractParameters(Method method, String entityType) throws DatatypeConfigurationException {
		try {
			Class<?> loadedClass = ClassLoader.getSystemClassLoader().loadClass("uk.icat3.client." + entityType);
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.add(new Parameter("sessionId", String.class));
			parameters.add(new Parameter("bean", loadedClass));
			return parameters;
		} catch (ClassNotFoundException e) {
			throw new DatatypeConfigurationException("Type " + entityType + " is not supported");
		}
	}

}
