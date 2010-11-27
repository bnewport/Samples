package com.devwebsphere.wxs.jdbcloader;

import java.io.Serializable;
import java.sql.Date;

import com.ibm.websphere.projector.annotations.Id;

public class Customer implements Serializable
{
	@Id
	String id;
	String firstName;
	String surname;
	Date dob;
}
