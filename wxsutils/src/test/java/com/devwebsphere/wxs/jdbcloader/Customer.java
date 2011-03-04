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
