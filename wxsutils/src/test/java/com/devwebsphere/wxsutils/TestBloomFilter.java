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

import java.util.BitSet;

import junit.framework.Assert;

import org.junit.Test;

import com.devwebsphere.wxsutils.bloom.BloomFilter;

public class TestBloomFilter 
{
	@Test
	public void testPowersOfTwo()
	{
		for(int power = 2; power < 32; ++power)
		{
			int expected = 1 << power;
//			System.out.println("" + expected + " -> " + BloomFilter.nextHighestPowerOfTwo(expected - 1));
			Assert.assertEquals(expected, BloomFilter.nextHighestPowerOfTwo(expected - 1));
		}
	}
	
	@Test
	public void testBitSet()
	{
		// for these sizes as powers of two, we check if the BitSet
		// actually returns the correct size, just in case
		for(int power = 6; power < 20; ++power)
		{
			int maxBits = 1 << power;
			BitSet bs = new BitSet(maxBits);
			Assert.assertEquals(maxBits, bs.size());
		}
	}
	
	@Test
	public void testUnsigned()
	{
		Assert.assertEquals(0, BloomFilter.getUnsignedByteAsInt((byte)0));
		Assert.assertEquals(127, BloomFilter.getUnsignedByteAsInt((byte)127));
		Assert.assertEquals(255, BloomFilter.getUnsignedByteAsInt((byte)-1));
	}
	
	static int numCases = 250000;
	@Test
	public void testBloomFilterAddAndContains()
	{
		BloomFilter filter = new BloomFilter(numCases);
		
		for(int i = 0; i < numCases * 2; i += 2)
		{
			String key = Integer.toString(i);
			filter.add(key.getBytes());
			Assert.assertTrue(filter.contains(key.getBytes()));
		}
		int numHits = 0;
		for(int i = 1; i < numCases * 2; i+= 2)
		{
			String key = Integer.toString(i);
			numHits += (filter.contains(key.getBytes())) ? 1 : 0;
		}
		double falsePositive = numHits / (double)numCases;
		Assert.assertTrue(falsePositive < filter.getExpectedFalsePositive());
	}

	@Test
	public void testSpeed()
	{
		for(int loop = 0; loop < 20; ++loop)
		{
			long start = System.nanoTime();
			testBloomFilterAddAndContains();
			long duration = System.nanoTime() - start;
			double time = duration / 1000000000.0;
			System.out.println("Time for " + (numCases * 2) + "k operations is " + time);
		}
	}
}
