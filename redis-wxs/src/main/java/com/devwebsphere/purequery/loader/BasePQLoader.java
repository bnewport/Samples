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

import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanManager;
import com.ibm.pdq.runtime.Data;
import com.ibm.websphere.objectgrid.TxID;

/**
 * This class is meant as a base class for purequery loaders. It's main purpose is to provide
 * a common table name and the getData method.
 * @author bnewport
 *
 */
public abstract class BasePQLoader
{
	String mapName = LoaderMBeanManager.UNKNOWN_MAP;
	// Public class with just a key for purequery queries
	static public class KeyClass
	{
		public Object keyz;
	}
	
	/**
	 * This returns the table to use for the POJOs mapped using this Loader
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * This is normally specified in the objectgrid.xml file to specify
	 * the table name for the mapper
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	String tableName;

	/**
	 * This will create a Data instance or return the current one associated with this
	 * WXS transaction.
	 * @param tx
	 * @return A PureQuery Data instance that can be used for the specified tx
	 */
	static public Data getData(TxID tx)
	{
		/*
		 * These purequery Loaders MUST have a transaction callback specified of the type PQTxCallback.
		 */
		PQTxCallback cb = (PQTxCallback)tx.getSession().getObjectGrid().getTransactionCallback();
		return cb.getData(tx);
	}
}
