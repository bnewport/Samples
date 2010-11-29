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

import java.util.Map;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ConnectException;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

/**
 * This is an agent or RPC called by a client to run code on the grid side. This code
 * is initialized with the key and value class names, targetMap, operation
 * and key and value JSON strings if needed.
 * Once the code runs on the grid side then the agent acts as a gateway
 * and then executes the operation on the correct key while creating
 * the key and value POJOs using JSON operations. Any results are
 * encoded as JSON strings and returned to the caller.
 * @author bnewport
 */
public class JSONPUDIGatewayAgent implements MapGridAgent 
{

	/**
	 * These operations are supported. For insert, use put
	 * @author bnewport
	 *
	 */
	public enum OPCODE {isGet, isDelete, isInvalidate, isPut};

	/**
	 * Set your operation code here
	 */
	public OPCODE opCode;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6571775638703152250L;

	/**
	 * The name of the class used as the key on the grid side. This class
	 * is required for the grid side but not for the client side.
	 */
	public String keyClassName;
	/**
	 * The name of the class used as the value on the grid side. This class
	 * is required for the grid side but not for the client side.
	 */
	public String valueClassName;
	/**
	 * The name of the map containing the key/value pairs of interest
	 */
	public String targetMap;
	/**
	 * The key object encoded as a JSON string.
	 */
	public String keyString;
	/**
	 * The value object encoded as a JSON string. Only used for put operation
	 */
	public String valueString;

	/**
	 * Helper method to get a routable client connection to the grid whose
	 * container is running in this JVM. This is needed because the grid used
	 * by agents is a local reference to the primary, it doesn't have any routing
	 * logic so you cannot use the Session passed to an Agent for operations
	 * on entries not stored in that partition.
	 * @param gridName
	 * @param mapName
	 * @return
	 * @throws ConnectException
	 */
	public static WXSMap<Object, Object> getRemoteMap(String gridName, String mapName)
		throws ConnectException
	{
		ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
		ObjectGrid clientOG = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName);
		WXSUtils utils = new WXSUtils(clientOG);
		WXSMap<Object,Object> remoteMap = utils.getCache(mapName);
		return remoteMap;
	}
	
	@Override
	/**
	 * This is called to execute the gateway operation. This method constructs POJOs from
	 * the JSON key and value strings and then invokes a grid operation
	 * on a client connection to execute the desired operation.
	 */
	public Object process(Session localSession, ObjectMap map, Object zz) 
	{
		try
		{
			// get a client connect to the grid.
			// the sess above is a LOCAL shard with no routing layer.
			WXSMap<Object,Object> remoteMap = JSONPUDIGatewayAgent.getRemoteMap(localSession.getObjectGrid().getName(), targetMap);
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Class keyClazz = Class.forName(keyClassName);
			Class valueClazz = Class.forName(valueClassName);
			Object key = gson.fromJson((String)keyString, keyClazz);
			
			// now that we have the key POJO, we can use the remote map to route the get to the
			// right grid container.
			Object value = null;

			switch(opCode)
			{
			case isGet:
				value = remoteMap.get(key);
				value = gson.toJson(value, valueClazz);
				break;
			case isDelete:
				value = remoteMap.remove(key);
				value = gson.toJson(value, valueClazz);
				break;
			case isInvalidate:
				remoteMap.invalidate(key);
				value = "\"TRUE\"";
				break;
			case isPut:
				value = gson.fromJson((String)valueString, valueClazz);
				remoteMap.put(key, value);
				value = "\"TRUE\"";
				break;
			}
			if(value == null)
				value = "\"NOT FOUND\"";
			return value;
		}
		catch(Exception e)
		{
			return "{\"_exception\":\"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Not used
	 */
	@Override
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
