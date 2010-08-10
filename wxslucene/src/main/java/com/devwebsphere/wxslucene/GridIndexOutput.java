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
import java.util.logging.Logger;

import org.apache.lucene.store.IndexOutput;

import com.devwebsphere.wxs.fs.GridOutputStream;

/**
 * This seems to just wrapper GridOutputStream. Doesn't seem to add
 * a lot of value.
 * @author bnewport
 *
 */
public class GridIndexOutput extends IndexOutput 
{
	static Logger logger = Logger.getLogger(GridIndexOutput.class.getName());
	GridOutputStream stream;
	
	public GridIndexOutput(GridOutputStream s)
	{
		stream = s;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	@Override
	public long getFilePointer() 
	{
		return stream.getPosition();
	}

	@Override
	public long length() throws IOException {
		return stream.getMetaData().getActualSize();
	}

	@Override
	public void seek(long n) throws IOException 
	{
		stream.seek(n);
	}

	@Override
	public void writeByte(byte b) throws IOException {
		stream.write(b);
	}

	@Override
	public void writeBytes(byte[] b, int off, int len) throws IOException {
		stream.write(b, off, len);
	}

}
