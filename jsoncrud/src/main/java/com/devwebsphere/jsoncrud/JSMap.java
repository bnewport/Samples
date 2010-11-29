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
package com.devwebsphere.jsoncrud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.gson.Gson;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;

/**
 * This is used by the Javascript shell to manipulate entries in this Map.
 * The user obtains this using the JSGrid "grid" variable.
 * @author bnewport
 *
 */
public class JSMap 
{
	/**
	 * We use a special Map for routing called Router. The only thing
	 * special about it is that its not a customer map. WXS can optimize
	 * a Map using type information from the first oepration on it
	 * so we don't want to force a String key on a Map. The JSON
	 * key string is used always with this Map and the request
	 * is forwarded from where ever that key hashes to onwards
	 * to the correct partition using the Agent code.
	 */
	static String ROUTING_MAP = "Router";
	
	ObjectGrid clientOG;
	
	/**
	 * This is provided by the constructor
	 */
	String keyClazz;
	/**
	 * This is provided by the constructor
	 */
	String valueClazz;
	/**
	 * This is provided by the constructor
	 */
	String mapName;
	/**
	 * The Rhino shell context
	 */
	Context ctxt;
	Scriptable scope;

	/**
	 * This takes a Rhino object and converts it to a Java version using Maps
	 * and lists of scalars. This is usually then just converted to a JSON
	 * string by gson. This allows the user to easily make keys and values
	 * using JavaScript versions of the Java POJOs rather than json strings
	 * directly.
	 * @param v
	 * @return
	 */
	static private Object convertNativeObjectToJava(Object v)
	{
		if(v instanceof ScriptableObject)
		{
			ScriptableObject scriptable = (ScriptableObject) v;

			String className = scriptable.getClassName();
			if( className.equals( "Date" ) )
			{
				Object time = ScriptableObject.callMethod( scriptable, "getTime", null );
				if( time instanceof Number )
					return new Date( ( (Number) time ).longValue() ).toString();
			}
			else if( className.equals( "String" ) )
			{
				return v.toString();
			}
			else if(v instanceof NativeArray)
			{
				NativeArray na = (NativeArray)v;
				ArrayList<Object> l = new ArrayList<Object>((int)na.getLength());
				for(int i = 0; i < na.getLength();++i)
				{
					l.add(convertNativeObjectToJava(ScriptableObject.getProperty(na, i)));
				}
				return l;
			}
			Map<String, Object> m = new HashMap<String, Object>();

			Object[] ids = scriptable.getAllIds();
			for( Object id : ids )
			{
				String key = id.toString();
				Object value = convertNativeObjectToJava( ScriptableObject.getProperty( scriptable, key ) );
				m.put( key, value );
			}
			return m;
		}
		else 
			return v.toString();
	}

	/**
	 * Converts a Rhino object to a JSON string.
	 * @param s The Rhino object
	 * @return A JSON equivalent
	 */
	static public String convertToJSON(Object s)
	{
		Gson gson = new Gson();
		if(s instanceof String)
		{
			return gson.toJson(s, String.class);
		}
		else
		{
			// Convert possible Rhino NativeObject to a Java Map/List construct
			Object data = convertNativeObjectToJava(s);
			// JSONize it
			return gson.toJson(data);
		}
	}
	
	/**
	 * This evaluates the JSON result and stores it in a variable. This is a quick way of
	 * converting the returned data to a JSON object for easy consumption by the user
	 * @param c
	 * @param scope
	 * @param s
	 * @return
	 */
	static public Object convertJSONToNative(Context c, Scriptable scope, String s)
	{

		// _rc is used as an internal variable to store the last result
		String expr = "_rc = " + s;
		return c.evaluateString(scope, expr, "result", 1, null);
	}
	
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
		clientOG = cog;
		this.mapName = mapName;
		this.keyClazz = keyClazz;
		this.valueClazz = valueClazz;
		this.ctxt = ctxt;
		this.scope = scope;
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
		
		Map<String, String> map = amgr.callMapAgent(agent, Collections.singleton(agent.keyString));
		String result = map.get(agent.keyString);
		return result;
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
