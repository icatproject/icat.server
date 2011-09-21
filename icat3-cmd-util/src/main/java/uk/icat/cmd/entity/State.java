// $Id: State.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.entity;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class State {

	private String[] args;
	private String methodName;
	private Method method;
	private List<Parameter> parameters;
	private Options options;
	private CommandLine commandLine;
	private List<Object> createdParams;
	private Object result;

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setParameters(List<Parameter> extractParameters) {
		this.parameters = extractParameters;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setOptions(Options allOptions) {
		this.options = allOptions;
	}

	public Options getOptions() {
		return options;
	}

	public void setCommandLine(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	public CommandLine getCommandLine() {
		return commandLine;
	}

	public void setCreatedParameters(List<Object> list) {
		this.createdParams = list;
	}

	public List<Object> getCreatedParams() {
		return createdParams;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

}
