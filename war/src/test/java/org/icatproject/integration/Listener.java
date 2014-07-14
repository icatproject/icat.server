package org.icatproject.integration;

import java.util.LinkedList;
import java.util.Queue;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class Listener implements MessageListener {

	Queue<ObjectMessage> q = new LinkedList<ObjectMessage>();

	@Override
	public void onMessage(Message message) {
		synchronized (q) {
			q.add((ObjectMessage) message);
		}
	}

	public ObjectMessage getMessage() {
		synchronized (q) {
			return q.poll();
		}
	}

}
