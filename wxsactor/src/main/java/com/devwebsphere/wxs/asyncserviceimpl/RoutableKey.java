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

import com.ibm.websphere.objectgrid.plugins.PartitionableKey;

public class RoutableKey implements Serializable, PartitionableKey 
{
	final class PartitionValue
	{
		public int hashCode()
		{
			return partitionId;
		}
	}
	// route to this partition
	public Object ibmGetPartition() 
	{
		return new PartitionValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + partitionId;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoutableKey other = (RoutableKey) obj;
		if (partitionId != other.partitionId)
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3469321770299848852L;
	int partitionId;
	String uuid;

	public RoutableKey() {}
	
	public RoutableKey(int pid, String uuid)
	{
		partitionId = pid;
		this.uuid = uuid;
	}
	
	public String getUUID()
	{
		return uuid;
	}
	
	public String toString()
	{
		return "RoutableKey<" + uuid + ",P" + partitionId + ">";
	}
}
