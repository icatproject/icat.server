// $Id: ParameterExtractor.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import org.apache.commons.lang.ArrayUtils;

import uk.icat.cmd.entity.State;
import uk.icat.cmd.util.MethodHelper;
import uk.icat.cmd.util.ParameterUtil;

public class ParameterExtractor extends Command {

	MethodHelper methodHelper;

	private static final String[] EBB_METHODS = new String[] { "create", "delete" };

	@Override
	public void process(State state) throws Exception {
		state.setMethodName(state.getArgs()[0]);
		state.setMethod(methodHelper.extractMethod(state.getMethodName()));
		if (ArrayUtils.contains(EBB_METHODS, state.getMethodName())) {
			state.setParameters(ParameterUtil.extractParameters(state.getMethod(), state.getArgs()[1]));
		} else {
			state.setParameters(ParameterUtil.extractParameters(state.getMethod()));
		}

		passToNext(state);
	}

	public void setMethodHelper(MethodHelper methodHelper) {
		this.methodHelper = methodHelper;
	}

}
