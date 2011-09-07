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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.devwebsphere.wxssearch.IndexManager;
import com.devwebsphere.wxsutils.ByteArrayKey;

public class TestDigestCollisions 
{
	/**
	 * This just returns the digest we will use to generate hashes. SHA and MD5 are very good
	 * hash functions. MD5 is about 40% faster than SHA so we'll use MD5
	 * @return
	 */
	public static MessageDigest getDigest()
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return digest;
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testDigestCollisions()
		throws IOException
	{
        InputStream is = IndexManager.class.getResourceAsStream("/search/malenames.txt");
        BufferedReader fr = new BufferedReader(new InputStreamReader(is));
        
        Map<byte[], String> firstNames = new HashMap<byte[], String>();
        Map<byte[], String> names = new HashMap<byte[], String>();
        
        MessageDigest digest = getDigest();
        
        while (true)
        {
            String firstname = fr.readLine();
            if (firstname == null)
                break;
            byte[] hash = digest.digest(firstname.getBytes());
            
            // verify ByteArrayKey really works. Important methods are equals and hashCode
            ByteArrayKey key1 = new ByteArrayKey(hash);
            byte[] hash2 = digest.digest(firstname.getBytes());
            ByteArrayKey key2 = new ByteArrayKey(hash2);
            Assert.assertFalse(firstNames.containsKey(hash));
            Assert.assertEquals(key1, key2);
            Assert.assertEquals(key1.hashCode(), key2.hashCode());
            firstNames.put(hash, firstname);
            InputStream sis = IndexManager.class.getResourceAsStream("/search/surnames.txt");
            BufferedReader sr = new BufferedReader(new InputStreamReader(sis));
            while (true)
            {
                String surname = sr.readLine();
                if (surname == null)
                    break;

                String name = firstname + " " + surname;
                hash = digest.digest(name.getBytes());
                Assert.assertFalse(names.containsKey(hash));
                names.put(hash, name);
            }
        }
        Map<byte[], String> surnames = new HashMap<byte[], String>();
        InputStream sis = IndexManager.class.getResourceAsStream("/search/surnames.txt");
        BufferedReader sr = new BufferedReader(new InputStreamReader(sis));
        while (true)
        {
            String surname = sr.readLine();
            if (surname == null)
                break;

            byte[] hash = digest.digest(surname.getBytes());
            Assert.assertFalse(names.containsKey(hash));
            names.put(hash, surname);
        }
	}
}
