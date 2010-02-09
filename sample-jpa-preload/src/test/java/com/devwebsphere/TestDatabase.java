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

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import com.devwebsphere.samplepreload.data.Customer;
import com.mysql.jdbc.Driver;

public class TestDatabase 
{
	@Test
	public void testInsert()
		throws SQLException
	{
		Driver driver = new Driver();
		DriverManager.registerDriver(driver);
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("myPU");
		EntityManager em = emf.createEntityManager();
		
		Customer c = new Customer(0, "Jack", "Alfonso", "Bauer");
		
		em.getTransaction().begin();
		em.persist(c);
		em.getTransaction().commit();
	}
}
