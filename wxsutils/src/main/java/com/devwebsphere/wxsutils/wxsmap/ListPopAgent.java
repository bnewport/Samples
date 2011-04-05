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
package com.devwebsphere.wxsutils.wxsmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

@Deprecated
public class ListPopAgent<V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(ListPopAgent.class.getName());
	static public class EmptyMarker implements Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5608399827890355903L;
		
	};
	
	public boolean isLeft;
	
	static public <V extends Serializable> Object pop(Session sess, ObjectMap map, Object key, boolean isLeft)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), ListPopAgent.class.getName());
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
		catch(Exception e)
		{
			mbean.getKeysMetric().logException(e);
			logger.log(Level.SEVERE, "Exception:", e);
			throw new ObjectGridRuntimeException(e);
		}
		return rc;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		return pop(sess, map, key, isLeft);
	}
	
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
