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
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import com.devwebsphere.jdbc.loader.CompositeKey;
import com.devwebsphere.jdbc.loader.Map;

@Map(routable=false)
@CompositeKey(clazz=DeviceFeature_PK.class, description="featureKey")
@Table(name="FEATURES")
public class DeviceFeatures implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7789404635910271474L;
	
	@Column(name="ACTIVE_DATE")
	Date activeFrom;

}
