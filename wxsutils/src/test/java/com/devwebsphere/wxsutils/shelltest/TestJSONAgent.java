//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.shelltest;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Main;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.shell.JSGrid;
import com.devwebsphere.wxsutils.shell.JSMap;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example
 * with the xml files in this folder. These xmls just add a third Map which doesn't
 * use client side caching.
 *
 */
public class TestJSONAgent 
{
	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap personMap;
	static String PERSON_MAP = "Person";
	
	
	@BeforeClass
	public static void setupTest()
	{
		// do everything in one JVM for test
		ogclient = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");
		// switch to this to connect to remote grid instead.
//		ogclient = WXSUtils.connectClient("localhost:2809", "Grid", "/objectgrid.xml");
		utils = new WXSUtils(ogclient);
		personMap = ogclient.getMap(PERSON_MAP);
	}

	/**
	 * This clears the FarMap3 in preparation for any tests
	 */
	public static void clearMap()
	{
		try
		{
			ogclient.getSession().getMap(PERSON_MAP).clear();
		}
		catch(ObjectGridException e)
		{
			Assert.fail("Exception during clear");
		}
	}

	@Test
	public void testPut()
		throws Exception
	{
//		clearMap();
		String personJSON = "{\"firstName\":\"Pedro\",\"middleInitial\":\"A\",\"surname\":\"Platinum\",\"dateOfBirth\":44661454547,\"creditLimit\":10000.0}";
		String keyString = "\"bnewport\""; // note double quotes around string
		
		JSMap.put(ogclient, PERSON_MAP, String.class.getName(), Person.class.getName(), keyString, personJSON);
		
	}

	@Test
	@Ignore
	public void testJSShell()
		throws Exception
	{
		Context ctxt = Context.enter();
		ErrorReporter reporter = new ErrorReporter() {
			
			public void warning(String arg0, String arg1, int arg2, String arg3,
					int arg4) 
			{
				System.out.println("WARNING");
			}
			
			public EvaluatorException runtimeError(String arg0, String arg1, int arg2,
					String arg3, int arg4) {
				System.out.println("RUNTIMEERROR");
				// TODO Auto-generated method stub
				return null;
			}
			
			public void error(String msg, String file, int linenum, String arg3, int arg4) {
				System.out.println("ERROR:" + msg);
			}
		};
		ctxt.setErrorReporter(reporter);
		
		Scriptable scope = ctxt.initStandardObjects();
		
		
		JSGrid gridShim = new JSGrid(ogclient, ctxt, scope);
		Object wrappedGridShim = Context.javaToJS(gridShim, scope);
		
		testPut();
		
		Main.global.init(Main.shellContextFactory);
		Main.global.defineProperty("grid", wrappedGridShim, ScriptableObject.CONST);

		JSMap mapShim = new JSMap(ogclient, PERSON_MAP, String.class.getName(), Person.class.getName(), ctxt, scope);
		Object wrappedMapShim = Context.javaToJS(mapShim, scope);
		Main.global.defineProperty("map", wrappedMapShim, ScriptableObject.CONST);
		
		while(true)
		{
			Main.processSource(ctxt, null);
			System.out.println("Loop");
		}
	}
	
	
}
