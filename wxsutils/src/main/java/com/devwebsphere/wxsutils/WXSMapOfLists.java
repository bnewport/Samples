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

import java.util.ArrayList;
import java.util.List;

import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.wxsmap.BigListHead;
import com.devwebsphere.wxsutils.wxsmap.BigListPushAgent;

/**
 * This allows entries in a Map to be lists. The implementation actually uses
 * two Maps for every List Map. One is named with the application name
 * but only holds meta data for the actual list. The value used for this
 * is BigListHead. The actual data for the list is stored in another map
 * in the same partition as the real map. This map is called "mapName_b". The lists is split in to blocks
 * and these blocks are stored in the second map using a key like "name#NNN"
 * where name is the key as a string and NNN is the block number. The blocks
 * are up to a certain size. This implementation means that as the list
 * gets bigger the impact of the change is limited to BUCKET_SIZE entries
 * at a time. So, even a list with 10000 elements in it would only see
 * replication of at most BUCKET_SIZE entries when an item is pushed or popped.
 * All element buckets are kept in the same partition as the list meta data for
 * performance. It's assumed that lists with large sizes are evenly distributed
 * throughout all partitions. If one partition has an unusually large list then this
 * may cause memory problems.
 * @author bnewport
 * @see BigListPushAgent#BUCKET_SIZE
 * @see BigListHead
 *
 * @param <K>
 * @param <V>
 */
public interface WXSMapOfLists<K,V> {

	/**
	 * This trims the list at key K to at most size elements. Entries
	 * are removed from the right to achieve this.
	 * @param key The key for the list
	 * @param size The maximum number of elements in the list
	 */
	public void rtrim(K key, int size);

	/**
	 * This pushes a value on the left side of the list
	 * @param key The key for the list
	 * @param value The value to push
	 * @param dirtySet The key for the per shard dirty set to add this key to. Optional
	 */
	public void lpush(K key, V value, K... dirtySet);

	/**
	 * This removes and returns the left most element in the list
	 * @param key The key for the list
	 * @return NULL if the list is empty or the leftmost element
	 */
	public V lpop(K key);
	
	/**
	 * This is the same as lpop but if the list is empty
	 * afterwards then its key is removed from the set named
	 * dirtySet in the Map mapName_dirty
	 * @param key
	 * @param dirtyKey
	 * @return
	 */
	public V lpop(K key, K dirtyKey);

	/**
	 * This removes all items from the list and returns them.
	 * @param key
	 * @return All items
	 */
	public ArrayList<V> popAll(K key);
	
	/**
	 * This removes all items from the list, returns them and removes the list key from the dirtySet
	 * @param key
	 * @param dirtyKey
	 * @return
	 */
	public ArrayList<V> popAll(K key, K dirtyKey);

	/**
	 * Remove the list for this key if it exists
	 * @param key
	 */
	public void remove(K key);
	/**
	 * This removes and returns the rightmost element in the list
	 * @param key The key for the list
	 * @return NULL if the list is empty or the rightmost element
	 */
	public V rpop(K key);
	
	/**
	 * This is the same as rpop but if the list is empty
	 * afterwards then its key is removed from the set named
	 * dirtySet in the Map mapName_dirty
	 * @param key
	 * @param dirtyKey
	 * @return
	 */
	public V rpop(K key, K dirtyKey);

	/**
	 * This pushes the value on the righthand side of the list
	 * @param key The key for the list
	 * @param value The value to add on the right side of the list
	 * @param dirtySet The key for the per shard dirty set to add this key to. Optional
	 */
	public void rpush(K key, V value, K... dirtySet);

	/**
	 * This returns the elements in the list from index low to index high
	 * inclusive. This list may be shorter than usual if the
	 * list is size is less than low or high. The returned elements can be filtered
	 * also using the optional filter parameter, note: only one filter can be specified
	 * @param key The key for the list
	 * @param low The 0-index for the left most element to return
	 * @param high The 0-index for the right most element to return
	 * @param filter If specified then elements are only returned if they match the Filter
	 * @return An array with the list elements. It may be shorter than expected
	 */
	public ArrayList<V> lrange(K key, int low, int high, Filter... filter);

	/**
	 * The length of the list for the key. 0 if the list doesn't
	 * exist
	 * @param key The key for the list
	 * @return The size of the list
	 */
	public int llen(K key);

}
