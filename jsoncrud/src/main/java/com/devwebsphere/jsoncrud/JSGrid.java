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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This is a Javascript shell exposed helper to get instances of Maps
 * for this grid.
 * 
 * js> personMap = grid.getMap("Person", "java.lang.String", "com.devwebsphere.Person")
 * 
 * @author bnewport
 *
 */
public class JSGrid 
{
	ObjectGrid clientOG;
	Context ctxt;
	Scriptable scope;
	
	public JSGrid(ObjectGrid og, Context c, Scriptable scope)
	{
		clientOG = og;
		ctxt = c;
		this.scope = scope;
	}
	
	public JSMap getMap(String mapName, String keyClassName, String valueClassName)
	{
		return new JSMap(clientOG, mapName, keyClassName, valueClassName, ctxt, scope);
	}
}
