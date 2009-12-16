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

import java.sql.Connection;
import java.sql.DriverManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import junit.framework.TestCase;

import org.junit.Test;

import com.devwebsphere.manualpartition.data.PartitionMapping;
import com.mysql.jdbc.Driver;



public class TestJPA extends TestCase
{
	@Test
	public void test_OpenCloseJDBC()
	{
		try
		{
			DriverManager.registerDriver(new Driver());
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost/TEST");
		}
		catch(Exception e)
		{
			fail("Exception " + e.toString());
		}
	}
	
	@Test
	public void test_OpenCloseJPA()
	{
		try
		{
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("myPU");
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();
			PartitionMapping p = em.find(PartitionMapping.class, "BAD_KEY");
			if(p != null)
				fail("Found unknown key");
			em.getTransaction().commit();
		}
		catch(PersistenceException e)
		{
			fail("Exception" + e.toString());
		}
	}

}
