package com.devwebsphere.wxsutils.continuousfilter;

import java.util.Collection;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.notification.Producer;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class CFAgent implements ReduceGridAgent {
	static Logger logger = Logger.getLogger(CFAgent.class.getName());
	private static final long serialVersionUID = -1633890598594705262L;
	static String CONTINUOUS_FILTER_PREFIX = "CF_";

	enum Operation {
		SET, CLOSE
	};

	private Filter filter;
	private Producer producer;
	private Operation operation;
	private String mapName;

	static String getRegistrationMap(String mapName) {
		return CONTINUOUS_FILTER_PREFIX + mapName;
	}

	public CFAgent(String mapName, Filter filter, Producer producer, Operation op) {
		this(mapName, producer, op);
		this.filter = filter;
	}

	public CFAgent(String mapName, Producer producer, Operation op) {
		this.mapName = mapName;
		this.producer = producer;
		this.operation = op;
	}

	public Object reduce(Session s, ObjectMap map) {
		try {
			// grab the lock
			map.getForUpdate(producer);
			int partitionId = s.getObjectGrid().getMap(mapName).getPartitionId();
			switch (operation) {
			case SET:
				map.put(producer, filter);
				CFilterListener.setNotification(partitionId, mapName, producer, filter);
				return Boolean.TRUE;
			case CLOSE:
				map.remove(producer);
				CFilterListener.setNotification(partitionId, mapName, producer, null);

				return Boolean.TRUE;
			}
		} catch (ObjectGridException e) {
			throw new ObjectGridRuntimeException(e);
		}

		return Boolean.FALSE;
	}

	public Object reduce(Session s, ObjectMap m, Collection keys) {
		return null;
	}

	public Object reduceResults(Collection results) {
		boolean rc = true;
		for (Object o : results) {
			if (o instanceof EntryErrorValue) {
				return o;
			}
			if (o instanceof Boolean) {
				Boolean b = (Boolean) o;
				rc = rc && b;
			}
			if (!rc)
				break;
		}
		return rc;
	}

}
