package com.devwebsphere.wxsutils.multijob;
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


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;

public class Person implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2362806479824691807L;
	String firstName;
	String middleInitial;
	String surname;
	Date dateOfBirth;
	double creditLimit;
	
	public String toString()
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			Field[] fields = this.getClass().getDeclaredFields();
			sb.append("[[" + this.getClass().getName() + ":::");
			for(Field f : fields)
			{
				f.setAccessible(true);
				Object value = f.get(this);
				value = (value != null) ? value.toString() : "NULL";
				sb.append(f.getName() + "=" + value + "::");
			}
			sb.append("]]");
			return sb.toString();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public final String getFirstName() {
		return firstName;
	}
	public final void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public final String getMiddleInitial() {
		return middleInitial;
	}
	public final void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}
	public final String getSurname() {
		return surname;
	}
	public final void setSurname(String surname) {
		this.surname = surname;
	}
	public final Date getDateOfBirth() {
		return dateOfBirth;
	}
	public final void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public final double getCreditLimit() {
		return creditLimit;
	}
	public final void setCreditLimit(double creditLimit) {
		this.creditLimit = creditLimit;
	}
}
