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
package com.devwebsphere.wxs.asyncserviceimpl;

import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

public class ThreadLocalGridClient extends ThreadLocal<ObjectGrid> 
{
	ObjectGrid localGrid;
	
	public ThreadLocalGridClient(ObjectGrid localGridRef)
		throws ObjectGridException
	{
		localGrid = localGridRef;
	}
	
	@Override
	protected ObjectGrid initialValue() 
	{
		try
		{
			ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
			ObjectGrid clientGrid = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, localGrid.getName());
			return clientGrid;
		}
		catch(ObjectGridException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

}
