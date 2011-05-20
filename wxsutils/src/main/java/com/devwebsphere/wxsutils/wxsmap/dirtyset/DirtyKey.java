package com.devwebsphere.wxsutils.wxsmap.dirtyset;

import java.io.Serializable;

/**
 * This is a time stamped wrapper class. It can be stored in a hashmap or set
 * but only considers the wrapped value for equals or hashCode. However, for
 * compare then only the timestamp is considered.
 * @author bnewport
 *
 * @param <V>
 */
public class DirtyKey<V extends Serializable> implements Serializable, Comparable<DirtyKey<V>> 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5537792293319626344L;

	V value;
	/**
	 * This is the time the record was marked as dirty
	 */
	long timeStamp;
	
	/**
	 * Ignore timestamp in Sets
	 */
	@Override
	public boolean equals(Object arg0) {
		DirtyKey<V> other = (DirtyKey<V>)arg0;
		return (value.equals(other.value));
	}

	/**
	 * Ignore timestamp in Sets
	 */
	public int hashCode()
	{
		return value.hashCode();
	}
	
	public DirtyKey(V v)
	{
		timeStamp = System.currentTimeMillis();
		value = v;
	}
	
	public DirtyKey() {}
	
	/**
	 * Only use the timestamp for ordering
	 */
	public int compareTo(DirtyKey<V> arg0) 
	{
		Long t0 = new Long(timeStamp);
		Long t1 = new Long(arg0.timeStamp);
		int rc = t0.compareTo(t1);
		if(rc == 0 && !value.equals(arg0.value))
			rc = -1;
		return rc;
	}
	
	public String toString()
	{
		return "SetElement<" + timeStamp + ":" + value.toString() + ">";
	}

	public final V getValue() {
		return value;
	}

	public final void setValue(V value) {
		this.value = value;
	}

	public final long getTimeStamp() {
		return timeStamp;
	}

	public final void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
}
