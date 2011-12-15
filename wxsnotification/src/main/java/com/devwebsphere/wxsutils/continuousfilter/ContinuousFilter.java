package com.devwebsphere.wxsutils.continuousfilter;

import java.io.Closeable;
import java.util.Set;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.continuousfilter.CFAgent.Operation;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.notification.Connection;
import com.devwebsphere.wxsutils.notification.Consumer;
import com.devwebsphere.wxsutils.notification.Producer;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

public class ContinuousFilter<K, V> implements Closeable {
	private WXSUtils utils;
	private String mapName;
	private Producer producer;
	private Consumer<CFCallback> consumer;
	private CFSet<K> cfSet = new CFSet<K>();

	public ContinuousFilter(WXSUtils utils, String mapName, Connection c) {
		utils.getCache(mapName);
		this.utils = utils;
		this.mapName = mapName;
		producer = c.createProducer();
		consumer = c.createConsumer();
		try {
			consumer.start(cfSet);
		} catch (Exception e) {
			throw new ObjectGridRuntimeException(e);
		}
	}

	public boolean setFilter(Filter f) {
		CFAgent agent = new CFAgent(mapName, f, producer, Operation.SET);
		return invokeAgent(agent);
	}

	public void close() {
		CFAgent agent = new CFAgent(mapName, null, producer, Operation.CLOSE);
		invokeAgent(agent);
		consumer.stop();
	}

	private boolean invokeAgent(CFAgent agent) {
		Session s = utils.getSessionForThread();
		try {
			ObjectMap m = s.getMap(CFAgent.getRegistrationMap(mapName));
			AgentManager am = m.getAgentManager();
			Object o = am.callReduceAgent(agent);
			if (o instanceof EntryErrorValue) {
				EntryErrorValue eev = (EntryErrorValue) o;
				throw new ObjectGridRuntimeException(
						eev.getErrorExceptionString(), eev.getException());
			}
			return (Boolean) o;
		} catch (UndefinedMapException e) {
			throw new ObjectGridRuntimeException(e);
		}
	}

	public CFSet<K> asSet() {
		return cfSet;
	}
}
