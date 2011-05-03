package com.devwebsphere.wxsutils;

import java.sql.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxs.jdbcloader.Customer;
import com.devwebsphere.wxsutils.snapshot.CreateJSONSnapshotAgent;
import com.devwebsphere.wxsutils.snapshot.ReadJSONSnapshotAgent;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;


public class TestJSONSnapshot
{
	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap bmFarMap3;
	
	@BeforeClass
	public static void setupTest()
		throws Exception
	{
		// do everything in one JVM for test
//		ogclient = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");
		// switch to this to connect to remote grid instead.
		utils = WXSUtils.getDefaultUtils();
		ogclient = utils.getObjectGrid();
		bmFarMap3 = ogclient.getMap("FarMap3");
	}

	/**
	 * This clears the FarMap3 in preparation for any tests
	 */
	public static void clearMap()
	{
		try
		{
			ogclient.getSession().getMap(bmFarMap3.getName()).clear();
		}
		catch(ObjectGridException e)
		{
			Assert.fail("Exception during clear");
		}
	}

	@Test
	public void testCreateSnapshot()
		throws Exception
	{
		CreateJSONSnapshotAgent agent = new CreateJSONSnapshotAgent();
		agent.rootFolder = "/tmp";

		clearMap();
		
		WXSMap<String, Customer> map = utils.getCache(bmFarMap3.getName());

		for(int i = 0; i < 1000; ++i)
		{
			Customer c = new Customer();
			c.id = Integer.toString(i);
			c.dob = new Date(1945, 5, 3);
			c.firstName = "Billy";
			c.surname = "Newport";
			map.put(c.id, c);
		}
		
		AgentManager am = utils.getSessionForThread().getMap(bmFarMap3.getName()).getAgentManager();
		Object rawRC = am.callReduceAgent(agent);
		List<Integer> pids = (List<Integer>)rawRC; 
		Assert.assertEquals(bmFarMap3.getPartitionManager().getNumOfPartitions(), pids.size());
		for(int i = 0; i < pids.size(); ++i)
		{
			Assert.assertTrue(pids.contains(new Integer(i)));
		}
	}
	
	@Test
	public void testReadSnapshot()
		throws Exception
	{
		ReadJSONSnapshotAgent agent = new ReadJSONSnapshotAgent();
		agent.rootFolder = "/tmp";
		agent.gridName = utils.getObjectGrid().getName();
		agent.mapName = bmFarMap3.getName();

		clearMap();
		
		WXSMap<String, Customer> map = utils.getCache(bmFarMap3.getName());

		ObjectGrid perContainerClient = WXSUtils.connectClient("localhost:2809", "PerContainerGrid", "/objectgrid.xml");
		
		AgentManager am = perContainerClient.getSession().getMap("M.MAIN").getAgentManager();
		Object rawRC = am.callReduceAgent(agent);
		if(rawRC instanceof EntryErrorValue)
		{
			System.out.println("Failed " + rawRC.toString());
			Assert.fail();
		}
		List<Integer> pids = (List<Integer>)rawRC; 
		Assert.assertEquals(bmFarMap3.getPartitionManager().getNumOfPartitions(), pids.size());
		for(int i = 0; i < pids.size(); ++i)
		{
			Assert.assertTrue(pids.contains(new Integer(i)));
		}
		
		for(int i = 0; i < 1000; ++i)
		{
			Customer c = map.get(Integer.toString(i));
			Assert.assertNotNull(c);
			Assert.assertEquals(Integer.toString(i), c.id);
		}
		
	}
}
