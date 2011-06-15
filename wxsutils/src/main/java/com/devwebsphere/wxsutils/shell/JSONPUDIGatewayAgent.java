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

import java.io.Serializable;

import com.devwebsphere.wxsutils.WXSMap;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

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
public class JSONPUDIGatewayAgent extends GatewayAgent 
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
	 * The key object encoded as a JSON string.
	 */
	public String keyString;
	/**
	 * The value object encoded as a JSON string. Only used for put operation
	 */
	public String valueString;

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
			WXSMap<Serializable,Serializable> remoteMap = JSONPUDIGatewayAgent.getRemoteMap(localSession.getObjectGrid().getName(), targetMap);
			
			Serializable key = stringAsKey(keyString);
			
			
			// now that we have the key POJO, we can use the remote map to route the get to the
			// right grid container.
			Serializable value = null;

			switch(opCode)
			{
			case isGet:
				value = remoteMap.get(key);
				value = valueAsString(value);
				break;
			case isDelete:
				value = remoteMap.remove(key);
				value = valueAsString(value);
				break;
			case isInvalidate:
				remoteMap.invalidate(key);
				value = "\"TRUE\"";
				break;
			case isPut:
				value = stringAsValue((String)valueString);
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
}
