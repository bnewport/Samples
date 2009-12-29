package com.devwebsphere.purequery.loader;

//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import com.ibm.pdq.runtime.Data;
import com.ibm.pdq.runtime.factory.DataFactory;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.TxID;
import com.ibm.websphere.objectgrid.plugins.TransactionCallback;
import com.ibm.websphere.objectgrid.plugins.TransactionCallbackException;
import com.mysql.jdbc.Driver;

/**
 * This is the purequery transaction callback. This allocates a slot that is used to store the reference to
 * the Data instance for a given transaction. It initializes a DataSource for database connections
 * and then provides a purequery Data instance for transactions that need one using the getData
 * method. No data instance is created per transaction unless one is needed.
 * @author bnewport
 *
 */
public class PQTxCallback implements TransactionCallback 
{
	public String getConnecturi() {
		return connecturi;
	}

	public void setConnecturi(String connecturi) {
		this.connecturi = connecturi;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Property which must be set for the JDBC connection URL
	 */
	String connecturi;
	/**
	 * The username for the database
	 */
	String username;
	/**
	 * The password for the database
	 */
	String password;
	
	/**
	 * Use the classname for the slot name. Needs to be unique
	 */
	public static String DATASLOT = PQTxCallback.class.getName();
	
	/**
	 * The DataSource for the database
	 */
	DataSource ds;
	
	/**
	 * The index of the slot that will store the Data instance for a given transaction
	 */
	int dataSlot;

	/**
	 * Make sure the DB2 database driver is loaded.
	 */
	static
	{
		try {
//			DB2Driver driver = new DB2Driver();
//			DriverManager.registerDriver(driver);
			Driver d = new Driver();
			DriverManager.registerDriver(d);
		} catch (Exception e) {
			throw new RuntimeException ("Unable to load the driver. Message " + e.getMessage ());
		}
	}

	/**
	 * This is called on every shard when its initialized within a JVM. We should really have a single DataSource per JVM,
	 * not per shard
	 */
	public void initialize(ObjectGrid og) throws TransactionCallbackException 
	{
		if(og.getObjectGridType() == og.SERVER)
		{
			/**
			 * Reserve a slot in the TxID to store the Data instance reference in.
			 */
			dataSlot = og.reserveSlot(TxID.SLOT_NAME);
			/**
			 * Connect to the database. We share a single data source between all primary shards within
			 * a single JVM.
			 */
			ds = setupDataSource(connecturi, username, password);
			try
			{
				// check we can connect
				Connection conn = ds.getConnection();
				if(conn == null)
					throw new RuntimeException("Cannot open driver");
				conn.close();
			}
			catch(SQLException e)
			{
				throw new RuntimeException("Cannot open driver", e);
			}
		}
	}

	/**
	 * Do nothing here, we don't want to slow down transactions creating or grabbing a Data instance
	 * unless we have to.
	 */
	public void begin(TxID tx) throws TransactionCallbackException 
	{
	}

	/**
	 * This is called when a WXS transaction commits. If a Data instance was created for the transaction then
	 * commit it and clean up.
	 */
	public void commit(TxID tx) throws TransactionCallbackException 
	{
		if(tx.getSession().getObjectGrid().getObjectGridType() == ObjectGrid.SERVER)
		{
			Data d = (Data)tx.getSlot(dataSlot);
			if(d != null)
			{
				d.commit();
				d.close();
				tx.putSlot(dataSlot, null);
			}
		}
	}

	/**
	 * This is called when a WXS transaction needs to rollback. If a Data instance was created for the transaction
	 * then roll it back and clean up
	 */
	public void rollback(TxID tx) throws TransactionCallbackException 
	{
		if(tx.getSession().getObjectGrid().getObjectGridType() == ObjectGrid.SERVER)
		{
			Data d = (Data)tx.getSlot(dataSlot);
			if(d != null)
			{
				d.rollback();
				d.close();
				tx.putSlot(dataSlot, null);
			}
		}
	}
	
	public DataSource getDataSource()
	{
		return ds;
	}
	
	/**
	 * Create a DataSource using the tomcat OCDP pool. Only make one per JVM. There is a PQTxCallback
	 * instance for each primary shard placed in a JVM. This shares a single DataSource instance
	 * between all of the PQTxCallback instances. We keep a Map of DataSources with an entry
	 * per connectURI. This allows this generic class to be used for multiple grids
	 * within the same JVM.
	 * @param connectURI
	 * @param user
	 * @param password
	 * @return
	 */
	
	static Map<String,BasicDataSource> dataSourceList = new HashMap<String,BasicDataSource>();
	
	public synchronized static DataSource setupDataSource(String connectURI, String user, String password) 
	{
		String key = connectURI + ":" + user + ":" + password;
		DataSource rc = dataSourceList.get(key);
		if(rc == null)
		{
	        //
	        // First, we'll need a ObjectPool that serves as the
	        // actual pool of connections.
	        //
	        // We'll use a GenericObjectPool instance, although
	        // any ObjectPool implementation will suffice.
	        //

			BasicDataSource bds = new BasicDataSource();
			bds.setInitialSize(5);
			bds.setUrl(connectURI);
			bds.setUsername(user);
			bds.setPassword(password);
			bds.setPoolPreparedStatements(true);
			
			rc = bds;
			
	        dataSourceList.put(key, bds);
		}
        return rc;
    }
	
	public boolean isExternalTransactionActive(Session arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This returns the index of the slot where the Data instance is stored
	 * @return
	 * @see TxID#putSlot(int, Object)
	 * @see TxID#getSlot(int)
	 * @see ObjectGrid#reserveSlot(String)
	 */
	public int getDataslot()
	{
		return dataSlot;
	}

	/**
	 * This is called by Loaders to get a Data instance when they need it. The TxCallbacks job is to make one and store a reference
	 * to it in the slot for all Loaders used in this transaction to share.
	 * @param tx
	 * @return
	 */
	public Data getData(TxID tx)
	{
		// is there already a purequery Data for this transaction?
		Data d = (Data)tx.getSlot(dataSlot);
		if(d == null)
		{
			// create one if needed
			d = DataFactory.getData(getDataSource());
			d.setAutoCommit(false);
			// store in the Tx slot for the Loaders to reuse for this transaction in the future
			tx.putSlot(dataSlot, d);
		}
		return d;
	}
}
