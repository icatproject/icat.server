// $Id: MethodHelper.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.util;

import java.lang.reflect.Method;

import uk.icat.cmd.exception.MissingMethodException;

public class MethodHelper {

	private Class<?> targetClass;

	public MethodHelper(Class<?> clazz) {
		targetClass = clazz;
	}

	public Method[] getMethods() {
		return targetClass.getMethods();
	}

	public final Method extractMethod(String methodName) throws MissingMethodException {
		for (Method method : targetClass.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		throw new MissingMethodException("Unable to find method: " + methodName);
	}

	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

}
