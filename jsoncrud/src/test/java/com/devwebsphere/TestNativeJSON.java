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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.devwebsphere.jsoncrud.JSMap;
import com.google.gson.Gson;


public class TestNativeJSON 
{
	@Test
	public void testSerializePerson()
	{
		Gson gson = new Gson();
		
		Person p = new Person();
		p.setCreditLimit(10000.0);
		p.setFirstName("Pedro");
		p.setSurname("Platinum");
		p.setMiddleInitial("A");
		Calendar cal = Calendar.getInstance();
		cal.set(1971, 5, 1);
		p.setDateOfBirth(cal.getTime());
		
		String str = gson.toJson(p);
		System.out.println(str);
	}
	
	@Test
	public void testInflatePerson()
	{
		// {"firstName":"Pedro","middleInitial":"A","surname":"Platinum","dateOfBirth":"Mar 28, 3868 12:00:00 AM","creditLimit":10000.0}
		String input = "{\"firstName\":\"Pedro\",\"middleInitial\":\"A\",\"surname\":\"Platinum\",\"dateOfBirth\":\"Mar 28, 3868 12:00:00 AM\",\"creditLimit\":10000.0}";
		
		Gson gson = new Gson();
		Person p = gson.fromJson(input, Person.class);
		System.out.println(p.toString());
	}
	
	@Test 
	public void testSerializeString()
	{
		Gson gson = new Gson();
		System.out.println(gson.toJson("bnewport", String.class));
	}
	
	@Test
	public void testInflateString()
	{
		String input = "\"bnewport\"";
		
		Gson gson = new Gson();
		String s = gson.fromJson(input, String.class);
		System.out.println(s.toString());
	}
	
	@Test 
	public void testSerializeInteger()
	{
		Gson gson = new Gson();
		System.out.println(gson.toJson(new Integer(56)));
	}
	
	@Test
	public void testInflateInteger()
	{
		String input = "712";
		
		Gson gson = new Gson();
		Integer i = gson.fromJson(input, Integer.class);
		System.out.println(i.toString());
	}
	
	@Test
	public void testConversion()
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
