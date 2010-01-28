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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxssearch.Index;
import com.devwebsphere.wxssearch.IndexManager;
import com.devwebsphere.wxssearch.PrefixIndexImpl;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;

public class TestSubstringIndex
{
	static ObjectGrid clientOG;
	static WXSUtils utils;
	static IndexManager<TestBusinessObject, Long> indexManager;
	static Index<TestBusinessObject,Long> firstNameIndex;
	static Index<TestBusinessObject,Long> middleNameIndex;
	static Index<TestBusinessObject,Long> surnameIndex;
	static WXSMap realRecordsMap;
	
	@BeforeClass
	static public void initGrid()
	{
		// creates an in JVM complete grid using these xml files for testing and returns
		// a client reference to it.
		clientOG = WXSUtils.startTestServer("Grid", "/search/testog.xml", "/search/testdep.xml");

		// create the utility library for the client grid
		utils = new WXSUtils(clientOG);
		
		// this should be placed in a static or similar device
		indexManager = new IndexManager(utils, TestBusinessObject.class);
		// create the name index. Looking it up creates it.
		firstNameIndex = indexManager.getIndex("firstName");
		Assert.assertNotNull(firstNameIndex);
		middleNameIndex = indexManager.getIndex("middleName");
		Assert.assertNotNull(middleNameIndex);
		surnameIndex = indexManager.getIndex("surname");
		Assert.assertNotNull(surnameIndex);
		// create a map for the real records
		realRecordsMap = utils.getCache("RealRecords");
	}

	@Test
	public void testGeneratePrefix()
	{
		Set<String> results = PrefixIndexImpl.sgenerate("Billy");
		
		Set<String> correct = new HashSet<String>();
		correct.add("B"); correct.add("BI"); correct.add("BIL"); correct.add("BILL"); correct.add("BILLY");
		
		Assert.assertEquals(correct, results);
		
		results = PrefixIndexImpl.sgenerate("");
		Assert.assertTrue(results.isEmpty());
		
		results = PrefixIndexImpl.sgenerate("A");
		correct.clear();
		correct.add("A");
		Assert.assertEquals(correct, results);
	}
	
	@Test
	public void preloadGrid()
		throws IOException
	{
        InputStream is = IndexManager.class.getResourceAsStream("/search/malenames.txt");
        BufferedReader fr = new BufferedReader(new InputStreamReader(is));

        long start = System.currentTimeMillis();
        long count = 0;
        
        Map<Long, TestBusinessObject> entries = new HashMap<Long, TestBusinessObject>();
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
                
                TestBusinessObject bo = new TestBusinessObject();
                bo.firstName = firstname;
                bo.middleName = "";
                bo.surname = surname;
                entries.put(count, bo);
                if (entries.size() > 1000)
                {
                	// insert the real records using a Long key
                	realRecordsMap.putAll(entries);
                	
                	// update the index for each record also. The index just keeps
                	// a reference to the key in RealRecords
                	indexManager.indexAll(entries);
                	entries = new HashMap<Long, TestBusinessObject>();
                }
                count++;
            }
            if(count > 1000)
            	break;
        }
        
        // flush any remaining entries, above loop just does every 1000
        // there will likely be some extra at the end, i.e. there wont
        // be a multiple of a 1000 names, it's unlikely.
        if (entries.size() > 0)
        {
        	indexManager.indexAll(entries);
        	realRecordsMap.putAll(entries);
        	entries = new HashMap<Long, TestBusinessObject>();
        }
        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println(Long.toString(count) + " names inserted and indexed in " + duration + " seconds");
	}
	
	@Test
	public void testLookup()
	{
        Collection<Long> matches = null;
		for(int loop = 0; loop < 10; ++loop)
        {
            long st_time = System.nanoTime();
            int numIterations = 10;
            for (int i = 0; i < numIterations; ++i)
            {
            	// get the keys for the records whose 'name' contains EN
            	TestBusinessObject criteria = new TestBusinessObject();
            	criteria.firstName = "MES"; // anywhere
            	criteria.surname = "ALLEN"; // exact

            	matches = indexManager.searchMultipleIndexes(criteria, true);
            }
            double d = (System.nanoTime() - st_time) / 1000000000.0;
            System.out.println("Throughput is " + Double.toString(numIterations / d) + "/sec");
            System.out.println("Found " + matches.size());
        }
		// print out the records that matches
	    Map<Long, TestBusinessObject> bos = realRecordsMap.getAll(matches);
	    for (TestBusinessObject bo : bos.values())
	    {
	        System.out.println(bo.firstName + " " + bo.middleName + " " + bo.surname);
	    }
	}
}
