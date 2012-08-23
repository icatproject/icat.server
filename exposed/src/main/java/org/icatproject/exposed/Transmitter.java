package org.icatproject.exposed;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;

import org.apache.log4j.Logger;
import org.icatproject.core.entity.NotificationRequest.DestType;
import org.icatproject.core.manager.NotificationMessages;
import org.icatproject.core.manager.NotificationMessages.Message;

public class Transmitter {

	private static Logger logger = Logger.getLogger(Transmitter.class);

	public static void processMessages(NotificationMessages notificationMessages,
			QueueConnection queueConnection, Queue queue, TopicConnection topicConnection,
			Topic topic) throws JMSException {

		Session qSession = null;
		Session tSession = null;
		MessageProducer qProducer = null;
		MessageProducer tProducer = null;

		/* Ensure that we have at most one session of each type with one producer */
		for (Message message : notificationMessages.getMessages()) {

			Session session = null;
			MessageProducer producer = null;
			if (message.getDestType() == DestType.P2P) {
				if (qSession == null) {
					qSession = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					qProducer = qSession.createProducer(queue);
					logger.debug("QueueSession created");
				}
				session = qSession;
				producer = qProducer;
			} else {
				if (tSession == null) {
					tSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					tProducer = tSession.createProducer(topic);
					logger.debug("TopicSession created");
				}
				session = tSession;
				producer = tProducer;
			}

			ObjectMessage jmsg = session.createObjectMessage();

			String notificationName = message.getNotificationName();
			if (notificationName != null) {
				jmsg.setStringProperty(NotificationMessages.NOTIFICATIONNAME, notificationName);
			}

			String userId = message.getUserId();
			if (userId != null) {
				jmsg.setStringProperty(NotificationMessages.USERID, userId);
			}

			String entityName = message.getEntityName();
			if (entityName != null) {
				jmsg.setStringProperty(NotificationMessages.ENTITYNAME, entityName);
			}

			String query = message.getQuery();
			if (query != null) {
				jmsg.setStringProperty(NotificationMessages.QUERY, query);
			}

			Long pk = message.getPk();
			if (pk != null) {
				jmsg.setLongProperty(NotificationMessages.ENTITYID, pk);
			}
			producer.send(jmsg);
		}

		if (qSession != null) {
			qSession.close();
			logger.debug("QueueSession closed");
		}
		if (tSession != null) {
			tSession.close();
			logger.debug("TopicSession closed");
		}

	}

}
