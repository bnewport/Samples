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
package com.devwebsphere.wxs.fs;

import java.io.Serializable;

public class FileMetaData implements Serializable 
{
	static public final short O_READ = 1;
	static public final short O_WRITE = 2;
	static public final short O_DIRECTORY = 4;
	static public final short O_EXEC = 8;
	static public final short O_ZIP = 16;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5719374758924704175L;
	String name;
	short permissions; // same as unix 777, 9 bits
	long lastModifiedTime;
	long actualSize;
	
	public String toString()
	{
		if(isDirectory())
			return "GMD(DIR:" + name + ":P" + permissions + ":LMT" + lastModifiedTime + ")";
		else
			return "GMD(FILE:" + name + ":P" + permissions + ":LMT" + lastModifiedTime + ":SZ" + actualSize + ")";
	}
	
	public final String getName() {
		return name;
	}
	public final void setName(String name) {
		this.name = name;
	}
	public final short getPermissions() {
		return permissions;
	}
	public final void setPermissions(short permissions) {
		this.permissions = permissions;
	}
	public final long getLastModifiedTime() {
		return lastModifiedTime;
	}
	public final void setLastModifiedTime(long lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	public final long getActualSize() {
		return actualSize;
	}
	public final void setActualSize(long actualSize) {
		this.actualSize = actualSize;
	}

	public boolean canRead() {
		return (permissions & O_READ) != 0;
	}

	public void setReadOnly()
	{
		permissions |= O_READ;
	}

	public void setWriteable()
	{
		permissions |= O_WRITE;
	}
	
	public boolean canWrite() {
		// TODO Auto-generated method stub
		return (permissions & O_WRITE) != 0;
	}

	public void setExecutable()
	{
		permissions |= O_EXEC;
	}
	
	public boolean isExecutable() {
		// TODO Auto-generated method stub
		return (permissions & O_EXEC) != 0;
	}

	public void setCompressed()
	{
		permissions |= O_ZIP;
	}
	
	public boolean isCompressed() {
		// TODO Auto-generated method stub
		return (permissions & O_ZIP) != 0;
	}

	public boolean isDirectory() {
		// TODO Auto-generated method stub
		return (permissions & O_DIRECTORY) != 0;
	}
}
