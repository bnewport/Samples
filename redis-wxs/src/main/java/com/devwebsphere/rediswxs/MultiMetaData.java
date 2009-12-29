package com.devwebsphere.rediswxs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.rediswxs.agent.MultiAttributeGetAgent;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;

public class MultiMetaData<T>
{
	class Attribute
	{
		Method getter;
		Method setter;
		
		String name;
	};

	Map<String, Attribute> stringAttributes;
	Map<String, Attribute> longAttributes;
	
	ArrayList<String> stringAttributeNames;
	ArrayList<String> longAttributeNames;
	
	BackingMap string_string_map;
	BackingMap string_long_map;
	
	Class<T> clazz;
	
	ScalarKey getScalarKey(String key)
	{
		StringBuilder sbKey = new StringBuilder(key.length() + 1);
		sbKey.append('{'); sbKey.append(key);
		ScalarKey sk = new ScalarKey(sbKey.toString());
		return sk;
	}
	
	public MultiMetaData(Class<T> clazz)
	{
		this.clazz = clazz;
	}
	
	public T get(String prefix)
	{
		try
		{
			T rc = clazz.newInstance();
			ScalarKey sk = getScalarKey(prefix);
			
			AgentManager am = R.r.thread.getNoCacheSession().getMap(R.str_str.getMapName()).getAgentManager();
	
			// make agent to get all attributes at once
			MultiAttributeGetAgent agent = new MultiAttributeGetAgent();
			agent.batchStringKeys = stringAttributeNames;
			agent.batchLongKeys = longAttributeNames;
			agent.stringMapName = R.str_str.getMapName();
			agent.longMapName = R.str_long.getMapName();

			Map<String, Object> values = am.callMapAgent(agent, Collections.singleton(sk));
			
			Object[] params = new Object[1];
			for(String name : stringAttributeNames)
			{
				StringBuilder theKey = new StringBuilder();
				theKey.append(sk.getKey());
				theKey.append(name);
				String val = (String)values.get(theKey.toString());
				params[0] = val;
				stringAttributes.get(name).setter.invoke(rc, params);
			}
			for(String name : longAttributeNames)
			{
				StringBuilder theKey = new StringBuilder();
				theKey.append(sk.getKey());
				theKey.append(name);
				Long val = (Long)values.get(theKey.toString());
				params[0] = val;
				longAttributes.get(name).setter.invoke(rc, params);
			}
			return rc;
		}
		catch(Throwable e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void put(String prefix, T pojo)
	{
		ScalarKey sk = getScalarKey(prefix);
	}
	
	public void remove(String prefix)
	{
		ScalarKey sk = getScalarKey(prefix);
	}
}

