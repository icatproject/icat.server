// $Id: CommandLineProcessorTest.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;

import uk.icat.cmd.entity.State;
import uk.icat.cmd.input.OptionsBuilder;
import uk.icat.cmd.util.ParameterUtil;
import uk.icat3.client.EntityBaseBean;
import uk.icat3.client.ICAT;
import uk.icat3.client.Investigation;

public class CommandLineProcessorTest {

	private CommandLineProcessor commandLineProcessor;
	private State state;
	private Command command;

	@Before
	public void setUp() throws SecurityException, NoSuchMethodException {
		state = new State();
		state.setMethod(ICAT.class.getMethod("create", String.class, EntityBaseBean.class));
		state.setParameters(ParameterUtil.extractParameters(state.getMethod()));
		state.setOptions(OptionsBuilder.getAllOptions(state.getMethod(), state.getParameters()));

		commandLineProcessor = new CommandLineProcessor();

		command = mock(Command.class);
		commandLineProcessor.setNext(command);

	}

	@Test
	public void sholdPrintHelp() throws Exception {
		state.setArgs(new String[] { "create", "Investigation"});

		commandLineProcessor.process(state);

		verifyZeroInteractions(command);
	}

	@Test
	public void shouldGoFurther() throws Exception {
		state.setArgs(new String[] { "createInvestigation", "params" });

		commandLineProcessor.process(state);

		verify(command).process(state);
	}

}
