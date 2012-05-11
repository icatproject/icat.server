package uk.icat3.sessionbeans.manager;

import java.io.Serializable;

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

import uk.icat3.entity.NotificationRequest.DestType;
import uk.icat3.manager.NotificationMessages;
import uk.icat3.manager.NotificationMessages.Message;

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

			String name = message.getNotificationName();
			if (name != null) {
				jmsg.setStringProperty("notificationName", name);
			}

			String userId = message.getUserId();
			if (userId != null) {
				jmsg.setStringProperty("userId", userId);
			}

			name = message.getEntityName();
			if (name != null) {
				jmsg.setStringProperty("entityName", name);
			}

			String args = message.getArgs();
			if (args != null) {
				jmsg.setStringProperty("entityArgs", args);
			}

			jmsg.setObject((Serializable) message.getPk());
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
