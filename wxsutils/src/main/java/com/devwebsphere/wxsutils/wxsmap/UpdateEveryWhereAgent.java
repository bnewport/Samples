//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.wxsmap;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.utils.ClassSerializer;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This agent will do a put operation for the entries and remove the keys in EVERY partition. The changes are made to
 * the current primary partition and all routing is therefore ignored. This is useful when you want to store the SAME
 * data in every partition. This agent is used to maintain that data when it changes.
 * 
 * @author bnewport
 * 
 * @param <K>
 * @param <V>
 */
public class UpdateEveryWhereAgent<K extends Serializable, V extends Serializable> implements ReduceGridAgent, Externalizable {
	private static final long serialVersionUID = -8306827952007113258L;

	static Logger logger = Logger.getLogger(UpdateEveryWhereAgent.class.getName());

	/**
	 * This holds the entries to PUT
	 */
	SortedMap<K, V> entries;
	/**
	 * This holds the keys of entries to remove if present
	 */
	List<K> entriesToRemove = new ArrayList<K>();

	/**
	 * This reduce operation returns a String. The String is zero length if it was successful otherwise it returns the
	 * exception as a string
	 */
	public Object reduce(Session sess, ObjectMap map) {
		try {
			for (Map.Entry<K, V> e : entries.entrySet()) {
				V oldValue = (V) map.getForUpdate(e.getKey());
				if (oldValue != null) {
					map.update(e.getKey(), e.getValue());
				} else {
					map.insert(e.getKey(), e.getValue());
				}
			}
			for (K k : entriesToRemove) {
				map.remove(k);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			return e.toString();
		}
		return "";
	}

	public Object reduce(Session arg0, ObjectMap arg1, Collection keys) {
		logger.log(Level.INFO, "Do not specify keys when using this agent");
		throw new RuntimeException("This method is not implemented and should never be called");
	}

	/**
	 * This will be called on the client with the string return values from every partition. If any string was non zero
	 * length then that string is returned. This lets the client see one of the exceptions that occurred during the
	 * operation
	 */
	public Object reduceResults(Collection values) {
		for (Object o : values) {
			if (o instanceof String) {
				String e = (String) o;
				if (e.length() > 0)
					return e;
			}
			if (o instanceof EntryErrorValue) {
				return o.toString();
			}
		}
		return "";
	}

	/**
	 * This helper method shows how to use this agent
	 * 
	 * @param <K>
	 * @param <V>
	 * @param sess
	 * @param entries
	 * @param entriesToRemove
	 * @param mapName
	 * @throws ObjectGridException
	 */
	static public <K extends Serializable, V extends Serializable> void updateEveryWhere(Session sess, Map<K, V> entries, List<K> entriesToRemove,
			String mapName) throws ObjectGridException {
		UpdateEveryWhereAgent<K, V> agent = new UpdateEveryWhereAgent<K, V>();
		if (entries instanceof SortedMap) {
			agent.entries = (SortedMap) entries;
		} else {
			agent.entries = new TreeMap<K, V>(entries);
		}

		agent.entriesToRemove = entriesToRemove;
		AgentManager am = sess.getMap(mapName).getAgentManager();
		Object rc = am.callReduceAgent(agent);
		if (rc != null && rc instanceof String) {
			String rcs = (String) rc;
			if (rcs.length() > 0)
				throw new ObjectGridRuntimeException(rcs);
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ClassSerializer serializer = WXSUtils.getSerializer();
		entries = (SortedMap<K, V>) serializer.readObject(in);
		entriesToRemove = serializer.readList(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		ClassSerializer serializer = WXSUtils.getSerializer();
		serializer.writeObject(out, entries);
		serializer.writeList(out, entriesToRemove);
	}
}
