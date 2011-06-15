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
package com.devwebsphere.wxsutils.shell;

import java.util.Collections;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

/**
 * This is used by the Javascript shell to manipulate entries in this Map.
 * The user obtains this using the JSGrid "grid" variable.
 * @author bnewport
 *
 */
public class JSMap extends BaseJSMap
{
	/**
	 * Typically only called from JSGrid
	 * @see JSGrid#getMap(String, String, String)
	 * @param cog
	 * @param mapName
	 * @param keyClazz
	 * @param valueClazz
	 * @param ctxt
	 */
	public JSMap(ObjectGrid cog, String mapName, String keyClazz, String valueClazz, Context ctxt, Scriptable scope)
	{
		super(cog, mapName, keyClazz, valueClazz, ctxt, scope);
	}

	/**
	 * This is called from Javascript and returns the JSON string for the specified key in this Map
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Object get(Object key)
		throws Exception
	{
		String s = convertToJSON(key);
		return convertJSONToNative(ctxt, scope, JSMap.get(clientOG, mapName, keyClazz, valueClazz, s));
	}

	/**
	 * This is called from Javascript and deletes the entry with the specified key and
	 * returns the current value
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Object remove(Object key)
	throws Exception
	{
		String s = convertToJSON(key);
		return convertJSONToNative(ctxt, scope, JSMap.remove(clientOG, mapName, keyClazz, valueClazz, s));
	}
	
	/**
	 * This is called from Javascript and invalidates the entry with the specified key
	 * if it exists. It's a no operation if the key isn't present
	 * @param key
	 * @throws Exception
	 */
	public void invalidate(Object key)
	throws Exception
	{
		String s = convertToJSON(key);
		JSMap.invalidate(clientOG, mapName, keyClazz, valueClazz, s);
	}
	
	/**
	 * This is called from Javascript and puts an entry with the specified key/value. The
	 * entry is created if it doesn't exist and updated otherwise.
	 * @param key
	 * @param value
	 * @return The previous value if it existed
	 * @throws Exception
	 */
	public Object put(Object key, Object value)
	throws Exception
	{
		String s = convertToJSON(key);
		String v = convertToJSON(value);
		return convertJSONToNative(ctxt, scope, JSMap.put(clientOG, mapName, keyClazz, valueClazz, s, v));
	}
	
	/**
	 * Low level code to invoke the Agent to forward a JSON operation to the grid
	 * @param ogclient
	 * @param mapName
	 * @param keyClazz
	 * @param valueClazz
	 * @param key
	 * @param value
	 * @param opCode
	 * @return
	 * @throws Exception
	 */
	static private String dualOpPrim(ObjectGrid ogclient, String mapName, String keyClazz, String valueClazz, String key, String value, JSONPUDIGatewayAgent.OPCODE opCode)
	throws Exception
	{
		JSONPUDIGatewayAgent agent = new JSONPUDIGatewayAgent();
		
		agent.opCode = opCode;
		agent.keyClassName = keyClazz;
		agent.valueClassName = valueClazz;
		agent.targetMap = mapName;
		
		agent.keyString = key; // note double quotes around string
		agent.valueString = value;
		
		Session sess = ogclient.getSession();
		AgentManager amgr = sess.getMap(ROUTING_MAP).getAgentManager();
		
		// use the key hash to route request to a random partition
		Integer fakeKey = new Integer(agent.keyString.hashCode());
		Map<String, Object> map = amgr.callMapAgent(agent, Collections.singleton(fakeKey));
		Object result = map.get(fakeKey);
		if(result instanceof EntryErrorValue)
		{
			System.out.println("Error executing code on grid side: " + result.toString());
		}
		return (result instanceof String) ? result.toString() : "\"ibm_error\"";
	}
	
	/**
	 * Convenience method
	 * @param clientOg
	 * @param mapName
	 * @param keyClazz
	 * @param valueClazz
	 * @param key
	 * @param opCode
	 * @return
	 * @throws Exception
	 */
	static private String singleOpPrim(ObjectGrid clientOg, String mapName, String keyClazz, String valueClazz, String key, JSONPUDIGatewayAgent.OPCODE opCode)
		throws Exception
	{
		return dualOpPrim(clientOg, mapName, keyClazz, valueClazz, key, null, opCode);
	}
	
	/**
	 * Macro
	 * @param clientOg
	 * @param mapName
	 * @param keyClazz
	 * @param valueClazz
	 * @param key
	 * @return
	 * @throws Exception
	 */
	static private String get(ObjectGrid clientOg, String mapName, String keyClazz, String valueClazz, String key)
		throws Exception
	{
		return singleOpPrim(clientOg, mapName, keyClazz, valueClazz, key, JSONPUDIGatewayAgent.OPCODE.isGet);
	}
	
	/**
	 * Macro
	 * @param clientOG
	 * @param mapName
	 * @param keyClazz
	 * @param valueClazz
	 * @param key
	 * @return
	 * @throws Exception
	 */
	static private String remove(ObjectGrid clientOG, String mapName, String keyClazz, String valueClazz, String key)
		throws Exception
	{
		return singleOpPrim(clientOG, mapName, keyClazz, valueClazz, key, JSONPUDIGatewayAgent.OPCODE.isDelete);
	}
	
	/**
	 * Macro
	 * @param clientOG
	 * @param mapName
	 * @param keyClazz
	 * @param valueClazz
	 * @param key
	 * @return
	 * @throws Exception
	 */
	static private String invalidate(ObjectGrid clientOG, String mapName, String keyClazz, String valueClazz, String key)
		throws Exception
	{
		return singleOpPrim(clientOG, mapName, keyClazz, valueClazz, key, JSONPUDIGatewayAgent.OPCODE.isInvalidate);
	}
	
	// public for test use
	static public String put(ObjectGrid clientOG, String mapName, String keyClazz, String valueClazz, String key, String value)
		throws Exception
	{
		return dualOpPrim(clientOG, mapName, keyClazz, valueClazz, key, value, JSONPUDIGatewayAgent.OPCODE.isPut);
	}

}
