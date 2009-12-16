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
package com.devwebsphere.manualpartition.data;

import java.io.Serializable;


import com.devwebsphere.manualpartition.data.keyutils.FixedHashInteger;
import com.devwebsphere.manualpartition.data.keyutils.ThreadLocalObjectMap;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.plugins.PartitionableKey;

/**
 * This is a key for the Symbol Map. It overrides the default hash to return a
 * non calculated hash. The key is routed to the partition whose value is stored
 * in the WXS Map PartitionMap using the key.
 * @author bnewport
 *
 */
public class MyKey implements PartitionableKey, Serializable 
{
	// the actual key
	String key;
	
	/**
	 * Each thread needs its own ObjectMap to look up the partition for a key.
	 */
	static ThreadLocalObjectMap threadLocalObjectMap = new ThreadLocalObjectMap(PartitionMapping.MAP);

	public MyKey() {}
	
	public MyKey(String key) { this.key = key; }
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7661728059654858367L;

	/**
	 * This is called whenever a client needs to get the partition for a key. This
	 * method returns the partition id that is stored in the ObjectMap PartitionMap.
	 * A ThreadLocal is used to get an ObjectMap. The partition is determined by looking
	 * up the key in the Map and returning an Object which hashes to that partition. It is
	 * expected that the ObjectMap uses OPTIMISTIC_LOCKING so that a near cache
	 * gives very high performance. The key to partition mapping is expected to be READ ONLY.
	 */
	public Object ibmGetPartition() 
	{
		try
		{
			// get a ThreadLocal ObjectMap
			ObjectMap map = threadLocalObjectMap.get();
			// look up the partition id for this key using the cached database table
			// to provide the mapping
			Integer partitionId = (Integer)map.get(key);
			// return an object guaranteed to return the partition id
			// as the hasCode
			return new FixedHashInteger(partitionId.intValue());
		}
		catch(ObjectGridException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
}
