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
package com.devwebsphere.mp.setup;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import com.devwebsphere.manualpartition.data.PartitionMapping;

/**
 * This is just a helper class to initialize a database table with some
 * initial values for testing.
 *
 */
public class InitializeDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
		throws PersistenceException 
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("myPU");
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();
		
		PartitionMapping symbol = new PartitionMapping("IBM", 0);
		em.persist(symbol);
		// SUNW is in partition 0
		symbol = new PartitionMapping("SUNW", 0);
		em.persist(symbol);
		
		// CSCO is in partition 1
		symbol = new PartitionMapping("CSCO", 1);
		em.persist(symbol);
		
		// ORCL and BEAS are in partition 2
		symbol = new PartitionMapping("ORCL", 2);
		em.persist(symbol);
		symbol = new PartitionMapping("BEAS", 2);
		em.persist(symbol);
		em.getTransaction().commit();
		em.close();
	}

}
