package org.icatproject.core;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.apache.log4j.Logger;
import org.icatproject.core.manager.NotificationMessage;
import org.icatproject.core.manager.NotificationMessage.Message;

@DependsOn("LoggingConfigurator")
@Singleton
public class Transmitter {

	private static Logger logger = Logger.getLogger(Transmitter.class);

	// TODO - this use of mappedName rather than name lags elegance - but it
	// works
	@Resource(mappedName = "jms/ICATTopic")
	private Topic topic;

	@Resource(name = "jms/ICATTopicConnectionFactory")
	private TopicConnectionFactory topicConnectionFactory;

	private TopicConnection topicConnection;

	@PostConstruct
	private void init() {

		try {
			topicConnection = topicConnectionFactory.createTopicConnection();
			logger.info("Transmitter created");
		} catch (JMSException e) {
			logger.fatal("Problem with JMS " + e);
			throw new IllegalStateException(e.getMessage());
		}

	}

	@PreDestroy()
	private void exit() {
		try {
			if (topicConnection != null) {
				topicConnection.close();
			}
			logger.info("Transmitter closing down");
		} catch (JMSException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public void processMessage(NotificationMessage notificationMessage) throws JMSException {
		Message message = notificationMessage.getMessage();
		if (message != null) {
			Session jmsSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer jmsProducer = jmsSession.createProducer(topic);
			ObjectMessage jmsg = jmsSession.createObjectMessage();
			jmsg.setStringProperty("entity", message.getEntityName());
			jmsg.setStringProperty("operation", message.getOperation());
			jmsg.setObject(message.getEntityId());
			jmsProducer.send(jmsg);
			logger.debug("Sent jms message " + message.getOperation() + " "
					+ message.getEntityName() + " " + message.getEntityId());
			jmsSession.close();
		}

	}
}
