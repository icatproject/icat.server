// $Id: ICatCmdClient.java 935 2011-08-09 13:25:38Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ICatCmdClient {

	public static void main(String[] args) throws Exception {
		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
			CmdProcessor cmdProcessor = context.getBean(CmdProcessor.class);
			cmdProcessor.processInput(args);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}
}
