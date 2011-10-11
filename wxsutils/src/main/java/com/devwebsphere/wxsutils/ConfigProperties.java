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
package com.devwebsphere.wxsutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This is a class to read the wxsutils.properties file. This class also has the strings used to name properties in
 * wxsutils.properties
 * 
 * @author bnewport
 * 
 */
public class ConfigProperties {
	static Logger logger = Logger.getLogger(ConfigProperties.class.getName());

	/**
	 * This is the property name for catalog end point
	 */
	static final String CEP_PROP = "cep";
	String cep;

	/**
	 * This is the property name for the grid name
	 */
	static final String GRIDNAME_PROP = "grid";
	String gridName;

	/**
	 * This is the property name for objectgrid.xml full path including file name
	 */
	static final String OGXML_PROP = "og_xml_path";
	String ogXMLPath;

	/**
	 * This is the property name for the deployment.xml full path including file name
	 */
	static final String DPXML_PROP = "dp_xml_path";
	String dpXMLPath;

	/**
	 * This is the property name for the wxsutils client side thread pool
	 */
	static final String THREADS_PROP = "threads";
	int numThreads;

	/**
	 * This specifies how big to make the thread pool queue
	 */
	static final String QUEUE_PROP = "queue";
	int queueLength;

	/**
	 * This is the property name for the time wxsutils waits for agents to complete
	 */
	static final String AGENTWAITTIME_PROP = "agent_timeout";
	static final int DEFAULT_AGENT_WAIT_TIME = 120;
	int agentWaitTimeMaximumSecs = DEFAULT_AGENT_WAIT_TIME;

	static final String BIGLIST_BUCKET_SIZE = "biglist_bucket_size";
	public static final int DEFAULT_BIGLIST_BUCKET_SIZE = 5;
	public int bigListBucketSize = DEFAULT_BIGLIST_BUCKET_SIZE;

	static final String BIGLIST_LIMIT = "biglist_limit";
	public static final int DEFAULT_BIGLIST_LIMIT = -1;
	public int bigListLimit = DEFAULT_BIGLIST_LIMIT;

	static private InputStream findWXSPropertyFile(ClassLoader cl, boolean useRootSlash) {
		String prefix = useRootSlash ? "/" : "";
		InputStream is = cl.getResourceAsStream(prefix + "wxsutils.properties");
		if (is == null) {
			is = cl.getResourceAsStream(prefix + "META-INF/wxsutils.properties");
		}
		return is;
	}

	public ConfigProperties() throws IOException, FileNotFoundException {
		Properties props = new Properties();
		// BN Modified to use getResourceAsStream instead of FileInputStream
		// BN so it works with property files in jars
		// now using context class loader for when its a shared lib
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		boolean usingTCCL = true;
		if (cl == null) {
			cl = WXSUtils.class.getClassLoader();
			usingTCCL = false;
		}
		InputStream is = findWXSPropertyFile(cl, false);
		if (is == null) {
			is = findWXSPropertyFile(WXSUtils.class.getClassLoader(), false);
			usingTCCL = false;
		}
		if (usingTCCL) {
			logger.log(Level.INFO, "Property file locate using Thread Context Loader " + cl.toString());
		} else {
			logger.log(Level.INFO, "Property file locate using class loader for WXSUtils " + cl.toString());
		}
		if (is == null) {
			logger.log(Level.SEVERE, "/[META-INF/]wxsutils.properties not found on classpath");
			throw new FileNotFoundException("/[META-INF/]wxsutils.properties");
		}
		props.load(is);
		is.close(); // BN added close

		cep = props.getProperty(CEP_PROP);
		if (cep == null) {
			logger.log(Level.INFO, "No catalog endpoint specified, starting test server intra JVM");
		}
		gridName = props.getProperty(GRIDNAME_PROP);
		ogXMLPath = props.getProperty(OGXML_PROP);
		dpXMLPath = props.getProperty(DPXML_PROP);
		if (ogXMLPath == null) {
			ogXMLPath = "/objectgrid.xml";
			logger.log(Level.INFO, "og xml path defaulted to " + ogXMLPath);
		}
		if (dpXMLPath == null) {
			dpXMLPath = "/deployment.xml";
			logger.log(Level.INFO, "dp xml path defaulted to " + dpXMLPath);
		}
		if (gridName == null) {
			gridName = "Grid";
			logger.log(Level.INFO, "gridName defaulted to " + gridName);
		}
		numThreads = -1;
		String intValue = props.getProperty(THREADS_PROP);
		if (intValue != null) {
			numThreads = Integer.parseInt(intValue);
		}
		queueLength = 1;
		intValue = props.getProperty(QUEUE_PROP);
		if (intValue != null) {
			queueLength = Integer.parseInt(intValue);
		}

		intValue = props.getProperty(AGENTWAITTIME_PROP);
		if (intValue != null) {
			agentWaitTimeMaximumSecs = Integer.parseInt(intValue);
		}

		intValue = props.getProperty(BIGLIST_BUCKET_SIZE);
		if (intValue != null) {
			bigListBucketSize = Integer.parseInt(intValue);
		}

		intValue = props.getProperty(BIGLIST_LIMIT);
		if (intValue != null) {
			bigListLimit = Integer.parseInt(intValue);
		}
	}

	/**
	 * This gets a connection to the specified grid. It returns a client connection if possible
	 * 
	 * @return
	 */
	ObjectGrid connect() {
		ObjectGrid grid = null;
		if (cep != null) {
			logger.log(Level.INFO, "Default CEP = " + cep + "; Grid = " + gridName + "; ogXMLPath=" + ogXMLPath);
			grid = WXSUtils.connectClient(cep, gridName, ogXMLPath);
		} else {
			logger.log(Level.INFO, "Test Server; Grid = " + gridName + "; ogXMLPath = " + ogXMLPath + "; dpXMLPath = " + dpXMLPath);
			grid = WXSUtils.startTestServer(gridName, ogXMLPath, dpXMLPath);
		}
		return grid;
	}

	static public long getAgentTimeout(ConfigProperties cprops) {
		long waitTime = DEFAULT_AGENT_WAIT_TIME;
		if (cprops != null) {
			waitTime = cprops.agentWaitTimeMaximumSecs;
		}
		return TimeUnit.NANOSECONDS.convert(waitTime, TimeUnit.SECONDS);
	}

}
