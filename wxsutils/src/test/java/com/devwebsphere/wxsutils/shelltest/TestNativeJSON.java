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
package com.devwebsphere.wxsutils.shelltest;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.devwebsphere.wxsutils.shell.JSMap;


public class TestNativeJSON 
{
	@Test
	public void testSerializePerson()
		throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		
		Person p = new Person();
		p.setCreditLimit(10000.0);
		p.setFirstName("Pedro");
		p.setSurname("Platinum");
		p.setMiddleInitial("A");
		Calendar cal = Calendar.getInstance();
		cal.set(1971, 5, 1);
		p.setDateOfBirth(cal.getTime());
		
		String str = mapper.writeValueAsString(p);
		System.out.println(str);
	}
	
	@Test
	public void testInflatePerson()
		throws IOException
	{
		String input = "{\"firstName\":\"Pedro\",\"middleInitial\":\"A\",\"surname\":\"Platinum\",\"dateOfBirth\":44661454547,\"creditLimit\":10000.0}";

		ObjectMapper mapper = new ObjectMapper();
		Person p = mapper.readValue(input, Person.class);
		System.out.println(p.toString());
	}
	
	@Test 
	public void testSerializeString()
	throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString("bnewport"));
		System.out.println(mapper.writeValueAsString("\"bnewport\""));
	}
	
	@Test
	public void testInflateString()
	throws IOException
	{
		String input = "\"bnewport\"";

		ObjectMapper mapper = new ObjectMapper();
		String s = mapper.readValue(input, String.class);
		System.out.println(s.toString());
	}
	
	@Test 
	public void testSerializeInteger()
	throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(new Integer(56)));
	}
	
	@Test
	public void testInflateInteger()
	throws IOException
	{
		String input = "712";
		
		ObjectMapper mapper = new ObjectMapper();
		Integer i = mapper.readValue(input, Integer.class);
		System.out.println(i.toString());
	}
	
	@Test
	public void testConversion()
	throws IOException
	{
		String s = JSMap.convertToJSON("bnewport");
		System.out.println(s);
		Map m = new HashMap();
		m.put("fn", "billy"); m.put("sn", "newport");
		m.put("age", 32);
		s = JSMap.convertToJSON(m);
		System.out.println(s);
		Context ctxt = Context.enter();
		Scriptable scope = ctxt.initStandardObjects();
		s = "x = {\"f\":\"billy\",\"s\":\"newport\"}";
		s = "x = \"billy\"";
		Object o = ctxt.evaluateString(scope, s, "Billy", 1, null);
		System.out.println(o.toString());
	}
}
