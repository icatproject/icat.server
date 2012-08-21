import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class Listener implements MessageListener {

	Queue<Map<String, String>> q = new LinkedList<Map<String, String>>();

	@Override
	public void onMessage(Message message) {
		try {
			Map<String, String> msg = new HashMap<String, String>();
			@SuppressWarnings("rawtypes")
			Enumeration names = message.getPropertyNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				msg.put(name, message.getStringProperty(name));

			}
			synchronized (q) {
				q.add(msg);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	public Map<String, String> getMessage() {
		synchronized (q) {
			return q.poll();
		}
	}

}
