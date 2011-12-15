package com.devwebsphere.wxsutils.notification;

import java.io.Serializable;

public interface Consumer<T> extends Serializable {
	void start(T cb) throws Exception;

	void stop();
}
