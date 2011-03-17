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
package com.devwebsphere.wxsutils;

import java.util.List;

import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfSetsMBeanManager;


public class WXSMapOfSetsImpl<K,V> extends WXSBaseMap implements WXSMapOfSets<K, V> 
{
	static LazyMBeanManagerAtomicReference<WXSMapOfSetsMBeanManager> wxsMapOfSetsMBeanManager = new LazyMBeanManagerAtomicReference<WXSMapOfSetsMBeanManager>(WXSMapOfSetsMBeanManager.class);
	protected WXSMapOfSetsImpl(WXSUtils utils, String mapName)
	{
		super(utils, mapName);
	}

	public boolean sadd(K key, V value) {
		// TODO Auto-generated method stub
		return false;
	}

	public int scard(K key) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean sismember(K key, V value) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<V> smembers(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean srem(K key, V value) {
		// TODO Auto-generated method stub
		return false;
	}

}
