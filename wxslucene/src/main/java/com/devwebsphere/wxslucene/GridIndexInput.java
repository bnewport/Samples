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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.store.IndexInput;

import com.devwebsphere.wxs.fs.GridInputStream;

public class GridIndexInput extends IndexInput 
{
	static Logger logger = Logger.getLogger(GridIndexOutput.class.getName());
	GridInputStream stream;

	public GridIndexInput(GridInputStream s)
	{
		stream = s;
	}
	
	@Override
	public void close() throws IOException 
	{
		stream.close();
	}

	@Override
	public long getFilePointer() 
	{
		logger.log(Level.WARNING, "getFilePointer called for " + stream.toString());
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long length() 
	{
		return stream.getMetaData().getActualSize();
	}

	@Override
	public byte readByte() throws IOException {
		return (byte)stream.read();
	}

	@Override
	public void readBytes(byte[] b, int off, int len) throws IOException 
	{
		stream.read(b, off, len);
	}

	@Override
	public void seek(long n) throws IOException 
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "GridIndexInput " + stream.toString() + ":seek " + n);
		}
		stream.seek(n);
	}

}
