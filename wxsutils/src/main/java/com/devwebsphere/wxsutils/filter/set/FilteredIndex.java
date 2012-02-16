//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.filter.set;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.filter.Filter;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.plugins.index.MapIndex;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.SerializedValue;

/**
 * This is a helper class to layer Filters on top of hash and range indexes. Its really designed to work on a local
 * shard, not on a client. There will be other classes to move the filter processing grid side so that it's efficient on
 * clients.
 * 
 * @author bnewport
 * 
 * @param <K>
 * @param <V>
 */
public class FilteredIndex<K extends Serializable, V extends Serializable> {
	MapIndex index;
	WXSMap<K, V> map;

	public FilteredIndex(WXSMap<K, V> map, MapIndex index) {
		this.map = map;
		this.index = index;
	}

	/**
	 * This fetches all records from the index and returns only the ones matching a filter.
	 * 
	 * @param f
	 * @return
	 */
	public Map<K, V> filterAll(Filter f) {
		try {
			Iterator<K> iter = (Iterator<K>) index.findAll();
			return filterIterator(iter, f);
		} catch (Exception e) {
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * This returns all entries which the selected attribute and that match the filter
	 * 
	 * @param v
	 * @param f
	 * @return
	 */
	public Map<K, V> eq(Object v, Filter f) {
		try {
			Iterator<K> iter = (Iterator<K>) index.findAll(v);
			return filterIterator(iter, f);
		} catch (Exception e) {
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * This is a generic method to filter a Map of entries by applying the filter to all values.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param input
	 * @param f
	 * @return
	 */
	static public <K, V> Map<K, V> filterMap(Map<K, V> input, Filter f) {
		boolean convert = f.requiresDataObjectContext();

		try {
			Map<K, V> rc = new HashMap<K, V>();
			for (Map.Entry<K, V> e : input.entrySet()) {
				K key = e.getKey();
				Object value = e.getValue();
				if (f.filter(value)) {
					if (convert) {
						value = ((SerializedValue) value).getObject();
					}

					rc.put(key, (V) value);
				}
			}
			return rc;
		} catch (Exception e) {
			throw new ObjectGridRuntimeException(e);
		}
	}

	protected Map<K, V> filterIterator(Iterator<K> iter, Filter f) {
		boolean convert = f.requiresDataObjectContext();

		Map<K, V> rc = new HashMap<K, V>();
		while (iter.hasNext()) {
			K key = iter.next();
			Object value = map.get(key);
			if (f.filter(value)) {
				if (convert) {
					value = ((SerializedValue) value).getObject();
				}
				rc.put(key, (V) value);
			}
		}

		return rc;
	}

}
