// $Id: ParameterParser.java 935 2011-08-09 13:25:38Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;

import uk.icat.cmd.entity.Parameter;
import uk.icat.cmd.entity.State;
import uk.icat.cmd.exception.MissingParameterException;
import uk.icat.cmd.util.IcatUtil;
import uk.icat.cmd.util.ParameterUtil;

public class ParameterParser extends Command {

	private IcatUtil icatUtil;

	@Override
	public void process(State state) throws Exception {
		ArrayList<Object> createdParams = new ArrayList<Object>();
		createdParams.add(icatUtil.getSid());

		for (Parameter p : state.getParameters()) {
			checkMandatoryParameterFields(p, state.getCommandLine().getOptions());
		}

		for (int i = 1; i < state.getParameters().size(); i++) { // first parameter is sessionId
			Object parameter = ParameterUtil.createParameterInstance(state.getParameters().get(i), state.getCommandLine().getOptions(), i);
			createdParams.add(parameter);
		}

		state.setCreatedParameters(createdParams);

		passToNext(state);
	}

	private void checkMandatoryParameterFields(Parameter p, Option[] options) throws Exception {
		if (p.getType().getPackage().getName().startsWith("uk.icat3.client")) {
			List<String> mandatoryParameters = icatUtil.getMandatoryParameters(p.getType());
			for (String field : mandatoryParameters) {
				boolean found = false;
				for (Option o : options) {
					if (field.equals(o.getLongOpt())) {
						found = true;
					}
				}
				if (!found) {
					throw new MissingParameterException("Missing mandatory parameter: " + field + "\nMandatory parameters are: " + mandatoryParameters.toString());
				}
			}
		}
	}

	public void setIcatUtil(IcatUtil icatUtil) {
		this.icatUtil = icatUtil;
	}

}
