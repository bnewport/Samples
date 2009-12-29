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

package com.devwebsphere.rediswxs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This provides a data structures API for IBM WebSphere eXtreme Scale. This is modeled
 * after the redis open source project API.
 * @author bnewport
 *
 * @see R
 */
public interface IRedis  <K extends Serializable, V extends Serializable> 
{
	/**
	 * This sets the value for a key.
	 * @param key The key
	 * @param value The value to set the entry for K to
	 */
	public void set(K key, V value);

	/**
	 * Returns the value for the key K or null.
	 * @param key The key whose entry is needed
	 * @return The value or NULL
	 */
	public V get(K key);

	/**
	 * This removes the value for the key if present.
	 * @param key The entry to remove. This only works
	 * on scalar entries. It does not remove LIST or SET
	 * entries.
	 * @return true if the element was present
	 */
	public boolean remove(K key);

	/**
	 * Remove an entry from the near cache if present. This only affects the client which calls the method.
	 * Other clients are not impacted by this method at all.
	 * @param key The entry key to remove from the near cache
	 */
	public void invalidate(K key);
	/**
	 * Atomically increment the long at this key. The V MUST be a LONG type
	 * to use this method
	 * @param key The key containing the long
	 * @return the incremented value
	 */
	public long incr(K key);

	/**
	 * Atomically add delta to the long at entry key. The V MUST be a Long type
	 * to use this method
	 * @param key The key for the entry
	 * @param delta This is added to the current value
	 * @return The new value
	 */
	public long incrby(K key, long delta);

	/**
	 * This trims the list at key K to at most size elements. Entries
	 * are removed from the right to achieve this.
	 * @param key The key for the list
	 * @param size The maximum number of elements in the list
	 * @return TRUE if the value exists otherwise FALSE
	 */
	public boolean ltrim(K key, int size);

	/**
	 * This pushes a value on the left size of the list
	 * @param key The key for the list
	 * @param value The value to push
	 */
	public void lpush(K key, V value);

	/**
	 * This removes and returns the left most element in the list
	 * @param key The key for the list
	 * @return NULL if the list is empty or the leftmost element
	 */
	public V lpop(K key);

	/**
	 * This removes and returns the rightmost element in the list
	 * @param key The key for the list
	 * @return NULL if the list is empty or the rightmost element
	 */
	public V rpop(K key);

	/**
	 * This pushes the value on the righthand side of the list
	 * @param key The key for the list
	 * @param value The value to add on the right side of the list
	 */
	public void rpush(K key, V value);

	/**
	 * This returns the elements in the list from index low to index high
	 * inclusive. This list may be shorter than usual if the
	 * list is size is less than low or high.
	 * @param key The key for the list
	 * @param low The 0-index for the left most element to return
	 * @param high The 0-index for the right most element to return
	 * @return An array with the list elements. It may be shorter than expected
	 */
	public ArrayList<V> lrange(K key, int low, int high);

	/**
	 * The length of the list for the key. 0 if the list doesn't
	 * exist
	 * @param key The key for the list
	 * @return The size of the list
	 */
	public int llen(K key);

	/**
	 * This adds a value to the set named key.
	 * @param key The key for the set
	 * @param value The value to add to the set
	 * @return TRUE if successful
	 */
	public boolean sadd(K key, V value);

	/**
	 * This removes a value from the set named key.
	 * @param key The key for the set
	 * @param value The value to remove from the set
	 * @return TRUE if the value was found
	 */
	public boolean srem(K key, V value);

	/**
	 * This returns the number of values in the set
	 * for the key.
	 * @param key The name of the set
	 * @return The size of the set or 0 if empty
	 */
	public int scard(K key);

	/**
	 * This tests of the value is present in the set for the key.
	 * @param key The key for the set
	 * @param value The value to test for inclusion
	 * @return true if the element is present in the set
	 */
	public boolean sismember(K key, V value);

	/**
	 * This returns all the members of the set for the key.
	 * @param key The key for the set
	 * @return The list of entries. It's empty if the set doesn't exist
	 */
	public List<V> smembers(K key);
	
	/**
	 * This invokes an asynchronous event which runs on the server side. It is
	 * named and takes parameters. The server side needs to register
	 * a handler for that name and then process the event parameters. These
	 * are stored in replication memory for fault tolerance, not the database
	 * @param eventName The name of the event
	 * @param properties The parameters for the event
	 */
	public void invokeAsyncOperation(String eventName, Map<String,String> properties);
	
	public String getMapName();
}