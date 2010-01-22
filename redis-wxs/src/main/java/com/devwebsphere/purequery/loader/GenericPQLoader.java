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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.devwebsphere.rediswxs.agent.set.SetLoaderOperations;
import com.devwebsphere.rediswxs.data.list.ListItem;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanImpl;
import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanManager;
import com.ibm.pdq.runtime.Data;
import com.ibm.pdq.runtime.exception.DataRuntimeException;
import com.ibm.pdq.runtime.exception.UpdateManyException;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.TxID;
import com.ibm.websphere.objectgrid.plugins.Loader;
import com.ibm.websphere.objectgrid.plugins.LoaderException;
import com.ibm.websphere.objectgrid.plugins.LogElement;
import com.ibm.websphere.objectgrid.plugins.LogSequence;
import com.ibm.websphere.objectgrid.plugins.OptimisticCollisionException;
import com.ibm.websphere.projector.annotations.Id;

/**
 * This is a first attempt at a generic purequery Loader for POJOs. It expects a POJO that has public field attributes. Some
 * of these are annotated the WXS Id annotation to indicate key fields in the database. The database table is specified using
 * the table name attribute. The POJO attribute names must be the same as the database table column names.
 * @author bnewport
 *
 */
public class GenericPQLoader extends BasePQLoader implements Loader
{
	static Logger logger = Logger.getLogger(GenericPQLoader.class.getName());
	
	public String getClassName() {
		return theClass.getName();
	}
	
	public void setClassName(String className) 
	{
		try
		{
			theClass = Class.forName(className);
			createSQLStrings();
		}
		catch(ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Unknown class name: " + className);
		}
	}

	/**
	 * The POJO class to persist. Need this to reflect the annotations
	 */
	private Class theClass;

	/**
	 * This is the insert SQL command 
	 */
	String insertSQL;
	/**
	 * This is the update SQL command
	 */
	String updateSQL;
	/**
	 * This is the delete SQL command
	 */
	String deleteSQL;
	/**
	 * This is the select SQL command
	 */
	String selectSQL;

	/**
	 * This reflects the POJO and builds the SQL strings for insert, select, update and delete commands
	 * @throws LoaderException
	 */
	void createSQLStrings()
	{
		if(insertSQL != null)
			return;
		ArrayList<String> keyFieldNames = new ArrayList<String>();
		ArrayList<String> normalFieldNames = new ArrayList<String>();
		ArrayList<String> allFieldNames = new ArrayList<String>();
		Field[] allFields = theClass.getDeclaredFields();
		for(Field f : allFields)
		{
			// ignore static fields
			if((f.getModifiers() & Modifier.STATIC) == 0)
			{
				// is it a key field?
				if(f.getAnnotation(Id.class) != null)
				{
					keyFieldNames.add(f.getName());
				}
				else
				{
					normalFieldNames.add(f.getName());
				}
				allFieldNames.add(f.getName());
			}
		}
		insertSQL = "INSERT INTO " + tableName + " (";
		for(int i = 0; i < allFieldNames.size(); ++i)
		{
			insertSQL += allFieldNames.get(i).toUpperCase() + ((i != allFieldNames.size() - 1) ? "," : "");
		}
		insertSQL += ") VALUES (";
		for(int i = 0; i < allFieldNames.size(); ++i)
		{
			insertSQL += ":" + allFieldNames.get(i) + ((i != allFieldNames.size() - 1) ? "," : "");
		}
		insertSQL += ")";
		deleteSQL = "DELETE FROM " + tableName;
		String whereSQL = " WHERE ";
		for(int i = 0; i < keyFieldNames.size(); ++i)
		{
			whereSQL += keyFieldNames.get(i).toUpperCase() + "=:" + keyFieldNames.get(i) + ((i != keyFieldNames.size() - 1) ? " AND " : "");
		}
		deleteSQL += whereSQL;
		updateSQL = "UPDATE " + tableName + " SET ";
		for(int i = 0; i < normalFieldNames.size(); ++i)
		{
			updateSQL += normalFieldNames.get(i).toUpperCase() + "=:" + normalFieldNames.get(i) + ((i != normalFieldNames.size() - 1) ? "," : "");
		}
		updateSQL += whereSQL;
		selectSQL = "SELECT * FROM " + tableName + whereSQL;
		logger.fine("ISQL: " + insertSQL);
		logger.fine("USQL: " + updateSQL);
		logger.fine("DSQL: " + deleteSQL);
	}
	
	/**
	 * This is called to write changes at the end of a transaction or a flush to the backend this
	 * Loader works with which in this case in a database. We use purequery to flush everything
	 * in a single batch statement. Purequery does the POJO attribute to SQL parameter work
	 * for us.
	 */
	public void batchUpdate(TxID tx, LogSequence ls)
			throws LoaderException, OptimisticCollisionException 
	{
		mapName = ls.getMapName();
		LoaderMBeanImpl mbean = WXSUtils.getLoaderMBeanManager().getBean(ls.getMapName());
		mbean.getBatchSizeMetrics().logTime(ls.size());
		long startNS = System.nanoTime();
		// get the Data instance for this transaction
		Data data = getData(tx);
		// start a hetero batch.
		// make empty lists for the objects that are inserted or updated or deleted
		ArrayList<Object> iList = new ArrayList<Object>(1000);
		ArrayList<Object> uList = new ArrayList<Object>(1000);
		ArrayList<Object> dList = new ArrayList<Object>(1000);
		try
		{
//			data.startBatch(HeterogeneousBatchKind.heterogeneousModify__);
			// get the changes in this transaction
			Iterator<LogElement> iter = ls.getPendingChanges();
			// the key object. We may need to wrap primitive POJOs (Long, Integer etc) with a wrapped
			// with a key attribute for purequery.
			Object key;
			while(iter.hasNext())
			{
				// get next transaction change
				LogElement e = iter.next();
				switch(e.getType().getCode())
				{
				case LogElement.CODE_INSERT: // add to insert POJO list
					iList.add(e.getCurrentValue());
					break;
				case LogElement.CODE_UPDATE:
					uList.add(e.getCurrentValue()); // add to update POJO list
					break;
				case LogElement.CODE_DELETE: // add to delete POJO list
					key = e.getCacheEntry().getKey();
					// make need to wrap key for purequery
					if(key.getClass().isPrimitive() || key instanceof String)
					{
						KeyClass k = new KeyClass();
						k.keyz = key;
						dList.add(k);
					}
					else
						dList.add(key);
					break;
				}
			}
			// execute the statements within the batch for each type of operation
			if(iList.size() > 0)
			{
				try
				{
					logger.fine("Inserting " + ls.getMapName() + " " + iList.toString());
					data.updateMany(insertSQL, iList);
				}
				catch(UpdateManyException e)
				{
					mbean.getBatchUpdateMetrics().logException(e);
					DataRuntimeException next = e.getNextException();
					System.out.println("Insert failed with a next = " + next);
					throw e;
				}
			}
			if(uList.size() > 0)
			{
				try
				{
					logger.fine("Updating " + ls.getMapName() + " " + uList.toString());
					data.updateMany(updateSQL, uList);
				}
				catch(UpdateManyException e)
				{
					mbean.getBatchUpdateMetrics().logException(e);
					DataRuntimeException next = e.getNextException();
					System.out.println("Update failed with a next = " + next);
					throw e;
				}
			}
			if(dList.size() > 0)
			{
				try
				{
					logger.fine("Deleting " + ls.getMapName() + " " + dList.toString());
					data.updateMany(deleteSQL, dList);
				}
				catch(UpdateManyException e)
				{
					mbean.getBatchUpdateMetrics().logException(e);
					DataRuntimeException next = e.getNextException();
					System.out.println("Delete failed with a next = " + next);
					throw e;
				}
			}
			mbean.recordOperationRows(iList.size(), uList.size(), dList.size());
//			data.endBatch();
			mbean.getBatchUpdateMetrics().logTime(System.nanoTime() - startNS);
		}
		catch(DataRuntimeException e)
		{
			mbean.getBatchUpdateMetrics().logException(e);
			DataRuntimeException next = e;
			if(next == null)
			{
				System.out.println("next is null");
			}
			else
			{
				while(next != null)
				{
					System.out.println("Exception in Loader: " + ls.getMapName() + ":" + next.toString());
					next.printStackTrace();
					next = e.getNextException();
				}
			}
			throw new LoaderException("Map " + ls.getMapName(), e);
		}
		catch(Exception e)
		{
			mbean.getBatchUpdateMetrics().logException(e);
			throw new LoaderException("Map " + ls.getMapName(), e);
		}
	}

	/**
	 * This is called when a client looks up a key which isn't contained in the grid. The grid
	 * then calls this method to fetch the values for the keys. If a key doesn't exist in the
	 * backend then Loader.KEY_NOT_FOUND is returned.
	 */
	@SuppressWarnings("unchecked")
	public List get(TxID tx, List keys, boolean arg2) throws LoaderException 
	{
		LoaderMBeanImpl mbean = WXSUtils.getLoaderMBeanManager().getBean(mapName);
		try
		{
			mbean.getGetSizeMetrics().logTime(keys.size());
			long startNS = System.nanoTime();
			// Get a Data instance for these database selects
			Data data = getData(tx);
			// list for the results
			ArrayList rc = new ArrayList();
			Iterator iter = keys.iterator();
			KeyClass key = new KeyClass();
			// for each key in the list
			while(iter.hasNext())
			{
				Object keyValue = iter.next();
				Object value = null;
				// wrap with a Key object for purequery if needed
				if(keyValue.getClass().isPrimitive() || keyValue instanceof String)
				{
					key.keyz = keyValue;
					value = data.queryFirst(selectSQL, theClass, key);
				}
				else
				{
					value = data.queryFirst(selectSQL, theClass, keyValue);
				}
				// if we found the value then add it otherwise add KEY_NOT_FOUND
				if(value != null)
				{
					rc.add(value);
					logger.fine("Found " + keyValue + " in " + tableName + " = " + value);
				}
				else
				{
					rc.add(Loader.KEY_NOT_FOUND);
					logger.fine("Cant find " + keyValue + " in " + tableName);
				}
			}
			mbean.getGetMetrics().logTime(System.nanoTime() - startNS);
			return rc;
		}
		catch(Exception e)
		{
			mbean.getGetMetrics().logException(e);
			throw new LoaderException(e);
		}
	}

	public void preloadMap(Session sess, BackingMap map)
			throws LoaderException {
		// TODO Auto-generated method stub

	}
}
