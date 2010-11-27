package com.devwebsphere.jdbc.loader;

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
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.TxID;
import com.ibm.websphere.objectgrid.plugins.TransactionCallback;
import com.ibm.websphere.objectgrid.plugins.TransactionCallbackException;

/**
 * This is the JDBC transaction callback. This allocates a slot that is used to store the reference to
 * the Connection instance for a given transaction. It initializes a DataSource for database connections
 * and then provides a Connection instance for transactions that need one using the getConnection
 * method. No Connection instance is created per transaction unless one is needed.
 * @author bnewport
 *
 */
public class JDBCTxCallback implements TransactionCallback 
{
	/**
	 * Use the classname for the slot name. Needs to be unique
	 */
	public static String DATASLOT = JDBCTxCallback.class.getName();
	
	/**
	 * The DataSource for the database
	 */
	DataSource ds;
	
	/**
	 * The index of the slot that will store the Data instance for a given transaction
	 */
	int dataSlot;

	/**
	 * This is called on every shard when its initialized within a JVM. We should really have a single DataSource per JVM,
	 * not per shard
	 */
	public void initialize(ObjectGrid og) throws TransactionCallbackException 
	{
		if(og.getObjectGridType() == ObjectGrid.SERVER)
		{
			/**
			 * Reserve a slot in the TxID to store the Data instance reference in.
			 */
			dataSlot = og.reserveSlot(TxID.SLOT_NAME);
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
			try
			{
				Connection c = (Connection)tx.getSlot(dataSlot);
				if(c != null)
				{
					c.commit();
					c.close();
					tx.putSlot(dataSlot, null);
				}
			}
			catch(SQLException e)
			{
				throw new TransactionCallbackException(e);
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
			try
			{
				Connection c = (Connection)tx.getSlot(dataSlot);
				if(c != null)
				{
					c.rollback();
					c.close();
					tx.putSlot(dataSlot, null);
				}
			}
			catch(SQLException e)
			{
				throw new TransactionCallbackException(e);
			}
		}
	}
	
	public DataSource getDataSource()
	{
		return ds;
	}

	/**
	 * This must be wired to the DataSource to use with the class.
	 * @param ds
	 */
	public void setDataSource(DataSource ds)
	{
		this.ds = ds;
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
	public Connection getConnection(TxID tx)
		throws SQLException
	{
		// is there already a JDBC Connection for this transaction?
		Connection c = (Connection)tx.getSlot(dataSlot);
		if(c == null)
		{
			// create one if needed
			c = getDataSource().getConnection();
			c.setAutoCommit(false);
			// store in the Tx slot for the Loaders to reuse for this transaction in the future
			tx.putSlot(dataSlot, c);
		}
		return c;
	}
}
