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
package com.devwebsphere.wxslucene;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import com.devwebsphere.wxs.fs.GridFile;
import com.devwebsphere.wxs.fs.GridInputStream;
import com.devwebsphere.wxs.fs.GridOutputStream;
import com.devwebsphere.wxs.fs.MapNames;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

public class GridDirectory extends Directory 
{
	static Logger logger = Logger.getLogger(GridDirectory.class.getName());
	
	WXSUtils client;
	WXSMap<String, Set<String>> dirMap;
	String name;

	public GridDirectory(WXSUtils client, String directoryName)
	{
		if(logger.isLoggable(Level.INFO))
		{
			logger.log(Level.INFO, "Creating GridDirectory: " + directoryName);
		}
		this.client = client;
		dirMap = client.getCache(MapNames.DIR_MAP);
		name = directoryName;
		setLockFactory(new WXSLockFactory(client));
		getLockFactory().setLockPrefix(directoryName);
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public IndexOutput createOutput(String pathname) throws IOException 
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "Creating IndexOutput: " + pathname + " in directory " + name);
		}
		Set<String> files = getFiles();
		if(!files.contains(pathname))
		{
			if(logger.isLoggable(Level.FINE))
				logger.log(Level.FINE, "Adding " + pathname + " to directory " + name);
			files.add(pathname);
			dirMap.put(name, files);
		}
		GridFile file = new GridFile(client, pathname);
		GridOutputStream os = new GridOutputStream(client, file);
		return new GridIndexOutput(os);
	}

	@Override
	public void deleteFile(String pathname) throws IOException 
	{
		if(logger.isLoggable(Level.INFO))
		{
			logger.log(Level.INFO, "GridDirectory#delete: " + pathname);
		}
		Set<String> files = getFiles();
		if(files.contains(pathname))
		{
			files.remove(pathname);
			GridFile file = new GridFile(client, pathname);
			file.delete();
			dirMap.put(name, files);
		}
		else
		{
			logger.log(Level.SEVERE, "Attempt to delete unknown file " + pathname + " from directory " + name);
		}
	}

	@Override
	public boolean fileExists(String pathname) throws IOException 
	{
		GridFile file = new GridFile(client, pathname);
		return file.exists();
	}

	@Override
	public long fileLength(String pathname) throws IOException 
	{
		GridFile file = new GridFile(client, pathname);
		return file.length();
	}

	@Override
	public long fileModified(String pathname) throws IOException {
		GridFile file = new GridFile(client, pathname);
		return file.lastModified();
	}

	Set<String> getFiles()
	{
		Set<String> rc = dirMap.get(name);
		if(rc == null)
		{
			rc = new HashSet<String>();
		}
		return rc;
	}
	
	@Override
	public String[] list() throws IOException 
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "GridDirectory#list: " + name);
		}
		Set<String> files = getFiles();

		String[] rc = new String[files.size()];
		files.toArray(rc);
		if(logger.isLoggable(Level.FINER))
			logger.log(Level.FINER, "GridDirectory#list: (" + name +")" + " = " + files.toString());
		return rc;
	}

	@Override
	public IndexInput openInput(String pathname) throws IOException 
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "GridDirectory#openInput: " + pathname);
		}
		GridFile file = new GridFile(client, pathname);
		GridInputStream is = new GridInputStream(client, file);
		return new GridIndexInput(is);
	}

	@Override
	public void renameFile(String arg0, String arg1) throws IOException 
	{
		throw new RuntimeException("rename not implemented");
	}

	@Override
	public void touchFile(String pathname) throws IOException 
	{
		GridFile file = new GridFile(client, pathname);
		file.setLastModified(System.currentTimeMillis());
	}

}
