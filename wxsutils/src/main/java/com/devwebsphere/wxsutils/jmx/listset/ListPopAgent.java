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
package com.devwebsphere.wxsutils.jmx.listset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class ListPopAgent<V extends Serializable> implements MapGridAgent 
{

	static public class EmptyMarker implements Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5608399827890355903L;
		
	};
	
	public boolean isLeft;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		Object rc = null;
		try
		{
			ArrayList<V> list = (ArrayList<V>)map.getForUpdate(key);
			if(list != null)
			{
				if(list.isEmpty())
					rc = new EmptyMarker();
				else
				{
					if(isLeft)
						rc = list.remove(0);
					else
						rc = list.remove(list.size() - 1);
				}
				map.update(key, list);
			}
			else
			{
				rc = new EmptyMarker();
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			e.printStackTrace();
			throw new ObjectGridRuntimeException(e);
		}
		return rc;
	}
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
