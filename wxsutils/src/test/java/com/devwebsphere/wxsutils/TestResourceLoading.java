package com.devwebsphere.wxsutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import junit.framework.Assert;


public class TestResourceLoading 
{
	@Test
	public void testLoading()
		throws IOException
	{
		Properties props = new Properties();
		// BN Modified to use getResourceAsStream instead of FileInputStream
		// BN so it works with property files in jars
		InputStream is = WXSUtils.class.getResourceAsStream("/wxsutils.properties");
		if(is == null)
		{
			is = WXSUtils.class.getResourceAsStream("/META-INF/wxsutils.properties");
		}
		if(is == null)
		{
			throw new FileNotFoundException("/[META-INF/]wxsutils.properties");
		}
		props.load(is);
		Assert.assertEquals(props.getProperty("cep"), "localhost:2809");
		is.close(); // BN added close
	}

}
