package org.icatproject.exposed;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;

import org.apache.log4j.Logger;
import org.icatproject.core.manager.NotificationMessages;
import org.icatproject.core.manager.NotificationMessages.Message;

public class Transmitter {

	private static Logger logger = Logger.getLogger(Transmitter.class);

	public static void processMessages(NotificationMessages notificationMessages,
			TopicConnection topicConnection, Topic topic) throws JMSException {

		Session tSession = null;
		MessageProducer tProducer = null;

		/* Ensure that we have at most one session of each type with one producer */
		for (Message message : notificationMessages.getMessages()) {

			Session session = null;
			MessageProducer producer = null;

			if (tSession == null) {
				tSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				tProducer = tSession.createProducer(topic);
				logger.debug("TopicSession created");
			}
			session = tSession;
			producer = tProducer;

			ObjectMessage jmsg = session.createObjectMessage();
			jmsg.setStringProperty("entity", message.getEntityName());
			jmsg.setStringProperty("operation", message.getOperation());
			jmsg.setObject(message.getEntityId());
			producer.send(jmsg);
		}

		if (tSession != null) {
			tSession.close();
			logger.debug("TopicSession closed");
		}

	}

}
