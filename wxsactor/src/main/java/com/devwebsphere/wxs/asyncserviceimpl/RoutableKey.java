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
package com.devwebsphere.wxs.asyncserviceimpl;

import java.io.Serializable;

import com.devwebsphere.wxsutils.PartitionKey;

public class RoutableKey extends PartitionKey<String> implements Serializable {
	private static final long serialVersionUID = -3469321770299848852L;

	public RoutableKey(int pid, String uuid) {
		super(Integer.valueOf(pid), uuid);
	}

	public String getUUID() {
		return getKey();
	}

	public String toString() {
		return "RoutableKey<" + getKey() + ",P" + ibmGetPartition() + ">";
	}
}
