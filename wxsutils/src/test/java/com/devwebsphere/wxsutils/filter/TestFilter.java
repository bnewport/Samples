//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxs.jdbcloader.Customer;
import com.devwebsphere.wxsutils.filter.path.PojoFieldPath;
import com.devwebsphere.wxsutils.filter.path.PojoPropertyPath;
import com.devwebsphere.wxsutils.utils.ClassSerializer;


public class TestFilter 
{
	static Customer c;
	
	@BeforeClass
	static public void setup()
	{
		c = new Customer();
		c.setFirstName("Billy"); c.setSurname("Newport");
		c.setId("1234");
	}
	
	@Test
	public void testValueExtractor()
	{
		PojoPropertyPath v = new PojoPropertyPath("FirstName");
		Assert.assertEquals(c.getFirstName(), v.get(c));
	}
	
	@Test
	public void testEquals()
	{
		PojoPropertyPath v = new PojoPropertyPath("FirstName");
		
		EQFilter ef = new EQFilter(v, "Billy");
		Assert.assertTrue(ef.filter(c));
		NotFilter nf = new NotFilter(ef);
		Assert.assertFalse(nf.filter(c));
		
		NEQFilter nef = new NEQFilter(v, "Bobby");
		Assert.assertTrue(nef.filter(c));
	}
	
	@Test
	public void testQB()
	{
		ValuePath fn = new PojoPropertyPath("FirstName");
		ValuePath sn = new PojoFieldPath("surname");
		
		FilterBuilder fb = new FilterBuilder();
		Filter f = fb.and(fb.eq(fn, "Billy"), fb.eq(sn, "Newport"));
		
		Assert.assertEquals(f.filter(c), true);
	}
	@Test
	public void testSerializer()
		throws IOException
	{
		ValuePath fn = new PojoPropertyPath("FirstName");
		ValuePath sn = new PojoFieldPath("surname");
		
		FilterBuilder fb = new FilterBuilder();
		Filter f = fb.and(fb.eq(fn, "Billy"), fb.eq(sn, "Newport"));
		
		ClassSerializer serializer = new ClassSerializer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream dos = new ObjectOutputStream(bos);
		serializer.writeObject(dos, f);
		dos.close();
		bos.close();
		byte[] rawBytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(rawBytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object o = serializer.readObject(ois);
		Filter copyf = (Filter)f;
	}
}
