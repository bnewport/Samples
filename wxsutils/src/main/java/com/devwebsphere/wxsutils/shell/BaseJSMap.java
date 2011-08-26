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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This is used by the Javascript shell to manipulate entries in this Map.
 * The user obtains this using the JSGrid "grid" variable.
 * @author bnewport
 *
 */
public class BaseJSMap 
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
	static String ROUTING_MAP = "RouterKeyI32";
	
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
	
	public String toString()
	{
		return "jsmap:[mapName=" + mapName + ",key=" + keyClazz + ",value=" + valueClazz+"]";
	}

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
		throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		if(s instanceof String)
		{
			return mapper.writeValueAsString(s);
		}
		else
		{
			// Convert possible Rhino NativeObject to a Java Map/List construct
			Object data = convertNativeObjectToJava(s);
			// JSONize it
			return mapper.writeValueAsString(data);
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
	public BaseJSMap(ObjectGrid cog, String mapName, String keyClazz, String valueClazz, Context ctxt, Scriptable scope)
	{
		clientOG = cog;
		this.mapName = mapName;
		this.keyClazz = keyClazz;
		this.valueClazz = valueClazz;
		this.ctxt = ctxt;
		this.scope = scope;
	}

}
