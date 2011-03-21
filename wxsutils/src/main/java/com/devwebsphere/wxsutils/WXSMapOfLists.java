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
	 */
	public void lpush(K key, V value);

	/**
	 * This removes and returns the left most element in the list
	 * @param key The key for the list
	 * @return NULL if the list is empty or the leftmost element
	 */
	public V lpop(K key);

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

}
