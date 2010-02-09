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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.samplepreload.data.Customer;
import com.devwebsphere.wxssearch.IndexManager;
import com.mysql.jdbc.Driver;

/**
 * This test case is intended to be a sample showing how to read records from
 * a set of files and insert them in the database
 *
 */
public class TestPreloadDatabaseFromFile 
{
	static EntityManagerFactory emf;
	
	@BeforeClass
	static public void initJPA()
		throws SQLException
	{
		Driver driver = new Driver();
		DriverManager.registerDriver(driver);
		emf = Persistence.createEntityManagerFactory("myPU");
	}
	
	/**
	 * This will insert entries into the database using JPA. This assumes
	 * the database table is empty to start. OpenJPA should automatically
	 * create the table if the table isn't present.
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test
	public void testPreloadDatabase()
		throws IOException, SQLException
	{
		System.out.println("TEstPreload");
		EntityManager em = emf.createEntityManager();
		
        InputStream is = IndexManager.class.getResourceAsStream("/search/malenames.txt");
        BufferedReader fr = new BufferedReader(new InputStreamReader(is));

        long start = System.currentTimeMillis();
        long count = 0;
        
        em.getTransaction().begin();
        
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
                Customer c = new Customer(count, firstname, "", surname);
                em.persist(c);
                count++;
                if(count % 5000 == 0)
                {
                	em.getTransaction().commit();
                	em.getTransaction().begin();
                	System.out.println("Inserted record #" + count);
                }
            }
        }
        
       	em.getTransaction().commit();
        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println(Long.toString(count) + " names inserted and indexed in " + duration + " seconds");
	}

}
