package com.devwebsphere.wxsutils.notification;

import java.io.Serializable;


public interface Producer extends Serializable {
	void start() throws Exception;

	void stop();

	void send(Message m) throws Exception;
}
