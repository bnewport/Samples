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

import com.ibm.websphere.objectgrid.plugins.PartitionableKey;

public class DeviceFeature_PK implements Serializable, PartitionableKey
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6116383023304988282L;

	@Column(name="MDM")
	String mdm;
	
	@Column(name="FEATURE_CODE")
	String featureCode;
	
	public Object ibmGetPartition() 
	{
		return mdm;
	}
}
