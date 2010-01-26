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
package com.devwebsphere.wxssearch.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxssearch.Index;
import com.devwebsphere.wxssearch.IndexManager;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;

public class TestSubstringIndex
{
	static ObjectGrid clientOG;
	static WXSUtils utils;
	static IndexManager indexManager;
	static Index<Long> nameIndex;
	
	@BeforeClass
	static public void initGrid()
	{
		// creates an in JVM complete grid using these xml files for testing and returns
		// a client reference to it.
		clientOG = WXSUtils.startTestServer("Grid", "/search/testog.xml", "/search/testdep.xml");

		// create the utility library for the client grid
		utils = new WXSUtils(clientOG);
		
		// this should be placed in a static or similar device
		indexManager = new IndexManager(utils);
		// create the name index. Looking it up creates it.
		nameIndex = indexManager.getIndex("Name");
	}

	@Test
	public void preloadGrid()
		throws IOException
	{
        InputStream is = IndexManager.class.getResourceAsStream("/search/malenames.txt");
        BufferedReader fr = new BufferedReader(new InputStreamReader(is));

        long start = System.currentTimeMillis();
        long count = 0;
        
        Map<Long, String> entries = new HashMap<Long, String>();
        while (true)
        {
            String firstname = fr.readLine();
            if (firstname == null)
                break;
            InputStream sis = IndexManager.class.getResourceAsStream("/search/surnames.txt");
            BufferedReader sr = new BufferedReader(new InputStreamReader(sis));
            while (true)
            {
                String surname = sr.readLine();
                if (surname == null)
                    break;
                String s = firstname + " " + surname;
                entries.put(count, s);
                if (entries.size() > 1000)
                {
                	// insert the real records using a Long key
                	utils.putAll(entries, utils.getObjectGrid().getMap("RealRecords"));
                	
                	// update the index for each record also. The index just keeps
                	// a reference to the key in RealRecords
                	nameIndex.insert(entries);
                	entries = new HashMap<Long, String>();
                }
                count++;
            }
            break;
        }
        
        // flush any remaining entries, above loop just does every 1000
        // there will likely be some extra at the end, i.e. there wont
        // be a multiple of a 1000 names, it's unlikely.
        if (entries.size() > 0)
        {
        	nameIndex.insert(entries);
        	utils.putAll(entries, utils.getObjectGrid().getMap("RealRecords"));
        	entries = new HashMap<Long, String>();
        }
        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println(Long.toString(count) + " names inserted and indexed in " + duration + " seconds");
	}
	
	@Test
	public void testLookup()
	{
        Collection<Long> matches = null;
		for(int loop = 0; loop < 1000; ++loop)
        {
            long st_time = System.nanoTime();
            int numIterations = 1000;
            for (int i = 0; i < numIterations; ++i)
            {
            	// get the keys for the records whose 'name' contains EN
                matches = nameIndex.contains("EN");
            }
            double d = (System.nanoTime() - st_time) / 1000000000.0;
            System.out.println("Throughput is " + Double.toString(numIterations / d) + "/sec");
            System.out.println("Found " + matches.size());
        }
		// print out the records that matches
	    Map<Long, String> names = utils.getAll(matches, utils.getObjectGrid().getMap("RealRecords"));
	    for (String name : names.values())
	    {
	        System.out.println(name);
	    }
	}
}
