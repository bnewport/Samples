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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.utils.ClassSerializer;
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
public interface WXSMapOfLists<K,V> 
{
	
	/**
	 * This is used when bulk pushing values in to lists. It
	 * allows the values to be pushed as well as a Filter
	 * so it behaves like a bulk lcpush
	 * @author bnewport
	 *
	 * @param <V>
	 * @see WXSMapOfLists#lpush(Map)
	 * @see WXSMapOfLists#rpush(Map)
	 */
	public class BulkPushItem<V> implements Externalizable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6589018995986915360L;
		/**
		 * The values to push on the list
		 */
		V value;
		/**
		 * If specified, each value is pushed using a cpush command with this filter
		 */
		Filter filter;
		
		public BulkPushItem() {};

		/**
		 * This creates an object to store the values to
		 * push and the filter to test them with
		 * @param v A list of values to push
		 * @param f The Filter, can be null
		 */
		public BulkPushItem(V v, Filter f)
		{
			value = v;
			filter = f;
		}

		public final V getValue() {
			return value;
		}

		public final Filter getFilter() {
			return filter;
		}

		public void readExternal(ObjectInput in) throws IOException,
				ClassNotFoundException 
		{
			ClassSerializer serializer = WXSUtils.getSerializer();
			value = (V)serializer.readObject(in);
			filter = (Filter)serializer.readNullableObject(in);
		}

		public void writeExternal(ObjectOutput out) throws IOException 
		{
			ClassSerializer serializer = WXSUtils.getSerializer();
			serializer.writeObject(out, value);
			serializer.writeNullableObject(out, filter);
		}
		
	}

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
	 * This will push the value on the list only if
	 * no existing element matches the Filter
	 * @param key
	 * @param value
	 * @param condition
	 */
	public void lcpush(K key, V value, Filter condition);
	
	/**
	 * This will push the value on the list only if
	 * no existing element matches the Filter. If an item
	 * is pushed on the list then the list key will
	 * be added to the set DirtyKey
	 * @param key
	 * @param value
	 * @param condition
	 */
	public void lcpush(K key, V value, Filter condition, K dirtyKey);

	/**
	 * This will push the value on the list only if
	 * no existing element matches the Filter
	 * @param key
	 * @param value
	 * @param condition
	 */
	public void rcpush(K key, V value, Filter condition);
	
	/**
	 * This will push the value on the list only if
	 * no existing element matches the Filter. If an item
	 * is pushed on the list then the list key will
	 * be added to the set DirtyKey
	 * @param key
	 * @param value
	 * @param condition
	 */
	public void rcpush(K key, V value, Filter condition, K dirtyKey);
	
	/**
	 * This pushes a value on the left side of the list
	 * @param key The key for the list
	 * @param value The value to push
	 * @param dirtySet The key for the per shard dirty set to add this key to. Optional
	 */
	public void lpush(K key, V value, K dirtySet);

	/**
	 * This pushes the value on the left of the list moving over the list from 0 to its size
	 * @param key
	 * @param values
	 */
	public void lpush(K key, List<V> values);
	
	/**
	 * This pushes the value on the left of the list moving over the list from 0 to its size
	 * @param key
	 * @param values
	 * @param dirtySet
	 */
	public void lpush(K key, List<V> values, K dirtySet);

	/**
	 * This takes a Map of list keys and the entries to push and does a push
	 * of all those key and lists in bulk. Each Map Entry is like a normal
	 * call to lpush(K, List<V>). It tries to do at most one RPC
	 * per partition and does those RPCs in parallel using
	 * the WXSUtils thread pool.
	 * @param items
	 */
	public void lpush(Map<K, List<BulkPushItem<V>>> items);
	
	/**
	 * This takes a Map of list keys and the entries to push and does a push
	 * of all those key and lists in bulk. Each Map Entry is like a normal
	 * call to lpush(K, List<V>). It tries to do at most one RPC
	 * per partition and does those RPCs in parallel using
	 * the WXSUtils thread pool.
	 * @param items
	 * @param dirtySet
	 */
	public void lpush(Map<K, List<BulkPushItem<V>>> items, K dirtySet);
	
	/**
	 * This takes a Map of list keys and the entries to push and does a push
	 * of all those key and lists in bulk. Each Map Entry is like a normal
	 * call to rpush(K, List<V>). It tries to do at most one RPC
	 * per partition and does those RPCs in parallel using
	 * the WXSUtils thread pool.
	 * @param items
	 */
	public void rpush(Map<K, List<BulkPushItem<V>>> items);
	
	/**
	 * This takes a Map of list keys and the entries to push and does a push
	 * of all those key and lists in bulk. Each Map Entry is like a normal
	 * call to rpush(K, List<V>). It tries to do at most one RPC
	 * per partition and does those RPCs in parallel using
	 * the WXSUtils thread pool.
	 * @param items
	 * @param dirtySet
	 */
	public void rpush(Map<K, List<BulkPushItem<V>>> items, K dirtySet);

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
	 * This pops up to N items from the left of the list. If the List had [1,2,3,4] and you
	 * do a lpop(2) then it returns [1,2]
	 * @param key
	 * @param numItems
	 * @param dirtyKey
	 * @return The first items popped if the first element of the returned list and so on.
	 */
	public List<V> lpop(K key, int numItems, K dirtyKey);
	/**
	 * This pops up to N items from the left of the list. If the List had [1,2,3,4] and you
	 * do a lpop(2) then it returns [1,2]
	 * @param key
	 * @param numItems
	 * @return The first items popped if the first element of the returned list and so on.
	 */
	public List<V> lpop(K key, int numItems);
	/**
	 * This pops up to N items from the left of the list. If the List had [1,2,3,4] and you
	 * do a rpop(2) then it returns [4,3]
	 * @param key
	 * @param numItems
	 * @param dirtyKey
	 * @return The first items popped if the first element of the returned list and so on.
	 */
	public List<V> rpop(K key, int numItems, K dirtyKey);
	/**
	 * This pops up to N items from the left of the list. If the List had [1,2,3,4] and you
	 * do a rpop(2) then it returns [4,3]
	 * @param key
	 * @param numItems
	 * @param dirtyKey
	 * @return The first items popped if the first element of the returned list and so on.
	 */
	public List<V> rpop(K key, int numItems);

	/**
	 * This removes N items from the right and returns the number of items
	 * actually removed
	 * @param key
	 * @param numItems
	 * @return
	 */
	public int rremove(K key, int numItems);
	/**
	 * This removes N items from the right and returns the number of items
	 * actually removed
	 * 
	 * @param key
	 * @param numItems
	 * @param dirtyKey
	 * @param releaseLease
	 * @return
	 */
	public int rremove(K key, int numItems, K dirtyKey, boolean releaseLease);
	
	/**
	 * This removes N items from the left and returns the number of items
	 * actually removed
	 * @param key
	 * @param numItems
	 * @return
	 */
	public int lremove(K key, int numItems);
	/**
	 * This removes N items from the left and returns the number of items
	 * actually removed
	 * 
	 * @param key
	 * @param numItems
	 * @param dirtyKey
	 * @param releaseLease
	 * @return
	 */
	public int lremove(K key, int numItems, K dirtyKey, boolean releaseLease);
	
	/**
	 * This pushes the value on the righthand side of the list
	 * @param key The key for the list
	 * @param value The value to add on the right side of the list
	 */
	public void rpush(K key, V value);
	
	/**
	 * This pushes the value on the righthand side of the list
	 * @param key The key for the list
	 * @param value The value to add on the right side of the list
	 * @param dirtySet The key for the per shard dirty set to add this key to. Optional
	 */
	public void rpush(K key, V value, K dirtySet);

	/**
	 * This pushes a list of value together on to the right side
	 * of a list.
	 * @param key
	 * @param values
	 */
	public void rpush(K key, List<V> values);

	/**
	 * This pushes a list of value together on to the right side
	 * of a list.
	 * @param key
	 * @param values
	 * @param dirtySet
	 */
	public void rpush(K key, List<V> values, K dirtySet);

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
	public ArrayList<V> lrange(K key, int low, int high, Filter filter);

	/**
	 * The length of the list for the key. 0 if the list doesn't
	 * exist
	 * @param key The key for the list
	 * @return The size of the list
	 */
	public int llen(K key);
	
	/**
	 * This returns true if the list has no elements. This is usually faster
	 * than calling size to compare with zero.
	 * @param key
	 * @return
	 */
	public boolean isEmpty(K key);
	
	/**
	 * This marks the list for eviction after a specified time.
	 * @param key The list to evict
	 * @param type Last Creation time or last access time
	 * @param intervalSeconds The time in seconds after which to evict
	 */
	@Beta
	public void evict(K key, EvictionType type, int intervalSeconds);
}
