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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanImpl;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.TxID;
import com.ibm.websphere.objectgrid.plugins.Loader;
import com.ibm.websphere.objectgrid.plugins.LoaderException;
import com.ibm.websphere.objectgrid.plugins.LogElement;
import com.ibm.websphere.objectgrid.plugins.LogSequence;
import com.ibm.websphere.objectgrid.plugins.OptimisticCollisionException;


/**
 * This is a custom Loader for tables with a two column schema which must be called key and value.
 * The types of the key and value columns can vary. This abstract class needs to be subclasses and
 * methods overridden for a concrete POJO which defines the types.
 * This is different than GenericPQLoader because the value is not a POJO, it's a scalar type like
 * Long, String, Double etc
 * The StringKey Loaders can also use the ScalarKey key wrapper to support
 * collocated affinity for similar entries
 * @author bnewport
 * @see ScalarKey
 *
 */
public abstract class JDBCValueLoader<K,V> extends BaseJDBCLoader implements Loader
{
	static Logger logger = Logger.getLogger(JDBCValueLoader.class.getName());
	/**
	 * This returns a POJO for the LogElement value
	 * @param e The value for the change
	 * @return A POJO initialized with the value and key
	 */
	abstract public BaseKeyValue<K,V> getPOJOProperty(LogElement e);
	/**
	 * This should return the Class for the POJO
	 * @return
	 */
	abstract public Class<BaseKeyValue<K, V>> getPOJOClass();
	
	AtomicReference<ArrayList<Field>> allFields = new AtomicReference<ArrayList<Field>>();
	
	volatile ArrayList<Field> allKeyFields;

	void initializeMetaData()
		throws NoSuchFieldException
	{
		if(allFields.get() != null)
			return;
		Class<BaseKeyValue<K, V>> c = getPOJOClass();
		Field keyField = c.getField("keyz");
		Field valueField = c.getField("value");
		
		ArrayList<Field> ckeyFields = new ArrayList<Field>();
		ckeyFields.add(keyField);
		
		ArrayList<Field> c_allFields = new ArrayList<Field>(ckeyFields);
		c_allFields.add(valueField);
		
		allKeyFields = ckeyFields;
		
		ArrayList<String> kAllKeyFields = new ArrayList<String>();
		kAllKeyFields.add("keyz");
		
		if(allFields.compareAndSet(null, c_allFields))
		{
		}
	}
	
	public void batchUpdate(TxID tx, LogSequence ls)
			throws LoaderException, OptimisticCollisionException 
	{
		mapName = ls.getMapName();
		LoaderMBeanImpl mbean = WXSUtils.getLoaderMBeanManager().getBean(tx.getSession().getObjectGrid().getName(), mapName);
		mbean.getBatchSizeMetrics().logTime(ls.size());
		long startNS = System.nanoTime();
		try
		{
			initializeMetaData();
			Connection conn = getConnection(tx);
			Iterator<LogElement> iter = ls.getPendingChanges();
			ArrayList<BaseKeyValue<K, V>> iList = new ArrayList<BaseKeyValue<K, V>>();
			ArrayList<BaseKeyValue<K, V>> uList = new ArrayList<BaseKeyValue<K, V>>();
			ArrayList<BaseKeyValue<K, V>> dList = new ArrayList<BaseKeyValue<K, V>>();
			while(iter.hasNext())
			{
				LogElement e = iter.next();
				switch(e.getType().getCode())
				{
				case LogElement.CODE_INSERT:
					iList.add(getPOJOProperty(e));
					break;
				case LogElement.CODE_UPDATE:
					uList.add(getPOJOProperty(e));
					break;
				case LogElement.CODE_DELETE:
					dList.add(getPOJOProperty(e));
					break;
				}
			}
			if(iList.size() > 0)
			{
				try
				{
					String sql = "INSERT INTO " + tableName + " (KEYZ,VALUE) VALUES(:keyz, :value)";
					logger.fine("Inserting " + ls.getMapName() + " " + iList.toString());
					copyPojoListToBatch(conn, sql, iList, allFields.get());
				}
				catch(Exception e)
				{
					mbean.getBatchUpdateMetrics().logException(e);
					logger.log(Level.SEVERE, "Insert failed", e);
					throw e;
				}
			}
			if(uList.size() > 0)
			{
				try
				{
					String sql = "UPDATE " + tableName + " SET VALUE = :value WHERE KEYZ = :keyz";
					logger.fine("Updating " + ls.getMapName() + " " + uList.toString());
					copyPojoListToBatch(conn, sql, uList, allFields.get());
				}
				catch(Exception e)
				{
					mbean.getBatchUpdateMetrics().logException(e);
					logger.log(Level.SEVERE, "Update failed", e);
					throw e;
				}
			}
			if(dList.size() > 0)
			{
				try
				{
					String sql = "DELETE FROM " + tableName + " where KEYZ = :keyz";
					logger.fine("Deleting " + ls.getMapName() + " " + dList.toString());
					copyPojoListToBatch(conn, sql, dList, allKeyFields);
				}
				catch(Exception e)
				{
					mbean.getBatchUpdateMetrics().logException(e);
					logger.log(Level.SEVERE, "Delete failed", e);
					throw e;
				}
			}
			mbean.recordOperationRows(iList.size(), uList.size(), dList.size());
			mbean.getBatchUpdateMetrics().logTime(System.nanoTime() - startNS);
		}
		catch(Exception e)
		{
			mbean.getBatchUpdateMetrics().logException(e);
			throw new LoaderException("JDBCValueLoader exception:" + tableName, e);
		}
//		data.endBatch();
	}

	/**
	 * Fetch the corresponding value for each key. If the key isn't found
	 * then return Loader.KEY_NOT_FOUND. If the Key is a ScalarKey then
	 * extract the real key for the SQL
	 */
	public List get(TxID tx, List keys, boolean arg2) throws LoaderException 
	{
		LoaderMBeanImpl mbean = WXSUtils.getLoaderMBeanManager().getBean(tx.getSession().getObjectGrid().getName(), mapName);
		long startNS = System.nanoTime();
		try
		{
			initializeMetaData();
			Connection conn = getConnection(tx);
			ArrayList<V> rc = new ArrayList<V>();
			Iterator<K> iter = keys.iterator();
			String selectSQL = "SELECT KEYZ, VALUE FROM " + tableName + " where KEYZ = ?";
			PreparedStatement ps = conn.prepareStatement(selectSQL);
			while(iter.hasNext())
			{
				K key = iter.next();
				// extract real key is map uses a scalar key
				if(key instanceof ScalarKey)
				{
					ScalarKey sk = (ScalarKey)key;
					key = (K)sk.getKey();
				}
				Class<BaseKeyValue<K, V>> valueClass = getPOJOClass();
				ps.setObject(1, key);
				ResultSet rs = ps.executeQuery();
				
				if(rs.first())
				{
					BaseKeyValue<K, V> value = copyResultSetToPojo(rs, valueClass, allFields.get());
					logger.fine("Found " + key + " in " + tableName + " = " + value);
					rc.add(value.baseGetValue());
				}
				else
				{
					rc.add((V)Loader.KEY_NOT_FOUND);
					logger.fine("Didnt find " + key + " in " + tableName);
				}
			}
			mbean.getGetMetrics().logTime(System.nanoTime() - startNS);
			return rc;
		}
		catch(Exception e)
		{
			mbean.getGetMetrics().logException(e);
			logger.log(Level.SEVERE, "Loader:get " + tableName, e);
			throw new LoaderException("Loader:get " + tableName, e);
		}
	}

	public void preloadMap(Session sess, BackingMap map)
			throws LoaderException {
		// TODO Auto-generated method stub

	}
}
