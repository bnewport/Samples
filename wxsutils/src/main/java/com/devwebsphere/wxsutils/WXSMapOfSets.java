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

import java.util.Set;

import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.wxsmap.SetAddRemoveAgent;

/**
 * This allows a client to interact with a WXS Map that is treated
 * as a map of key/set pairs. Each entry consists of a key and the value
 * is a Set<V>. This implementation is optimized to store the set
 * as a distinct group of N Sets. This lowers the cost of adding
 * or removing elements or checking for membership but
 * the size of a set is still bounded within a single shard.
 * @author bnewport
 * @see SetAddRemoveAgent#NUM_BUCKETS
 *
 * @param <K> The key for the set
 * @param <V> Each value set consists of this elements of this type only
 */
public interface WXSMapOfSets<K,V> 
{
	/**
	 * This is to indicate for a contains operation whether
	 * ALL or ANY of the values is being tested for.
	 * @author bnewport
	 *
	 */
	public enum Contains {ANY, ALL};

	/**
	 * This adds a value to the set named key.
	 * @param key The key for the set
	 * @param value The value to add to the set
	 * @return TRUE if successful
	 */
	public void add(K key, V... value);

	/**
	 * This adds the values to the set BUT if the current set
	 * is maxSize entries or bigger then the current value
	 * is returned and the set is set to only the values. Normally
	 * the empty set is returned if the existing set is smaller
	 * than maxSize entries
	 * @param key
	 * @param maxSize
	 * @param values
	 * @return
	 */
	public Set<V> addAndFlush(K key, int maxSize, V... values);

	/**
	 * This removes a value from the set named key.
	 * @param key The key for the set
	 * @param value The value to remove from the set
	 * @return TRUE if the value was found
	 */
	public void remove(K key, V... value);

	/**
	 * This returns the number of values in the set
	 * for the key.
	 * @param key The name of the set
	 * @return The size of the set or 0 if empty
	 */
	public int size(K key);

	/**
	 * This tests if all values are present in the set for the key.
	 * @param key The key for the set
	 * @param testAll If true then return true if all values present otherwise any members are present
	 * @param value The values to test for inclusion
	 * @return true if the elements are present in the set
	 */
	public boolean contains(K key, Contains op, V... values);

	/**
	 * This returns all the members of the set for the key. It can also
	 * only return elements matching an optional filter.
	 * @param key The key for the set
	 * @param filter The optional filter to use. Only one should be specified
	 * @return The list of entries. It's empty if the set doesn't exist
	 */
	public Set<V> get(K key, Filter... filter);

	/**
	 * This removes the set with the specified key if it exists
	 * @param key
	 */
	public void remove(K key);
}
