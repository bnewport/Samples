package com.devwebsphere.wxsutils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.devwebsphere.wxsutils.snapshot.CreateJSONSnapshotAgent;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class TestMapGridAgent implements MapGridAgent {

	public Object process(Session arg0, ObjectMap arg1, Object arg2) 
	{
		throw new RuntimeException("Shouldn't be calling this");
	}

	public Map processAllEntries(Session sess, ObjectMap map) 
	{
		Map<String, String> rc = new HashMap<String, String>();
		try
		{
			Iterator iter = CreateJSONSnapshotAgent.getAllKeys(map);
			while(iter.hasNext())
			{
				String key = (String)iter.next();
				rc.put(key, (String)map.get(key));
			}
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
		return rc;
	}

}
