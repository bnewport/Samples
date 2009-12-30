package com.devwebsphere.rediswxs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.rediswxs.agent.MultiAttributeGetAgent;
import com.devwebsphere.rediswxs.agent.MultiAttributePutAgent;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;

public class MultiMetaData<T>
{
	Map<String, Field> stringAttributes;
	Map<String, Field> longAttributes;
	
	ArrayList<String> stringAttributeNames;
	ArrayList<String> longAttributeNames;
	
	Class<T> clazz;
	
	public MultiMetaData(Class<T> clazz)
	{
		this.clazz = clazz;
		
		stringAttributes = new HashMap<String, Field>();
		longAttributes = new HashMap<String, Field>();
		stringAttributeNames = new ArrayList<String>();
		longAttributeNames = new ArrayList<String>();
		
		Field[] all = clazz.getFields();
		for(Field f : all)
		{
			if(f.getType().equals(String.class))
			{
				stringAttributes.put(f.getName(), f);
				stringAttributeNames.add(f.getName());
			}
			if(f.getType().equals(Long.class))
			{
				longAttributes.put(f.getName(), f);
				longAttributeNames.add(f.getName());
			}
		}
	}
	
	ScalarKey getScalarKey(String key)
	{
		StringBuilder sbKey = new StringBuilder(key.length() + 1);
		sbKey.append('{'); sbKey.append(key);
		ScalarKey sk = new ScalarKey(sbKey.toString());
		return sk;
	}
	
	public T get(String prefix)
	{
		try
		{
			T rc = clazz.newInstance();

			ArrayList<ScalarKey> keys = new ArrayList<ScalarKey>();
			if(stringAttributeNames.size() > 0)
			{
				for(String field : stringAttributeNames)
				{
					keys.add(new ScalarKey(prefix, field));
				}
				BackingMap str_str_bmap = R.r.nocacheog.getMap(R.str_str.getMapName());
				Map<ScalarKey, String> rc_str = R.r.wxsutils.getAll(keys, str_str_bmap);
				
				for(int i = 0; i < keys.size(); ++i)
				{
					String val = rc_str.get(keys.get(i));
					stringAttributes.get(stringAttributeNames.get(i)).set(rc, val);
				}
			}

			if(longAttributeNames.size() > 0)
			{
				keys = new ArrayList<ScalarKey>();
				for(String field : longAttributeNames)
				{
					keys.add(new ScalarKey(prefix, field));
				}
				BackingMap str_long_bmap = R.r.nocacheog.getMap(R.str_long.getMapName());
				Map<ScalarKey, Long> rc_long = R.r.wxsutils.getAll(keys, str_long_bmap);
				for(int i = 0; i < keys.size(); ++i)
				{
					Long val = rc_long.get(keys.get(i));
					longAttributes.get(longAttributeNames.get(i)).setLong(rc, val);
				}
			}
			return rc;
		}
		catch(Throwable e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	public void put(String prefix, T pojo)
	{
		try
		{
			Map<ScalarKey, String> str_batch = new HashMap<ScalarKey, String>();
			for(int i = 0; i < stringAttributeNames.size(); ++i)
			{
				String fieldName = stringAttributeNames.get(i);
				String value = (String)stringAttributes.get(fieldName).get(pojo);
				str_batch.put(new ScalarKey(prefix, fieldName), value);
			}
			BackingMap str_str_bmap = R.r.nocacheog.getMap(R.str_str.getMapName());
			R.r.wxsutils.putAll(str_batch, str_str_bmap);
			
			Map<ScalarKey, Long> long_batch = new HashMap<ScalarKey, Long>();
			for(int i = 0; i < longAttributeNames.size(); ++i)
			{
				String fieldName = longAttributeNames.get(i);
				Long value = longAttributes.get(fieldName).getLong(pojo);
				long_batch.put(new ScalarKey(prefix, fieldName), value);
			}
			BackingMap str_long_bmap = R.r.nocacheog.getMap(R.str_long.getMapName());
			R.r.wxsutils.putAll(str_batch, str_long_bmap);
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	public void remove(String prefix)
	{
		try
		{
			ArrayList<ScalarKey> keys = new ArrayList<ScalarKey>();
			for(String field : stringAttributeNames)
			{
				keys.add(new ScalarKey(prefix, field));
			}
			BackingMap str_str_bmap = R.r.nocacheog.getMap(R.str_str.getMapName());
			R.r.wxsutils.removeAll(keys, str_str_bmap);
			
			keys = new ArrayList<ScalarKey>();
			for(String field : longAttributeNames)
			{
				keys.add(new ScalarKey(prefix, field));
			}
			BackingMap str_long_bmap = R.r.nocacheog.getMap(R.str_long.getMapName());
			R.r.wxsutils.removeAll(keys, str_long_bmap);
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
}

