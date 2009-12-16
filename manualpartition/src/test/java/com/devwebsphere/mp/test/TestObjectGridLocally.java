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
package com.devwebsphere.mp.test;

import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;


import com.devwebsphere.manualpartition.data.PartitionMapping;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;


public class TestObjectGridLocally extends TestCase
{
	@Test
	public void test_TestJPALoader()
	{
		try
		{
			URL og_xml = TestObjectGridLocally.class.getResource("/META-INF/objectgrid.xml");
			ObjectGrid og = ObjectGridManagerFactory.getObjectGridManager().createObjectGrid("Grid", og_xml);
			Session s = og.getSession();
			ObjectMap pmap = s.getMap(PartitionMapping.MAP);
			PartitionMapping mapping = (PartitionMapping)pmap.get("IBM");
			assertNotNull(mapping);
			System.out.println(mapping);
		}
		catch(Exception e)
		{
			fail("Exception: " + e.toString());
		}
	}

}
