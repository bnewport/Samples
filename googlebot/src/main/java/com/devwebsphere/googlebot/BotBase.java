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

package com.devwebsphere.googlebot;

import java.util.Collection;

import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridManager;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup.ShardEvents;

/**
 * This base class is used by BotXMPPSingleton and others using different IM technology
 * under testing.
 * @author bnewport
 *
 */
abstract public class BotBase implements ObjectGridEventListener,ObjectGridEventGroup.ShardEvents 
{
	// Reference to singleton grid for BOT
	ObjectGrid botSingleton;
	
	// Reference to scalable data grid for BOT state
	ObjectGrid botStateClient;

	/**
	 * This method is called when the BOT singleton is assigned to this
	 * JVM. There is one instance of this class per active partition in
	 * a container JVM. The shard parameters is a local reference to
	 * the state for this partition. A BOT can keep non scalable state
	 * in this shard. Data which needs to scale from a capacity point
	 * of view should be stored in the state grid.
	 * @see ShardEvents#shardActivated(ObjectGrid)
	 */
	public void shardActivated(ObjectGrid shard) 
	{
		try
		{
			botSingleton = shard;
			
			// get a client connection for the state Grid
			ObjectGridManager ogm = ObjectGridManagerFactory.getObjectGridManager();
			ClientClusterContext cc = ogm.connect(null, null);
			botStateClient = ogm.getObjectGrid(cc, "BotState");
		}
		catch(ObjectGridException e)
		{
			System.out.println("Exception connecting to grid:" + e.toString());
		}
	}

	/**
	 * This increments a named counter in a named Map "Counter" in a grid.
	 * @param grid The grid to use
	 * @param mapName The map to use
	 * @param name The name of the country entry
	 */
	protected int incrementCounter(ObjectGrid grid, String mapName, String name)
	{
		Session s = null;
		try
		{
			// note we're using the state grid here, not the botSingleton grid
			s = grid.getSession();
			s.begin();
			ObjectMap map = s.getMap(mapName);
			// this serializes around the key whether it exists or not
			Integer chatCount = (Integer)map.getForUpdate(name);
			int rc = 0;
			if(chatCount == null)
			{
				// if it doesn't exist then insert it
				map.insert(name, new Integer(1));
				rc = 1;
			}
			else
			{
				// otherwise update it
				rc = chatCount.intValue() + 1;
				map.update(name, rc);
			}
			s.commit();
			return rc;
		}
		catch(ObjectGridException e)
		{
			System.out.println("Exception updating " + name);
			if(s != null && s.isTransactionActive())
			{
				try { s.rollback(); } catch(ObjectGridException e2) {}
			}
			return -1;
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void initialize(Session arg0) {
		// TODO Auto-generated method stub

	}

	public void transactionBegin(String arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	public void transactionEnd(String arg0, boolean arg1, boolean arg2,
			Collection arg3) {
		// TODO Auto-generated method stub

	}

}
