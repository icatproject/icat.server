// $Id$
package uk.icat.cmd;

import uk.icat.cmd.chain.Command;
import uk.icat.cmd.entity.State;

public class CmdProcessor {

	Command chain;

	public void processInput(String[] args) throws Exception {
		State state = new State();
		state.setArgs(args);
		chain.process(state);
	}

	public void setChain(Command chain) {
		this.chain = chain;
	}

}
