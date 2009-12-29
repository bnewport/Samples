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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanManager;
import com.ibm.websphere.objectgrid.TxID;

/**
 * This class is meant as a base class for purequery loaders. It's main purpose is to provide
 * a common table name and the getData method.
 * @author bnewport
 *
 */
public abstract class BaseJDBCLoader
{
	String mapName = LoaderMBeanManager.UNKNOWN_MAP;
	
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
	 * This will create a JDBC Connection instance or return the current one associated with this
	 * WXS transaction.
	 * @param tx
	 * @return A JDBC Connection that can be used for the specified tx
	 */
	static public Connection getConnection(TxID tx)
		throws SQLException
	{
		/*
		 * These purequery Loaders MUST have a transaction callback specified of the type PQTxCallback.
		 */
		JDBCTxCallback cb = (JDBCTxCallback)tx.getSession().getObjectGrid().getTransactionCallback();
		return cb.getConnection(tx);
	}

	static <I> void copyPojoListToBatch(Connection conn, String sql, ArrayList<I> list, ArrayList<Field> fields)
		throws SQLException, IllegalAccessException, NoSuchFieldException
	{
		NamedParameterStatement s = new NamedParameterStatement(conn, sql);
		
		for(int row = 0; row < list.size(); ++row)
		{
			I item = list.get(row);
			copyPojoToStatement(s, item, fields);
			s.addBatch();
		}
		s.executeUpdate();
	}
	
	static public <I> void copyPojoToStatement(NamedParameterStatement s, I item, ArrayList<Field> fields)
		throws IllegalAccessException, SQLException, NoSuchFieldException
	{
		for(int i = 0; i < fields.size(); ++i)
		{
			Field f_typical = fields.get(i);
			// in case the key class is different, assume fields have
			// same name
			Field f = item.getClass().getField(f_typical.getName());
			String name = f_typical.getName();
			if(f.getType().equals(String.class))
			{
				String sv = (String)f.get(item);
				s.setString(name, sv);
			} else if(f.getType().equals(Integer.class))
			{
				Integer o = (Integer)f.get(item);
				s.setInt(name, o.intValue());
			} else if(f.getType().getName().equals("int"))
			{
				int ii = f.getInt(item);
				s.setInt(name, ii);
			} else if(f.getType().equals(Long.class))
			{
				Long l = (Long)f.get(item);
				s.setLong(name, l.longValue());
			} else if(f.getType().getName().equals("long"))
			{
				long l = f.getLong(item);
				s.setLong(name, l);
			} else
			{
				// add more types above if you get this exception
				Class t = f.getType();
				throw new IllegalArgumentException("Non supported type:" + t.getName());
			}
		}
	}
	
	static public <T> T copyResultSetToPojo(ResultSet rs, Class<T> c, ArrayList<Field> allFields)
		throws InstantiationException, IllegalAccessException, SQLException
	{
		T value = c.newInstance();
		// jdbc columns are numbered from 1 so we +1 here
		for(int i = 0; i < allFields.size(); ++i)
		{
			Field f = allFields.get(i);
			if(f.getType().equals(String.class))
			{
				f.set(value, rs.getString(i+1));
			} else if(f.getType().equals(Integer.class))
			{
				f.set(value, new Integer(rs.getInt(i+1)));
			} else if(f.getType().getName().equals("int"))
			{
				f.setInt(value, rs.getInt(i+1));
			} else if(f.getType().equals(Long.class))
			{
				f.set(value, new Long(rs.getLong(i+1)));
			} else if(f.getType().getName().equals("long"))
			{
				f.setLong(value, rs.getLong(i+1));
			} else
			{
				// add more types above if you get this exception
				throw new IllegalArgumentException("Non supported type: " + f.getType().getName());
			}
		}
		return value;
	}

}
