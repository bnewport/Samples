package com.devwebsphere.wxsutils.continuousfilter;

import javax.jms.JMSException;

import com.devwebsphere.wxsutils.notification.Consumer;
import com.devwebsphere.wxsutils.notification.Producer;
import com.devwebsphere.wxsutils.notification.Connection;

public class ActiveMQTransport implements Connection {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -6966421323874694607L;

	Producer producer;
	ActiveMQConsumer consumer;

	public ActiveMQTransport() throws JMSException {
		consumer = new ActiveMQConsumer();
		producer = new ActiveMQProducer(consumer.getQueue());
	}

	public Producer createProducer() {
		return producer;
	}

	public Consumer createConsumer() {
		return consumer;
	}
}
