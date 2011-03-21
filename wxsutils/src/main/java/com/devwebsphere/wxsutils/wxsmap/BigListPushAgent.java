package com.devwebsphere.wxsutils.wxsmap;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class BigListPushAgent <V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(BigListPushAgent.class.getName());

	public static int BUCKET_SIZE = 20;
	
	public boolean isLeft;
	public V value;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5627208135087330201L;

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), BigListPushAgent.class.getName());
		long startNS = System.nanoTime();
		try
		{
			BigListHead<V> head = (BigListHead<V>)map.getForUpdate(key);
			if(head == null)
			{
				// this inserts the head in the map also.
				head = new BigListHead<V>(sess, map, key, value, BUCKET_SIZE);
			}
			else
			{
				// this updates the head in the map also
				head.push(sess, map, key, isLeft, value);
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		}
		catch(UndefinedMapException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			e.printStackTrace();
			throw new ObjectGridRuntimeException(e);
		}
		return Boolean.TRUE;
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
