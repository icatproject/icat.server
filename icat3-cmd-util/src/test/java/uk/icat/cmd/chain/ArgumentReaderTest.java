// $Id: ArgumentReaderTest.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import uk.icat.cmd.entity.State;
import uk.icat.cmd.util.HelpUtil;
import uk.icat.cmd.util.MethodHelper;

public class ArgumentReaderTest {

	private ArgumentReader argumentReader;
	private State state;
	private MethodHelper methodHelper;

	@Before
	public void setUp() {
		argumentReader = new ArgumentReader();
		methodHelper = mock(MethodHelper.class);
		when(methodHelper.getMethods()).thenReturn(new Method[] {});
		argumentReader.setMethodHelper(methodHelper);
		argumentReader.setHelpUtil(mock(HelpUtil.class));
		state = new State();
	}

	@Test
	public void shouldPrintMethodList() throws Exception {
		state.setArgs(new String[] { "-l" });

		argumentReader.process(state);

		verify(methodHelper).getMethods();
	}

	@Test
	public void shouldPrintHelp() throws Exception {
		state.setArgs(new String[] { "-h" });

		argumentReader.process(state);

	}

}
