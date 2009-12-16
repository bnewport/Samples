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
package com.devwebsphere.manualpartition.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class is used as the value for the PartitionMap map. It's linked to a database table
 * using the WXS JPALoader.
 * @author bnewport
 *
 */
@Entity
@Table(name="mapping")
public class PartitionMapping implements Serializable, Cloneable
{
	static final public String MAP = "PartitionMap";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9134164894385327447L;

	String key;
	int partition;
	
	@Id
	@Column(name="NAME")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	@Column(name="PARTITION")
	public int getPartition() {
		return partition;
	}
	public void setPartition(int partition) {
		this.partition = partition;
	}

	public PartitionMapping()
	{
		
	}
	
	public PartitionMapping(String name, int partition)
	{
		setKey(name);
		setPartition(partition);
	}

	/**
	 * Diagnostic aid
	 */
	public String toString()
	{
		return "PM<" + getKey() + ", " + getPartition() + ">";
	}
	
	/**
	 * Performance aid, avoids the need for Serialization to copy the object.
	 */
	public Object clone()
	{
		PartitionMapping copy = new PartitionMapping(getKey(), getPartition());
		return copy;
	}
}
