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

public interface MapNames 
{
	/**
	 * This map stores blocks for the 'file'. The key is the file name + block index
	 */
	public String CHUNK_MAP = "ChunkMap";
	/**
	 * This maps stores meta data such as actual file size about a file
	 */
	public String MD_MAP = "FileMetaData";
	/**
	 * Directory contents such as list of files are stored here
	 */
	public String DIR_MAP = "DirectoryMetaData";
	/**
	 * Locks for specific files are stored here
	 */
	public String LOCK_MAP = "LockMap";
}
