package com.devwebsphere.wxsutils.wxsmap;

import java.io.Serializable;

import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This is a time stamped wrapper class. It can be stored in a hashmap or hashset
 * but only considers the wrapped value for equals or hashCode.
 * @author bnewport
 *
 * @param <V>
 */
public class SetElement<V extends Serializable> implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5537792293319626344L;

	V value;
	long timeStamp;
	
	/**
	 * Ignore timestamp in Sets
	 */
	@Override
	public boolean equals(Object arg0) {
		SetElement<V> other = (SetElement<V>)arg0;
		return (value.equals(other.value));
	}

	/**
	 * Ignore timestamp in Sets
	 */
	public int hashCode()
	{
		return value.hashCode();
	}
	
	public SetElement(V v)
	{
		timeStamp = System.currentTimeMillis();
		value = v;
	}
	
	/**
	 * Don't put this in a sorted collection
	 */
	public int compareTo(SetElement<V> arg0) 
	{
		throw new ObjectGridRuntimeException("Cannot use comparable on SetElement");
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
