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
package com.devwebsphere.purequery.loader.value;


import com.devwebsphere.purequery.loader.BaseKeyValue;
import com.ibm.websphere.objectgrid.plugins.LogElement;

/**
 * This is a generic key value POJO used for buffering POJOs from
 * tables with long/long string/string string/long and long/string tables
 * @author bnewport
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public class TemplateProperty <K,V> extends BaseKeyValue<K,V>
{
	/**
	 * The key attribute
	 */
	public K keyz;
	/**
	 * The value attribute
	 */
	public V value;
	
	public TemplateProperty()
	{	
	}

	/**
	 * Construct using the key and value if possible from a LogElement
	 * @param elem
	 */
	public TemplateProperty(LogElement elem)
	{
		keyz = (K)elem.getCacheEntry().getKey();
		if(elem.getCurrentValue() != null)
			value = (V)elem.getCurrentValue();
	}

	@Override
	public K baseGetKey() 
	{
		return keyz;
	}

	@Override
	public V baseGetValue() {
		return value;
	}
}
