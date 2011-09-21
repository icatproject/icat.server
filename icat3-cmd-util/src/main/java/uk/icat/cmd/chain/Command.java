// $Id: Command.java 935 2011-08-09 13:25:38Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import uk.icat.cmd.entity.State;

public abstract class Command {

	private Command next;

	public abstract void process(State state) throws Exception;

	protected void passToNext(State state) throws Exception {
		if (next != null) {
			next.process(state);
		}
	}

	public void setNext(Command next) {
		this.next = next;
	}

}
