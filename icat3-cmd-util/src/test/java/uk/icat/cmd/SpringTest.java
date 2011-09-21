// $Id: SpringTest.java 937 2011-08-09 14:38:49Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
public class SpringTest {

	@Autowired
	CmdProcessor client;

	@Test
	public void shouldCreateCmdProcessorBean() {
		assertNotNull(client);
	}

}
