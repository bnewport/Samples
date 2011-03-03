package com.devwebsphere.wxsutils;

import java.util.Collection;
import java.util.Map;

/**
 * This is a simplified interface to a WXS Map. It throws runtime exceptions and is completely
 * thread safe. It doesn't use transactions. It provides implementations for commonly used
 * Map methods like put, bulk methods and so on.
 *
 */

public abstract class WXSMap<K,V> {
	/**
	 * Clear the Map
	 */
	abstract public void clear();
	/**
	 * Fetch a value from the Map
	 * @param k
	 * @return
	 */
	abstract public V get(K k);

	/**
	 * Fetch all the values for the specified keys. Null is returned if the key
	 * isn't found.
	 * @param keys
	 * @return
	 */
	abstract public Map<K,V> getAll(Collection<K> keys);

	/**
	 * Set the value for the key. If the entry doesn't exist then it
	 * is inserted otherwise it's updated.
	 * @param k
	 * @param v
	 */
	abstract public void put(K k, V v);
	
	/**
	 * This does a single entry insert. If the key already exists then an exception is thrown.
	 * @param k
	 * @param v
	 */
	abstract public void insert(K k, V v);
	
	/**
	 * Parallel put all the entries.
	 * @param batch
	 */
	abstract public void putAll(Map<K,V> batch);

	/**
	 * This will update the entries to the newValue only if the current value for the
	 * key matches the original value or if absent. True is returned for each key changed
	 * during this operation
	 * @param originalValues The original value to check against
	 * @param newValues The new value to be used if the entry value is unchanged compared with original
	 * @return A true for each updated key entry
	 */
	abstract public Map<K, Boolean> cond_putAll(Map<K,V> originalValues, Map<K,V> newValues);
	
	/**
	 * Parallel insert all the entries. This does a real insert, not a put (get/update)
	 * @param batch
	 */
	abstract public void insertAll(Map<K,V> batch);
	
	/**
	 * Remove the entry from the Map
	 * @param k
	 * @return The last value otherwise null
	 */
	abstract public V remove(K k);
	
	/**
	 * Remove all entries with these keys
	 * @param keys
	 */
	abstract public void removeAll(Collection<K> keys);

	/**
	 * This invalidates the entry with the specified key. If it does not exist
	 * then its a no operation.
	 * @param k
	 */
	abstract public void invalidate(K k);
	abstract public void invalidateAll(Collection<K> keys);
	/**
	 * Check if the entry exists for the key
	 * @param k
	 * @return
	 */
	abstract public boolean contains(K k);
	
	/**
	 * This get an advisory lock on a key. In reality, it inserts a record in a 'lock' map to acquire ownership
	 * of a notional named lock (the name is the key). It will try to acquire the lock for at least timeoutMs.
	 * Once acquired, the lock is permanent until the lock is removed OR evictor by configuring a default evictor
	 * on the lock map.
	 * @param k The name of the lock
	 * @param value Any value, doesn't matter, typically use a Boolean
	 * @param timeOutMS Desired max wait time for a lock
	 * @return true if lock is acquired.
	 */
	abstract public boolean lock(K k, V value, int timeOutMS);
	
	/**
	 * Unlock a lock acquired by lock.
	 * @param k The name of the lock
	 */
	abstract public void unlock(K k);
}
