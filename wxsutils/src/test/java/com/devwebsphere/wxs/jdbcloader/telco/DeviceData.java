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

import com.devwebsphere.jdbc.loader.Map;
import com.devwebsphere.jdbc.loader.SimpleKey;

@Map(routable=false)
@SimpleKey(clazz=Integer.class, name="DEVICE_CODE", description="deviceCode") // deviceCode
@Table(name="DEVICEDATA")
public class DeviceData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5530464406362252012L;
	
	@Column(name="DEVICE_NAME")
	String deviceName;
	
	@Column(name="IS3G")
	boolean is3G;
	
	@Column(name="ISSMART")
	boolean isSmart;
	
	@Column(name="VENDOR")
	String vendor;
}
