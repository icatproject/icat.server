package org.icatproject.core.manager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.icatproject.core.manager.NotificationMessage.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Singleton
public class NotificationTransmitter {

	private static Logger logger = LoggerFactory.getLogger(NotificationTransmitter.class);
	private final static Marker fatal = MarkerFactory.getMarker("FATAL");

	private Topic topic;

	@EJB
	PropertyHandler propertyHandler;

	private TopicConnection topicConnection;

	@PostConstruct
	private void init() {

		try {
			InitialContext ic = new InitialContext();
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ic
					.lookup(propertyHandler.getJmsTopicConnectionFactory());
			topicConnection = topicConnectionFactory.createTopicConnection();
			topic = (Topic) ic.lookup("jms/ICAT/Topic");
			logger.info("Notification Transmitter created");
		} catch (JMSException | NamingException e) {
			logger.error(fatal, "Problem with JMS " + e);
			throw new IllegalStateException(e.getMessage());
		}

	}

	@PreDestroy()
	private void exit() {
		try {
			if (topicConnection != null) {
				topicConnection.close();
			}
			logger.info("Notification Transmitter closing down");
		} catch (JMSException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public void processMessage(NotificationMessage notificationMessage) throws JMSException {
		Message message = notificationMessage.getMessage();
		if (message != null) {
			try ( Session jmsSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE) )
			{
				MessageProducer jmsProducer = jmsSession.createProducer(topic);
				ObjectMessage jmsg = jmsSession.createObjectMessage();
				jmsg.setStringProperty("entity", message.getEntityName());
				jmsg.setStringProperty("operation", message.getOperation());
				jmsg.setObject(message.getEntityId());
				jmsProducer.send(jmsg);
				logger.debug("Sent jms notification message " + message.getOperation() + " " + message.getEntityName() + " "
					+ message.getEntityId());
			} catch (JMSException e) {
				logger.error("Failed to send jms notification message ");
			}
		}

	}
}
