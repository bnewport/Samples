package com.devwebsphere.wxsutils.continuousfilter;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.devwebsphere.wxsutils.continuousfilter.CFCallback;
import com.devwebsphere.wxsutils.notification.Consumer;

public class ActiveMQConsumer implements Consumer<CFCallback>, MessageListener {
	private static final long serialVersionUID = -2727136385578064159L;
	private Queue queue;
	private transient MessageConsumer jmsConsumer;
	private CFCallback callback;

	public ActiveMQConsumer() throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
		Connection c = factory.createConnection();
		c.start();
		Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = s.createTemporaryQueue();
		jmsConsumer = s.createConsumer(queue);
		jmsConsumer.setMessageListener(this);
	}

	public Queue getQueue() {
		return queue;
	}

	public void start(CFCallback cb) throws Exception {
		callback = cb;
	}

	public void stop() {
		callback = null;

		try {
			jmsConsumer.close();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	public void onMessage(Message jmsMsg) {
		try {
			jmsMsg.acknowledge();
			if (callback == null) {
				return;
			}

			ObjectMessage oMsg = (ObjectMessage) jmsMsg;
			callback.received((CFMessage) oMsg.getObject());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
