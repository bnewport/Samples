package com.devwebsphere.wxsutils.notification;

public interface Connection {
	Producer createProducer();

	<T> Consumer<T> createConsumer();
}
