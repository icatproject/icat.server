// $Id: MethodExecutorTest.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.icat.cmd.entity.State;
import uk.icat3.client.EntityBaseBean;
import uk.icat3.client.ICAT;
import uk.icat3.client.Investigation;

public class MethodExecutorTest {

	private State state;
	private MethodExecutor methodExecutor;
	private Command command;
	private ICAT icat;

	@Before
	public void setUp() throws SecurityException, NoSuchMethodException {
		state = new State();
		state.setMethod(ICAT.class.getMethod("create", String.class, EntityBaseBean.class));
		List<Object> tmp = new ArrayList<Object>();
		tmp.add("session_id");
		tmp.add(new Investigation());
		state.setCreatedParameters(tmp);

		methodExecutor = new MethodExecutor();

		command = mock(Command.class);
		methodExecutor.setNext(command);

		icat = mock(ICAT.class);
		methodExecutor.setTargetService(icat);
	}

	@Test
	public void shouldExecuteMethod() throws Exception {

		methodExecutor.process(state);

		verify(icat).create(any(String.class), any(Investigation.class));
	}

}
