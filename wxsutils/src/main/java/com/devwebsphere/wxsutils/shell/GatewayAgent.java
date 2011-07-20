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
import java.io.Serializable;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
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
public abstract class GatewayAgent implements MapGridAgent 
{

	transient ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6571775638703152250L;

	/**
	 * The name of the class used as the key on the grid side. This class
	 * is required for the grid side but not for the client side.
	 */
	public String keyClassName;
	transient private Class keyClass;
	transient private Class valueClass;
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
	public static WXSMap<Serializable, Serializable> getRemoteMap(String gridName, String mapName)
		throws ConnectException
	{
		ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
		ObjectGrid clientOG = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName);
		WXSUtils utils = new WXSUtils(clientOG);
		WXSMap<Serializable,Serializable> remoteMap = utils.getCache(mapName);
		return remoteMap;
	}
	
	/**
	 * Not used
	 */
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Class getKeyClass()
		throws ClassNotFoundException
	{
		if(keyClass == null)
		{
			keyClass = Class.forName(keyClassName);
		}
		return keyClass;
	}
	
	public Class getValueClass()
		throws ClassNotFoundException
	{
		if(valueClass == null)
		{
			valueClass = Class.forName(valueClassName);
		}
		return valueClass;
	}

	public Serializable stringAsKey(String keyString)
		throws ClassNotFoundException, IOException
	{
		Serializable key = (Serializable)mapper.readValue((String)keyString, getKeyClass());
		return key;
	}
	
	public Serializable stringAsValue(String valueString)
		throws ClassNotFoundException, IOException
	{
		Class valueClazz = Class.forName(valueClassName);
		Serializable value = (Serializable)mapper.readValue((String)valueString, getValueClass());
		return value;
	}
	
	public String keyAsString(Serializable key)
		throws IOException
	{
		return mapper.writeValueAsString(key);
	}
	
	public String valueAsString(Serializable value)
		throws IOException
	{
		return mapper.writeValueAsString(value);
	}
}
