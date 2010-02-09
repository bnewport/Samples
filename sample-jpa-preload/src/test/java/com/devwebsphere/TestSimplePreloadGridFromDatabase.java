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
package com.devwebsphere;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.samplepreload.data.Customer;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.mysql.jdbc.Driver;

/**
 * This test case is intended as a sample to show how to preload records from
 * a database table (Customer) into a grid.
 *
 */
public class TestSimplePreloadGridFromDatabase 
{
	static ObjectGrid clientOG;
	static WXSUtils utils;
	
	// WXSUtils wrapper for using ObjectMaps in extreme scale
	// adds generics and bulk operations
	static WXSMap<Long,Customer> customerMap;
	static EntityManagerFactory emf;
	
	@BeforeClass
	static public void initGrid()
		throws SQLException
	{
		// creates an in JVM complete grid using these xml files for testing and returns
		// a client reference to it.
		clientOG = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");

		// create the utility library for the client grid
		utils = new WXSUtils(clientOG);
		
		// create a map for the real records
		customerMap = utils.getCache("Customer");
		// create a JPA EM on the client because we use it to read the database table on the client
		// to fetch the records to preload in the grid.
		Driver driver = new Driver();
		DriverManager.registerDriver(driver);
		emf = Persistence.createEntityManagerFactory("myPU");
	}
	
	/**
	 * This reads all the records matching a query using JPA from a database and then bulk inserts them in to
	 * the grid.
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test
	public void testPreloadGrid()
		throws IOException, SQLException
	{
		System.out.println("Test preload objects from a database table in to the grid");
		EntityManager em = emf.createEntityManager();
		
        long start = System.currentTimeMillis();
        long count = 0;

        // start a database transaction
        em.getTransaction().begin();
        Map<Long, Customer> entries = new HashMap<Long, Customer>();
        
        // make sure the grid map is empty, we're using insertAll later.
        customerMap.clear();
        
        // change this query to select the records you want to preload.
        Query allQuery = em.createQuery("SELECT c FROM Customer c");
        List<Customer> list = allQuery.getResultList();
        
        // we want at least a 1000 entries per partition when bulk loading. When insertAll is called then
        // the bulk map is split in to a bucket per partition. This bucket per partition needs to be
        // an optimal size. A 1000 entries per bucket is a good start
        // so fetch the number of partitions and multiple by a 1000
        int batchSize = utils.getObjectGrid().getMap("Customer").getPartitionManager().getNumOfPartitions() * 1000;
        
        // add each customer record in to a map with the key for it
        for(Customer c : list)
        {
            entries.put(c.getId(), c);
            // flush to grid every batchSize entries
            if (entries.size() > batchSize)
            {
            	// insert the real records using a Long key
            	customerMap.insertAll(entries);
            	// reset the batch otherwise, we'd insert the same entries multiple times
            	entries.clear();
            	System.out.println("Inserted " + count);
            }
            count++;
        }
        
        // flush any remaining entries, above loop just does every X
        // there will likely be some extra at the end, i.e. there wont
        // be a multiple of a X names, it's unlikely.
        if (entries.size() > 0)
        {
        	customerMap.insertAll(entries);
        	entries.clear();
        }
        
        // finish the database transaction
    	em.getTransaction().commit();
        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println(Long.toString(count) + " names inserted and indexed in " + duration + " seconds");
	}

	/**
	 * This checks we can look up certain records.
	 */
	@Test
	public void testLookupFromGrid()
	{
		for(long customerId = 0; customerId < 50000; ++customerId)
		{
			Customer c = customerMap.get(customerId);
			System.out.println("Found " + c.toString() + " for key " + customerId);
		}
	}
}
