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
package com.devwebsphere;

import java.io.IOException;
import java.util.Random;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxs.fs.GridFile;
import com.devwebsphere.wxs.fs.GridInputStream;
import com.devwebsphere.wxs.fs.GridOutputStream;
import com.devwebsphere.wxslucene.GridDirectory;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;

public class TestGridFile 
{
	static ObjectGrid testGrid;
	static WXSUtils utils;
	static GridDirectory dir;
	
	@BeforeClass
	static public void initialize()
		throws ObjectGridException
	{
		testGrid = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");
		
		utils = new WXSUtils(testGrid);
		dir = new GridDirectory(utils, "test");
	}
	
	@Test
	public void testWriteReadFileInBytesRandom()
		throws IOException
	{
		
		GridFile file = new GridFile(dir, "test_random");
		
		GridOutputStream gos = new GridOutputStream(utils, file);
		
		int numBytes = 100000;
		for(int i = 0; i < numBytes; ++i)
		{
			int v = i % 256;
			byte b = (byte)(v);
			gos.write(v);
		}
		
		gos.close();
		
		GridInputStream gis = new GridInputStream(utils, file);

		for(int i = 0; i < numBytes; ++i)
		{
			int by = gis.read();
			if(by < 0) by += 256;
			Assert.assertEquals(i % 256, by);
		}
		
		Random r = new Random();
		byte[] data = new byte[20];
		for(int i = 0; i < numBytes; ++i)
		{
			int testPosition = Math.abs(r.nextInt()) % (numBytes - data.length);
			gis.seek(testPosition);
			readFully(gis, data);
			for(int j = 0; j < data.length; ++j)
			{
				int expected = (testPosition + j) % 256;
				int actual = data[j];
				if(actual < 0) actual += 256;
				Assert.assertEquals(expected, actual);
			}
		}
		
		// try bigger blocks
		data = new byte[file.getParent().getBlockSize() * 2 + 3];
		for(int i = 0; i < numBytes; ++i)
		{
			int testPosition = Math.abs(r.nextInt()) % (numBytes - data.length);
			gis.seek(testPosition);
			readFully(gis, data);
			for(int j = 0; j < data.length; ++j)
			{
				int expected = (testPosition + j) % 256;
				int actual = data[j];
				if(actual < 0) actual += 256;
				Assert.assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void testWriteReadFile()
		throws IOException
	{
		
		GridFile file = new GridFile(dir, "test");
		
		GridOutputStream gos = new GridOutputStream(utils, file);
		
		String pattern = "My name is Billy Newport";
		String delimiter = ":";
		StringBuilder b = new StringBuilder();
		
		for(int i = 0; i < 1; ++i)
		{
			b.append(pattern);
			b.append(delimiter);
		}
		
		String originalString = b.toString();
		byte[] bytes = originalString.getBytes();
		
		gos.write(bytes);
		gos.flush();
		
		GridInputStream gis = new GridInputStream(utils, file);

		byte[] copy = new byte[bytes.length];
		int toGo = copy.length;
		int offset = 0;
		while(toGo > 0)
		{
			int count = gis.read(copy, offset, toGo);
			toGo -= count;
			offset += count;
			Assert.assertFalse(count == 0);
		}
		
		String copyString = new String(copy);
		
		Assert.assertEquals(originalString, copyString);
	}
	@Test
	public void testWriteReadFileInBytes()
		throws IOException
	{
		
		GridFile file = new GridFile(dir, "test2");
		
		GridOutputStream gos = new GridOutputStream(utils, file);
		
		String pattern = "My name is Billy Newport";
		String delimiter = ":";
		StringBuilder b = new StringBuilder();
		
		for(int i = 0; i < 1; ++i)
		{
			b.append(pattern);
			b.append(delimiter);
		}
		
		String originalString = b.toString();
		byte[] bytes = originalString.getBytes();

		for(int i = 0; i < bytes.length; ++i)
		{
			gos.write(bytes[i]);
			if(i % 20 == 0)
				gos.flush();
		}
		
		gos.close();
		gos.flush();
		
		GridInputStream gis = new GridInputStream(utils, file);

		byte[] copy = new byte[bytes.length];
		int toGo = copy.length;
		int offset = 0;
		while(toGo > 0)
		{
			int count = gis.read(copy, offset, toGo);
			toGo -= count;
			offset += count;
			Assert.assertFalse(count == 0);
		}
		
		String copyString = new String(copy);
		
		Assert.assertEquals(originalString, copyString);
	}
	@Test
	public void testWriteReadFileInBytes2()
		throws IOException
	{
		
		GridFile file = new GridFile(dir, "test3");
		
		GridOutputStream gos = new GridOutputStream(utils, file);
		
		String pattern = "My name is Billy Newport";
		String delimiter = ":";
		StringBuilder b = new StringBuilder();
		
		for(int i = 0; i < 1; ++i)
		{
			b.append(pattern);
			b.append(delimiter);
		}
		
		String originalString = b.toString();
		byte[] bytes = originalString.getBytes();

		for(int i = 0; i < bytes.length; ++i)
		{
			gos.write(bytes[i]);
			if(i % 20 == 0)
				gos.flush();
		}
		
		gos.close();
		gos.flush();
		
		GridInputStream gis = new GridInputStream(utils, file);

		byte[] copy = new byte[bytes.length];
		int toGo = copy.length;
		int offset = 0;
		for(int i = 0; i < copy.length; ++i)
		{
			int by = gis.read();
			copy[i] = (byte)by;
		}
		
		String copyString = new String(copy);
		
		Assert.assertEquals(originalString, copyString);
	}
	
	static void readFully(GridInputStream gis, byte[] copy)
		throws IOException
	{
		int toGo = copy.length;
		int offset = 0;
		while(toGo > 0)
		{
			int c = gis.read(copy, offset, toGo);
			toGo -= c;
			offset += c;
		}
	}
}
