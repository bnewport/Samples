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
package com.devwebsphere.samplepreload.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CUSTOMER")
public class Customer implements Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4848845835354621287L;
	long id;
	String firstName;
	String middleName;
	String surname;

	@Id
	@Column(name="ID")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Column(name="FIRSTNAME")
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	@Column(name="MIDDLENAME")
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	@Column(name="SURNAME")
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	public Customer()
	{
	}
	
	public Customer(long id, String f, String m, String s)
	{
		setId(id);
		setFirstName(f);
		setMiddleName(m);
		setSurname(s);
	}

	/**
	 * Diagnostic aid
	 */
	public String toString()
	{
		return "Customer<" + getFirstName() + ", " + getMiddleName() + ", " + getSurname() +">";
	}
	
	/**
	 * Performance aid, avoids the need for Serialization to copy the object.
	 * Recommended
	 */
	public Object clone()
	{
		Customer copy = new Customer(getId(), getFirstName(), getMiddleName(), getSurname());
		return copy;
	}
}
