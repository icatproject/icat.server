// $Id: CommandLineProcessor.java 951 2011-08-11 13:18:19Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import org.apache.commons.cli.PosixParser;

import uk.icat.cmd.entity.State;
import uk.icat.cmd.input.OptionsBuilder;
import uk.icat.cmd.util.HelpUtil;

public class CommandLineProcessor extends Command {

	private static final String HELP_OPTION = "h";
	
	HelpUtil helpUtil;

	@Override
	public void process(State state) throws Exception {
		state.setOptions(OptionsBuilder.getAllOptions(state.getMethod(), state.getParameters()));
		state.setCommandLine(new PosixParser().parse(state.getOptions(), state.getArgs(), false));
		if (checkIfHelpRequested(state)) {
			helpUtil.printDetailedHelp(state.getMethod(), state.getOptions());
		} else {
			passToNext(state);
		}
	}

	private boolean checkIfHelpRequested(State state) {
		return state.getCommandLine().hasOption(HELP_OPTION);
	}
	
	public void setHelpUtil(HelpUtil helpUtil) {
		this.helpUtil = helpUtil;
	}

}
