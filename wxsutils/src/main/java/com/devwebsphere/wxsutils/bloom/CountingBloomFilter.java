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
package com.devwebsphere.wxsutils.bloom;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * This is a classic Bloom Filter. This is a probabilistic data structure
 * which can be used to check if an item is definitely in a 'list' and also
 * check if an item is probably not in the list. It's implemented as a bit array
 * whose size depends on the expected number of entries and the required
 * probability of false positive in the not in the list case.
 * @author bnewport
 *
 */
public final class CountingBloomFilter implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5465191270790202776L;
	BitSet bitset;
	double expectedFalsePositive;
	
	/**
	 * This is the probability that a key that really isn't present is returned
	 * as present, the false positive rate. More phases and lower saturation
	 * settings should lower this.
	 * @return
	 */
	public final double getExpectedFalsePositive() {
		return expectedFalsePositive;
	}

	// the number of hash functions to apply, i.e. K
	int numPhases;
	
	// the number of entries in the bit array
	int numEntries;

	// the number of bits for each entry
	final static int bitsPerEntry = 4;
	
	/**
	 * Construct a filter which is expected to hold a certain number of entries
	 * with a K way hash
	 * @param numExpectedEntries The number of entries expected
	 * @param numPhases The number of hashes to apply to keys
	 * @param saturation The percentage of bits that are one when fully populated (0.5 is good)
	 */
	public CountingBloomFilter(int numExpectedEntries, int numPhases, double saturation)
	{
		this.numPhases = numPhases;
		double numBitsNeeded = (double)numExpectedEntries * (double)numPhases / - Math.log(saturation);
		double numBytes = numBitsNeeded / 8;
		// round up to a power of two to make modulo arithmetic easy
		int iNumBytes = nextHighestPowerOfTwo(((int)numBytes) + 1);
		numEntries = iNumBytes * 8;
		
		// multiple bits for each entry
		iNumBytes *= bitsPerEntry;
		expectedFalsePositive = Math.pow(saturation, numPhases);
		bitset = new BitSet(iNumBytes);
	}
	
	/**
	 * The default constructor, just takes the number of expected entries.
	 * @param numExpectedEntries
	 */
	public CountingBloomFilter(int numExpectedEntries)
	{
		// defaults are 5 phases and a 50% saturation
		this(numExpectedEntries, 5, 0.5);
	}

	/**
	 * This adds a new key to the bloom filter, if the key isn't a byte[] then
	 * it must be converted to one.
	 * @param key
	 */
	public void add(byte[] key)
	{
		MessageDigest digest = getDigest();
		for(int i = 0; i < numPhases; ++i)
		{
			byte[] hash = digest.digest(key);
			int index = getBitIndex(hash);
			int value = 0;
			for(int b = 0; b < bitsPerEntry; ++b)
				value = (value << 1) + (bitset.get(index + b) ? 1 : 0);
			value++;
			bitset.set(index);
			key = hash;
		}
	}

	/**
	 * This checks if a key is present using the filter. This can return
	 * true for keys not in the filter with the probability of
	 * the expectedFalsePositive rate.
	 * @param key
	 * @return
	 */
	public boolean contains(byte[] key)
	{
		MessageDigest digest = getDigest();
		for(int i = 0; i < numPhases; ++i)
		{
			byte[] hash = digest.digest(key);
			// BitSet is guaranteed a power of two so this makes getting the modulo
			// pretty easy
			int index = getBitIndex(hash);
			if(!bitset.get(index))
					return false;
			bitset.set(index);
			key = hash;
		}
		return true;
	}
	
	/**
	 * Clear the bloom filter
	 */
	public void clear()
	{
		bitset.clear();
	}
	
	/**
	 * Find the next highest power of two for a 32 bit number for numbers at least 64
	 * @param v
	 * @return
	 */
	static public int nextHighestPowerOfTwo(int v)
	{
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v+=(v!=0) ? 1 : 0;
		return v;
	}
	
	/**
	 * This just returns the digest we will use to generate hashes. SHA and MD5 are very good
	 * hash functions. MD5 is about 40% faster than SHA so we'll use MD5
	 * @return
	 */
	public static MessageDigest getDigest()
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return digest;
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This takes an unsigned byte stored in a signed byte and returns the unsigned value
	 * as an integer.
	 * @param b
	 * @return
	 */
	static public int getUnsignedByteAsInt(byte b)
	{
		return (b >= 0) ? b : 256 + b;
	}

	/**
	 * This converts a digest to a bit index. It returns the number represented
	 * by the byte[] in little endian form modulo the number of bits in the filter. 
	 * @param b
	 * @return
	 */
	int getBitIndex(byte[] b)
	{
		long p0 = (long)getUnsignedByteAsInt(b[0]);
		long p1 = (long)getUnsignedByteAsInt(b[1]) << 8;
		long p2 = (long)getUnsignedByteAsInt(b[2]) << 16;
		long p3 = (long)getUnsignedByteAsInt(b[3]) << 24;
		long index = p0 + p1 + p2 + p3;
// the above is faster than the below by around 10%, go figure
//		ByteBuffer buff = ByteBuffer.wrap(b);
//		IntBuffer ib = buff.asIntBuffer();
//		long index = ib.get(0);
//		if(index < 0) index += Integer.MAX_VALUE + 1L;
		return (int)(index % numEntries);
	}
	
}
