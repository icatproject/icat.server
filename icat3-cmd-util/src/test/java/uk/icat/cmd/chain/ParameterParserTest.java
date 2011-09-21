// $Id: ParameterParserTest.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.junit.Before;
import org.junit.Test;

import uk.icat.cmd.entity.State;
import uk.icat.cmd.exception.MissingMethodException;
import uk.icat.cmd.input.OptionsBuilder;
import uk.icat.cmd.util.IcatUtil;
import uk.icat.cmd.util.ParameterUtil;
import uk.icat3.client.EntityBaseBean;
import uk.icat3.client.ICAT;
import uk.icat3.client.Investigation;

public class ParameterParserTest {

	private static final String EXPERIMENT = "experiment";
	private static final String SID = "sid";
	private State state;
	private ParameterParser parameterParser;

	@Before
	public void setUp() throws SecurityException, NoSuchMethodException, MissingMethodException, ParseException {
		state = new State();
		state.setMethod(ICAT.class.getMethod("create", String.class, EntityBaseBean.class));
		state.setParameters(ParameterUtil.extractParameters(state.getMethod()));
		state.setOptions(OptionsBuilder.getAllOptions(state.getMethod(), state.getParameters()));
		state.setArgs(new String[] { "create", "Investigation", "--invType", EXPERIMENT });
		state.setCommandLine(new PosixParser().parse(state.getOptions(), state.getArgs(), false));

		parameterParser = new ParameterParser();

		IcatUtil icatUtil = mock(IcatUtil.class);
		when(icatUtil.getSid()).thenReturn(SID);
		parameterParser.setIcatUtil(icatUtil);

	}

	@Test
	public void shouldCreateParameters() throws Exception {

		parameterParser.process(state);

		List<Object> parameters = state.getCreatedParams();
		assertNotNull(parameters);
		assertEquals(SID, parameters.get(0));
		assertTrue(parameters.get(1) instanceof Investigation);
		assertEquals(EXPERIMENT, ((Investigation) parameters.get(1)).getInvType());

	}

}
