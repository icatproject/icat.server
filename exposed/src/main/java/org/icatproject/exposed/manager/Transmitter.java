package org.icatproject.exposed.manager;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.icatproject.core.entity.NotificationRequest.DestType;
import org.icatproject.core.manager.NotificationMessages;
import org.icatproject.core.manager.NotificationMessages.Message;

public class Transmitter {

	public static void processMessages(NotificationMessages notificationMessages, QueueConnection queueConnection,
			Queue queue, TopicConnection topicConnection, Topic topic) throws JMSException {
		QueueSender qs = null;
		TopicPublisher tp = null;
		for (Message message : notificationMessages.getMessages()) {
			MessageProducer producer;
			ObjectMessage jmsg;
			if (message.getDestType() == DestType.P2P) {
				QueueSession session = null;
				if (qs == null) {
					session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
					qs = session.createSender(queue);
				}
				producer = session.createProducer(queue);
				jmsg = session.createObjectMessage();

			} else {
				TopicSession session = null;
				if (tp == null) {
					session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
					tp = session.createPublisher(topic);
				}
				producer = session.createProducer(topic);
				jmsg = session.createObjectMessage();
			}

			String notificationName = message.getNotificationName();
			if (notificationName != null) {
				jmsg.setStringProperty("notificationName", notificationName);
			}

			String userId = message.getUserId();
			if (userId != null) {
				jmsg.setStringProperty("userId", userId);
			}

			String entityName = message.getEntityName();
			if (entityName != null) {
				jmsg.setStringProperty("entityName", entityName);
			}

			String query = message.getQuery();
			if (query != null) {
				jmsg.setStringProperty("query", query);
			}

			Long pk = message.getPk();
			if (pk != null) {
				jmsg.setLongProperty("query", pk);
			}
			producer.send(jmsg);
		}

		if (qs != null) {
			qs.close();
		}
		if (tp != null) {
			tp.close();
		}

	}

}
