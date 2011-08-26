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
package com.devwebsphere.wxsutils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;

import com.devwebsphere.wxsutils.wxsmap.BigListHead;
import com.devwebsphere.wxsutils.wxsmap.SetElement;
import com.devwebsphere.wxsutils.wxsmap.dirtyset.DirtyKey;


public class TestLinkedHashSets 
{
	static class Element<V extends Serializable> implements Comparable<Element<V>>
	{
		long timeStamp;
		V v;
		
		@Override
		public boolean equals(Object arg0) {
			Element<V> other = (Element<V>)arg0;
			return (v.equals(other.v));
		}

		public int hashCode()
		{
			return (int)timeStamp + v.hashCode();
		}
		
		public Element(V value)
		{
			timeStamp = System.currentTimeMillis();
			this.v = value;
		}
		
		public Element() {}
		
		public Element(Element<V> copy)
		{
			timeStamp = copy.timeStamp;
			v = copy.v;
		}
		
		public String toString()
		{
			return "E<" + timeStamp + ":" + v.toString() + ">";
		}

		public int compareTo(Element<V> arg0) 
		{
			Long t0 = new Long(timeStamp);
			Long t1 = new Long(arg0.timeStamp);
			return t0.compareTo(t1);
		}
	}
	
	@Test
	public void testLinkedHashSet()
		throws InterruptedException
	{
		HashSet<Element<String>> set = new LinkedHashSet<Element<String>>();
		
		ArrayList<Element<String>> items = new ArrayList<Element<String>>();
		
		for(int i = 0; i < 10; ++i)
		{
			Element<String> e = new Element<String>(Integer.toString(i));
			if(!set.contains(e))
				set.add(e);
			set.add(e);
			Assert.assertEquals(e, e);
			items.add(e);
			Thread.sleep(100);
		}
		
		Iterator<Element<String>> iter = set.iterator();

		long lastTime = 0;
		while(iter.hasNext())
		{
			Element<String> e = iter.next();
			Assert.assertTrue(lastTime <= e.timeStamp);
			lastTime = e.timeStamp;
		}
		Assert.assertEquals(items.size(), set.size());

		Element<String> copy = new Element(items.get(5));
		Assert.assertEquals(copy, items.get(5));
		Assert.assertTrue(set.contains(items.get(5)));
		Assert.assertTrue(set.remove(copy));
	}
	
	@Test
	public void testFindLowestElements()
	{
		int numSets = 20;
		int numItemsPerSet = 200;
		
		ArrayList<Set<Element<String>>> sets = new ArrayList<Set<Element<String>>>();
		Random r = new Random();
		for(int i = 0; i < numSets; ++i)
		{
			Set<Element<String>> set = new LinkedHashSet<Element<String>>();
			sets.add(set);
			for(int j = 0; j < numItemsPerSet; ++j)
			{
				Element<String> e = new Element<String>();
				e.timeStamp = r.nextInt(10000);
				e.v = Integer.toString((int)e.timeStamp);
				set.add(e);
			}
		}
		
		Set<Element<String>> result = new TreeSet<Element<String>>();
		for(int i = 0; i < sets.size(); ++i)
		{
			result.addAll(sets.get(i));
		};
		for(Element<String> e : result)
		{
			System.out.println(e.toString());
		}
	}
	
	@Test
	public void testDirtyKey()
	{
		int numItems = 10;
		LinkedHashSet<DirtyKey<String>> set = new LinkedHashSet<DirtyKey<String>>();
		for(int i = 0; i < numItems; ++i)
		{
			DirtyKey<String> item = new DirtyKey(Integer.toString(i));
			set.add(item);
		}
		Assert.assertEquals(numItems, set.size());
		TreeSet<DirtyKey<String>> tree = new TreeSet<DirtyKey<String>>();
		for(int i = 0; i < numItems; ++i)
		{
			DirtyKey<String> item = new DirtyKey(Integer.toString(i));
			tree.add(item);
		}
		Assert.assertEquals(numItems, tree.size());
	}
	
	@Test
	public void testSetElement()
	{
		int numItems = 10;
		LinkedHashSet<SetElement<String>> set = new LinkedHashSet<SetElement<String>>();
		for(int i = 0; i < numItems; ++i)
		{
			SetElement<String> item = new SetElement(Integer.toString(i));
			set.add(item);
		}
		Assert.assertEquals(numItems, set.size());
//		TreeSet<SetElement<String>> tree = new TreeSet<SetElement<String>>();
//		for(int i = 0; i < numItems; ++i)
//		{
//			SetElement<String> item = new SetElement(Integer.toString(i));
//			tree.add(item);
//		}
//		Assert.assertEquals(numItems, tree.size());
	}
	
	@Test
	public void testEvictionBasics()
	{
		for(int i = 0; i < 24; ++i)
			System.out.println(BigListHead.getEvictionTime(60 * i));
	}
}
