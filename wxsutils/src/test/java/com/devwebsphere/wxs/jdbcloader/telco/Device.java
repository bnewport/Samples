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
package com.devwebsphere.wxs.jdbcloader.telco;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

import com.devwebsphere.jdbc.loader.ManyToOne;
import com.devwebsphere.jdbc.loader.Map;
import com.devwebsphere.jdbc.loader.OneToMany;
import com.devwebsphere.jdbc.loader.SecondaryIndex;
import com.devwebsphere.jdbc.loader.SimpleKey;

@Map(routable=true)
@SimpleKey(clazz=String.class, name="MDM", description="mdm")
@OneToMany(target=DeviceFeatures.class)
@Table(name="DEVICE")
public class Device implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8477295523646049637L;

	/**
	 * Define secondary index field
	 */
	
	@SecondaryIndex(mapName="PhoneNumberToMDM")
	@Column(name="PHONE_NUMBER")
	String phoneNumber;
	
	@Column(name="ACTIVE")
	boolean isActive;

	@ManyToOne(target=DeviceData.class)
	@Column(name="DEVICE_CODE")
	int deviceCode;
}
