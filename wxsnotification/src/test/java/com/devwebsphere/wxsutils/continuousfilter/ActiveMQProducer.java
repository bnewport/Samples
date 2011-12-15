package com.devwebsphere.wxsutils.continuousfilter;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.devwebsphere.wxsutils.notification.Message;
import com.devwebsphere.wxsutils.notification.Producer;

public class ActiveMQProducer implements Producer {
	private static final long serialVersionUID = -6172122029334834418L;
	private Queue queue;
	private transient Connection conn;
	private transient Session session;
	private transient MessageProducer jmsProducer;

	public ActiveMQProducer(Queue queue) {
		this.queue = queue;
	}

	protected ActiveMQProducer() {

	}

	public void start() throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
		conn = factory.createConnection();
		conn.start();
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

		jmsProducer = session.createProducer(queue);
		jmsProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}

	public void stop() {
		try {
			session = null;
			jmsProducer.close();
			conn.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void send(Message m) throws JMSException {
		if (session != null) {
			ObjectMessage oMsg = session.createObjectMessage(m);
			jmsProducer.send(oMsg);
		}
	}

	@Override
	public int hashCode() {
		return queue.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == ActiveMQProducer.class) {
			return queue.equals(((ActiveMQProducer) obj).queue);
		}
		return false;
	}

}
