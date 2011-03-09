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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.filter.Filter;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.plugins.index.MapRangeIndex;

/**
 * This is a class for applying a Filter layer on top of range indexes. This
 * is designed for a local shard, not clients.
 * @author bnewport
 *
 * @param <K>
 * @param <V>
 */
public class FilteredRangeIndex <K,V> extends FilteredIndex<K, V> 
{
	private enum Operation
	{
		LT,LTE,GT,GTE
	}
	
	MapRangeIndex rindex;
	public FilteredRangeIndex(WXSMap<K,V> map, MapRangeIndex index)
	{
		super(map, index);
		this.map = map;
		this.index = index;
		rindex = (MapRangeIndex)index;
	}

	/**
	 * This returns entries whose attributes are between the specified
	 * low and high values.
	 * @param low
	 * @param high
	 * @param f
	 * @return
	 * @see MapRangeIndex#findRange(Object, Object)
	 */
	public Map<K,V> btwn(Object low, Object high, Filter f)
	{
		try
		{
			Map<K,V> rc = new HashMap<K, V>();
			Iterator<K> iter = (Iterator<K>)rindex.findRange(low, high);
			while(iter.hasNext())
			{
				K key = iter.next();
				V value = (V)map.get(key);
				if(f.filter(value))
					rc.put(key, value);
			}
			return rc;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * This is an implementation method for implementing the relational
	 * methods
	 * @param op
	 * @param a
	 * @param f
	 * @return
	 */
	protected Map<K,V> filterCompare(Operation op, Object a, Filter f)
	{
		try
		{
			Map<K,V> rc = new HashMap<K, V>();
			
			Iterator<K> iter = null;
			switch(op)
			{
			case LT:
				iter = (Iterator<K>)rindex.findLess(a);
				break;
			case LTE:
				iter = (Iterator<K>)rindex.findLessEqual(a);
				break;
			case GT:
				iter = (Iterator<K>)rindex.findGreater(a);
				break;
			case GTE:
				iter = (Iterator<K>)rindex.findGreaterEqual(a);
				break;
			default:
				throw new ObjectGridRuntimeException("Unimplementated operator");
			}
			while(iter.hasNext())
			{
				K key = iter.next();
				V value = (V)map.get(key);
				if(f.filter(value))
					rc.put(key, value);
			}
			return rc;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	/**
	 * This returns all entries with attributes less than a and which match the filter
	 * @param a Only values whose attribute < a
	 * @param f and match this filter
	 * @return
	 */
	public Map<K,V> lt(Object a, Filter f)
	{
			return filterCompare(Operation.LT, a, f);
	}

	/**
	 * This returns all entries with attributes less or equal than a and which match the filter
	 * @param a Only values whose attribute <= a
	 * @param f and match this filter
	 * @return
	 */
	public Map<K,V> lte(Object a, Filter f)
	{
			return filterCompare(Operation.LTE, a, f);
	}

	/**
	 * This returns all entries with attributes greater than or equal to a and which match the filter
	 * @param a Only values whose attribute >= a
	 * @param f and match this filter
	 * @return
	 */
	public Map<K,V> gte(Object a, Filter f)
	{
			return filterCompare(Operation.GTE, a, f);
	}

	/**
	 * This returns all entries with attributes greater than a and which match the filter
	 * @param a Only values whose attribute > a
	 * @param f and match this filter
	 * @return
	 */
	public Map<K,V> gt(Object a, Filter f)
	{
			return filterCompare(Operation.GT, a, f);
	}
}
