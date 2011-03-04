package com.devwebsphere.wxsutils.filter;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxs.jdbcloader.Customer;
import com.devwebsphere.wxsutils.filter.path.PojoFieldPath;
import com.devwebsphere.wxsutils.filter.path.PojoPropertyPath;


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
		FilterBuilder fb = new FilterBuilder();
		ValuePath fn = new PojoPropertyPath("FirstName");
		ValuePath sn = new PojoFieldPath("surname");
		
		Filter f = fb.and(fb.eq(fn, "Billy"), fb.eq(sn, "Newport"));
		
		Assert.assertEquals(f.filter(c), true);
	}
}
