package org.icatproject.core.manager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateKeeperListener implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(GateKeeperListener.class);

	@Override
	public void onMessage(Message message) {
		TextMessage tm = (TextMessage) message;
		String msg = null;
		try {
			msg = tm.getText();
			logger.debug("msg was " + msg);
		} catch (JMSException e) {
			logger.error("JMS Exception", e);
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
