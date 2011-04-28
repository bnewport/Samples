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
package com.devwebsphere.wxs.jdbcloader;

import java.io.Serializable;
import java.sql.Date;

import com.ibm.websphere.projector.annotations.Id;

public class Customer implements Serializable
{
	@Id
	public String id;
	public String firstName;
	public String surname;
	public Date dob;
	public final String getId() {
		return id;
	}
	public final String getFirstName() {
		return firstName;
	}
	public final String getSurname() {
		return surname;
	}
	public final Date getDob() {
		return dob;
	}
	public final void setId(String id) {
		this.id = id;
	}
	public final void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public final void setSurname(String surname) {
		this.surname = surname;
	}
	public final void setDob(Date dob) {
		this.dob = dob;
	}
}
