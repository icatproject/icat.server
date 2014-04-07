package org.icatproject.core.manager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

/* This ought to be an MDB - but not able to persuade it to connect to remote destination with Glassfish */
public class GateKeeperListener implements MessageListener {

	private static final Logger logger = Logger.getLogger(GateKeeperListener.class);

	@Override
	public void onMessage(Message message) {
		TextMessage tm = (TextMessage) message;
		String msg = null;
		try {
			msg = tm.getText();
			logger.debug("msg was " + msg);
		} catch (JMSException e) {
			logger.error(e);
		}
		if (msg.equals("updatePublicTables")) {
			SingletonFinder.getGateKeeper().markStalePublicTables();
		} else if (msg.equals("updatePublicSteps")) {
			SingletonFinder.getGateKeeper().markStalePublicSteps();
		} else {
			logger.error("Unexpected message " + msg);
		}
	}

}
