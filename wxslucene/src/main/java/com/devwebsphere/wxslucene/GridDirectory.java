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
import org.apache.lucene.store.NIOFSDirectory;

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
	boolean isAsyncEnabled;
	boolean isCompressionEnabled = true;

	public final boolean isAsyncEnabled() {
		return isAsyncEnabled;
	}

	public final void setAsyncEnabled(boolean isAsyncEnabled) {
		this.isAsyncEnabled = isAsyncEnabled;
		logger.log(Level.INFO, "Async enabled = " + isAsyncEnabled + " for directory " + name);
	}

	public String getName()
	{
		return name;
	}
	
	public WXSUtils getWXSUtils()
	{
		return client;
	}
	
	/**
	 * This constructor obtains a grid connection using the wxsutils.properties file
	 * loads a File based directory at the specified path and then copies it in to
	 * the grid using a directory named after the file directory path.
	 * @param fileDirectoryName The location of the file index to copy.
	 * @see WXSUtils#getDefaultUtils()
	 */
	public GridDirectory(String fileDirectoryName)
	{
		try
		{
			WXSUtils c = WXSUtils.getDefaultUtils();
			init(c, fileDirectoryName);
			Directory diskDir = NIOFSDirectory.getDirectory(fileDirectoryName);
			Directory.copy(diskDir, this, false);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception creating GridDirectory from file ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This creates a named Grid Directory thats stored in the grid represented by
	 * the client parameter
	 * @param client
	 * @param directoryName
	 */
	public GridDirectory(WXSUtils client, String directoryName)
	{
		if(logger.isLoggable(Level.INFO))
		{
			logger.log(Level.INFO, "Creating GridDirectory: " + directoryName);
		}
		init(client, directoryName);
	}
	
	private void init(WXSUtils client, String directoryName)
	{
		this.client = client;
		dirMap = client.getCache(MapNames.DIR_MAP);
		name = directoryName;
		setLockFactory(new WXSLockFactory(client));
		getLockFactory().setLockPrefix(directoryName);
		// turn on compression by default
		setCompressionEnabled(true);
		// turn on async put by default
		setAsyncEnabled(true);
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
		GridFile file = new GridFile(this, pathname);
		GridOutputStream os = new GridOutputStream(client, file);
		if(isAsyncEnabled)
			os.enableAsyncWrite();
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
			GridFile file = new GridFile(this, pathname);
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
		GridFile file = new GridFile(this, pathname);
		return file.exists();
	}

	@Override
	public long fileLength(String pathname) throws IOException 
	{
		GridFile file = new GridFile(this, pathname);
		return file.length();
	}

	@Override
	public long fileModified(String pathname) throws IOException {
		GridFile file = new GridFile(this, pathname);
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
		GridFile file = new GridFile(this, pathname);
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
		GridFile file = new GridFile(this, pathname);
		file.setLastModified(System.currentTimeMillis());
	}

	public final boolean isCompressionEnabled() {
		return isCompressionEnabled;
	}

	public final void setCompressionEnabled(boolean isCompressionEnabled) {
		this.isCompressionEnabled = isCompressionEnabled;
		logger.log(Level.INFO, "Compression enabled = " + isCompressionEnabled + " for directory " + name);
	}

}
