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
package com.devwebsphere.jdbc.loader.value;


import com.devwebsphere.jdbc.loader.BaseKeyValue;
import com.devwebsphere.purequery.loader.ScalarKey;
import com.ibm.websphere.objectgrid.plugins.LogElement;

/**
 * This is a String Key generic value POJO used for buffering POJOs from
 * tables with long/long string/string string/long and long/string tables.
 * This uses the ScalarKey if present.
 * @author bnewport
 *
 * @param <V> The type of the value
 */
public abstract class TemplateStringKeyProperty <V> extends BaseKeyValue<String,V>
{
	/**
	 * The key attribute
	 */
	public String keyz;
	
	public TemplateStringKeyProperty()
	{	
	}

	/**
	 * Construct using the key and value if possible from a LogElement. If the key
	 * is a ScalarKey then extract the true key.
	 * @param elem
	 * @see ScalarKey
	 */
	public TemplateStringKeyProperty(LogElement elem)
	{
		Object a = elem.getCacheEntry().getKey();
		if(a instanceof ScalarKey)
		{
			ScalarKey sk = (ScalarKey)a;
			keyz = sk.getKey();
		}
		else
			keyz = (String)a;
	}

	@Override
	public String baseGetKey() 
	{
		return keyz;
	}
}
